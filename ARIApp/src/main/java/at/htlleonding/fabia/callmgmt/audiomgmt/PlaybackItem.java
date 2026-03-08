package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;
import io.smallrye.mutiny.Uni;
import lombok.Getter;

/**
 * This item is data to represent a sound, which is played by asterisk.
 */
public final class PlaybackItem extends AudioItem {
    public PlaybackItem(String name, String channelId, AriUtil ariUtil, ActiveAudioRegistry audioRegistry, String mediaName) {
        super(mediaName, channelId, ariUtil, audioRegistry);
    }

    @Override
    public Uni<Void> startAsync() {
        if(getName() == null){
            return Uni.createFrom().voidItem();
        }
        audioRegistry.registerPlayback(this);
        return ariUtil.playSoundAsync(getChannelId(), getName(), getName());
    }
}
