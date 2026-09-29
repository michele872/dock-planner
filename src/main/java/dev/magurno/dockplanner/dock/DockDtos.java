package dev.magurno.dockplanner.dock;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class DockDtos {

	private DockDtos() {
	}

	public record DockRequest(
			@NotBlank @Size(max = 20) String code,
			@NotBlank @Size(max = 100) String name,
			@NotNull @DecimalMin("4.0") @DecimalMax("30.0") BigDecimal lengthMeters,
			@NotNull @DecimalMin("2.0") @DecimalMax("6.0") BigDecimal maxHeightMeters) {
	}

	public record DockView(Long id, String code, String name, BigDecimal lengthMeters, BigDecimal maxHeightMeters,
			boolean active) {

		public static DockView from(Dock dock) {
			return new DockView(dock.getId(), dock.getCode(), dock.getName(), dock.getLengthMeters(),
					dock.getMaxHeightMeters(), dock.isActive());
		}

	}

}
