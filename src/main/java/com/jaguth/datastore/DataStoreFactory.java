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
                String uri = obj.getString("type");
                String index = obj.getString("index");

                return new ElasticSearchDataStore(uri, index);
            }
            default:
                throw new DataStoreException(String.format("DataStoreFactory Error: The data store '%1$s' is not implemented.", dataStoreType));
        }
    }
}
