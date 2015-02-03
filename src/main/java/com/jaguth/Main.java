package com.jaguth;

public class Main
{
    public static void main(String[] args)
    {
        FetcherManager fetcherManager = new FetcherManager();

        String[] test = new String[] {"job.json"};

        if (fetcherManager.ParseArgs(test))
            fetcherManager.Run();
    }
}
