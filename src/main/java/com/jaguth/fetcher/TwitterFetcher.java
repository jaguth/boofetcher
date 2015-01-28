package com.jaguth.fetcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jaguth.util.LogUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import twitter4j.*;
import twitter4j.conf.*;

public class TwitterFetcher extends Fetcher
{
    public String setOAuthConsumerKey;
    public String setOAuthConsumerSecret;
    public String setOAuthAccessToken;
    public String setOAuthAccessTokenSecret;
    public String query;

    public Date querySince;
    public Date queryUntil;
    public DataStore dataStore;

    private static int pageSize = 100; // if page size not specified, set to 15
    private static int chunckSize = 1000; // save fetched items every 500 items
    private static String shortDateFormat = "yyyy-MM-dd";

    private Twitter twitterClient;
    private boolean twitterClientInitiated;

    private TwitterStream twitterStreamClient;
    private boolean twitterStreamClientInitiated;

    public TwitterFetcher()
    {
        twitterClientInitiated = false;
        twitterStreamClientInitiated = false;
    }

    public void InitTwitterClient()
    {
        if (twitterClientInitiated)
            return;

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(setOAuthConsumerKey)
                .setOAuthConsumerSecret(setOAuthConsumerSecret)
                .setOAuthAccessToken(setOAuthAccessToken)
                .setOAuthAccessTokenSecret(setOAuthAccessTokenSecret);

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
                .setOAuthConsumerKey(setOAuthConsumerKey)
                .setOAuthConsumerSecret(setOAuthConsumerSecret)
                .setOAuthAccessToken(setOAuthAccessToken)
                .setOAuthAccessTokenSecret(setOAuthAccessTokenSecret);

        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        twitterStreamClient = tf.getInstance();

        twitterStreamClientInitiated = true;
    }

    public void Dispose()
    {
        dataStore.Dispose();
    }

    public void FetchPastMessages()
    {
        if (!fetchPastMessages)
            return;

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
                    dataStore.InsertBulkData(messages); // chunk size met, insert data
                    messages.clear();
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
        if (!fetchStreamingMessages)
            return;

        InitTwitterStreamClient();

        if (!twitterStreamClientInitiated)
            return;

        StatusListener listener = new StatusListener()
        {
            public void onStatus(Status status)
            {
                List<Message> messages = new ArrayList<Message>();
                messages.add(CreateTweetMessage(status));
                dataStore.InsertBulkData(messages);
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