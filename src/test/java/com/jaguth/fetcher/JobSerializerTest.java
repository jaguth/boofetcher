package com.jaguth.fetcher;

import junit.framework.TestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JobSerializerTest extends TestCase
{
    @Test
    public void testSerialize() throws Exception
    {
        Job job = new Job();
        job.jobName = "zelda";
        job.ownerName = "jaguth";

        job.fetcher = new TwitterFetcher();
        ((TwitterFetcher)job.fetcher).setOAuthConsumerKey = "k";
        ((TwitterFetcher)job.fetcher).setOAuthConsumerSecret = "k";
        ((TwitterFetcher)job.fetcher).setOAuthAccessToken = "k";
        ((TwitterFetcher)job.fetcher).setOAuthAccessTokenSecret = "k";
        ((TwitterFetcher)job.fetcher).query = "k";
        job.dataStore = new ElasticSearchDataStore();
        ((ElasticSearchDataStore)job.dataStore).uri = "zelda";
        ((ElasticSearchDataStore)job.dataStore).indexName = "zelda";

        String json = new JobSerializer().serialize(job);

        assertNotNull(json);
    }

    @Test
    public void testDeserialize() throws Exception
    {
        String text = "";

        try
        {
            text = new String(Files.readAllBytes(Paths.get("./job.json")));
        }
        catch (Exception e)
        {
            System.out.println(String.format("Failed to read job.json: %1$s", e.toString()));
            return;
        }

        JobSerializer serializer = new JobSerializer();
        Job job = serializer.deserialize(text);

        assertNotNull(job);
    }
}