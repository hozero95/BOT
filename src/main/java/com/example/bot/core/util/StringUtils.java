package com.example.bot.core.util;

import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {
    /**
     * 문자열 리스트에서 suffix 문자 제거 메소드
     *
     * @param list   p1
     * @param suffix p2
     * @return List<String>
     */
    public static List<String> stringListRemoveSuffix(List<String> list, String suffix) {
        return list.stream()
                .map(str -> str.endsWith(suffix) ? str.substring(0, str.length() - suffix.length()) : str)
                .collect(Collectors.toList());
    }
}
