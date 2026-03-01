package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;
import io.smallrye.mutiny.Uni;

/**
 * This item is data to represent a sound, which is played by asterisk.
 */
public final class PlaybackItem extends AudioItem {
    private final String mediaName;

    public PlaybackItem(String mediaName, String bridgeId, AriUtil ariUtil, ActiveAudioRegistry audioRegistry) {
        super(mediaName, bridgeId, ariUtil, audioRegistry);
        this.mediaName = mediaName;
    }

    @Override
    public Uni<Void> startAsync() {
        if(mediaName == null){
            return Uni.createFrom().voidItem();
        }
        audioRegistry.registerPlayback(this);
        return ariUtil.playSoundAsync(bridgeId, mediaName);
    }
}
