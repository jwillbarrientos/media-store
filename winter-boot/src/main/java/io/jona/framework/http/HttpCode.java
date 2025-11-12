package io.jona.framework.http;

public enum HttpCode {

    OK_200(200, "Accepted"),
    NO_CONTENT_204(204, "No Content"),
    PARTIAL_CONTENT_206(206, "Partial Content"),
    FOUND_302(302, "Found"),
    UNAUTHORIZED_401(401, "Unauthorized"),
    NOT_FOUND_404(404, "Not Found"),
    CONFLICT_409(409, "Conflict"),
    SERVER_ERROR_500(500, "Server Error");

    public final int code;
    public final String desc;

    HttpCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
