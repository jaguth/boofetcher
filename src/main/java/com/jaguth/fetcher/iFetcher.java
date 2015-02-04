package com.jaguth.fetcher;

import org.json.JSONObject;

public interface iFetcher
{
    public abstract void Initialize(JSONObject obj);
    public abstract void BeginFetching();
}
