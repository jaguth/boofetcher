package com.jaguth.fetcher;

public class FetcherFactory
{
    public static Fetcher GetFetcher(String fetcherType)
    {
        switch (fetcherType)
        {
            case "TwitterFetcher":
                return new TwitterFetcher();
            default:
                throw new FetcherException(String.format("DataStoreFactory Error: The data store '%1$s' is not implemented.", fetcherType));
        }
    }
}

