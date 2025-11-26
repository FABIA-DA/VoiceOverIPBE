package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.AriContext;
import ch.loway.oss.ari4java.tools.RestException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@ApplicationScoped
public final class AriUtil {
    @Inject
    AriContext ariContext;

    private final Logger logger = LoggerFactory.getLogger(AriUtil.class);

    public void startRecording(String channelId, String recordingName) {
        try {
            ariContext.ari.channels()
                    .record(channelId, recordingName, "wav")
                    .setMaxDurationSeconds(10)
                    .setMaxSilenceSeconds(5)
                    .setBeep(true)
                    .execute();
        } catch (RestException e) {
            logger.info("Failed to start recording {} for channel {}: {}", recordingName, channelId, e.getMessage());
        }
    }

    public void playSound(String channelId, String sound) {
        try {
            ariContext.ari.channels().play(channelId, "sound:" + sound).execute();
        } catch (RestException e) {
            logger.info("Failed to start playback {} for channel {}: {}", sound, channelId, e.getMessage());
        }
    }

    public void hangup(String channelId) {
        try {
            ariContext.ari.channels().hangup(channelId).execute();
        } catch (RestException e) {
            logger.info("Failed to hangup call for channel {}: {}", channelId, e.getMessage());
        }
    }
}

