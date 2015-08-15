package com.nikosglikis.FtpReflector;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

import java.util.ArrayList;
import java.util.Vector;

//TODO add path in command line
//TODO maximum threads
//TODO detect end of files and exit.
public class FtpReflector
{
    static ArrayList<FtpWorker> workers = new ArrayList<FtpWorker>();
    static boolean verbose = false;
    static String outputDirectory = "output";
    public static void main(String args[])
    {

        try
        {
            if (args.length != 3)
            {
                System.err.println("Usage: java -cp build/:lib/ftp4j-1.7.2.jar com.nikosglikis.FtpReflector.FtpReflector " + "<IpAddress> <UserName> <Password>");
                System.exit(1);
            }

            // Assignment
            String ipAddress = args[0];
            String userName = args[1];
            String password = args[2];

            System.out.println("Ip Address = " + ipAddress);
            System.out.println("User = " + userName);


            // FTP Program operations start from here
            FTPClient client = null;
            try
            {
                client = new FTPClient();

                client.setSecurity(FTPClient.SECURITY_FTP);

                client.connect(ipAddress);

                client.login(userName, password);
                client.setPassive(true);
                client.noop();

                if (client != null)
                {
                    client.disconnect(true);
                    FtpWorker ftpWorker = new FtpWorker(ipAddress, userName, password, outputDirectory, verbose );
                    ftpWorker.processDirectory(new FtpDirectory(""));
                    ftpWorker.ftpClient.disconnect(true);
                    int threadLimit = 15;
                    for (int i = 0 ; i < threadLimit; i++) {
                        Thread.sleep(100);
                        ftpWorker = new FtpWorker(ipAddress, userName, password, outputDirectory, verbose );
                        ftpWorker.start();
                        workers.add(ftpWorker);
                    }
                    while (true) {
                        int workersCount = getWorkersCountAndRemoveIdle();
                        if (workersCount < threadLimit) {
                            ftpWorker = new FtpWorker(ipAddress, userName, password, outputDirectory, verbose );
                            ftpWorker.start();
                            workers.add(ftpWorker);
                        }
                        System.out.println("Alive workers: " + workersCount);
                        System.out.println("Pending files: "+ftpWorker.listManager.getPendingCount());
                        Thread.sleep(30000);
                    }
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
