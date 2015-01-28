package com.jaguth.fetcher;

import com.jaguth.util.FileUtil;
import com.jaguth.util.StringUtil;


public class FetcherManager
{
    private static String jobFilePath = "./job.json";

    public FetcherManager()
    {

    }

    public void Run()
    {
        Job job = DeserializeJob();

        if (job == null)
            return;

        job.BeginFetching();
    }

    public Job DeserializeJob()
    {
        String text = FileUtil.ReadUnicodeTextFile(jobFilePath);

        if (StringUtil.isNullOrWhitespace(text))
            return null;

        JobSerializer serializer = new JobSerializer();
        Job job = serializer.deserialize(text);
        TwitterFetcher fetcher = (TwitterFetcher)job.fetcher; // hard coded for now to get proof of concept out the door
        fetcher.dataStore = job.dataStore;
        job.fetcher = fetcher;

        return job;
    }
}
