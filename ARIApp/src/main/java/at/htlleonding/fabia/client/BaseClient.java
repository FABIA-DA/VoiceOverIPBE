package at.htlleonding.fabia.client;

import at.htlleonding.fabia.callmgmt.util.HttpRequestException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class BaseClient {
    private final OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private OkHttpClient HTTP_CLIENT;

    protected final static ObjectMapper OBJECT_MAPPER = new ObjectMapperBuilder()
            .registerJodaModule()
            .useIndentation()
            .disableTimestamps()
            .build();

    private OkHttpClient buildHttpClient() {
        OkHttpClient.Builder httpClientBuilder = this.httpClientBuilder;
        configureHttpClient(httpClientBuilder);

        return httpClientBuilder.build();
    }

    protected void configureHttpClient(OkHttpClient.Builder builder) {
    }

    private OkHttpClient getHttpClient() {
        if (HTTP_CLIENT == null) {
            HTTP_CLIENT = buildHttpClient();
        }

        return HTTP_CLIENT;
    }

    protected <T> T getResponse(Request request, TypeReference<T> type) throws IOException {
        logger.info("Trying to call {} with method {}", request.url(), request.method());
        try (Response response = getHttpClient().newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) {
                logger.error("HTTP response code {}, reason {}", response.code(), body);
                throw new HttpRequestException(response.code(), request.url().toString(), request.method(), body, "HTTP Request unsuccessful");
            }

            if (response.code() == 204) {
                logger.debug("No content");
                return null;
            } else {
                return OBJECT_MAPPER.readValue(body, type);
            }
        }
    }
}
