package com.perunovpavel.util;

import jakarta.servlet.http.HttpServletResponse;

public class ServletUtil {
    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    public static void setResponseHeaders(HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(CHARACTER_ENCODING);
    }
}
