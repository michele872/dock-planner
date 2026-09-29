package dev.magurno.dockplanner.booking;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A half-open time interval [start, end). Two windows that only touch
 * (one ends when the other starts) do not overlap, so a vehicle can arrive at a
 * dock at the exact moment the previous one leaves.
 */
public record TimeWindow(Instant start, Instant end) {

	public TimeWindow {
		if (start == null || end == null || !end.isAfter(start)) {
			throw new IllegalArgumentException("end must be after start");
		}
	}

	public boolean overlaps(TimeWindow other) {
		return start.isBefore(other.end) && other.start.isBefore(end);
	}

	public Duration duration() {
		return Duration.between(start, end);
	}

	/**
	 * Returns the free gaps inside this window, given the busy windows
	 * (in any order, possibly overlapping each other or the edges).
	 */
	public List<TimeWindow> freeSlots(List<TimeWindow> busy) {
		List<TimeWindow> free = new ArrayList<>();
		Instant cursor = start;
		for (TimeWindow taken : busy.stream().sorted(Comparator.comparing(TimeWindow::start)).toList()) {
			if (taken.start.isAfter(cursor)) {
				Instant gapEnd = taken.start.isBefore(end) ? taken.start : end;
				if (gapEnd.isAfter(cursor)) {
					free.add(new TimeWindow(cursor, gapEnd));
				}
			}
			if (taken.end.isAfter(cursor)) {
				cursor = taken.end;
			}
			if (!cursor.isBefore(end)) {
				return free;
			}
		}
		free.add(new TimeWindow(cursor, end));
		return free;
	}

}
