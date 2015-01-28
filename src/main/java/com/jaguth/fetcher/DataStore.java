package com.jaguth.fetcher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo( use = JsonTypeInfo.Id.NAME )
@JsonSubTypes(
        {
                @JsonSubTypes.Type( name = "ElasticSearchDataStore", value = ElasticSearchDataStore.class )
        }
)
public abstract class DataStore
{
    public abstract void InitDataStore();
    public abstract void InsertBulkData(List<Message> messages);
    public abstract void Dispose();
}
