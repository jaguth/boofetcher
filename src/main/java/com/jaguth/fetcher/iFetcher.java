package com.jaguth.fetcher;

public interface iFetcher
{

    public abstract void BeginFetching();
    public abstract void FetchPastMessages();
    public abstract void FetchStreamingMessages();
}
