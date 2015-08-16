package com.nikosglikis.FtpReflector;

public class FtpReflectorHelper
{
    static String rTrim(String s, char character)
    {
        int i,c=0,res,j;
        for(i=s.length()-1;i>=0;i--)
        {
            char ch=s.charAt(i);   //charAt() treats string like an array
            if(ch!=character)
                break;
        }
        StringBuffer sb = new StringBuffer();
        for(j=0;j<i+1;j++)
        {
            sb.append(s.charAt(j));
        }
        return sb.toString();
    }
}
