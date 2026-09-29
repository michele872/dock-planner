package dev.magurno.dockplanner.booking;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	/** Confirmed bookings on a dock that overlap [from, to). */
	@Query("""
			select b from Booking b
			where b.dock.id = :dockId
			  and b.status = dev.magurno.dockplanner.booking.BookingStatus.CONFIRMED
			  and b.arrival < :to and :from < b.departure
			order by b.arrival
			""")
	List<Booking> findConfirmedOnDock(Long dockId, Instant from, Instant to);

	/** Confirmed bookings of a vehicle, on any dock, that overlap [from, to). */
	@Query("""
			select b from Booking b
			where b.vehicle.id = :vehicleId
			  and b.status = dev.magurno.dockplanner.booking.BookingStatus.CONFIRMED
			  and b.arrival < :to and :from < b.departure
			order by b.arrival
			""")
	List<Booking> findConfirmedForVehicle(Long vehicleId, Instant from, Instant to);

	/** Schedule view: bookings overlapping [from, to), optionally for one dock. */
	@Query("""
			select b from Booking b
			join fetch b.dock join fetch b.vehicle
			where (:dockId is null or b.dock.id = :dockId)
			  and b.arrival < :to and :from < b.departure
			order by b.arrival
			""")
	List<Booking> findSchedule(Long dockId, Instant from, Instant to);

}
