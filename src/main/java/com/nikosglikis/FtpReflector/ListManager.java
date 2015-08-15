package com.nikosglikis.FtpReflector;

import java.util.Stack;
import java.util.Vector;

public class ListManager
{
    private Stack<Processable> toBeProcessed = new Stack<Processable>();
    private Stack<Processable> all = new Stack<Processable>();

    public synchronized void addDirectory(String directory)
    {
        FtpDirectory ftpDirectory = new FtpDirectory(directory);
        all.add(ftpDirectory);
        toBeProcessed.add(ftpDirectory);
    }

    public synchronized void addFile(String filePath)
    {
        FtpFile ftpDirectory = new FtpFile(filePath);
        all.add(ftpDirectory);
        toBeProcessed.add(ftpDirectory);
    }

    public synchronized Processable getNext()
    {
        try
        {
            //TODO what happens in end.

            if (toBeProcessed.size() > 0) {
                Processable processable = toBeProcessed.pop();

                return processable;
            } else {
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
