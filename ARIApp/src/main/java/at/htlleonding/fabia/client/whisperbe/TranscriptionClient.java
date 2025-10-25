package at.htlleonding.fabia.client.whisperbe;

import at.htlleonding.fabia.client.BaseClient;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class TranscriptionClient extends BaseClient {
    private static TranscriptionClient client = null;

    private static final HttpUrl URL = new  HttpUrl.Builder()
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

    public String transcribe(String filePath) throws IOException {
        System.out.println("Transcription file: " + filePath);
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

        return getResponse(request, new TypeReference<WhisperResponse>() {}).getText();
    }
}
