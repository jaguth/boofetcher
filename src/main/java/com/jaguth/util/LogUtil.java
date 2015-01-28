package com.jaguth.util;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogUtil
{
    private static String errorLogFile = "errorlog.txt";
    private static String verboseLogFile = "verboselog.txt";

    public static void Log(String message)//, boolean isError, boolean verbose)
    {
        try
        {
            message = DateTime.now().toString("yyyy/MM/dd hh:mm:ss") + " " + message;
            System.out.println(message);

            String logFile = errorLogFile;

            //if (verbose)
              //  logFile = verboseLogFile;

            //if (!isError && !verbose)
              //  return;

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            out.println(message);
            out.close();
        }
        catch (IOException e)
        {
            System.out.println(String.format("Failed to write error message to log: %1$s", e.toString()));
        }
    }
}
