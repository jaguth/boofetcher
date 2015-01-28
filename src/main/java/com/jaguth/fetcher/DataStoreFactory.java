package com.jaguth.fetcher;

public class DataStoreFactory
{
    public static DataStore GetDataStore(String dataStoreType)
    {
        switch (dataStoreType)
        {
            case "ElasticSearchDataStore":
                return new ElasticSearchDataStore();
            default:
                throw new DataStoreException(String.format("DataStoreFactory Error: The data store '%1$s' is not implemented.", dataStoreType));
        }
    }
}
