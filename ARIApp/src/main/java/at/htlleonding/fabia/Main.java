package at.htlleonding.fabia;

import at.htlleonding.fabia.callmgmt.CallProcessor;
import at.htlleonding.fabia.callmgmt.SessionManager;
import at.htlleonding.fabia.callmgmt.audiomgmt.ActiveAudioRegistry;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import ch.loway.oss.ari4java.tools.RestException;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@QuarkusMain
public class Main implements QuarkusApplication {
    @Inject
    AriContext ariContext;
    @Inject
    SessionManager sessionManager;
    @Inject
    ActiveAudioRegistry activeAudioRegistry;
    @Inject
    CallProcessor callProcessor;
    @Inject
    AriUtil arUtil;
    @Inject
    @ConfigProperty(name = "fabia.ari.name")
    String stasisApp;
    @Inject
    AriUtil ariUtil;

    @Override
    public int run(String... args) {
        try {
            ariContext.getAri()
                    .events()
                    .eventWebsocket(stasisApp)
                    .execute(new AriEventHandler(sessionManager, activeAudioRegistry, callProcessor, ariUtil));
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect ARI WebSocket", e);
        }

        Quarkus.waitForExit();
        return 0;
    }
}