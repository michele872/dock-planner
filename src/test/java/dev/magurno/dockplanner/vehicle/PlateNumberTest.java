package dev.magurno.dockplanner.vehicle;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PlateNumberTest {

	@ParameterizedTest
	@ValueSource(strings = { "AB123CD", "FA123BC", "GD654NP", "ZZ999ZZ" })
	void acceptsCurrentItalianPlates(String plate) {
		assertThat(PlateNumber.Validator.isValid(plate)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "AB123IO", "QU123AB", "AB12CD", "AB1234C", "ab123cd", "AB 123 CD", "MI123456" })
	void rejectsWrongFormatOrLettersNeverUsed(String plate) {
		assertThat(PlateNumber.Validator.isValid(plate)).isFalse();
	}

}
