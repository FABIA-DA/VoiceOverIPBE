package at.htlleonding.fabia.client.formbe;

import at.htlleonding.fabia.client.BaseClient;
import okhttp3.HttpUrl;

public abstract class FormBaseClient extends BaseClient {
    protected static HttpUrl.Builder getBuilder() {
        return new HttpUrl.Builder()
                .scheme("http")
                .host("form-be")
                .port(8080);
    }
}
