package dev.bogdanjovanovic.firewall.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

  private List<String> valueList = null;

  @Override
  public void initialize(final EnumValidator constraintAnnotation) {
    final Class<? extends Enum<?>> enumClass = constraintAnnotation.value();
    final Enum<?>[] enumConstants = enumClass.getEnumConstants();
    valueList = Arrays.stream(enumConstants)
        .map(e -> e.toString().toUpperCase())
        .toList();
  }

  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    return valueList.contains(value.toUpperCase());
  }

}
