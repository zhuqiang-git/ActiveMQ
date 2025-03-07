

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface API {
    Status status();

    String since() default "";

    String[] consumers() default {"*"};

    public static enum Status {
        INTERNAL,
        DEPRECATED,
        EXPERIMENTAL,
        MAINTAINED,
        STABLE;

        private Status() {
        }
    }
}
