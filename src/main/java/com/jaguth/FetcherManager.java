package com.jaguth;

import com.jaguth.fetcher.FetcherFactory;
import com.jaguth.fetcher.iFetcher;
import com.jaguth.util.FileUtil;
import com.jaguth.util.LogUtil;
import com.jaguth.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;


public class FetcherManager
{
    private String jobFilePath;

    public FetcherManager()
    {

    }

    public boolean ParseArgs(String[] args)
    {
        if (args.length != 1)
        {
            PrintUsage();
            return false;
        }

        jobFilePath = args[0];
        return true;
    }

    public void PrintUsage()
    {
        System.out.println("Usage: java -jar fetcher.jar <file_path_to_job.json>");
    }

    public void Run()
    {
        Job job = DeserializeJob();

        if (job == null)
        {
            LogUtil.Log("Failed to deserialize job config. Exiting.");
            return;
        }

        job.StartJob();
    }

    public Job DeserializeJob()
    {
        String text = FileUtil.ReadUnicodeTextFile(jobFilePath);

        if (StringUtil.isNullOrWhitespace(text))
        {
            LogUtil.Log(String.format("%1$s is null or empty", jobFilePath));
            return null;
        }

        try
        {
            Job job = new Job();
            JSONObject obj = new JSONObject(text);

            job.jobName = obj.getString("jobName");
            job.ownerName = obj.getString("ownerName");
            job.query = obj.getString("query");

            JSONArray fetcherArray = obj.getJSONArray("fetchers");
            job.fetchers = new iFetcher[fetcherArray.length()];

            for (int i = 0; i < fetcherArray.length(); i++)
                job.fetchers[i] = FetcherFactory.GetFetcher(job.query, fetcherArray.getJSONObject(i));

            return job;
        }
        catch (Exception e)
        {
            LogUtil.Log(String.format("Failed to parse %1$s: %2$s: ", jobFilePath, e.toString()));
            return null;
        }
    }
}
