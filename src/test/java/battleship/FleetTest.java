package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fleet Unit Tests")
class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@Test
	@DisplayName("Should initialize an empty fleet")
	void testEmptyFleetInitialization() {
		assertNotNull(fleet.getShips());
		assertTrue(fleet.getShips().isEmpty());
		assertTrue(fleet.getFloatingShips().isEmpty());
		assertTrue(fleet.getSunkShips().isEmpty());
	}

	@Test
	@DisplayName("Should add valid ships and reject out-of-bounds, colliding, or excess ships")
	void testAddShipConstraints() {
		Ship validBarge = new Barge(Compass.NORTH, new Position(0, 0));
		assertTrue(fleet.addShip(validBarge));
		assertEquals(1, fleet.getShips().size());

		// Adjacent/colliding ship rejected
		Ship collidingBarge = new Barge(Compass.NORTH, new Position(0, 1));
		assertFalse(fleet.addShip(collidingBarge));

		// Out-of-board ship rejected
		Ship outOfBoardShip = new Frigate(Compass.EAST, new Position(5, Game.BOARD_SIZE - 1));
		assertFalse(fleet.addShip(outOfBoardShip));

		// Exceeding FLEET_SIZE limit (ships.size() <= FLEET_SIZE allows up to FLEET_SIZE + 1 ships)
		Fleet fullFleet = new Fleet();
		for (int i = 0; i <= IFleet.FLEET_SIZE; i++) {
			int row = (i / 5) * 2;
			int col = (i % 5) * 2;
			assertTrue(fullFleet.addShip(new Barge(Compass.NORTH, new Position(row, col))));
		}
		assertFalse(fullFleet.addShip(new Barge(Compass.NORTH, new Position(9, 9))));
	}

	@Test
	@DisplayName("Should filter ships by category and locate ships by position")
	void testGetShipsLikeAndShipAt() {
		Ship barge1 = new Barge(Compass.NORTH, new Position(0, 0));
		Ship barge2 = new Barge(Compass.NORTH, new Position(0, 3));
		Ship caravel = new Caravel(Compass.EAST, new Position(3, 0));

		fleet.addShip(barge1);
		fleet.addShip(barge2);
		fleet.addShip(caravel);

		List<IShip> barges = fleet.getShipsLike("Barca");
		assertEquals(2, barges.size());
		assertTrue(fleet.getShipsLike("Galeao").isEmpty());

		assertEquals(caravel, fleet.shipAt(new Position(3, 1)));
		assertNull(fleet.shipAt(new Position(9, 9)));
	}

	@Test
	@DisplayName("Should accurately separate floating ships from sunk ships")
	void testFloatingAndSunkShips() {
		Ship barge1 = new Barge(Compass.NORTH, new Position(0, 0));
		Ship barge2 = new Barge(Compass.NORTH, new Position(2, 2));

		fleet.addShip(barge1);
		fleet.addShip(barge2);

		assertEquals(2, fleet.getFloatingShips().size());
		assertEquals(0, fleet.getSunkShips().size());

		barge1.sink();

		assertEquals(1, fleet.getFloatingShips().size());
		assertEquals(1, fleet.getSunkShips().size());
		assertTrue(fleet.getSunkShips().contains(barge1));
		assertTrue(fleet.getFloatingShips().contains(barge2));
	}

	@Test
	@DisplayName("Should execute all print helper methods without errors")
	void testPrintMethods() {
		Ship barge = new Barge(Compass.NORTH, new Position(0, 0));
		fleet.addShip(barge);

		assertDoesNotThrow(() -> {
			fleet.printStatus();
			fleet.printShipsByCategory("Barca");
			fleet.printFloatingShips();
			fleet.printAllShips();
		});
	}

	@Test
	@DisplayName("Should create a complete random fleet of 11 valid non-colliding ships")
	void testCreateRandomFleet() {
		IFleet randomFleet = Fleet.createRandom();
		assertNotNull(randomFleet);
		assertEquals(11, randomFleet.getShips().size());
		assertEquals(11, randomFleet.getFloatingShips().size());
	}
}