package api.testdata;

import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.RandomStringUtils;

public final class ObservationValueGenerator {
    private ObservationValueGenerator() {}

    public static TextNode alphabeticText(int length) {
        return TextNode.valueOf(
                RandomStringUtils.secure()
                        .nextAlphabetic(length)
        );
    }
}
