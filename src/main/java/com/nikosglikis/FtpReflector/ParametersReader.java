package com.nikosglikis.FtpReflector;

import org.ini4j.Ini;

import java.io.File;
import java.util.prefs.Preferences;

public class ParametersReader
{
    static private String PARAMS_FILE = "parameters.ini";

    static private String defaultOutputDirectory = "output";
    static private int defaultSleepBetweenThreadChecks = 5;
    static private boolean defaultVerbose = false;

    public static String getOutputDirectory()
    {
        try
        {
            Ini ini = getMainIni();

            String outputPath =  ini.get("FtpReflector", "outputDirectory");
            if (outputPath == null) {
                throw new Exception("Value doesn't exist.");
            }
            return outputPath;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return defaultOutputDirectory;
    }

    public static int getSleepBetweenThreads()
    {
        try
        {
            Ini ini = getMainIni();

            String outputPath =  ini.get("FtpReflector", "sleepBetweenThreadChecks");
            if (outputPath == null) {
                throw new Exception("Value doesn't exist.");
            }
            return ini.get("FtpReflector", "sleepBetweenThreadChecks", int.class);

        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return defaultSleepBetweenThreadChecks;
    }

    public static Ini getMainIni()
    {
        try
        {
            return new Ini(new File(PARAMS_FILE));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static boolean getVerbose()
    {
        try
        {
            Ini ini = getMainIni();

            String verboseString =  ini.get("FtpReflector", "verbose");
            if (verboseString == null)
            {
                throw new Exception("Value doesn't exist.");
            }
            else
            {
                return ini.get("FtpReflector", "verbose", boolean.class);
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        return defaultVerbose;
    }
}
