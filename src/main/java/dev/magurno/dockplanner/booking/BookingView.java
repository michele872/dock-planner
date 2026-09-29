package dev.magurno.dockplanner.booking;

import java.time.Instant;

public record BookingView(
		Long id,
		Long dockId,
		String dockCode,
		Long vehicleId,
		String vehicleName,
		Instant arrival,
		Instant departure,
		BookingStatus status) {

	static BookingView from(Booking booking) {
		return new BookingView(booking.getId(), booking.getDock().getId(), booking.getDock().getCode(),
				booking.getVehicle().getId(), booking.getVehicle().getName(), booking.getArrival(),
				booking.getDeparture(), booking.getStatus());
	}

}
