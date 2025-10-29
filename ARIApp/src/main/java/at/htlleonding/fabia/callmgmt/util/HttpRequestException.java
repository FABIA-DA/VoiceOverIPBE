package at.htlleonding.fabia.callmgmt.util;

import lombok.Getter;

import java.io.IOException;

@Getter
public class HttpRequestException extends IOException {
    private final int statusCode;
    private final String url;
    private final String responseBody;
    private final String method;

    public HttpRequestException(int statusCode, String url, String method, String responseBody, String message) {
        super(message);
        this.statusCode = statusCode;
        this.url = url;
        this.method = method;
        this.responseBody = responseBody;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP ")
                .append(url)
                .append(" ")
                .append(method)
                .append(" - ")
                .append(statusCode)
                .append(": ")
                .append(responseBody);

        return builder.toString();
    }
}
