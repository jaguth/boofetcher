package com.jaguth.fetcher;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo( use = JsonTypeInfo.Id.NAME )
@JsonSubTypes(
        {
                @JsonSubTypes.Type( name = "TwitterFetcher", value = TwitterFetcher.class )
        }
)
public abstract class Fetcher
{
    public abstract void FetchPastMessages();
    public abstract void FetchStreamingMessages();
    public abstract void Dispose();
    public DataStore dataStore;
    public boolean fetchPastMessages;
    public boolean fetchStreamingMessages;
}
