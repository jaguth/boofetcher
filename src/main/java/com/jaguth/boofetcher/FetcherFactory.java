package com.jaguth.boofetcher;

import org.json.JSONObject;

public class FetcherFactory
{
    public static iFetcher GetFetcher(String query,
                                      JSONObject obj)
    {
        String type = obj.getString("type");
        switch (type)
        {
            case "TwitterFetcher":
            {
                return new TwitterFetcher(
                        query,
                        obj.getString("oAuthConsumerKey"),
                        obj.getString("oAuthConsumerSecret"),
                        obj.getString("oAuthAccessToken"),
                        obj.getString("oAuthAccessTokenSecret"),
                        null,
                        null,
                        true,
                        true);
            }
            default:
                throw new FetcherException(String.format("DataStoreFactory Error: The data store '%1$s' is not implemented.", type));
        }
    }
}

