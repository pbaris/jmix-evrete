package gr.netmechanics.jmix.evrete.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated class as an Evrete entity, making it available for use in rule property conditions.
 *
 * @author Panos Bariamis (pbaris)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EvreteEntity {

    /**
     * By default, only properties explicitly annotated with {@link EvreteProperty} are included in rule property conditions.
     * Set this attribute to false in order to include all properties of the class.
     *
     */
    boolean annotatedPropertiesOnly() default true;
}
