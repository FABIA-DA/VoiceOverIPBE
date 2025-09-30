package at.htlleonding.fabia;

import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.AriVersion;
import ch.loway.oss.ari4java.tools.ARIException;

public final class AriContext {
    public static final String stasisApp = "my-ari-app";
    private static ARI ari;

    public static ARI getInstance() {
        if (ari == null) {
            String ariUrl = "http://asterisk:8088";
            String ariUser = "ariuser";
            String ariPass = "aripass";

            try {
                ari = ARI.build(ariUrl, stasisApp, ariUser, ariPass, AriVersion.IM_FEELING_LUCKY);
            }
            catch (ARIException e) {
                throw new RuntimeException(e);
            }
        }
        return ari;
    }
}
