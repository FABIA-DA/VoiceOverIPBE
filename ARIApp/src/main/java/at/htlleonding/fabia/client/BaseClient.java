package at.htlleonding.fabia.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;

public abstract class BaseClient {
    protected final static OkHttpClient HTTP_CLIENT = new OkHttpClient();
    protected final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected static <T> T getResponse(Request request, TypeReference<T> type) throws IOException {
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Request failed: " + response.code() + " " + response.message());
            }

            if(response.code() == 204){
                return null;
            }
            else{
                return OBJECT_MAPPER.readValue(response.body().string(), type);
            }
        }
    }
}
