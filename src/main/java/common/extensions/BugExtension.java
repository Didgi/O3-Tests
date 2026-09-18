package common.extensions;

import common.annotations.Bug;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class BugExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Bug errors = context
                .getElement()
                .map(el -> el.getAnnotation(Bug.class)).orElse(null);

        if (errors == null){
            return ConditionEvaluationResult.enabled("Дефект отсутствует");
        } else {
            return ConditionEvaluationResult.disabled("Тест не запущен из-за дефекта");
        }
    }
}
