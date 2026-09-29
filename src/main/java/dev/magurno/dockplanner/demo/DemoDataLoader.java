package dev.magurno.dockplanner.demo;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import dev.magurno.dockplanner.booking.Booking;
import dev.magurno.dockplanner.booking.BookingRepository;
import dev.magurno.dockplanner.booking.TimeWindow;
import dev.magurno.dockplanner.dock.Dock;
import dev.magurno.dockplanner.dock.DockRepository;
import dev.magurno.dockplanner.vehicle.Vehicle;
import dev.magurno.dockplanner.vehicle.VehicleRepository;
import dev.magurno.dockplanner.vehicle.VehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Loads a small fictional warehouse when the "demo" profile is active and the database is empty. */
@Component
@Profile("demo")
class DemoDataLoader implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

	private final DockRepository docks;

	private final VehicleRepository vehicles;

	private final BookingRepository bookings;

	DemoDataLoader(DockRepository docks, VehicleRepository vehicles, BookingRepository bookings) {
		this.docks = docks;
		this.vehicles = vehicles;
		this.bookings = bookings;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (docks.count() > 0) {
			return;
		}

		Dock d1 = docks.save(new Dock("D1", "Inbound dock 1 (full trailers)", decimal("19.0"), decimal("4.2")));
		Dock d2 = docks.save(new Dock("D2", "Inbound dock 2", decimal("17.0"), decimal("4.0")));
		Dock d3 = docks.save(new Dock("D3", "Refrigerated dock", decimal("12.5"), decimal("4.0")));
		docks.save(new Dock("D4", "Rigid truck dock", decimal("12.0"), decimal("3.8")));
		Dock d5 = docks.save(new Dock("D5", "Van bay", decimal("8.0"), decimal("3.0")));

		Vehicle scania = vehicles.save(vehicle("Scania R450 · Trasporti Colombo", "FA123BC",
				VehicleType.ARTICULATED_LORRY, "16.5", "4.0"));
		Vehicle volvo = vehicles.save(vehicle("Volvo FH · Logistica Brambilla", "GB456CD",
				VehicleType.ARTICULATED_LORRY, "16.5", "4.0"));
		Vehicle iveco = vehicles.save(vehicle("Iveco Eurocargo · Freschi Nord", "EK789LM",
				VehicleType.REFRIGERATED_TRUCK, "10.2", "3.7"));
		Vehicle sprinter = vehicles.save(vehicle("Mercedes Sprinter · Corriere Espresso", "FT321AZ",
				VehicleType.VAN, "6.9", "2.8"));
		Vehicle daf = vehicles.save(vehicle("DAF XF · Autotrasporti Ferri", "GD654NP",
				VehicleType.ROAD_TRAIN, "18.75", "4.0"));
		vehicles.save(vehicle("Fiat Ducato · Consegne Rapide", "HA987TR", VehicleType.VAN, "6.0", "2.6"));

		Instant today = Instant.now().atOffset(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS).toInstant();
		Instant tomorrow = today.plus(Duration.ofDays(1));
		book(d1, scania, tomorrow.plus(Duration.ofHours(6)), Duration.ofHours(2));
		book(d2, volvo, tomorrow.plus(Duration.ofHours(7)), Duration.ofMinutes(90));
		book(d3, iveco, tomorrow.plus(Duration.ofHours(5)), Duration.ofHours(1));
		book(d5, sprinter, tomorrow.plus(Duration.ofHours(8)), Duration.ofMinutes(30));
		book(d5, sprinter, tomorrow.plus(Duration.ofHours(14)), Duration.ofMinutes(30));
		book(d1, daf, tomorrow.plus(Duration.ofHours(9)), Duration.ofHours(3));

		log.info("Demo data loaded: {} docks, {} vehicles, {} bookings", docks.count(), vehicles.count(),
				bookings.count());
	}

	private void book(Dock dock, Vehicle vehicle, Instant arrival, Duration stay) {
		bookings.save(new Booking(dock, vehicle, new TimeWindow(arrival, arrival.plus(stay))));
	}

	private static Vehicle vehicle(String name, String plate, VehicleType type, String length, String height) {
		return new Vehicle(name, plate, type, decimal(length), decimal(height));
	}

	private static BigDecimal decimal(String value) {
		return new BigDecimal(value);
	}

}
