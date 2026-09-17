package api.models.comparison;

public class Rule {
    private final FieldRule rule;
    private final String param;

    public Rule(FieldRule rule, String param) {
        this.rule = rule;
        this.param = param;
    }

    public FieldRule getRule() {
        return rule;
    }

    public String getParam() {
        return param;
    }
}
