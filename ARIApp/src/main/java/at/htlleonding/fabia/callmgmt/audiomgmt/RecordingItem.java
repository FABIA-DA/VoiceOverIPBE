package at.htlleonding.fabia.callmgmt.audiomgmt;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.Dependent;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents data for a recording, which is created by asterisk
 */
@Dependent
@NoArgsConstructor
public final class RecordingItem extends AudioItem {

    @Override
    public void initialize(String channelId, String mediaName){
        super.initialize("rec-" + UUID.randomUUID(), channelId);
    }

    @Override
    public Uni<Void> startAsync() {
        audioRegistry.registerRecording(this);
        return ariUtil.startRecordingAsync(channelId, getName());
    }
}
