package src.main.java.org.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name() default "";

    String catalog() default "";

    String schema() default "";

    UniqueConstraint[] uniqueConstraints() default {};

    Index[] indexes() default {};

}
@Target({})
@Retention(RetentionPolicy.RUNTIME)
 @interface Index {
    String name() default "";

    String columnList();

    boolean unique() default false;
}
@Target({})
@Retention(RetentionPolicy.RUNTIME)
 @interface UniqueConstraint {
    String name() default "";

    String[] columnNames();
}
