package at.htlleonding.fabia.callmgmt.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class Util {
    public static <T> String ConcatItems(Collection<T> list, Function<T, String> stringify) {
        List<String> strings = list.stream().map(stringify).toList();
        return String.join(", ", strings);
    }
}
