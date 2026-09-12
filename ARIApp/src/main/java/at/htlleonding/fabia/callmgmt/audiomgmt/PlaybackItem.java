package at.htlleonding.fabia.callmgmt.audiomgmt;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import lombok.NoArgsConstructor;

/**
 * This item is data to represent a sound, which is played by asterisk.
 */
@Dependent
@NoArgsConstructor
public final class PlaybackItem extends AudioItem {

    @Override
    public void initialize(String name, String channelId) {
        super.initialize(name, channelId);
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
