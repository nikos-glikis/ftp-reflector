package com.nikosglikis.FtpReflector;

/**
 * Created by User on 15/8/2015.
 */
public class FtpFile  extends Processable
{
    public FtpFile(String path)
    {
        super(path);
        this.type = Processable.TYPES_FILE;
    }


    public int getType()
    {
        return type;
    }
}
