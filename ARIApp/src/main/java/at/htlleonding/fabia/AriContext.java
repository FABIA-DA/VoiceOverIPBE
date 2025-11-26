package at.htlleonding.fabia;

import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.AriVersion;
import ch.loway.oss.ari4java.tools.ARIException;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public final class AriContext {
    @Inject
    AriEventHandler ariEventHandler;
    @Inject
    @ConfigProperty(name = "fabia.ari.url")
    String ariUrl;
    @Inject
    @ConfigProperty(name = "fabia.ari.name")
    String stasisApp;
    @Inject
    @ConfigProperty(name = "fabia.ari.user")
    String ariUser;
    @Inject
    @ConfigProperty(name = "fabia.ari.pass")
    String ariPass;

    public ARI ari;

    @PostConstruct
    void init() {
        try {
            ari = ARI.build(ariUrl, stasisApp, ariUser, ariPass, AriVersion.IM_FEELING_LUCKY);
            ari.events().eventWebsocket(stasisApp).execute(ariEventHandler);
        } catch (ARIException e) {
            throw new RuntimeException(e);
        }
    }
}
