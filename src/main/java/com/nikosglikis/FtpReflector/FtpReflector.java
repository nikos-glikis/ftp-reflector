package com.nikosglikis.FtpReflector;

import it.sauronsoftware.ftp4j.FTPClient;

import java.util.ArrayList;

//TODO add path in command line
//TODO maximum threads
//TODO detect end of files and exit.
//TODO output directory should be more dynamic according to arguments

public class FtpReflector
{
    static ArrayList<FtpWorker> workers = new ArrayList<FtpWorker>();

    /** How often to check for the number of threads.*/
    static int sleepBetweenThreadChecks;

    static String ftpHost;
    static String ftpUsername;
    static String ftpPassword;
    static String ftpPathToDownload;
    static int ftpPort;

    public static void main(String args[])
    {
        try
        {
            readArguments(args);
            readParams();

            try
            {
                FtpWorker ftpWorker = new FtpWorker(ftpHost, ftpUsername, ftpPassword, ftpPort);
                ftpWorker.prepare(ftpPathToDownload);

                ftpWorker.processDirectory(new FtpDirectory(ftpPathToDownload));
                ftpWorker.ftpClient.disconnect(true);
                int threadLimit = 15;
                for (int i = 0 ; i < threadLimit; i++) {
                    Thread.sleep(100);
                    ftpWorker = new FtpWorker(ftpHost, ftpUsername, ftpPassword, ftpPort);
                    ftpWorker.start();
                    workers.add(ftpWorker);
                }

                while (true)
                {
                    int workersCount = getWorkersCountAndRemoveIdle();
                    if (workersCount < threadLimit)
                    {
                        for (int i = workersCount; i < threadLimit; i++)
                        {
                            ftpWorker = new FtpWorker(ftpHost, ftpUsername, ftpPassword, ftpPort);
                            ftpWorker.start();
                            workers.add(ftpWorker);
                        }
                        workersCount = getWorkersCountAndRemoveIdle();
                    }
                    System.out.println("Alive workers: " + workersCount + " Pending files: "+ftpWorker.listManager.getPendingCount());
                    Thread.sleep(sleepBetweenThreadChecks*1000);
                }
            }
            catch (Exception e)
            {
                System.err.println("ERROR : Error in Connecting to Remote Machine. Exception information below: ");
                e.printStackTrace();
                System.exit(2);
            }
            finally
            {
                try
                {
                    //client.disconnect(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void readArguments(String args[])
    {
        if (args.length < 3)
        {
            System.err.println("Usage: java -cp build/:lib/ftp4j-1.7.2.jar com.nikosglikis.FtpReflector.FtpReflector " + "<IpAddress> <UserName> <Password> <port>");
            System.exit(1);
        }

        // Assignment
        ftpHost = args[0];
        ftpUsername = args[1];
        ftpPassword = args[2];
        int port = 21;
        if (args.length > 3)
        {
            ftpPathToDownload = args[3];
            ftpPathToDownload = FtpReflectorHelper.rTrim(ftpPathToDownload, '/');
        }
        else
        {
            ftpPathToDownload = "";
        }
        ftpPort = 21;
        if (args.length == 5)
        {
            try
            {
                ftpPort = Integer.parseInt(args[4]);
            }
            catch (Exception e)
            {
                ftpPort = 21;
            }

        }

        System.out.println("Ip Address = " + ftpHost);
        System.out.println("User = " + ftpUsername);
    }

    /**
     * Reads params from parameters.ini and sets global variables.
     */
    static void readParams()
    {
        sleepBetweenThreadChecks = ParametersReader.getSleepBetweenThreads();
    }

    static public int getWorkersCountAndRemoveIdle()
    {
        int counter=0;
        ArrayList<FtpWorker> idleWorkers = new ArrayList<FtpWorker>();
        for (FtpWorker worker : workers)
        {
            if (!worker.isActive())
            {
                idleWorkers.add(worker);
            }
            else
            {
                counter++;
            }
        }

        for (FtpWorker worker : idleWorkers)
        {
            workers.remove(worker);
        }

        return counter;
    }
}
