package dev.magurno.dockplanner.vehicle;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * A current-format Italian licence plate: two letters, three digits, two
 * letters (e.g. AB123CD). The letters I, O, Q and U are never used, because
 * they are too easy to confuse with digits or with each other.
 */
@Documented
@Constraint(validatedBy = PlateNumber.Validator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface PlateNumber {

	String message() default "must be a valid Italian licence plate, e.g. AB123CD";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	final class Validator implements ConstraintValidator<PlateNumber, String> {

		private static final String LETTER = "[A-HJ-NPR-TV-Z]";

		private static final Pattern FORMAT = Pattern.compile(LETTER + "{2}[0-9]{3}" + LETTER + "{2}");

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return value == null || isValid(value);
		}

		public static boolean isValid(String value) {
			return FORMAT.matcher(value).matches();
		}

	}

}
