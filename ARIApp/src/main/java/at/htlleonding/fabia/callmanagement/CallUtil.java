package at.htlleonding.fabia.callmanagement;

import at.htlleonding.fabia.AriContext;
import ch.loway.oss.ari4java.ARI;
import ch.loway.oss.ari4java.tools.RestException;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class CallUtil {
    public static void GreetUser(CallSession session) throws RestException {
        ARI ari = AriContext.getInstance();
        ari.channels().play(session.getChannelId(), "sound:greeting").execute();
    }

    public static void HandleGroups(CallSession session, String groupsSpeechName) throws RestException {
        ARI ari = AriContext.getInstance();

        ari.channels().play(session.getChannelId(), "sound:group_prechoose").execute();
        ari.channels().play(session.getChannelId(), "sound:" + groupsSpeechName).execute();
        ari.channels().play(session.getChannelId(), "sound:choose_group").execute();

        HandleRecording(session, "sound:" + groupsSpeechName);
    }

    public static void HandleForms(CallSession session, String formsSpeechName) throws RestException {
        ARI ari = AriContext.getInstance();

        ari.channels().play(session.getChannelId(), "sound:form_prechoose").execute();
        ari.channels().play(session.getChannelId(), "sound:" + formsSpeechName).execute();
        ari.channels().play(session.getChannelId(), "sound:choose_form").execute();

        HandleRecording(session, "sound:" + formsSpeechName);
    }

    public static void FormIntro(CallSession session, String fieldNamesFile) throws RestException {
        ARI ari = AriContext.getInstance();

        ari.channels().play(session.getChannelId(), "sound:form_prerequisites").execute();
        ari.channels().play(session.getChannelId(), "sound:" + fieldNamesFile).execute();
    }

    public static void StartFieldProcessing(CallSession session) throws RestException {
        ARI ari = AriContext.getInstance();

        ari.channels().play(session.getChannelId(), "sound:form_info").execute();
    }

    public static <T> String ConcatItems(Collection<T> list, Function<T, String> stringify) {
        List<String> strings = list.stream().map(stringify).toList();
        return String.join(", ", strings);
    }

    private static void HandleRecording(CallSession session, String recordingName) throws RestException {
        ARI ari = AriContext.getInstance();

        ari.channels()
                .record(session.getChannelId(), recordingName, "wav")
                .setMaxDurationSeconds(10)
                .setMaxSilenceSeconds(3)
                .setBeep(true)
                .execute();

        ari.channels().startMoh(session.getChannelId());
    }
}
