package com.jaguth.fetcher;

import com.jaguth.datastore.DataStoreFactory;
import com.jaguth.datastore.iDataStore;
import com.jaguth.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FetcherFactory
{
    public static iFetcher GetFetcher(String query,
                                      JSONObject obj)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        String type = obj.getString("type");



        switch (type)
        {
            case "TwitterFetcher":
            {
                Date querySince = null;
                Date queryUntil = null;

                try
                {
                    querySince = obj.getString("querySince") == null ? null : formatter.parse(obj.getString("querySince"));
                    queryUntil = obj.getString("queryUntil") == null ? null : formatter.parse(obj.getString("queryUntil"));
                }
                catch (Exception e)
                {
                    LogUtil.Log(String.format("FetcherFactor failed to parse querySince or queryUntil dates: %1$s", e.toString()));
                }

                JSONArray dataStoreArray = obj.getJSONArray("dataStores");
                iDataStore[] stores = new iDataStore[dataStoreArray.length()];

                for (int i = 0; i < dataStoreArray.length(); i++)
                    stores[i] = DataStoreFactory.GetDataStore(dataStoreArray.getJSONObject(i));

                return new TwitterFetcher(query,
                        obj.getString("oAuthConsumerKey"),
                        obj.getString("oAuthConsumerSecret"),
                        obj.getString("oAuthAccessToken"),
                        obj.getString("oAuthAccessTokenSecret"),
                        querySince,
                        queryUntil,
                        obj.getBoolean("fetchPastMessages"),
                        obj.getBoolean("fetchStreamingMessages"),
                        stores);
            }
            default:
                throw new FetcherException(String.format("DataStoreFactory Error: The data store '%1$s' is not implemented.", type));
        }
    }
}

