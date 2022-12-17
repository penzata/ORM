package src.main.java.org.example.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
@interface Index {
    String name() default "";

    String columnList();

    boolean unique() default false;
}
