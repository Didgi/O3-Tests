package common.extensions;

import api.config.Config;
import common.annotations.BrowserAnnotation;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BrowserExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(
            ExtensionContext context
    ) {
        if (context.getTestMethod().isEmpty()) {
            System.out.println("из baseTest");
            return ConditionEvaluationResult.enabled(
                    "Проверка выполняется на уровне метода"
            );
        }

        Optional<BrowserAnnotation> annotation =
                AnnotationSupport.findAnnotation(
                        context.getRequiredTestMethod(),
                        BrowserAnnotation.class
                );

        if (annotation.isEmpty()) {
            System.out.println("из теста");
            return ConditionEvaluationResult.enabled(
                    "Ограничений по браузеру нет"
            );
        }

        List<String> availableBrowsers =
                Arrays.stream(Config.getProperty("browsers").split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList();

        String[] allowedBrowsersForTest = annotation.get().browsers();

        if (allowedBrowsersForTest.length == 0) {
            return ConditionEvaluationResult.enabled(
                    "Ограничений по браузеру нет"
            );
        }

        boolean available = Arrays.stream(allowedBrowsersForTest)
                .map(String::trim)
                .map(String::toLowerCase)
                .anyMatch(availableBrowsers::contains);

        return available
                ? ConditionEvaluationResult.enabled(
                "Запуск разрешён. Найден подходящий браузер в конфигурации"
        )
                : ConditionEvaluationResult.disabled(
                "Запуск не разрешён. Нет совпадений между браузерами из конфигурации "
                        + availableBrowsers + " и аннотации " + Arrays.toString(allowedBrowsersForTest)
        );
    }
}