package common.annotations;

import common.extensions.OpenMrsFixtureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(OpenMrsFixtureExtension.class)
public @interface WithPatient {
}
