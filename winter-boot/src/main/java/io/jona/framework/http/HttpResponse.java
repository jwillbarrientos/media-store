package io.jona.framework.http;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class HttpResponse {
    private static final String CRLF = "\r\n";
    public static final long CHUNK_SIZE_BYTES = 1024 * 256L;
    private static final String SERVER_NAME = "JonaServer";
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    private final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT"));
    private final String date = now.format(ISO_DATE_FORMATTER);
    private final Map<HttpResponseHeader, String> responseHeaders = new HashMap<>();
    @Getter
    private final Map<String, String> cookies = new HashMap<>();

    @Setter
    private HttpCode responseCode;
    private String contentType;
    private MimeType mimeType;
    @Setter
    private String contentDisposition;
    private long headerRangeStart;
    private long headerRangeEnd;
    private long headerRangeTotal;
    @Getter
    private byte[] body;
    private boolean deleteCookies;
    private boolean blockedByInboundFilter;
    private boolean noCache;
    private String redirectUrl;

    public void setContentType(MimeType mimeType, String charset) {
        this.contentType = mimeType.value + "; charset=" + charset;
        this.mimeType = mimeType;
    }

    public void setContentType(MimeType mimeType) {
        this.contentType = mimeType.value;
        this.mimeType = mimeType;
    }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        log.trace("Body for response:\n {}", body);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setBodyWithRange(Path body, long rangeStart, long rangeEnd, long rangeTotal) throws IOException {
        headerRangeStart = rangeStart;
        headerRangeEnd = rangeEnd;
        headerRangeTotal = rangeTotal;
        try (RandomAccessFile file = new RandomAccessFile(body.toFile(), "r")) {
            long fileLength = file.length();
            if (headerRangeEnd > fileLength) {
                headerRangeEnd = fileLength - 1;
            }

            long chunkSize = headerRangeEnd - headerRangeStart + 1;
            if (chunkSize < 0 || chunkSize > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid chunk size: " + chunkSize);
            }

            byte[] buffer = new byte[(int) chunkSize];
            file.seek(headerRangeStart);
            file.readFully(buffer);
            this.body = buffer; // assign to the response body
        }
    }

    public void addCookie(String key, String value) {
        cookies.put(key, value);
    }

    public void deleteCookies() {
        this.deleteCookies = true;
    }

    public void truncateProcessing() {
        this.blockedByInboundFilter = true;
    }

    public boolean blockedByInboundFilter() {
        return blockedByInboundFilter;
    }

    public void noCache() {
        this.noCache = true;
    }

    public byte[] buildResponse() {
        if (!cookies.isEmpty() && deleteCookies) {
            throw new IllegalStateException("Cannot send clear cookies and set cookies in the same request");
        }

        byte[] headerBytes = constructHeaders();
        log.debug("Status: {}", responseCode.code);
        if (body == null)
            return headerBytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(headerBytes);
            baos.write(this.body);
        } catch (IOException ignored) { }

        return baos.toByteArray();
    }

    private byte[] constructHeaders() {
        StringBuilder headers = new StringBuilder("HTTP/1.1 ").append(responseCode.code).append(" ").append(responseCode.desc).append(CRLF);
        headers.append("Connection: close").append(CRLF);
        headers.append(HttpResponseHeader.DATE.headerKey).append(date).append(CRLF);
        headers.append(HttpResponseHeader.SERVER.headerKey).append(SERVER_NAME).append(CRLF);

        if(redirectUrl != null) {
            headers.append(HttpResponseHeader.LOCATION.headerKey).append(redirectUrl).append(CRLF);
        }

        if (body != null) {
            headers.append(HttpResponseHeader.CONTENT_LENGTH.headerKey).append(body.length).append(CRLF);
        }

        if (contentType != null) {
            headers.append(HttpResponseHeader.CONTENT_TYPE.headerKey).append(contentType).append(CRLF);
            if (mimeType.isVideo() && contentDisposition == null) {
                headers.append(HttpResponseHeader.CONTENT_RANGE.headerKey)
                        .append("bytes %s-%s/%s%s".formatted(headerRangeStart, headerRangeEnd, headerRangeTotal, CRLF));
                headers.append(HttpResponseHeader.ACCEPT_RANGES.headerKey).append("bytes").append(CRLF);
            }
        }

        if (contentDisposition != null) {
            headers.append(HttpResponseHeader.CONTENT_DISPOSITION.headerKey).append(contentDisposition).append(CRLF);
        }

        if (noCache) {
            headers.append(HttpResponseHeader.CACHE_CONTROL.headerKey).append("no-store").append(CRLF);
        }

        if (deleteCookies) {
            headers.append(HttpResponseHeader.CLEAR_SITE_DATA.headerKey).append("\"cookies\"").append(CRLF);
        }

        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            headers.append(HttpResponseHeader.SET_COOKIE.headerKey)
                    .append("%s=%s; Path=/%s".formatted(cookie.getKey(), cookie.getValue(), CRLF));
        }

        headers.append(CRLF);
        log.trace("Response HEADERS: \n {}", headers);
        return headers.toString().getBytes(StandardCharsets.US_ASCII);
    }

    public void redirect(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
