package common.retry;

import api.config.Config;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {

    public static <T> T retry(
            Supplier<T> action,
            Predicate<T> condition,
            int maxRetryAmounts,
            long waitMillis
    ) {
        int amounts = 0;
        while (amounts < maxRetryAmounts){
            amounts++;
            final T result = action.get();
            if (condition.test(result)){
                return result;
            }
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Ожидаемое условие: " + condition + " при выполнении " + action + " не получено");
    }

    public static <T> T retry(
            Supplier<T> action,
            Predicate<T> condition
    ) {
        return retry(action,
                condition,
                Integer.parseInt(Config.getProperty("max_retry_amounts")),
                Integer.parseInt(Config.getProperty("timeout_mills")));
    }
}
