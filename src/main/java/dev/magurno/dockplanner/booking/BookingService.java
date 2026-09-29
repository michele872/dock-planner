package dev.magurno.dockplanner.booking;

import java.util.List;

import dev.magurno.dockplanner.dock.Dock;
import dev.magurno.dockplanner.dock.DockRepository;
import dev.magurno.dockplanner.common.BusinessRuleException;
import dev.magurno.dockplanner.common.ConflictException;
import dev.magurno.dockplanner.common.NotFoundException;
import dev.magurno.dockplanner.vehicle.Vehicle;
import dev.magurno.dockplanner.vehicle.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Booking rules:
 * <ol>
 * <li>departure must be after arrival;</li>
 * <li>the dock must be in service and the vehicle must fit it (length and height);</li>
 * <li>the dock must be free for the whole stay;</li>
 * <li>the vehicle cannot be booked at another dock at the same time.</li>
 * </ol>
 * The dock row is locked while a booking is checked and saved, and PostgreSQL
 * exclusion constraints guarantee rules 3 and 4 even under concurrency.
 */
@Service
public class BookingService {

	private final BookingRepository bookings;

	private final DockRepository docks;

	private final VehicleRepository vehicles;

	public BookingService(BookingRepository bookings, DockRepository docks, VehicleRepository vehicles) {
		this.bookings = bookings;
		this.docks = docks;
		this.vehicles = vehicles;
	}

	@Transactional
	public BookingView book(BookingRequest request) {
		TimeWindow window = request.window();
		Dock dock = docks.findByIdForUpdate(request.dockId())
			.orElseThrow(() -> new NotFoundException("Dock", request.dockId()));
		Vehicle vehicle = vehicles.findById(request.vehicleId())
			.orElseThrow(() -> new NotFoundException("Vehicle", request.vehicleId()));

		if (!dock.isActive()) {
			throw new BusinessRuleException("Dock %s is not in service.".formatted(dock.getCode()));
		}
		if (!dock.fits(vehicle)) {
			throw new BusinessRuleException(
					"%s (%s m long, %s m high) does not fit dock %s (%s m long, %s m max height)."
						.formatted(vehicle.getName(), vehicle.getLengthMeters(), vehicle.getHeightMeters(), dock.getCode(),
								dock.getLengthMeters(), dock.getMaxHeightMeters()));
		}

		List<Booking> dockClashes = bookings.findConfirmedOnDock(dock.getId(), window.start(), window.end());
		if (!dockClashes.isEmpty()) {
			throw new ConflictException("Dock %s is already booked in that period.".formatted(dock.getCode()),
					ids(dockClashes));
		}
		List<Booking> vehicleClashes = bookings.findConfirmedForVehicle(vehicle.getId(), window.start(), window.end());
		if (!vehicleClashes.isEmpty()) {
			throw new ConflictException(
					"%s is already booked at another dock in that period.".formatted(vehicle.getName()),
					ids(vehicleClashes));
		}

		Booking booking = bookings.saveAndFlush(new Booking(dock, vehicle, window));
		return BookingView.from(booking);
	}

	/** Cancelling frees the dock. Cancelling twice is harmless. */
	@Transactional
	public BookingView cancel(Long id) {
		Booking booking = bookings.findById(id).orElseThrow(() -> new NotFoundException("Booking", id));
		booking.cancel();
		return BookingView.from(booking);
	}

	@Transactional(readOnly = true)
	public BookingView get(Long id) {
		return bookings.findById(id).map(BookingView::from).orElseThrow(() -> new NotFoundException("Booking", id));
	}

	@Transactional(readOnly = true)
	public List<BookingView> schedule(Long dockId, TimeWindow window) {
		return bookings.findSchedule(dockId, window.start(), window.end()).stream().map(BookingView::from).toList();
	}

	/** Free periods of a dock inside the given window. */
	@Transactional(readOnly = true)
	public List<TimeWindow> availability(Long dockId, TimeWindow window) {
		if (!docks.existsById(dockId)) {
			throw new NotFoundException("Dock", dockId);
		}
		List<TimeWindow> busy = bookings.findConfirmedOnDock(dockId, window.start(), window.end())
			.stream()
			.map(Booking::window)
			.toList();
		return window.freeSlots(busy);
	}

	/**
	 * Docks where the vehicle fits and that are free for the whole stay, smallest
	 * first: large docks stay available for large vehicles.
	 */
	@Transactional(readOnly = true)
	public List<Dock> suitableDocks(Long vehicleId, TimeWindow window) {
		Vehicle vehicle = vehicles.findById(vehicleId).orElseThrow(() -> new NotFoundException("Vehicle", vehicleId));
		return docks.findByActiveTrueOrderByLengthMetersAscCodeAsc()
			.stream()
			.filter(dock -> dock.fits(vehicle))
			.filter(dock -> bookings.findConfirmedOnDock(dock.getId(), window.start(), window.end()).isEmpty())
			.toList();
	}

	private static List<Long> ids(List<Booking> bookings) {
		return bookings.stream().map(Booking::getId).toList();
	}

}
