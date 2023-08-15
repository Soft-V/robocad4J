package io.github.crackanddie.common;

import java.util.ArrayList;
import java.util.List;

public class ParseChannels
{
    public static List<Float> ParseFloatChannel(String txt)
    {
        List<Float> outList = new ArrayList<>();
        try
        {
            String[] splitted = txt.replace(',', '.').split(";");
            for(String s : splitted)
            {
                outList.add(Float.parseFloat(s));
            }
        }
        catch (Exception e)
        {
            // could be a error :)
        }
        return outList;
    }

    public static List<Boolean> ParseBoolChannel(String txt)
    {
        List<Boolean> outList = new ArrayList<>();
        try
        {
            String[] splitted = txt.split(";");
            for(String s : splitted)
            {
                outList.add(s.equals("1"));
            }
        }
        catch (Exception e)
        {
            // could be a error :)
        }
        return outList;
    }

    public static String JoinFloatChannel(List<Float> lst)
    {
        List<String> strings = new ArrayList<>();
        for (float f : lst)
        {
            strings.add(Float.toString(f));
        }
        return String.join(";", strings);
    }

    public static String JoinBoolChannel(List<Boolean> lst)
    {
        List<String> strings = new ArrayList<>();
        for (boolean b : lst)
        {
            strings.add(b ? "1" : "0");
        }
        return String.join(";", strings);
    }
}
