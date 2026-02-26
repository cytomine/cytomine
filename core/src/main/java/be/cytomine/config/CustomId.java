package be.cytomine.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Custom ID generator annotation that uses the hibernate_sequence
 * and allows pre-assigned identifiers.
 */
@IdGeneratorType(CustomIdentifierGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD})
public @interface CustomId {
}
