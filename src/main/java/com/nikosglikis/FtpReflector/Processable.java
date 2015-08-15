package com.nikosglikis.FtpReflector;

/**
 * Created by User on 15/8/2015.
 */
public class Processable
{
    static final int TYPES_FILE = 1;
    static final int TYPES_DIRECTORY = 2;
    static final int TYPES_LINK = 3;

    String path;
    boolean processed = false;
    protected int type;

    public Processable(String path)
    {
        this.path = path;
    }

    public int getType()
    {
        return type;
    }

    public boolean isProcessed()
    {
        return processed;
    }

    public void setProcessed(boolean processed)
    {
        this.processed = processed;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
