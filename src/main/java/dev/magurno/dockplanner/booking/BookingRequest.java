package dev.magurno.dockplanner.booking;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(
		@NotNull Long dockId,
		@NotNull Long vehicleId,
		@NotNull OffsetDateTime arrival,
		@NotNull OffsetDateTime departure) {

	TimeWindow window() {
		if (!departure.isAfter(arrival)) {
			throw new IllegalArgumentException("departure must be after arrival");
		}
		return new TimeWindow(arrival.toInstant(), departure.toInstant());
	}

}
