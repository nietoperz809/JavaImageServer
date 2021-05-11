/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Administrator
 */
public class UrlEncodeUTF8
{
    static public final String utf8 = StandardCharsets.UTF_8.name();

    public static String transform(String in)
    {
        try
        {
            return URLEncoder.encode (in, utf8);
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
    }

    public static String retransform(String in)
    {
        try
        {
            return URLDecoder.decode (in, utf8);
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
    }
    
}
