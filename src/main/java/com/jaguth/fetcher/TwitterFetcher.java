package com.jaguth.fetcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jaguth.datastore.DataStoreFactory;
import com.jaguth.models.Message;
import com.jaguth.datastore.iDataStore;
import com.jaguth.util.LogUtil;
import org.json.*;
import org.json.JSONArray;
import org.json.JSONObject;
import twitter4j.*;
import twitter4j.conf.*;

public class TwitterFetcher implements iFetcher, Runnable
{
    private String oAuthConsumerKey;
    private String oAuthConsumerSecret;
    private String oAuthAccessToken;
    private String oAuthAccessTokenSecret;
    private String query;

    private Date querySince;
    private Date queryUntil;
    private iDataStore[] dataStores;

    private static int pageSize = 100; // max twitter items per page is 100
    private static int chunckSize = 1000; // save fetched items every 500 items
    private static String shortDateFormat = "yyyy-MM-dd";

    private Twitter twitterClient;
    private boolean twitterClientInitiated;

    private TwitterStream twitterStreamClient;
    private boolean twitterStreamClientInitiated;

    private boolean fetchPastMessage;
    private boolean fetchStreamingMessages;

    private Thread t;

    public TwitterFetcher()
    {
        twitterClientInitiated = false;
        twitterStreamClientInitiated = false;
    }

    public void Initialize(JSONObject obj)
    {
        this.query = obj.getString("query");
        this.oAuthConsumerKey = obj.getString("oAuthConsumerKey");
        this.oAuthConsumerSecret = obj.getString("oAuthConsumerSecret");
        this.oAuthAccessToken = obj.getString("oAuthAccessToken");
        this.oAuthAccessTokenSecret = obj.getString("oAuthAccessTokenSecret");

        SimpleDateFormat formatter = new SimpleDateFormat(shortDateFormat);

        try
        {
            this.querySince = obj.isNull("querySince") ? null : formatter.parse(obj.getString("querySince"));
            this.queryUntil = obj.isNull("queryUntil") ? null : formatter.parse(obj.getString("queryUntil"));
        }
        catch (Exception e)
        {
            LogUtil.Log(String.format("TwitterFetcher failed to parse querySince or queryUntil dates: %1$s", e.toString()));
            throw new FetcherException(e.toString());
        }

        this.fetchPastMessage = obj.getBoolean("fetchPastMessages");
        this.fetchStreamingMessages = obj.getBoolean("fetchStreamingMessages");

        JSONArray dataStoreArray = obj.getJSONArray("dataStores");
        this.dataStores = new iDataStore[dataStoreArray.length()];

        for (int i = 0; i < dataStoreArray.length(); i++)
            this.dataStores[i] = DataStoreFactory.GetDataStore(dataStoreArray.getJSONObject(i));
    }

    public void BeginFetching()
    {
        if (t == null)
        {
            t = new Thread(this);
            t.start();
        }
    }

    public void run()
    {
        /* Some data sources, like twitter, will not allow multiple fetchers to run at the same time with the same account.
         * It will fail with an error message describing that the same account is being used elseware.
         * So, the fetching types are run serial. First we fetch past messages, then we fetch streaming messages.
         */
        if (fetchPastMessage)
            FetchPastMessages();

        if (fetchStreamingMessages)
            FetchStreamingMessages();

        Dispose();
    }

    public void Dispose()
    {
        for (iDataStore dataStore : dataStores)
            dataStore.Dispose();
    }

    public void InitTwitterClient()
    {
        if (twitterClientInitiated)
            return;

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(oAuthConsumerKey)
                .setOAuthConsumerSecret(oAuthConsumerSecret)
                .setOAuthAccessToken(oAuthAccessToken)
                .setOAuthAccessTokenSecret(oAuthAccessTokenSecret);

        TwitterFactory tf = new TwitterFactory(cb.build());
        twitterClient = tf.getInstance();

        twitterClientInitiated = true;
    }

    public void InitTwitterStreamClient()
    {
        if (twitterStreamClientInitiated)
            return;

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(oAuthConsumerKey)
                .setOAuthConsumerSecret(oAuthConsumerSecret)
                .setOAuthAccessToken(oAuthAccessToken)
                .setOAuthAccessTokenSecret(oAuthAccessTokenSecret);

        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        twitterStreamClient = tf.getInstance();

        twitterStreamClientInitiated = true;
    }

