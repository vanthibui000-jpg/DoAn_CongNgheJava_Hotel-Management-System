package com.example.do_an_java.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public final class PaginationUtil {
    public static final int PAGE_SIZE = 15;

    private PaginationUtil() {
    }

    public static <T> void paginate(Model model, List<T> source, int page, HttpServletRequest request) {
        int totalItems = source.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));
        int start = Math.min(currentPage * PAGE_SIZE, totalItems);
        int end = Math.min(start + PAGE_SIZE, totalItems);
        int firstItem = totalItems == 0 ? 0 : start + 1;

        model.addAttribute("items", source.subList(start, end));
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("firstItem", firstItem);
        model.addAttribute("lastItem", end);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("pageNumbers", IntStream.range(0, totalPages).boxed().toList());
        model.addAttribute("pageBaseUrl", buildPageBaseUrl(request));
    }

    private static String buildPageBaseUrl(HttpServletRequest request) {
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String key = entry.getKey();
            if ("page".equals(key)) {
                continue;
            }

            for (String value : entry.getValue()) {
                if (value == null || value.isBlank()) {
                    continue;
                }

                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(encode(key)).append('=').append(encode(value));
            }
        }

        return request.getRequestURI() + (query.length() == 0 ? "?page=" : "?" + query + "&page=");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
