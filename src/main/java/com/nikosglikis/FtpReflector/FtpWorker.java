package com.nikosglikis.FtpReflector;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * Created by User on 15/8/2015.
 */
public class FtpWorker  extends Thread
{
    static final ListManager listManager = new ListManager();
    public String username;
    public String host;
    public String password;
    public FTPClient ftpClient = new FTPClient();
    public boolean verbose = true;
    public FtpWorker(String host, String username, String password)
    {
        this.host = host;
        this.username = username;
        this.password = password;
        connect();
    }

    public FtpWorker(String host, String username, String password, boolean verbose)
    {
        this(host, username, password);
        this.verbose  = verbose;
    }

    public void process(Processable processable)
    {
        try
        {
            if (processable.getType() == Processable.TYPES_DIRECTORY)
            {
                processDirectory((FtpDirectory)processable);
            }
            else if (processable.getType() == Processable.TYPES_FILE)
            {
                processFile((FtpFile)processable);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void processFile(FtpFile ftpFile)
    {
        //TODO download file
    }

    public void processDirectory(FtpDirectory directory)
    {
        try
        {
            String path = directory.getPath();
            if (!ftpClient.isConnected()) {
                try {
                    ftpClient.connect(host);
                    ftpClient.login(username, password);
                } catch (Exception e) {
                    System.out.print("e");
                    processDirectory(directory);
                }
            }
            ftpClient.changeDirectory(path);

            FTPFile[] fileArray = ftpClient.list();

            for (FTPFile file : fileArray)
            {
                if (file != null)
                {
                    if (file.getType() == FTPFile.TYPE_FILE) // File
                    {
                        if (verbose) System.out.println(path + "/" + file.getName());
                        listManager.addFile(path + "/" + file.getName());
                    }
                    else if (file.getType() == FTPFile.TYPE_DIRECTORY) // Directory
                    {
                        if (verbose) System.out.println(path + "/" + file.getName());
                        listManager.addDirectory(path + "/" + file.getName());
                    }
                    else if (file.getType() == FTPFile.TYPE_LINK) // Link
                    {
                        if (verbose)  System.out.println("Link Name = " + file.getName() + " ;Modified Date = "
                                + file.getModifiedDate());
                    }
                }
            }
        }
        catch (Exception e)
        {

            try
            {
                e.printStackTrace();
                //System.out.println(e);
                System.out.println("Got error, diconnecting ftp client");
                ftpClient.disconnect(true);
            }
            catch (Exception ee)
            {
                //e.printStackTrace();
            }

        }
    }

    public void run()
    {
        try
        {
            while (true)
            {

                Processable processable = listManager.getNext();
                if (processable == null ) {
                    System.out.print("n");
                    sleep(1000);
                    continue;
                }
                process(processable);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean connect()
    {
        try
        {
            ftpClient.connect(host);
            ftpClient.login(username, password);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //TODO handle bad password
            return false;
        }
    }
}
