package at.htlleonding.fabia;

import com.fasterxml.jackson.core.JsonProcessingException;
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
                    return file;
                })
                .flatMap(file -> Mono.using(
                        HttpClients::createDefault,
                        httpClient -> {
                            HttpPost uploadFile = new HttpPost(url);
                            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                            builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
                            HttpEntity multipart = builder.build();
                            uploadFile.setEntity(multipart);

                            return Mono.fromCallable(() -> httpClient.execute(uploadFile))
                                    .flatMap(response -> {
                                        int statusCode = response.getStatusLine().getStatusCode();
                                        HttpEntity responseEntity = response.getEntity();
                                        String responseString = null;
                                        try {
                                            responseString = responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                                        } catch (IOException e) {
                                            return Mono.<String>error(e);
                                        }
                                        System.out.println("Received status code: " + statusCode);
                                        System.out.println("Response body: >" + responseString + "<");

                                        if (statusCode >= 200 && statusCode < 300) {
                                            // Typed parsing into WhisperResponse
                                            WhisperResponse whisperResponse = null;
                                            try {
                                                whisperResponse = new ObjectMapper().readValue(responseString, WhisperResponse.class);
                                            } catch (JsonProcessingException e) {
                                                return Mono.<String>error(e);
                                            }
                                            return Mono.just(whisperResponse.getText());
                                        } else {
                                            return Mono.<String>error(new IOException("HTTP error " + statusCode + " Response: " + responseString));
                                        }
                                    });
                        },
                        httpClient -> {               // cleanup
                            try {
                                httpClient.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                ))
                .onErrorResume(e -> {
                    System.err.println("Error during file upload:");
                    e.printStackTrace();
                    return Mono.empty();
                });
    }
}

