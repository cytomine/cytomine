package be.cytomine.utils;

import java.math.BigInteger;

import org.apache.commons.text.CaseUtils;

public class SQLUtils {

    public static String toCamelCase( String text) {
        return toCamelCase(text, false);
    }

    public static String toCamelCase( String text, boolean capitalized ) {
        return CaseUtils.toCamelCase(text, capitalized, new char[]{'_'});
    }

    public static Long castToLong(Object o) {
        if (o==null) {
            return null;
        } else if (o instanceof BigInteger) {
            return ((BigInteger)o).longValue();
        } else {
            return Long.parseLong(o.toString());
        }
    }

    public static String toSnakeCase(String str)
    {

        // Empty String
        String result = "";

        // Append first character(in lower case)
        // to result string
        char c = str.charAt(0);
        result = result + Character.toLowerCase(c);

        // Traverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            // Check if the character is upper case
            // then append '_' and such character
            // (in lower case) to result string
            if (Character.isUpperCase(ch)) {
                result = result + '_';
                result
                        = result
                        + Character.toLowerCase(ch);
            }

            // If the character is lower case then
            // add such character into result string
            else {
                result = result + ch;
            }
        }

        // return the result
        return result;
    }
}
