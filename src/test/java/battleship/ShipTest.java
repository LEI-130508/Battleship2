package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ship Unit Tests")
class ShipTest {

    private Ship frigate;

    @BeforeEach
    void setUp() {
        frigate = new Frigate(Compass.EAST, new Position(2, 2));
    }

    @Test
    @DisplayName("Should build all valid ship kinds via factory method and return null for unknown kind")
    void testBuildShip() {
        Position pos = new Position(1, 1);

        assertInstanceOf(Barge.class, Ship.buildShip("barca", Compass.NORTH, pos));
        assertInstanceOf(Caravel.class, Ship.buildShip("caravela", Compass.SOUTH, pos));
        assertInstanceOf(Carrack.class, Ship.buildShip("nau", Compass.EAST, pos));
        assertInstanceOf(Frigate.class, Ship.buildShip("fragata", Compass.WEST, pos));
        assertInstanceOf(Galleon.class, Ship.buildShip("galeao", Compass.NORTH, pos));
        assertNull(Ship.buildShip("submarino", Compass.NORTH, pos));
    }

    @Test
    @DisplayName("Should reject null arguments in Ship constructor")
    void testConstructorNullValidation() {
        Position pos = new Position(0, 0);
        assertThrows(NullPointerException.class, () -> new Barge(null, pos));
        assertThrows(NullPointerException.class, () -> new Barge(Compass.NORTH, null));
    }

    @Test
    @DisplayName("Should return accurate metadata, bounding coordinates, and string representation")
    void testMetadataAndBounds() {
        assertEquals("Fragata", frigate.getCategory());
        assertEquals(Compass.EAST, frigate.getBearing());
        assertEquals(new Position(2, 2), frigate.getPosition());
        assertEquals(4, frigate.getSize());
        assertEquals(4, frigate.getPositions().size());

        assertEquals(2, frigate.getTopMostPos());
        assertEquals(2, frigate.getBottomMostPos());
        assertEquals(2, frigate.getLeftMostPos());
        assertEquals(5, frigate.getRightMostPos());
        assertEquals("[Fragata e C3]", frigate.toString());

        Ship verticalGalleon = new Galleon(Compass.SOUTH, new Position(1, 3));
        assertEquals(1, verticalGalleon.getTopMostPos());
        assertEquals(3, verticalGalleon.getBottomMostPos());
        assertEquals(2, verticalGalleon.getLeftMostPos());
        assertEquals(4, verticalGalleon.getRightMostPos());
    }

    @Test
    @DisplayName("Should return unique adjacent positions excluding ship's own cells")
    void testGetAdjacentPositions() {
        Ship barge = new Barge(Compass.NORTH, new Position(0, 0));
        List<IPosition> adjacents = barge.getAdjacentPositions();
        assertEquals(3, adjacents.size());
        assertFalse(adjacents.contains(new Position(0, 0)));

        List<IPosition> frigateAdjacents = frigate.getAdjacentPositions();
        assertEquals(14, frigateAdjacents.size());
        for (IPosition occupied : frigate.getPositions()) {
            assertFalse(frigateAdjacents.contains(occupied));
        }
    }

    @Test
    @DisplayName("Should verify whether ship occupies a given position")
    void testOccupies() {
        assertTrue(frigate.occupies(new Position(2, 2)));
        assertTrue(frigate.occupies(new Position(2, 5)));
        assertFalse(frigate.occupies(new Position(2, 6)));
        assertFalse(frigate.occupies(new Position(3, 2)));
    }

    @Test
    @DisplayName("Should detect proximity to another ship or position")
    void testTooCloseTo() {
        assertTrue(frigate.tooCloseTo(new Position(1, 2)));
        assertFalse(frigate.tooCloseTo(new Position(5, 5)));

        Ship adjacentShip = new Barge(Compass.NORTH, new Position(3, 5));
        Ship distantShip = new Barge(Compass.NORTH, new Position(8, 8));

        assertTrue(frigate.tooCloseTo(adjacentShip));
        assertFalse(frigate.tooCloseTo(distantShip));
    }

    @Test
    @DisplayName("Should validate whether all positions of the ship lie inside the board")
    void testIsInsideBoard() {
        assertTrue(frigate.isInsideBoard());

        Ship offLeft = new Barge(Compass.NORTH, new Position(0, -1));
        Ship offRight = new Frigate(Compass.EAST, new Position(0, Game.BOARD_SIZE - 2));
        Ship offTop = new Barge(Compass.NORTH, new Position(-1, 0));
        Ship offBottom = new Frigate(Compass.SOUTH, new Position(Game.BOARD_SIZE - 2, 0));

        assertFalse(offLeft.isInsideBoard());
        assertFalse(offRight.isInsideBoard());
        assertFalse(offTop.isInsideBoard());
        assertFalse(offBottom.isInsideBoard());
    }

    @Test
    @DisplayName("Should register shots, track floating status, and sink completely")
    void testShootStillFloatingAndSink() {
        assertTrue(frigate.stillFloating());

        // Shot that misses the ship does not change state
        frigate.shoot(new Position(0, 0));
        assertTrue(frigate.stillFloating());

        // Shoot 3 of 4 positions
        frigate.shoot(new Position(2, 2));
        frigate.shoot(new Position(2, 3));
        frigate.shoot(new Position(2, 4));
        assertTrue(frigate.stillFloating());

        // Shoot final position
        frigate.shoot(new Position(2, 5));
        assertFalse(frigate.stillFloating());

        // Test instant sink()
        Ship caravel = new Caravel(Compass.SOUTH, new Position(5, 5));
        assertTrue(caravel.stillFloating());
        caravel.sink();
        assertFalse(caravel.stillFloating());
    }
}