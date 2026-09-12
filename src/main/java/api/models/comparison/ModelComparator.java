package api.models.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ModelComparator {

    public static ComparisonResult compare(
            Object request,
            Object response,
            Map<String, Rule> rules
    ) {

        List<Mismatch> mismatches = new ArrayList<>();

        for (Map.Entry<String, Rule> entry : rules.entrySet()) {
            String field = entry.getKey();
            Rule rule = entry.getValue();

            Mismatch mismatch = rule.getRule()
                    .validate(request, response, field, rule.getParam());

            if (mismatch != null) {
                mismatches.add(mismatch);
            }
        }

        return new ComparisonResult(mismatches);
    }
}
