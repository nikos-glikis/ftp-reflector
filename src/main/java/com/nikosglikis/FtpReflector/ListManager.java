package com.nikosglikis.FtpReflector;

import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;

public class ListManager
{
    private LinkedList<Processable> toBeProcessed = new LinkedList<Processable>();
    private LinkedList<Processable> all = new LinkedList<Processable>();

    public synchronized void addDirectory(String directory, int retry)
    {
        FtpDirectory ftpDirectory = new FtpDirectory(directory);
        ftpDirectory.setReTries(retry);
        all.add(ftpDirectory);
        toBeProcessed.add(ftpDirectory);
    }

    public synchronized void addDirectory(String directory)
    {
        this.addDirectory(directory, 0);
    }

    public synchronized void addFile(String filePath, int retry)
    {
        FtpFile ftpFile = new FtpFile(filePath);
        ftpFile.setReTries(retry);
        all.add(ftpFile);
        toBeProcessed.add(ftpFile);
    }

    public synchronized void addFile(String directory)
    {
        this.addFile(directory, 0);
    }

    public synchronized Processable getNext()
    {
        try
        {
            //TODO what happens in end.
            if (toBeProcessed.size() > 0)
            {
                Processable processable = toBeProcessed.pop();
                return processable;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public int getPendingCount()
    {
        return toBeProcessed.size();
    }
}
