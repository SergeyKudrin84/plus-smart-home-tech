package analyzer.service;

import analyzer.model.ConditionOperation;
import org.springframework.stereotype.Component;

@Component
public class ConditionEvaluator {

    public boolean evaluate(
            int actualValue,
            ConditionOperation operation,
            int expectedValue
    ) {
        return switch (operation) {
            case EQUALS -> actualValue == expectedValue;
            case GREATER_THAN -> actualValue > expectedValue;
            case LOWER_THAN -> actualValue < expectedValue;
        };
    }
}