    public void FetchPastMessages()
    {
        InitTwitterClient();

        if (!twitterClientInitiated)
            return;

        Query twitterQuery = new Query(query);

        if (querySince != null)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(shortDateFormat);

            twitterQuery.setSince(dateFormat.format(querySince));

            if (queryUntil != null)
                twitterQuery.setUntil(dateFormat.format(queryUntil));
            else
                twitterQuery.setUntil(dateFormat.format(new Date()));
        }

        twitterQuery.setCount(pageSize);

        long collectedTweetCount = 0;

        try
        {
            LogUtil.Log("Beginning twitter fetch");

            QueryResult result = twitterClient.search(twitterQuery);

            if (result.getTweets().size() == 0)
            {
                LogUtil.Log(String.format("Search query '%1%s' returned no results", this.query)); // no tweets returned
                return;
            }

            List<Message> messages = new ArrayList<Message>();

            while (twitterQuery != null)
            {
                for (Status tweet : result.getTweets())
                {
                    messages.add(CreateTweetMessage(tweet)); // add tweets to list
                    collectedTweetCount++;
                }

                if (messages.size() >= chunckSize)
                {
                    for (iDataStore datastore : dataStores)
                    {
                        datastore.InsertBulkData(messages); // chunk size met, insert data
                        messages.clear();
                    }
                }

                HandleRateLimit(result.getRateLimitStatus());

                twitterQuery = result.nextQuery();

                if (twitterQuery != null)
                    result = twitterClient.search(twitterQuery); // grab next batch of tweets
            }

            LogUtil.Log(String.format("Collected tweet count: %1$d\r\n", collectedTweetCount));
        }
        catch (TwitterException e)
        {
            LogUtil.Log(String.format("TwitterFetcher Failed: %1$s", e.toString()));
        }
    }

    private void HandleRateLimit(RateLimitStatus rateLimitStatus)
    {
        if (rateLimitStatus.getRemaining() < 3)
        {
            try
            {
                LogUtil.Log(String.format("Rate limit hit. Waiting %1$d seconds...", rateLimitStatus.getSecondsUntilReset()));
                Thread.sleep(1000 * rateLimitStatus.getSecondsUntilReset() + 1000);
            }
            catch (Exception e)
            {
                LogUtil.Log(String.format("TwitterFetcher Wait Failed: %1$s", e.toString()));
            }
        }
    }

    private Message CreateTweetMessage(Status tweet)
    {
        /*
        GeoLocation geoLocation = null;

        if (tweet.getGeoLocation() != null)
            geoLocation = new GeoLocation(tweet.getGeoLocation().getLatitude(), tweet.getGeoLocation().getLongitude());
        */

        String geoLocation = null;

        if (tweet.getGeoLocation() != null)
            geoLocation = tweet.getGeoLocation().getLatitude() + "," + tweet.getGeoLocation().getLongitude();

        String countryCode = null;

        if (tweet.getPlace() != null)
            countryCode = tweet.getPlace().getCountryCode();

        return new Message(
                tweet.getId(),
                tweet.getUser().getScreenName(),
                geoLocation,
                countryCode,
                tweet.getUser().getTimeZone(),
                tweet.getUser().getLang(),
                tweet.getCreatedAt(),
                tweet.getFavoriteCount(),
                tweet.getRetweetCount(),
                tweet.getText(),
                "Twitter");
    }

    public void FetchStreamingMessages()
    {
        InitTwitterStreamClient();

        if (!twitterStreamClientInitiated)
            return;

        StatusListener listener = new StatusListener()
        {
            public void onStatus(Status status)
            {
                try
                {
                    List<Message> messages = new ArrayList<Message>();
                    messages.add(CreateTweetMessage(status));

                    for (iDataStore dataStore : dataStores)
                        dataStore.InsertBulkData(messages);
                }
                catch (Exception e)
                {
                    LogUtil.Log(String.format("TwitterFetcher Stream Fetch Failed. Will try to recover. Error Message: %1$s", e.toString()));
                    InitTwitterStreamClient();
                }
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
            public void onScrubGeo(long i, long i2) {}
            public void onStallWarning(StallWarning warning) {}
        };

        twitterStreamClient.addListener(listener);
        FilterQuery filter = new FilterQuery();

        String[] keywordsArray = { query };

        if (query.contains(("\" OR \"")))
            keywordsArray = query.split("\" OR \"");

        filter.track(keywordsArray);
        twitterStreamClient.filter(filter);
    }
}