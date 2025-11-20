package at.htlleonding.fabia.callmgmt.util;

import at.htlleonding.fabia.AriContext;
import ch.loway.oss.ari4java.tools.RestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public final class AriUtil {
    private static final Logger logger = LoggerFactory.getLogger(AriUtil.class);

    public static void startRecording(String channelId, String recordingName) {
        try {
            AriContext.getInstance().channels()
                    .record(channelId, recordingName, "wav")
                    .setMaxDurationSeconds(10)
                    .setMaxSilenceSeconds(5)
                    .setBeep(true)
                    .execute();
        } catch (RestException e) {
            logger.info("Failed to start recording {} for channel {}: {}", recordingName, channelId, e.getMessage());
        }
    }

    public static void playSound(String channelId, String sound) {
        try {
            AriContext.getInstance().channels().play(channelId, "sound:" + sound).execute();
        } catch (RestException e) {
            logger.info("Failed to start playback {} for channel {}: {}", sound, channelId, e.getMessage());
        }
    }

    public static void hangup(String channelId){
        try{
            AriContext.getInstance().channels().hangup(channelId).execute();
        } catch (RestException e) {
            logger.info("Failed to hangup call for channel {}: {}", channelId, e.getMessage());
        }
    }
}
