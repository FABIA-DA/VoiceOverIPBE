package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;

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

        AriUtil.playSound(channelId, mediaName);
        ActiveAudioRegistry.getInstance().registerPlayback(this);
    }
}
