package dev.magurno.dockplanner.common;

import java.util.List;

/** The request is valid but clashes with the current state (HTTP 409). */
public class ConflictException extends RuntimeException {

	private final List<Long> conflictingBookingIds;

	public ConflictException(String message) {
		this(message, List.of());
	}

	public ConflictException(String message, List<Long> conflictingBookingIds) {
		super(message);
		this.conflictingBookingIds = List.copyOf(conflictingBookingIds);
	}

	public List<Long> getConflictingBookingIds() {
		return conflictingBookingIds;
	}

}
