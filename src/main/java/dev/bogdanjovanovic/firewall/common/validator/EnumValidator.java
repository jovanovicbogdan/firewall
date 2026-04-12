package dev.bogdanjovanovic.firewall.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EnumValidatorImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@NotNull(message = "must not be null")
@ReportAsSingleViolation
public @interface EnumValidator {

  Class<? extends Enum<?>> value();

  String message() default "value is not valid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
