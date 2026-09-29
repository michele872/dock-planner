package dev.magurno.dockplanner.vehicle;

import java.net.URI;
import java.util.List;

import dev.magurno.dockplanner.common.ConflictException;
import dev.magurno.dockplanner.common.NotFoundException;
import dev.magurno.dockplanner.vehicle.VehicleDtos.VehicleRequest;
import dev.magurno.dockplanner.vehicle.VehicleDtos.VehicleView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vehicles")
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

	private final VehicleRepository vehicles;

	public VehicleController(VehicleRepository vehicles) {
		this.vehicles = vehicles;
	}

	@Operation(summary = "List all vehicles")
	@GetMapping
	public List<VehicleView> list() {
		return vehicles.findAllByOrderByNameAsc().stream().map(VehicleView::from).toList();
	}

	@Operation(summary = "Get one vehicle")
	@GetMapping("/{id}")
	public VehicleView get(@PathVariable Long id) {
		return vehicles.findById(id).map(VehicleView::from).orElseThrow(() -> new NotFoundException("Vehicle", id));
	}

	@Operation(summary = "Register a vehicle", description = "The Italian licence plate format is validated.")
	@PostMapping
	public ResponseEntity<VehicleView> create(@Valid @RequestBody VehicleRequest request) {
		if (vehicles.existsByPlateNumber(request.plateNumber())) {
			throw new ConflictException("A vehicle with plate %s is already registered.".formatted(request.plateNumber()));
		}
		Vehicle vehicle = vehicles.save(new Vehicle(request.name(), request.plateNumber(), request.type(),
				request.lengthMeters(), request.heightMeters()));
		return ResponseEntity.created(URI.create("/api/vehicles/" + vehicle.getId())).body(VehicleView.from(vehicle));
	}

}
