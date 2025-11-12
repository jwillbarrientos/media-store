package io.jona.framework.http;

public enum MimeType {
    TEXT_HTML("text/html", ".html", true),
    TEXT_CSS("text/css", ".css", true),
    TEXT_JAVASCRIPT("text/javascript", ".js", true),
    TEXT_PLAIN("text/plain", ".txt", true),
    FAVICON("image/x-icon", ".ico", false),
    IMAGE_PNG("image/png", ".png", false),
    IMAGE_JPG("image/jpeg", ".jpg", false),
    VIDEO_MP4("video/mp4", ".mp4", false),
    VIDEO_WEBM("video/webm", ".webm", false),
    VIDEO_OGG("video/ogg", ".ogv", false),
    APPLICATION_JSON("application/json", ".json", true),
    UNKNOWN("", "", false);

    public final String value;
    public final String extension;
    public final boolean requiresEncoding;

    MimeType(String value, String extension, boolean requiresEncoding) {
        this.value = value;
        this.extension = extension;
        this.requiresEncoding = requiresEncoding;
    }

    public boolean isVideo() {
        return this == VIDEO_MP4 || this == VIDEO_WEBM || this == VIDEO_OGG;
    }

    public static MimeType getMimeForExtension(String extension) {
        for (MimeType mimeType : MimeType.values()) {
            if (mimeType.extension.equals(extension)) {
                return mimeType;
            }
        }
        return UNKNOWN;
    }
}
