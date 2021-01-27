package nil.nadph.qnotified.util;

import androidx.annotation.NonNull;

import java.util.*;

/**
 * Utils stolen from com.bug.zqq
 *
 * @author BUG
 */
public class BugUtils {
    
    /**
     * @param size in bytes
     * @return A human readable string for the size
     */
    @NonNull
    public static String getSizeString(long size) {
        if (size < 0) {
            return "0B";
        }
        if (size < 1024) {
            return size + "B";
        }
        LinkedHashMap<Long, String> map = new LinkedHashMap<>();
        map.put(1152921504606846976L, "EB");
        map.put(1125899906842624L, "PB");
        map.put(1099511627776L, "TB");
        map.put(1073741824L, "GB");
        map.put(1048576L, "MB");
        map.put(1024L, "KB");
        for (Map.Entry<Long, String> entry : map.entrySet()) {
            long longValue = entry.getKey();
            String str = entry.getValue();
            if (size >= longValue) {
                String format = String.format("%.2f", ((double) size) / ((double) longValue));
                int indexOf = format.indexOf(".00");
                if (indexOf != -1) {
                    return format.substring(0, indexOf) + str;
                }
                return format + str;
            }
        }
        return "0B";
    }
}
