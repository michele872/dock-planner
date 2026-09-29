package dev.magurno.dockplanner.dock;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface DockRepository extends JpaRepository<Dock, Long> {

	List<Dock> findAllByOrderByCodeAsc();

	List<Dock> findByActiveTrueOrderByLengthMetersAscCodeAsc();

	boolean existsByCode(String code);

	/**
	 * Loads the dock with a row lock, so bookings for the same dock are
	 * checked and saved one at a time.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select b from Dock b where b.id = :id")
	Optional<Dock> findByIdForUpdate(Long id);

}
