package dev.magurno.dockplanner.vehicle;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class VehicleDtos {

	private VehicleDtos() {
	}

	public record VehicleRequest(
			@NotBlank @Size(max = 100) String name,
			@NotNull @PlateNumber String plateNumber,
			@NotNull VehicleType type,
			@NotNull @DecimalMin("3.0") @DecimalMax("25.25") BigDecimal lengthMeters,
			@NotNull @DecimalMin("1.5") @DecimalMax("4.5") BigDecimal heightMeters) {
	}

	public record VehicleView(Long id, String name, String plateNumber, VehicleType type, BigDecimal lengthMeters,
			BigDecimal heightMeters) {

		public static VehicleView from(Vehicle vehicle) {
			return new VehicleView(vehicle.getId(), vehicle.getName(), vehicle.getPlateNumber(), vehicle.getType(),
					vehicle.getLengthMeters(), vehicle.getHeightMeters());
		}

	}

}
