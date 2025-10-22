package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.AriContext;
import ch.loway.oss.ari4java.tools.RestException;

public final class CallAudio {
    public static void startRecording(String channelId, String recordingName) {
        try {
            AriContext.getInstance().channels()
                    .record(channelId, recordingName, "wav")
                    .setMaxDurationSeconds(10)
                    .setMaxSilenceSeconds(3)
                    .setBeep(true)
                    .execute();
        } catch (RestException e) {
            System.err.println("Failed to start recording " + recordingName + " for channel " + channelId);
            e.printStackTrace();
        }
    }

    public static void playSound(String channelId, String sound) {
        try {
            AriContext.getInstance().channels().play(channelId, "sound:" + sound).execute();
        } catch (RestException e) {
            System.out.println("Failed playback for sound " + sound + " for channel " + channelId);
            e.printStackTrace();
        }
    }
}
