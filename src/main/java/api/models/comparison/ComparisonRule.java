package api.models.comparison;

import java.util.Map;

public class ComparisonRule {

    private final String requestClass;
    private final String responseClass;
    private final Map<String, Rule> rules;

    public ComparisonRule(String requestClass, String responseClass, Map<String, Rule> rules) {
        this.requestClass = requestClass;
        this.responseClass = responseClass;
        this.rules = rules;
    }

    public Map<String, Rule> getRules() {
        return rules;
    }
}
