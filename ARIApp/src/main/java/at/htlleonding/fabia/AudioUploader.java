package at.htlleonding.fabia;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AudioUploader {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Mono<String> sendFile(String url, String filePath) {
        return Mono.fromCallable(() -> {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                throw new IllegalArgumentException("File does not exist or is not a file: " + filePath);
            }

            System.out.println("Starting upload of file: " + filePath + " to " + url);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost uploadFile = new HttpPost(url);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
                HttpEntity multipart = builder.build();
                uploadFile.setEntity(multipart);

                System.out.println("Executing HTTP request...");
                try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    System.out.println("Received status code: " + statusCode);
                    HttpEntity responseEntity = response.getEntity();
                    String responseString = responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                    System.out.println("Response body: " + responseString);

                    if (statusCode >= 200 && statusCode < 300) {
                        JsonNode root = mapper.readTree(responseString);
                        if (root.has("text")) {
                            return root.get("text").asText();
                        } else {
                            throw new IOException("Response JSON does not contain 'text' field");
                        }
                    } else {
                        throw new IOException("Failed with HTTP error code: " + statusCode + " Response: " + responseString);
                    }
                }
            }
        }).onErrorResume(e -> {
            System.err.println("Error during file upload:");
            e.printStackTrace();
            return Mono.empty();
        });
    }
}

