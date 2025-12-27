package at.htlleonding.fabia;

import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.AriVersion;
import ch.loway.oss.ari4java.tools.ARIException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A wrapper for the ari connection, which is established on startup
 */
@ApplicationScoped
public final class AriContext {
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

    @Getter
    private ARI ari;

    @PostConstruct
    void init() {
        try {
            ari = ARI.build(ariUrl, stasisApp, ariUser, ariPass, AriVersion.IM_FEELING_LUCKY);
        } catch (ARIException e) {
            throw new RuntimeException(e);
        }
    }
}
