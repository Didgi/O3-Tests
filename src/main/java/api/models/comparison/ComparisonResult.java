package api.models.comparison;

import java.util.List;

public class ComparisonResult {

    private final List<Mismatch> mismatches;

    public ComparisonResult(List<Mismatch> mismatches) {
        this.mismatches = mismatches;
    }

    public boolean isSuccess() {
        return mismatches.isEmpty();
    }

    public List<Mismatch> getMismatches() {
        return mismatches;
    }

    @Override
    public String toString() {
        if (isSuccess()) return "All fields match";

        StringBuilder sb = new StringBuilder("Mismatches:\n");
        for (Mismatch m : mismatches) {
            sb.append("- ")
                    .append(m.field)
                    .append(": expected=")
                    .append(m.expected)
                    .append(", actual=")
                    .append(m.actual)
                    .append("\n");
        }
        return sb.toString();
    }
}
