package org.apache.pig.contrib.eclipse;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
	
    /**
     * Splits the given string in a list where each element is a line.
     * 
     * @param string string to be splitted.
     * @return list of strings where each string is a line.
     * 
     * @note the new line characters are also added to the returned string.
     */
    public static List<String> splitInLines(String string) {
        List<String> ret = new ArrayList<String>();
        int len = string.length();

        char c;
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < len; i++) {
            c = string.charAt(i);

            buf.append(c);

            if (c == '\r') {
                if (i < len - 1 && string.charAt(i + 1) == '\n') {
                    i++;
                    buf.append('\n');
                }
                ret.add(buf.toString());
                buf=new StringBuilder();
            }
            if (c == '\n') {
                ret.add(buf.toString());
                buf=new StringBuilder();

            }
        }
        if (buf.length() != 0) {
            ret.add(buf.toString());
        }
        return ret;

    }
}
