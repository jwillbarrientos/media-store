package io.jona.framework.http;

public enum Protocol {
    HTTP_1_1("HTTP/1.1"),
    UNSUPPORTED("UNSUPPORTED");

    public final String desc;

    Protocol(String desc) {
        this.desc = desc;
    }
}
