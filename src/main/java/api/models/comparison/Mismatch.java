package api.models.comparison;

public class Mismatch {
    public final String field;
    public final Object expected;
    public final Object actual;

    public Mismatch(String field, Object expected, Object actual) {
        this.field = field;
        this.expected = expected;
        this.actual = actual;
    }
}
