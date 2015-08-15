package com.nikosglikis.FtpReflector;

/**
 * Created by User on 15/8/2015.
 */
public class FtpDirectory extends Processable
{
    public FtpDirectory(String path)
    {
        super(path);
        this.type = Processable.TYPES_DIRECTORY;
    }
}
