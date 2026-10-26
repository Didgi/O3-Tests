package metrics;

public final class TestClassifier {
    private TestClassifier() {}

    public static String classify(Class<?> clazz) {
        String pkg = clazz.getPackageName();
        if (pkg.equals("api") || pkg.startsWith("api.")) return "API";
        if (pkg.equals("ui") || pkg.startsWith("ui.")) return "UI";
        return "UNCLASSIFIED";
    }
}
