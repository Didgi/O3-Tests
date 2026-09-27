package common.annotations;

import api.config.Roles;
import common.extensions.UserExtensions;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@ExtendWith(UserExtensions.class)
public @interface WithUser {

    Roles role() default Roles.APP_CONFIG_FORMS;
}