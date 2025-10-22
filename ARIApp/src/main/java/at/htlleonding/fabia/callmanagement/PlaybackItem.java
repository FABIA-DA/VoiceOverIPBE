package at.htlleonding.fabia.callmanagement;

import java.util.UUID;

public final class PlaybackItem extends AudioItem {
    private final String mediaName;

    public PlaybackItem(String mediaName, String channelId) {
        super(mediaName, channelId);
        this.mediaName = mediaName;
    }

    @Override
    public void start() {
        if(mediaName == null){
            return;
        }

        CallAudio.playSound(channelId, mediaName);
        ActiveAudioRegistry.getInstance().registerPlayback(this);
    }
}
