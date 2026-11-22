package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Move and IMove Unit Tests")
class MoveTest {

    @Test
    @DisplayName("Should store move number, shots, shot results, and format toString")
    void testMoveGettersAndToString() {
        List<IPosition> shots = List.of(new Position(0, 0), new Position(1, 1));
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, true, null, false)
        );

        Move move = new Move(1, shots, results);

        assertEquals(1, move.getNumber());
        assertEquals(shots, move.getShots());
        assertEquals(results, move.getShotResults());
        assertEquals("Move{number=1, shots=2, results=2}", move.toString());
    }

    @Test
    @DisplayName("Should cover IMove default processEnemyFire() delegation")
    void testIMoveDefaultProcessEnemyFire() {
        IMove move = new Move(2, List.of(), List.of());
        String json = move.processEnemyFire(true);
        assertNotNull(json);
        assertTrue(json.contains("\"validShots\" : 0"));
        assertTrue(json.contains("\"outsideShots\" : " + Game.NUMBER_SHOTS));
    }

    @Test
    @DisplayName("Should process single repeated shot with verbose output and outside shots")
    void testSingleRepeatedShotVerbose() {
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false), // invalid shot ignored
                new IGame.ShotResult(true, true, null, false)    // 1 repeated shot
        );

        Move move = new Move(3, List.of(new Position(0, 0)), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 0"));
        assertTrue(json.contains("\"repeatedShots\" : 1"));
        assertTrue(json.contains("\"outsideShots\" : " + (Game.NUMBER_SHOTS - 1)));
    }

    @Test
    @DisplayName("Should process multiple repeated shots only with verbose output")
    void testMultipleRepeatedShotsVerbose() {
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        );

        Move move = new Move(4, List.of(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 0"));
        assertTrue(json.contains("\"repeatedShots\" : 3"));
        assertTrue(json.contains("\"outsideShots\" : 0"));
    }

    @Test
    @DisplayName("Should process valid hits, multiple sunk ships of same category, misses, and repeated shots")
    void testComplexMixedEnemyFireVerbose() {
        Ship barge1 = new Barge(Compass.NORTH, new Position(0, 0));
        Ship barge2 = new Barge(Compass.NORTH, new Position(0, 2));
        Ship caravel = new Caravel(Compass.EAST, new Position(2, 0));

        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, barge1, true),   // sunk barge 1
                new IGame.ShotResult(true, false, barge2, true),   // sunk barge 2 (count > 1)
                new IGame.ShotResult(true, false, caravel, false), // 1 hit on non-sunk caravel
                new IGame.ShotResult(true, false, null, false),    // miss 1
                new IGame.ShotResult(true, false, null, false),    // miss 2 (missedShots > 1)
                new IGame.ShotResult(true, true, null, false)      // repeated
        );

        Move move = new Move(5, List.of(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 5"));
        assertTrue(json.contains("\"repeatedShots\" : 1"));
        assertTrue(json.contains("\"missedShots\" : 2"));
        assertTrue(json.contains("\"type\" : \"Barca\""));
        assertTrue(json.contains("\"count\" : 2"));
        assertTrue(json.contains("\"type\" : \"Caravela\""));
        assertTrue(json.contains("\"hits\" : 1"));
    }

    @Test
    @DisplayName("Should process single hit and single sunk ship with zero misses and zero outside shots")
    void testSingleHitAndSunkWithoutMissesVerbose() {
        Ship barge = new Barge(Compass.NORTH, new Position(0, 0));
        Ship frigate = new Frigate(Compass.EAST, new Position(3, 0));

        List<IGame.ShotResult> results = new ArrayList<>();
        results.add(new IGame.ShotResult(true, false, barge, true));     // 1 sunk barge
        results.add(new IGame.ShotResult(true, false, frigate, false));  // hit 1 on frigate
        results.add(new IGame.ShotResult(true, false, frigate, false));  // hit 2 on frigate (hits > 1)

        Move move = new Move(6, List.of(), results);
        String json = move.processEnemyFire(true);

        assertTrue(json.contains("\"validShots\" : 3"));
        assertTrue(json.contains("\"missedShots\" : 0"));
        assertTrue(json.contains("\"outsideShots\" : 0"));
        assertTrue(json.contains("\"hits\" : 2"));
    }

    @Test
    @DisplayName("Should process single missed shot without printing verbose output")
    void testSingleMissNonVerbose() {
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false)
        );

        Move move = new Move(7, List.of(), results);
        String json = move.processEnemyFire(false);

        assertTrue(json.contains("\"validShots\" : 1"));
        assertTrue(json.contains("\"missedShots\" : 1"));
        assertTrue(json.contains("\"outsideShots\" : 2"));
    }
}