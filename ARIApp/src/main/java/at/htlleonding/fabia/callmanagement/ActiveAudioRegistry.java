package at.htlleonding.fabia.callmanagement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ActiveAudioRegistry {
    private static ActiveAudioRegistry instance;
    private final Map<String, AudioItem> activePlaybacks = new ConcurrentHashMap<>();
    private final Map<String, AudioItem> activeRecordings = new ConcurrentHashMap<>();

    public static ActiveAudioRegistry getInstance() {
        if (instance == null) {
            instance = new ActiveAudioRegistry();
        }
        return instance;
    }

    public void registerRecording(AudioItem item) {
        activeRecordings.put(item.getName(), item);
    }

    public void registerPlayback(AudioItem item) {
        activePlaybacks.put(item.getName(), item);
    }

    public AudioItem getRecording(String name) {
        return activeRecordings.remove(name);
    }

    public AudioItem getPlayback(String name) {
        return activePlaybacks.remove(name);
    }
}
