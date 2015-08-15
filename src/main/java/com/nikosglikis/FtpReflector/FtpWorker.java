package com.nikosglikis.FtpReflector;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

import java.io.File;

public class FtpWorker  extends Thread
{

    public static final int STATUS_IDLE = 1;
    public static final int STATUS_DOWNLOADING = 2;
    //TODO add path in command line
    //TODO maximum threads
    static final ListManager listManager = new ListManager();
    public String username;
    public String host;
    public String password;
    public String outputDirectory;
    public FTPClient ftpClient = new FTPClient();
    public boolean verbose = true;
    public boolean active = true;
    public int status = FtpWorker.STATUS_IDLE;
    private int nullProcessableCounter = 0;
    public FtpWorker(String host, String username, String password, String outputDirectory)
    {
        this.host = host;
        this.username = username;
        this.password = password;
        this.outputDirectory = outputDirectory;
        if (!connect()) {
            active = false;
        }
    }

    public FtpWorker(String host, String username, String password, String outputDirectory, boolean verbose)
    {
        this(host, username, password, outputDirectory);
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void processFile(FtpFile ftpFile)
    {
        try
        {
            if (!ftpClient.isConnected()) {
                die(ftpFile);
            }
            File destinationFile =  new File(outputDirectory+"/"+host+"/"+ftpFile.getPath() );
            //TODO date check
            if (destinationFile.exists()) {
                if (destinationFile.length() != ftpClient.fileSize(ftpFile.getPath())) {
                    System.out.println("Different size, redownloading: " + ftpFile.getPath());
                    downloadFile(ftpFile, destinationFile);
                }
            } else {
                ftpClient.downloadFile(ftpFile, destinationFile);
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            die(ftpFile);
        }
        //TODO download file
    }

    public void downloadFile(FtpFile ftpFile, File destinationFile)
    {
        try
        {
            setStatusDownloading();
            ftpClient.download(ftpFile.getPath(), destinationFile);
            setStatusIdle();
        }
        catch (Exception e)
        {
            die(ftpFile);
        }
    }

    public void die(Processable processable)
    {
        if (processable.getType() == Processable.TYPES_DIRECTORY) {
            listManager.addDirectory(processable.getPath());
        } else {
            listManager.addFile(processable.getPath());
        }
        die();
    }

    public void die()
    {
        try
        {
            ftpClient.disconnect(true);
        }
        catch (Exception e)
        {

        }
        active = false;
    }

    public void processDirectory(FtpDirectory directory)
    {
        try
        {
            String path = directory.getPath();
            File f = new File(outputDirectory+"/"+host+"/"+directory.getPath());

            f.mkdirs();

            if (!ftpClient.isConnected()) {
                die(directory);
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
            //e.printStackTrace();
            //System.out.println("Got error, diconnecting ftp client");
            die(directory);
        }
    }

    public void run()
    {
        try
        {
            while (true)
            {
                if (!active) {
                    //System.out.println("Thread is not active, return");
                    ftpClient.disconnect(true);
                    return;
                }
                if (!ftpClient.isConnected())
                {
                    die();
                    return;
                }

                Processable processable = listManager.getNext();
                if (processable == null )
                {
                    if (nullProcessableCounter++ > 10)
                    {
                        //TODO die gracefully.
                        System.out.println("\nNull Processable, queue empty ?");
                        die();
                    }
                    System.out.print("n");
                    sleep(30000);
                    continue;
                }
                else
                {
                    nullProcessableCounter=0;
                }
                process(processable);
            }
        }
        catch (Exception e)
        {
            try
            {
                ftpClient.disconnect(true);
            }
            catch (Exception ee)
            {

            }
            //e.printStackTrace();
        }
    }

    public boolean connect()
    {
        try
        {
            ftpClient.connect(host);
            ftpClient.login(username, password);
            ftpClient.setType(FTPClient.TYPE_BINARY);
            return true;
        }
        catch (Exception e)
        {
            //System.out.println("Cannot connect, thread dying");
            die();
            //e.printStackTrace();
            //TODO handle bad password
            return false;
        }
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatusIdle()
    {
        status = FtpWorker.STATUS_IDLE;
    }

    public void setStatusDownloading()
    {
        status = FtpWorker.STATUS_DOWNLOADING;
    }
}
