package dev.magurno.dockplanner.vehicle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	List<Vehicle> findAllByOrderByNameAsc();

	boolean existsByPlateNumber(String plateNumber);

}
