package dev.magurno.dockplanner.booking;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeWindowTest {

	private static TimeWindow window(int fromHour, int toHour) {
		return new TimeWindow(at(fromHour), at(toHour));
	}

	private static Instant at(int hour) {
		return Instant.parse("2030-06-14T00:00:00Z").plusSeconds(hour * 3600L);
	}

	@Test
	void rejectsEmptyOrReversedWindows() {
		assertThatThrownBy(() -> window(5, 5)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> window(6, 5)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void overlappingWindows() {
		assertThat(window(8, 12).overlaps(window(10, 14))).isTrue();
		assertThat(window(8, 12).overlaps(window(9, 10))).isTrue();
	}

	@Test
	void backToBackWindowsDoNotOverlap() {
		assertThat(window(8, 12).overlaps(window(12, 16))).isFalse();
		assertThat(window(12, 16).overlaps(window(8, 12))).isFalse();
	}

	@Test
	void freeSlotsWithoutBookingsIsTheWholeWindow() {
		assertThat(window(0, 24).freeSlots(List.of())).containsExactly(window(0, 24));
	}

	@Test
	void freeSlotsAreTheGapsBetweenBookings() {
		List<TimeWindow> busy = List.of(window(14, 18), window(6, 10));

		assertThat(window(0, 24).freeSlots(busy)).containsExactly(window(0, 6), window(10, 14), window(18, 24));
	}

	@Test
	void bookingsCrossingTheEdgesAndOverlappingEachOtherAreHandled() {
		List<TimeWindow> busy = List.of(window(-5, 3), window(10, 15), window(12, 20), window(22, 30));

		assertThat(window(0, 24).freeSlots(busy)).containsExactly(window(3, 10), window(20, 22));
	}

	@Test
	void fullyBookedWindowHasNoFreeSlots() {
		assertThat(window(0, 24).freeSlots(List.of(window(-1, 25)))).isEmpty();
	}

}
