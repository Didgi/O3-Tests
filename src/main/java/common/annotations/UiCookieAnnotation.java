package common.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
})
public @interface UiCookieAnnotation {
    boolean enabled() default true;
}
