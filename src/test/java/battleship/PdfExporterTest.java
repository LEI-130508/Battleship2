package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PdfExporterTest {

	private static final String TEST_PDF = "target/test_relatorio_jogadas.pdf";
	private Game game;

	@BeforeEach
	void setUp() {
		IFleet fleet = Fleet.createRandom();
		game = new Game(fleet);
	}

	@AfterEach
	void tearDown() {
		File file = new File(TEST_PDF);
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	void testExportGameWithNoMoves() {
		boolean success = PdfExporter.exportGameToPdf(game, TEST_PDF);
		assertTrue(success, "A exportação para PDF sem jogadas deve ser bem-sucedida.");

		File file = new File(TEST_PDF);
		assertTrue(file.exists(), "O ficheiro PDF deve existir.");
		assertTrue(file.length() > 0, "O ficheiro PDF não deve estar vazio.");
	}

	@Test
	void testExportGameWithMoves() {
		// Executar uma rajada de tiros
		game.fireShots(List.of(new Position(0, 0), new Position(1, 1), new Position(2, 2)));

		boolean success = PdfExporter.exportGameToPdf(game, TEST_PDF);
		assertTrue(success, "A exportação para PDF com jogadas deve ser bem-sucedida.");

		File file = new File(TEST_PDF);
		assertTrue(file.exists(), "O ficheiro PDF deve existir após jogadas.");
		assertTrue(file.length() > 0, "O ficheiro PDF deve conter conteúdo.");
	}

	@Test
	void testExportNullGameThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> PdfExporter.exportGameToPdf(null, TEST_PDF));
	}
}
