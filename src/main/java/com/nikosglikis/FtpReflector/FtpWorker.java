package com.nikosglikis.FtpReflector;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPConnector;
import it.sauronsoftware.ftp4j.FTPFile;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;

public class FtpWorker  extends Thread
{

    public static final int STATUS_IDLE = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_DIED = 3;

    static final ListManager listManager = new ListManager();
    public static String username;
    public static String host;
    public static String password;
    public static String outputDirectory;

    public static int port;

    public FTPClient ftpClient = new FTPClient();
    public static boolean verbose = true;

    public int status = FtpWorker.STATUS_IDLE;
    private int nullProcessableCounter = 0;

    public FtpWorker(String host, String username, String password, int port)
    {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;

        if (!connect()) {
            setStatusDied();
        }
    }

    public void prepare(String ftpDownloadPath)
    {
        verbose = ParametersReader.getVerbose();
        outputDirectory = ParametersReader.getOutputDirectory();
        outputDirectory = outputDirectory+"/"+host+"_"+username+"_"+port;
        new File(outputDirectory).mkdirs();
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
        try
        {
            if (!ftpClient.isConnected()) {
                die(ftpFile);
            }
            File destinationFile =  new File(new String(outputDirectory+"/"+ftpFile.getPath()) );
            //TODO date check
            if (destinationFile.exists()) {
                if (destinationFile.length() != ftpClient.fileSize(ftpFile.getPath())) {
                    System.out.println("Different size, redownloading: " + ftpFile.getPath());
                    downloadFile(ftpFile, destinationFile);
                }
            } else {
                downloadFile(ftpFile, destinationFile);
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            System.out.println("Error downloading file: " + ftpFile.getPath());
            //die(ftpFile);
        }
    }

    public void downloadFile(FtpFile ftpFile, File destinationFile) throws Exception
    {
        setStatusDownloading();
        ftpClient.download(ftpFile.getPath(), destinationFile);
        setStatusIdle();
    }

    public void die(Processable processable)
    {
        //TODO value in parameters
        if (processable.getReTries() < 3)
        {
            if (processable.getType() == Processable.TYPES_DIRECTORY)
            {
                listManager.addDirectory(processable.getPath(), processable.getReTries() + 1);
            }
            else
            {
                listManager.addFile(processable.getPath(), processable.getReTries() + 1);
            }
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
        setStatusDied();
    }

    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    String decodeUTF8(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }


    public void processDirectory(FtpDirectory directory)
    {
        try
        {
            String path = directory.getPath();
            File f = new File(outputDirectory +"/"+directory.getPath());

            f.mkdirs();

            if (!ftpClient.isConnected()) {
                die(directory);
            }

            ftpClient.changeDirectory(path);

            FTPFile[] fileArray = ftpClient.list();

            for (FTPFile file : fileArray)
            {
                //file = decodeUTF8(file)
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
           // e.printStackTrace();
           System.out.println("Process Directory error. Directory: "+directory.getPath()+"\n Error: "+e.toString());
            //die(directory);
        }
    }

    public void run()
    {
        try
        {
            while (true)
            {
                if (!isActive()) {

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
                    //TODO  value in parameters
                    sleep(5000);
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

    public boolean isActive()
    {
        if (getStatus() == FtpWorker.STATUS_DIED)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public void setStatusIdle()
    {
        status = FtpWorker.STATUS_IDLE;
    }

    public void setStatusDownloading()
    {
        status = FtpWorker.STATUS_DOWNLOADING;
    }

    public void setStatusDied()
    {
        status = FtpWorker.STATUS_DIED;
    }
}
