package at.htlleonding.fabia.callmanagement;

import java.util.UUID;

public final class RecordingItem extends AudioItem {

    public RecordingItem(String channelId) {
        super("rec-" + UUID.randomUUID(), channelId);
    }

    @Override
    public void start() {
        CallAudio.startRecording(channelId, getName());
        ActiveAudioRegistry.getInstance().registerRecording(this);
    }
}
