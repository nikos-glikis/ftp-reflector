package com.nikosglikis.FtpReflector;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

import java.util.Vector;

/**
 * Created by User on 15/8/2015.
 */

public class FtpReflector
{
    public static void main(String args[])
    {
        try
        {
            if (args.length != 3)
            {
                System.err.println("Usage: java FTP_ListFiles " + "<IpAddress> <UserName> <Password>");
                System.err.println("Example: java FTP_ListFiles 1.2.3.4 other other");
                System.exit(1);
            }

            // Assignment
            String ipAddress = args[0];
            String userName = args[1];
            String password = args[2];

            System.out.println("Ip Address = " + ipAddress);
            System.out.println("User = " + userName);
            //System.out.println("Pass = " + password);

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
                    FtpWorker ftpWorker = new FtpWorker(ipAddress, userName, password, false);
                    ftpWorker.processDirectory(new FtpDirectory(""));
                    ftpWorker.ftpClient.disconnect(true);
                    for (int i = 0 ; i < 25; i++) {
                        Thread.sleep(100);
                        ftpWorker = new FtpWorker(ipAddress, userName, password, false);
                        ftpWorker.start();
                    }

                    //listFilesFtpFiles(client, "");
                }
            }
            catch (Exception e)
            {
                System.err.println("ERROR : Error in Connecting to Remote Machine... Hence exitting...");
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
            //System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void listFilesFtpFiles(FTPClient ftpClient, String path)
    {
        try
        {
            System.out.println(path);
            ftpClient.changeDirectory(path);
            Vector<String> directories = new Vector<String>();
            /* List all file inside the directory */
            FTPFile[] fileArray = ftpClient.list();

            for (int i = 0; i < fileArray.length; i++)
            {
                FTPFile file = fileArray[i];
                if (file != null)
                {
                    if (file.getType() == FTPFile.TYPE_FILE) // File
                    {
                        System.out.println(path+"/"+file.getName());

                        //System.out.println("File Name = " + file.getName() + " ; File Size = " + file.getSize()
                          //      + " ;Modified Date = " + file.getModifiedDate());
                    }
                    else if (file.getType()== FTPFile.TYPE_DIRECTORY) // Directory
                    {
                        directories.add(path + "/" + file.getName());

                        //System.out.println("Directory Name = " + file.getName() + " ; Directory Size = " + file.getSize() + " ;Modified Date = " + file.getModifiedDate());
                    }
                    else if (file.getType() == FTPFile.TYPE_LINK) // Link
                    {
                        System.out.println("Link Name = " + file.getName() + " ;Modified Date = "
                                + file.getModifiedDate());
                    }
                }

            }
            for (int  i = 0 ; i<directories.size(); i++)
            {
                listFilesFtpFiles(ftpClient, directories.get(i));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
