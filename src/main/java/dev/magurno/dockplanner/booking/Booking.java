package dev.magurno.dockplanner.booking;

import java.time.Instant;

import dev.magurno.dockplanner.dock.Dock;
import dev.magurno.dockplanner.vehicle.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

@Entity
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dock_id")
	private Dock dock;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehicle_id")
	private Vehicle vehicle;

	@Column(nullable = false)
	private Instant arrival;

	@Column(nullable = false)
	private Instant departure;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookingStatus status = BookingStatus.CONFIRMED;

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Version
	private long version;

	protected Booking() {
	}

	public Booking(Dock dock, Vehicle vehicle, TimeWindow window) {
		this.dock = dock;
		this.vehicle = vehicle;
		this.arrival = window.start();
		this.departure = window.end();
	}

	public void cancel() {
		this.status = BookingStatus.CANCELLED;
	}

	public TimeWindow window() {
		return new TimeWindow(arrival, departure);
	}

	public Long getId() {
		return id;
	}

	public Dock getDock() {
		return dock;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public Instant getArrival() {
		return arrival;
	}

	public Instant getDeparture() {
		return departure;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
