package api.models.comparison;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ModelComparisonConfigLoader {

    private final Properties props = new Properties();

    public ModelComparisonConfigLoader(String file) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(file)) {
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ComparisonRule getRuleFor(Class<?> clazz) {
        String name = clazz.getSimpleName();
        String value = props.getProperty(name);

        if (value == null) return null;

        String[] parts = value.split(":");
        String responseClass = parts[0];
        String rulesPart = parts[1];

        return new ComparisonRule(name, responseClass, parseRules(rulesPart));
    }

    private Map<String, Rule> parseRules(String rulesPart) {
        Map<String, Rule> map = new HashMap<>();

        String[] entries = rulesPart.split(",");

        for (String entry : entries) {
            String[] kv = entry.split("=");

            String field = kv[0].trim();
            String ruleRaw = kv[1].trim();

            String[] ruleParts = ruleRaw.split(":");

            FieldRule rule = FieldRule.valueOf(ruleParts[0].toUpperCase());
            String param = ruleParts.length > 1 ? ruleParts[1] : null;

            map.put(field, new Rule(rule, param));
        }

        return map;
    }
}
