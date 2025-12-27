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

    /**
     * Asks asterisk to start a new recording.
     * @param channelId The channel id of the caller to record
     * @param recordingName The name of the recording
     */
    public void startRecording(String channelId, String recordingName) {
        try {
            ariContext.getAri().channels()
                    .record(channelId, recordingName, "wav")
                    .setMaxDurationSeconds(10)
                    .setMaxSilenceSeconds(5)
                    .setBeep(true)
                    .execute();
        } catch (RestException e) {
            logger.info("Failed to start recording {} for channel {}: {}", recordingName, channelId, e.getMessage());
        }
    }

    /**
     * Asks asterisk to play a specific sound.
     * @param channelId The channel id of the caller to play the sound for
     * @param sound The name of the sound
     */
    public void playSound(String channelId, String sound) {
        try {
            ariContext.getAri()
                    .channels()
                    .play(channelId, "sound:" + sound)
                    .execute();
        } catch (RestException e) {
            logger.info("Failed to start playback {} for channel {}: {}", sound, channelId, e.getMessage());
        }
    }

    /**
     * Tries to hang up a call in asterisk.
     * @param channelId The id of the callers channel
     */
    public void hangup(String channelId) {
        try {
            ariContext.getAri()
                    .channels()
                    .hangup(channelId)
                    .execute();
        } catch (RestException e) {
            logger.info("Failed to hangup call for channel {}: {}", channelId, e.getMessage());
        }
    }
}

