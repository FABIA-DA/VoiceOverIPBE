package at.htlleonding.fabia.callmgmt.audiomgmt;

import at.htlleonding.fabia.callmgmt.util.AriUtil;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import lombok.Getter;

public abstract class AudioItem {
    @Getter
    protected String name;
    @Getter
    protected String channelId;
    @Inject
    protected AriUtil ariUtil;
    @Inject
    protected ActiveAudioRegistry audioRegistry;

    protected AudioItem() {
    }

    protected void initialize(String name, String channelId) {
        this.name = name;
        this.channelId = channelId;
    }

    public abstract Uni<Void> startAsync();
}
