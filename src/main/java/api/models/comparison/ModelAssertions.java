package api.models.comparison;

import org.assertj.core.api.AbstractAssert;

public class ModelAssertions extends AbstractAssert<ModelAssertions, Object> {

    private final Object request;
    private final Object response;

    private ModelAssertions(Object request, Object response) {
        super(request, ModelAssertions.class);
        this.request = request;
        this.response = response;
    }

    public static ModelAssertions assertThatModels(Object req, Object res) {
        return new ModelAssertions(req, res);
    }

    public ModelAssertions match() {
        ModelComparisonConfigLoader loader =
                new ModelComparisonConfigLoader("model-comparison.properties");

        ComparisonRule rule = loader.getRuleFor(request.getClass());

        if (rule == null) {
            failWithMessage("No config for %s", request.getClass().getSimpleName());
        }

        ComparisonResult result = ModelComparator.compare(
                request,
                response,
                rule.getRules()
        );

        if (!result.isSuccess()) {
            failWithMessage(result.toString());
        }

        return this;
    }
}
