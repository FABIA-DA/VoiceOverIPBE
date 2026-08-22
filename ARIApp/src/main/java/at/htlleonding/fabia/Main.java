package at.htlleonding.fabia;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@QuarkusMain
public class Main implements QuarkusApplication {
    @Inject
    AriContext ariContext;
    @Inject
    @ConfigProperty(name = "fabia.ari.name")
    String stasisApp;
    @Inject
    AriEventHandler ariEventHandler;

    @Override
    public int run(String... args) {
        try {
            ariContext.getAri()
                    .events()
                    .eventWebsocket(stasisApp)
                    .execute(ariEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect ARI WebSocket", e);
        }

        Quarkus.waitForExit();
        return 0;
    }
}