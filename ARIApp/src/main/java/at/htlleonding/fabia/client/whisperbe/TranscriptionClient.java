package at.htlleonding.fabia.client.whisperbe;

import at.htlleonding.fabia.callmgmt.util.HttpRequestException;
import at.htlleonding.fabia.client.BaseClient;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class TranscriptionClient extends BaseClient {
    private static TranscriptionClient client = null;

    private static final HttpUrl URL = new HttpUrl.Builder()
            .scheme("http")
            .host("whisper-be")
            .port(8000)
            .addPathSegment("transcribe")
            .build();

    public static TranscriptionClient getClient() {
        if (client == null) {
            client = new TranscriptionClient();
        }
        return client;
    }

    @Override
    protected void configureHttpClient(OkHttpClient.Builder builder) {
        builder.connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.MINUTES)
                .protocols(List.of(Protocol.HTTP_1_1));
    }

    public String transcribe(String filePath) throws IOException {
        try{
            File file = Path.of(filePath).toFile();

            RequestBody fileBody = RequestBody.create(file, MediaType.parse("audio/wav"));

            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(URL)
                    .post(multipartBody)
                    .build();

            System.out.println(request);

            String text = getResponse(request, new TypeReference<WhisperResponse>() {}).getText();
            logger.debug("Transcribed file {} and got \"{}\"", file.getName(), text);
            return text;
        }
        catch (IOException e) {
            logger.error("Error during transcription of file {} {} with {}", filePath, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
