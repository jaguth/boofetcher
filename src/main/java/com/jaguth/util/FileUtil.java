package com.jaguth.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FileUtil
{
    public static String ReadUnicodeTextFile(String textFilePath)
    {
        String ret = "";

        try
        {
            FileInputStream is = new FileInputStream(textFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;

            while ((line = br.readLine()) != null)
                ret += line + "\n";

            return ret;
        }
        catch(Exception e)
        {
            LogUtil.Log(String.format("Failed to read text file: %1$s", e.toString()));
            return null;
        }
    }
}
