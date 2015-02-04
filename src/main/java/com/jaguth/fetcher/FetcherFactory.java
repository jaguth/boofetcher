package com.jaguth.fetcher;

import org.json.JSONObject;

public class FetcherFactory
{
    public static iFetcher GetFetcher(JSONObject obj)
    {
        String type = obj.getString("type");

        switch (type)
        {
            case "TwitterFetcher":
            {
                TwitterFetcher twitterFetcher = new TwitterFetcher();
                twitterFetcher.Initialize(obj);
                return twitterFetcher;
            }
            default:
                throw new FetcherException(String.format("DataStoreFactory Error: The data store '%1$s' is not implemented.", type));
        }
    }
}

