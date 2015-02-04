package com.jaguth.datastore;

import org.json.JSONObject;

public class DataStoreFactory
{
    public static iDataStore GetDataStore(JSONObject obj)
    {
        String dataStoreType = obj.getString("type");

        switch (dataStoreType)
        {
            case "ElasticSearchDataStore":
            {
                ElasticSearchDataStore elasticSearchDataStore = new ElasticSearchDataStore();
                elasticSearchDataStore.Initialize(obj);
                return elasticSearchDataStore;
            }
            default:
                throw new DataStoreException(String.format("DataStoreFactory Error: The data store '%1$s' is not implemented.", dataStoreType));
        }
    }
}
