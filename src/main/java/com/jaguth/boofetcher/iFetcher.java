package com.jaguth.boofetcher;

public interface iFetcher
{

    public abstract void BeginFetching();
    public abstract void FetchPastMessages();
    public abstract void FetchStreamingMessages();
}
