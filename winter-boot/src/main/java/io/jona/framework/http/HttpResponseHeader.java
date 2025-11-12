package io.jona.framework.http;

public enum HttpResponseHeader {
    DATE("Date"),
    SERVER("Server"),
    CONTENT_TYPE("Content-Type"),
    CONTENT_RANGE("Content-Range"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_DISPOSITION("Content-Disposition"),
    LOCATION("Location"),
    ACCEPT_RANGES("Accept-Ranges"),
    SET_COOKIE("Set-Cookie"),
    CLEAR_SITE_DATA("Clear-Site-Data"),
    CACHE_CONTROL("Cache-Control");

    public final String headerKey;

    HttpResponseHeader(String headerKey) {
        this.headerKey = headerKey + ": ";
    }
}
