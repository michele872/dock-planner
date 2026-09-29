package dev.magurno.dockplanner.booking;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookings")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

	private static final Duration DEFAULT_SCHEDULE_SPAN = Duration.ofDays(14);

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@Operation(summary = "Schedule", description = "Bookings overlapping [from, to), by default the next 14 days.")
	@GetMapping
	public List<BookingView> schedule(@RequestParam(required = false) Long dockId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
		Instant start = from != null ? from.toInstant() : Instant.now();
		Instant end = to != null ? to.toInstant() : start.plus(DEFAULT_SCHEDULE_SPAN);
		if (!end.isAfter(start)) {
			throw new IllegalArgumentException("the end of the period must be after its start");
		}
		return bookingService.schedule(dockId, new TimeWindow(start, end));
	}

	@Operation(summary = "Get one booking")
	@GetMapping("/{id}")
	public BookingView get(@PathVariable Long id) {
		return bookingService.get(id);
	}

	@Operation(summary = "Book a dock",
			description = "Fails with 422 if the vehicle does not fit and with 409 if the dock or the vehicle is already booked.")
	@PostMapping
	public ResponseEntity<BookingView> book(@Valid @RequestBody BookingRequest request) {
		BookingView booking = bookingService.book(request);
		return ResponseEntity.created(URI.create("/api/bookings/" + booking.id())).body(booking);
	}

	@Operation(summary = "Cancel a booking", description = "Frees the dock. Cancelling twice has no effect.")
	@PostMapping("/{id}/cancel")
	public BookingView cancel(@PathVariable Long id) {
		return bookingService.cancel(id);
	}

}
