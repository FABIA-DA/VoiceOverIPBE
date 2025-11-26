package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.AriContext;
import at.htlleonding.fabia.callmgmt.util.AriUtil;
import lombok.Getter;

public abstract class AudioItem {
    protected final String name;
    @Getter
    protected final String channelId;
    protected final AriUtil ariUtil;
    protected final ActiveAudioRegistry audioRegistry;

    protected AudioItem(String name, String channelId, AriUtil ariUtil, ActiveAudioRegistry audioRegistry) {
        this.name = name;
        this.channelId = channelId;
        this.ariUtil = ariUtil;
        this.audioRegistry = audioRegistry;
    }

    public String getName() {
        return name;
    }
    public abstract void start();
}
