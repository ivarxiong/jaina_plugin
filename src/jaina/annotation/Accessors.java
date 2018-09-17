package jaina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container for settings for the generation of getters and setters.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/experimental/Accessors.html">the project lombok features page for &#64;Accessors</a>.
 * <p>
 * Using this annotation does nothing by itself; an annotation that makes lombok generate getters and setters,
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Accessors {
    /**
     * If true, accessors will be named after the field and not include a <code>get</code> or <code>set</code>
     * prefix. If true and <code>chain</code> is omitted, <code>chain</code> defaults to <code>true</code>.
     * <strong>default: false</strong>
     */
    boolean fluent() default false;

    /**
     * If true, setters return <code>this</code> instead of <code>void</code>.
     * <strong>default: false</strong>, unless <code>fluent=true</code>, then <strong>default: true</code>
     */
    boolean chain() default false;

    /**
     * If present, only fields with any of the stated prefixes are given the getter/setter treatment.
     * Note that a prefix only counts if the next character is NOT a lowercase character or the last
     * letter of the prefix is not a letter (for instance an underscore). If multiple fields
     * all turn into the same name when the prefix is stripped, an error will be generated.
     */
    String[] prefix() default {};
}
