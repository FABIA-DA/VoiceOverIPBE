package at.htlleonding.fabia;

import at.htlleonding.fabia.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class FormHttpClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public FormHttpClient(String baseUrl) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    // FieldType methods
    public FieldType createFieldType(FieldType fieldType) throws IOException, InterruptedException {
        return post("/api/field-types", fieldType, FieldType.class);
    }

    public FieldType getFieldType(Long id) throws IOException, InterruptedException {
        return get("/api/field-types/" + id, FieldType.class);
    }

    public List<FieldType> getAllFieldTypes() throws IOException, InterruptedException {
        return getList("/api/field-types", new TypeReference<List<FieldType>>() {});
    }

    public void deleteFieldType(Long id) throws IOException, InterruptedException {
        delete("/api/field-types/" + id);
    }

    // Field methods
    public Field createField(Field field) throws IOException, InterruptedException {
        return post("/api/fields", field, Field.class);
    }

    public Field getField(Long id) throws IOException, InterruptedException {
        return get("/api/fields/" + id, Field.class);
    }

    public List<Field> getAllFields() throws IOException, InterruptedException {
        return getList("/api/fields", new TypeReference<List<Field>>() {});
    }

    public void deleteField(Long id) throws IOException, InterruptedException {
        delete("/api/fields/" + id);
    }

    // FieldResponse methods
    public FieldResponse createFieldResponse(FieldResponse fieldResponse) throws IOException, InterruptedException {
        return post("/api/field-responses", fieldResponse, FieldResponse.class);
    }

    public FieldResponse getFieldResponse(Long id) throws IOException, InterruptedException {
        return get("/api/field-responses/" + id, FieldResponse.class);
    }

    public List<FieldResponse> getAllFieldResponses() throws IOException, InterruptedException {
        return getList("/api/field-responses", new TypeReference<List<FieldResponse>>() {});
    }

    public void deleteFieldResponse(Long id) throws IOException, InterruptedException {
        delete("/api/field-responses/" + id);
    }

    // SingleChoiceField methods
    public SingleChoiceField createSingleChoiceField(SingleChoiceField singleChoiceField) throws IOException, InterruptedException {
        return post("/api/single-choice-fields", singleChoiceField, SingleChoiceField.class);
    }

    public SingleChoiceField getSingleChoiceField(Long id) throws IOException, InterruptedException {
        return get("/api/single-choice-fields/" + id, SingleChoiceField.class);
    }

    public List<SingleChoiceField> getAllSingleChoiceFields() throws IOException, InterruptedException {
        return getList("/api/single-choice-fields", new TypeReference<List<SingleChoiceField>>() {});
    }

    public void deleteSingleChoiceField(Long id) throws IOException, InterruptedException {
        delete("/api/single-choice-fields/" + id);
    }

    // OptionResponse methods
    public OptionResponse createOptionResponse(OptionResponse optionResponse) throws IOException, InterruptedException {
        return post("/api/option-responses", optionResponse, OptionResponse.class);
    }

    public OptionResponse getOptionResponse(Long id) throws IOException, InterruptedException {
        return get("/api/option-responses/" + id, OptionResponse.class);
    }

    public List<OptionResponse> getAllOptionResponses() throws IOException, InterruptedException {
        return getList("/api/option-responses", new TypeReference<List<OptionResponse>>() {});
    }

    public void deleteOptionResponse(Long id) throws IOException, InterruptedException {
        delete("/api/option-responses/" + id);
    }

    // FieldGroup methods
    public FieldGroup createFieldGroup(FieldGroup fieldGroup) throws IOException, InterruptedException {
        return post("/api/field-groups", fieldGroup, FieldGroup.class);
    }

    public FieldGroup getFieldGroup(Long id) throws IOException, InterruptedException {
        return get("/api/field-groups/" + id, FieldGroup.class);
    }

    public List<FieldGroup> getAllFieldGroups() throws IOException, InterruptedException {
        return getList("/api/field-groups", new TypeReference<List<FieldGroup>>() {});
    }

    public void deleteFieldGroup(Long id) throws IOException, InterruptedException {
        delete("/api/field-groups/" + id);
    }

    // Form methods
    public Form createForm(Form form) throws IOException, InterruptedException {
        return post("/api/forms", form, Form.class);
    }

    public Form getForm(Long id) throws IOException, InterruptedException {
        return get("/api/forms/" + id, Form.class);
    }

    public List<Form> getAllForms() throws IOException, InterruptedException {
        return getList("/api/forms", new TypeReference<List<Form>>() {});
    }

    public void deleteForm(Long id) throws IOException, InterruptedException {
        delete("/api/forms/" + id);
    }

    // Group methods
    public Group createGroup(Group group) throws IOException, InterruptedException {
        return post("/api/groups", group, Group.class);
    }

    public Group getGroup(Long id) throws IOException, InterruptedException {
        return get("/api/groups/" + id, Group.class);
    }

    public List<Group> getAllGroups() throws IOException, InterruptedException {
        return getList("/api/groups", new TypeReference<List<Group>>() {});
    }

    public void deleteGroup(Long id) throws IOException, InterruptedException {
        delete("/api/groups/" + id);
    }


    // Helper methods for HTTP operations
    private <T> T post(String endpoint, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response);
        return objectMapper.readValue(response.body(), responseType);
    }

    private <T> T get(String endpoint, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response);
        return objectMapper.readValue(response.body(), responseType);
    }

    private <T> List<T> getList(String endpoint, TypeReference<List<T>> typeReference) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response);
        return objectMapper.readValue(response.body(), typeReference);
    }

    private void delete(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponse(response);
    }

    private void checkResponse(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }
}
