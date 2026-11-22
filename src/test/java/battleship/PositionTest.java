package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Position Unit Tests")
class PositionTest {

	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	}

	@Test
	@DisplayName("Should create position using integer row and column coordinates")
	void testIntConstructorAndGetters() {
		assertEquals(2, position.getRow());
		assertEquals(3, position.getColumn());
		assertEquals('C', position.getClassicRow());
		assertEquals(4, position.getClassicColumn());
		assertFalse(position.isOccupied());
		assertFalse(position.isHit());
	}

	@Test
	@DisplayName("Should create position using classic char row and 1-based column")
	void testClassicConstructor() {
		Position upper = new Position('B', 5);
		assertEquals(1, upper.getRow());
		assertEquals(4, upper.getColumn());
		assertEquals('B', upper.getClassicRow());
		assertEquals(5, upper.getClassicColumn());

		Position lower = new Position('j', 10);
		assertEquals(9, lower.getRow());
		assertEquals(9, lower.getColumn());
	}

	@Test
	@DisplayName("Should generate random positions strictly inside the board")
	void testRandomPosition() {
		for (int i = 0; i < 30; i++) {
			Position randomPos = Position.randomPosition();
			assertNotNull(randomPos);
			assertTrue(randomPos.isInside());
		}
	}

	@Test
	@DisplayName("Should accurately check whether position is inside board boundaries")
	void testIsInside() {
		assertTrue(new Position(0, 0).isInside());
		assertTrue(new Position(Game.BOARD_SIZE - 1, Game.BOARD_SIZE - 1).isInside());
		assertFalse(new Position(-1, 0).isInside());
		assertFalse(new Position(0, -1).isInside());
		assertFalse(new Position(Game.BOARD_SIZE, 0).isInside());
		assertFalse(new Position(0, Game.BOARD_SIZE).isInside());
	}

	@Test
	@DisplayName("Should detect horizontal, vertical, diagonal, and non-adjacent positions")
	void testIsAdjacentTo() {
		assertTrue(position.isAdjacentTo(new Position(2, 3)));
		assertTrue(position.isAdjacentTo(new Position(1, 3)));
		assertTrue(position.isAdjacentTo(new Position(3, 4)));
		assertFalse(position.isAdjacentTo(new Position(4, 3)));
		assertFalse(position.isAdjacentTo(new Position(2, 5)));
	}

	@Test
	@DisplayName("Should return all valid adjacent positions for center and corner cells")
	void testAdjacentPositions() {
		List<IPosition> centerAdjacents = position.adjacentPositions();
		assertEquals(8, centerAdjacents.size());

		Position topLeftCorner = new Position(0, 0);
		List<IPosition> cornerAdjacents = topLeftCorner.adjacentPositions();
		assertEquals(3, cornerAdjacents.size());
		assertTrue(cornerAdjacents.contains(new Position(0, 1)));
		assertTrue(cornerAdjacents.contains(new Position(1, 0)));
		assertTrue(cornerAdjacents.contains(new Position(1, 1)));
	}

	@Test
	@DisplayName("Should update occupied and hit flags when occupy() and shoot() are called")
	void testOccupyAndShoot() {
		assertFalse(position.isOccupied());
		position.occupy();
		assertTrue(position.isOccupied());

		assertFalse(position.isHit());
		position.shoot();
		assertTrue(position.isHit());
	}

	@Test
	@DisplayName("Should compare equality, hashCode, and string representation properly")
	void testEqualsHashCodeAndToString() {
		Position sameCoords = new Position(2, 3);
		Position diffRow = new Position(4, 3);
		Position diffCol = new Position(2, 8);

		assertEquals(position, position);
		assertEquals(position, sameCoords);
		assertEquals(position.hashCode(), sameCoords.hashCode());
		assertNotEquals(position, diffRow);
		assertNotEquals(position, diffCol);
		assertNotEquals(position, null);
		assertNotEquals(position, "C4");
		assertEquals("C4", position.toString());
	}
}