package com.payroll.util;

import com.payroll.entity.BaseEmployee;
import java.util.*;
import java.util.regex.*;

public class JsonUtil {
    private static final Pattern KV_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}]+))");

    public static HashMap<String, String> parseJson(String json) {
        HashMap<String, String> map = new HashMap<>();
        if (json == null) return map;
        Matcher matcher = KV_PATTERN.matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1);
            String val = matcher.group(2) != null ? matcher.group(2) : matcher.group(3).trim();
            map.put(key, val);
        }
        return map;
    }

    public static String toJsonArray(List<BaseEmployee> list) {
        StringBuilder sb = new StringBuilder("[");
        Iterator<BaseEmployee> it = list.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toJson());
            if (it.hasNext()) sb.append(",");
        }
        return sb.append("]").toString();
    }

    public static String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
