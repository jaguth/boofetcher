package com.jaguth.fetcher;

public class Job
{
    public String jobName;
    public String ownerName;
    public Fetcher fetcher;
    public DataStore dataStore;

    public Job()
    {

    }

    public void BeginFetching()
    {
        fetcher.FetchPastMessages();
        fetcher.FetchStreamingMessages();
        //fetcher.Dispose();
    }
}
