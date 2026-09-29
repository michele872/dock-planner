package dev.magurno.dockplanner.dock;

import java.math.BigDecimal;

import dev.magurno.dockplanner.vehicle.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Dock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private BigDecimal lengthMeters;

	@Column(nullable = false)
	private BigDecimal maxHeightMeters;

	@Column(nullable = false)
	private boolean active = true;

	protected Dock() {
	}

	public Dock(String code, String name, BigDecimal lengthMeters, BigDecimal maxHeightMeters) {
		this.code = code;
		this.name = name;
		this.lengthMeters = lengthMeters;
		this.maxHeightMeters = maxHeightMeters;
	}

	/** A vehicle fits when it is not longer than the bay and not higher than the dock door. */
	public boolean fits(Vehicle vehicle) {
		return vehicle.getLengthMeters().compareTo(lengthMeters) <= 0
				&& vehicle.getHeightMeters().compareTo(maxHeightMeters) <= 0;
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getLengthMeters() {
		return lengthMeters;
	}

	public BigDecimal getMaxHeightMeters() {
		return maxHeightMeters;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
