package com.jaguth;

import com.jaguth.fetcher.iFetcher;

public class Job
{
    public String jobName;
    public String ownerName;
    public String query;
    public iFetcher[] fetchers;

    public Job()
    {

    }

    public void StartJob()
    {
        for (iFetcher fetcher : fetchers)
        {
            fetcher.BeginFetching();
        }
    }

}
