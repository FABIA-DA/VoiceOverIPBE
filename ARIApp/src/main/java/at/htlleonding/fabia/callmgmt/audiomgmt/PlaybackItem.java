package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;

/**
 * This item is data to represent a sound, which is played by asterisk.
 */
public final class PlaybackItem extends AudioItem {
    private final String mediaName;

    public PlaybackItem(String mediaName, String channelId, AriUtil ariUtil, ActiveAudioRegistry audioRegistry) {
        super(mediaName, channelId, ariUtil, audioRegistry);
        this.mediaName = mediaName;
    }

    @Override
    public void start() {
        if(mediaName == null){
            return;
        }

        ariUtil.playSound(channelId, mediaName);
        audioRegistry.registerPlayback(this);
    }
}
