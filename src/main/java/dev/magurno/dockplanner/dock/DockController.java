package dev.magurno.dockplanner.dock;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import dev.magurno.dockplanner.dock.DockDtos.DockRequest;
import dev.magurno.dockplanner.dock.DockDtos.DockView;
import dev.magurno.dockplanner.booking.BookingService;
import dev.magurno.dockplanner.booking.TimeWindow;
import dev.magurno.dockplanner.common.ConflictException;
import dev.magurno.dockplanner.common.NotFoundException;
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

@Tag(name = "Docks")
@RestController
@RequestMapping("/api/docks")
public class DockController {

	private final DockRepository docks;

	private final BookingService bookingService;

	public DockController(DockRepository docks, BookingService bookingService) {
		this.docks = docks;
		this.bookingService = bookingService;
	}

	@Operation(summary = "List all docks")
	@GetMapping
	public List<DockView> list() {
		return docks.findAllByOrderByCodeAsc().stream().map(DockView::from).toList();
	}

	@Operation(summary = "Get one dock")
	@GetMapping("/{id}")
	public DockView get(@PathVariable Long id) {
		return docks.findById(id).map(DockView::from).orElseThrow(() -> new NotFoundException("Dock", id));
	}

	@Operation(summary = "Add a dock")
	@PostMapping
	public ResponseEntity<DockView> create(@Valid @RequestBody DockRequest request) {
		if (docks.existsByCode(request.code())) {
			throw new ConflictException("A dock with code %s already exists.".formatted(request.code()));
		}
		Dock dock = docks.save(
				new Dock(request.code(), request.name(), request.lengthMeters(), request.maxHeightMeters()));
		return ResponseEntity.created(URI.create("/api/docks/" + dock.getId())).body(DockView.from(dock));
	}

	@Operation(summary = "Free periods of a dock", description = "Gaps between confirmed bookings inside [from, to).")
	@GetMapping("/{id}/availability")
	public List<TimeWindow> availability(@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
		return bookingService.availability(id, window(from, to));
	}

	@Operation(summary = "Docks that can host a vehicle",
			description = "Active docks where the vehicle fits and that are free for the whole stay, smallest first.")
	@GetMapping("/suitable")
	public List<DockView> suitable(@RequestParam Long vehicleId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime arrival,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime departure) {
		return bookingService.suitableDocks(vehicleId, window(arrival, departure))
			.stream()
			.map(DockView::from)
			.toList();
	}

	private static TimeWindow window(OffsetDateTime from, OffsetDateTime to) {
		if (!to.isAfter(from)) {
			throw new IllegalArgumentException("the end of the period must be after its start");
		}
		return new TimeWindow(from.toInstant(), to.toInstant());
	}

}
