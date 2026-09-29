package dev.magurno.dockplanner.vehicle;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true, length = 7)
	private String plateNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VehicleType type;

	@Column(nullable = false)
	private BigDecimal lengthMeters;

	@Column(nullable = false)
	private BigDecimal heightMeters;

	protected Vehicle() {
	}

	public Vehicle(String name, String plateNumber, VehicleType type, BigDecimal lengthMeters, BigDecimal heightMeters) {
		this.name = name;
		this.plateNumber = plateNumber;
		this.type = type;
		this.lengthMeters = lengthMeters;
		this.heightMeters = heightMeters;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPlateNumber() {
		return plateNumber;
	}

	public VehicleType getType() {
		return type;
	}

	public BigDecimal getLengthMeters() {
		return lengthMeters;
	}

	public BigDecimal getHeightMeters() {
		return heightMeters;
	}

}
