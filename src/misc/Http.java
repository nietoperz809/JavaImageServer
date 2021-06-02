package misc;

import java.util.Arrays;

public class Http {
    private final String[] httpLines;

    public Http(byte[] packet)
    {
        httpLines = new String(packet).split("\r\n");
    }

    public String getRequest()
    {
        return httpLines[0];
    }

    public String[] getRequestParts()
    {
        return httpLines[0].split(" ");
    }

    public String getRequestedResource()
    {
        return getRequestParts()[1].substring(1);
    }

    public String getHeaderValue(String key)
    {
        key = key.toLowerCase();
        for (String s: httpLines)
        {
            String s2 = s.toLowerCase();
            if (s2.startsWith(key+':'))
            {
                return s.substring(s.indexOf(':')+1).trim();
            }
        }
        return null;
    }

    public String toString()
    {
        return Arrays.toString(httpLines);
    }
}
