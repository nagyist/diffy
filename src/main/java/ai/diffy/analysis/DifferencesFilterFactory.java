package ai.diffy.analysis;

import java.util.function.Predicate;

public class DifferencesFilterFactory {
    public static Predicate<JoinedField> apply(double relative, double absolute) {
        return field ->
            field.raw().differences() > field.noise().differences()
                && field.relativeDifference() > relative
                && field.absoluteDifference() > absolute;
    }
}
