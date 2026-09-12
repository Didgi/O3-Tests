package api.requests.skelethon.requesters;

public record ReadOptions(String representation) {

    public ReadOptions {
        if (representation != null && representation.isBlank()) {
            throw new IllegalArgumentException("representation must not be blank");
        }
    }

    public static ReadOptions defaults() {
        return new ReadOptions(null);
    }

    public static ReadOptions ref() {
        return new ReadOptions("ref");
    }

    public static ReadOptions defaultRepresentation() {
        return new ReadOptions("default");
    }

    public static ReadOptions full() {
        return new ReadOptions("full");
    }

    public static ReadOptions custom(String fields) {
        if (fields == null || fields.isBlank()) {
            throw new IllegalArgumentException("custom representation fields must not be blank");
        }

        return new ReadOptions("custom:(" + fields + ")");
    }

    public boolean isExplicit() {
        return representation != null;
    }
}
