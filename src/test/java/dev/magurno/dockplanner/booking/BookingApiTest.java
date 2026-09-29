package dev.magurno.dockplanner.booking;

import java.math.BigDecimal;
import java.time.Instant;

import dev.magurno.dockplanner.TestcontainersConfiguration;
import dev.magurno.dockplanner.dock.Dock;
import dev.magurno.dockplanner.dock.DockRepository;
import dev.magurno.dockplanner.vehicle.Vehicle;
import dev.magurno.dockplanner.vehicle.VehicleRepository;
import dev.magurno.dockplanner.vehicle.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class BookingApiTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	DockRepository docks;

	@Autowired
	VehicleRepository vehicles;

	@Autowired
	BookingRepository bookings;

	Dock bigDock;

	Dock smallDock;

	Vehicle lorry;

	Vehicle van;

	@BeforeEach
	void setUp() {
		bigDock = docks.save(new Dock("D1", "Inbound dock", new BigDecimal("19.0"), new BigDecimal("4.2")));
		smallDock = docks.save(new Dock("D5", "Van bay", new BigDecimal("8.0"), new BigDecimal("3.0")));
		lorry = vehicles.save(new Vehicle("Scania R450", "FA123BC", VehicleType.ARTICULATED_LORRY,
				new BigDecimal("16.5"), new BigDecimal("4.0")));
		van = vehicles.save(
				new Vehicle("Mercedes Sprinter", "FT321AZ", VehicleType.VAN, new BigDecimal("6.9"), new BigDecimal("2.8")));
	}

	private MvcTestResult book(Dock dock, Vehicle vehicle, String arrival, String departure) {
		return mvc.post()
			.uri("/api/bookings")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"dockId": %d, "vehicleId": %d, "arrival": "%s", "departure": "%s"}
					""".formatted(dock.getId(), vehicle.getId(), arrival, departure))
			.exchange();
	}

	@Test
	void booksAFreeDock() {
		MvcTestResult result = book(bigDock, lorry, "2030-06-14T08:00:00+02:00", "2030-06-15T08:00:00+02:00");

		assertThat(result).hasStatus(HttpStatus.CREATED);
		assertThat(result).bodyJson().extractingPath("$.dockCode").isEqualTo("D1");
		assertThat(result).bodyJson().extractingPath("$.arrival").isEqualTo("2030-06-14T06:00:00Z");
		assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("CONFIRMED");
	}

	@Test
	void rejectsAVehicleThatDoesNotFit() {
		MvcTestResult result = book(smallDock, lorry, "2030-06-14T08:00:00Z", "2030-06-14T20:00:00Z");

		assertThat(result).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
		assertThat(result).bodyJson().extractingPath("$.detail").asString().contains("does not fit dock D5");
	}

	@Test
	void rejectsOverlappingBookingsOnTheSameDock() {
		book(smallDock, van, "2030-06-14T08:00:00Z", "2030-06-14T14:00:00Z");
		Long firstId = bookings.findAll().getFirst().getId();
		Vehicle otherVan = vehicles.save(
				new Vehicle("Fiat Ducato", "HA987TR", VehicleType.VAN, new BigDecimal("6.0"), new BigDecimal("2.6")));

		MvcTestResult result = book(smallDock, otherVan, "2030-06-14T12:00:00Z", "2030-06-14T18:00:00Z");

		assertThat(result).hasStatus(HttpStatus.CONFLICT);
		assertThat(result).bodyJson().extractingPath("$.conflictingBookingIds[0]").asNumber().isEqualTo(firstId.intValue());
	}

	@Test
	void allowsBackToBackBookings() {
		book(smallDock, van, "2030-06-14T08:00:00Z", "2030-06-14T14:00:00Z");
		Vehicle otherVan = vehicles.save(
				new Vehicle("Fiat Ducato", "HA987TR", VehicleType.VAN, new BigDecimal("6.0"), new BigDecimal("2.6")));

		assertThat(book(smallDock, otherVan, "2030-06-14T14:00:00Z", "2030-06-14T18:00:00Z"))
			.hasStatus(HttpStatus.CREATED);
	}

	@Test
	void aVehicleCannotBeAtTwoDocksAtOnce() {
		book(smallDock, van, "2030-06-14T08:00:00Z", "2030-06-14T14:00:00Z");

		MvcTestResult result = book(bigDock, van, "2030-06-14T10:00:00Z", "2030-06-14T12:00:00Z");

		assertThat(result).hasStatus(HttpStatus.CONFLICT);
		assertThat(result).bodyJson().extractingPath("$.detail").asString().contains("another dock");
	}

	@Test
	void cancellingFreesTheDock() {
		book(smallDock, van, "2030-06-14T08:00:00Z", "2030-06-14T14:00:00Z");
		Long id = bookings.findAll().getFirst().getId();

		assertThat(mvc.post().uri("/api/bookings/{id}/cancel", id)).hasStatusOk()
			.bodyJson()
			.extractingPath("$.status")
			.isEqualTo("CANCELLED");
		assertThat(book(smallDock, van, "2030-06-14T09:00:00Z", "2030-06-14T13:00:00Z"))
			.hasStatus(HttpStatus.CREATED);
	}

	@Test
	void rejectsDepartureBeforeArrival() {
		assertThat(book(smallDock, van, "2030-06-14T14:00:00Z", "2030-06-14T08:00:00Z"))
			.hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void unknownDockIsNotFound() {
		MvcTestResult result = mvc.post()
			.uri("/api/bookings")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"dockId": 999999, "vehicleId": %d,
					 "arrival": "2030-06-14T08:00:00Z", "departure": "2030-06-14T14:00:00Z"}
					""".formatted(van.getId()))
			.exchange();

		assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void availabilityListsTheFreeGaps() {
		book(smallDock, van, "2030-06-14T08:00:00Z", "2030-06-14T14:00:00Z");

		MvcTestResult result = mvc.get()
			.uri("/api/docks/{id}/availability?from={from}&to={to}", smallDock.getId(), "2030-06-14T00:00:00Z",
					"2030-06-15T00:00:00Z")
			.exchange();

		assertThat(result).hasStatusOk();
		assertThat(result).bodyJson().extractingPath("$[0].end").isEqualTo("2030-06-14T08:00:00Z");
		assertThat(result).bodyJson().extractingPath("$[1].start").isEqualTo("2030-06-14T14:00:00Z");
	}

	@Test
	void suitableDocksAreThoseThatFitAndAreFreeSmallestFirst() {
		MvcTestResult bothFree = mvc.get()
			.uri("/api/docks/suitable?vehicleId={v}&arrival={a}&departure={d}", van.getId(), "2030-06-14T08:00:00Z",
					"2030-06-14T14:00:00Z")
			.exchange();
		assertThat(bothFree).bodyJson().extractingPath("$[*].code").asArray().containsExactly("D5", "D1");

		book(smallDock, van, "2030-06-14T08:00:00Z", "2030-06-14T14:00:00Z");
		Vehicle otherVan = vehicles.save(
				new Vehicle("Fiat Ducato", "HA987TR", VehicleType.VAN, new BigDecimal("6.0"), new BigDecimal("2.6")));
		MvcTestResult smallBusy = mvc.get()
			.uri("/api/docks/suitable?vehicleId={v}&arrival={a}&departure={d}", otherVan.getId(),
					"2030-06-14T10:00:00Z", "2030-06-14T12:00:00Z")
			.exchange();
		assertThat(smallBusy).bodyJson().extractingPath("$[*].code").asArray().containsExactly("D1");
	}

	@Test
	void rejectsInvalidPlateNumbers() {
		MvcTestResult result = mvc.post()
			.uri("/api/vehicles")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"name": "Typo", "plateNumber": "AB123IO", "type": "VAN", "lengthMeters": 6, "heightMeters": 2.5}
					""")
			.exchange();

		assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(result).bodyJson()
			.extractingPath("$.errors.plateNumber")
			.isEqualTo("must be a valid Italian licence plate, e.g. AB123CD");
	}

	@Test
	void databaseRejectsOverlapsEvenIfTheServiceIsBypassed() {
		bookings.saveAndFlush(new Booking(smallDock, van,
				new TimeWindow(Instant.parse("2030-06-14T08:00:00Z"), Instant.parse("2030-06-14T14:00:00Z"))));
		Vehicle otherVan = vehicles.save(
				new Vehicle("Fiat Ducato", "HA987TR", VehicleType.VAN, new BigDecimal("6.0"), new BigDecimal("2.6")));

		assertThatThrownBy(() -> bookings.saveAndFlush(new Booking(smallDock, otherVan,
				new TimeWindow(Instant.parse("2030-06-14T12:00:00Z"), Instant.parse("2030-06-14T16:00:00Z")))))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

}
