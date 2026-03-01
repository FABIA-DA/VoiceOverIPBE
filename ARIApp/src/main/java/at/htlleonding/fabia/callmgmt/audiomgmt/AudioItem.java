package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;
import io.smallrye.mutiny.Uni;
import lombok.Getter;

public abstract class AudioItem {
    @Getter
    protected final String name;
    @Getter
    protected final String bridgeId;
    protected final AriUtil ariUtil;
    protected final ActiveAudioRegistry audioRegistry;

    protected AudioItem(String name, String bridgeId, AriUtil ariUtil, ActiveAudioRegistry audioRegistry) {
        this.name = name;
        this.bridgeId = bridgeId;
        this.ariUtil = ariUtil;
        this.audioRegistry = audioRegistry;
    }

    public abstract Uni<Void> startAsync();
}
