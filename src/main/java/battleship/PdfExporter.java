package battleship;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporter class responsible for generating a PDF report of a Battleship game session.
 */
public class PdfExporter {

	private static final Color PRIMARY_COLOR = new Color(26, 54, 93);
	private static final Color ACCENT_COLOR = new Color(49, 130, 206);
	private static final Color HEADER_BG = new Color(237, 242, 247);
	private static final Color ROW_ALT_BG = new Color(247, 250, 252);
	private static final Color HIT_COLOR = new Color(197, 48, 48);
	private static final Color WATER_COLOR = new Color(43, 108, 176);

	/**
	 * Exports the state and move history of a game to a PDF file.
	 *
	 * @param game the game instance to export
	 * @param filePath target file path for the PDF document
	 * @return true if export was successful, false otherwise
	 */
	public static boolean exportGameToPdf(IGame game, String filePath) {
		if (game == null) {
			throw new IllegalArgumentException("O jogo não pode ser nulo.");
		}

		Document document = new Document(PageSize.A4, 36, 36, 40, 40);

		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			PdfWriter.getInstance(document, fos);
			document.open();

			// Fontes
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_COLOR);
			Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
			Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PRIMARY_COLOR);
			Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
			Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

			// Título
			Paragraph title = new Paragraph("Relatório de Jogo - Batalha Naval", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			title.setSpacingAfter(4f);
			document.add(title);

			// Data/hora
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
			Paragraph subtitle = new Paragraph("Gerado em: " + timestamp, subtitleFont);
			subtitle.setAlignment(Element.ALIGN_CENTER);
			subtitle.setSpacingAfter(16f);
			document.add(subtitle);

			// Resumo Geral
			Paragraph summaryHeader = new Paragraph("Resumo Geral da Partida", sectionFont);
			summaryHeader.setSpacingAfter(8f);
			document.add(summaryHeader);

			PdfPTable summaryTable = new PdfPTable(4);
			summaryTable.setWidthPercentage(100);
			summaryTable.setSpacingAfter(16f);

			addSummaryCell(summaryTable, "Total de Jogadas", String.valueOf(game.getAlienMoves().size()), boldFont, normalFont);
			addSummaryCell(summaryTable, "Navios Restantes", String.valueOf(game.getRemainingShips()), boldFont, normalFont);
			addSummaryCell(summaryTable, "Navios Afundados", String.valueOf(game.getSunkShips()), boldFont, normalFont);
			addSummaryCell(summaryTable, "Tiros Repetidos", String.valueOf(game.getRepeatedShots()), boldFont, normalFont);

			document.add(summaryTable);

			// Histórico de Jogadas
			Paragraph movesHeader = new Paragraph("Histórico Detalhado de Jogadas (Rajadas)", sectionFont);
			movesHeader.setSpacingAfter(8f);
			document.add(movesHeader);

			List<IMove> moves = game.getAlienMoves();
			if (moves.isEmpty()) {
				Paragraph noMoves = new Paragraph("Nenhuma rajada foi realizada nesta partida até ao momento.", normalFont);
				noMoves.setSpacingAfter(16f);
				document.add(noMoves);
			} else {
				PdfPTable movesTable = new PdfPTable(new float[]{1.2f, 2.5f, 5.0f});
				movesTable.setWidthPercentage(100);
				movesTable.setSpacingAfter(16f);

				// Cabeçalho da tabela
				addTableHeader(movesTable, "Rajada nº", boldFont);
				addTableHeader(movesTable, "Coordenadas dos Tiros", boldFont);
				addTableHeader(movesTable, "Resultado da Jogada", boldFont);

				int rowIdx = 0;
				for (IMove move : moves) {
					Color rowBg = (rowIdx % 2 == 0) ? Color.WHITE : ROW_ALT_BG;

					// Coluna 1: Número da jogada
					PdfPCell cellNum = new PdfPCell(new Phrase(String.valueOf(move.getNumber()), normalFont));
					cellNum.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cellNum.setBackgroundColor(rowBg);
					cellNum.setPadding(6f);
					movesTable.addCell(cellNum);

					// Coluna 2: Coordenadas
					StringBuilder shotsStr = new StringBuilder();
					for (IPosition pos : move.getShots()) {
						if (!shotsStr.isEmpty()) shotsStr.append(", ");
						shotsStr.append(pos.getClassicRow()).append(pos.getClassicColumn());
					}
					PdfPCell cellShots = new PdfPCell(new Phrase(shotsStr.toString(), normalFont));
					cellShots.setHorizontalAlignment(Element.ALIGN_CENTER);
					cellShots.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cellShots.setBackgroundColor(rowBg);
					cellShots.setPadding(6f);
					movesTable.addCell(cellShots);

					// Coluna 3: Resultados dos tiros
					StringBuilder resultSummary = new StringBuilder();
					List<IGame.ShotResult> results = move.getShotResults();
					for (int i = 0; i < results.size(); i++) {
						IGame.ShotResult r = results.get(i);
						IPosition pos = (i < move.getShots().size()) ? move.getShots().get(i) : null;
						String posLabel = (pos != null) ? (pos.getClassicRow() + "" + pos.getClassicColumn()) : ("Tiro " + (i + 1));

						if (!r.valid()) {
							resultSummary.append("• ").append(posLabel).append(": Fora do tabuleiro\n");
						} else if (r.repeated()) {
							resultSummary.append("• ").append(posLabel).append(": Repetido\n");
						} else if (r.ship() == null) {
							resultSummary.append("• ").append(posLabel).append(": Água\n");
						} else {
							resultSummary.append("• ").append(posLabel).append(": Acerto em ")
									.append(r.ship().getCategory())
									.append(r.sunk() ? " (AFUNDADO!)" : "")
									.append("\n");
						}
					}
					// Remove newline final
					String summaryText = resultSummary.toString().trim();
					PdfPCell cellResult = new PdfPCell(new Phrase(summaryText, normalFont));
					cellResult.setBackgroundColor(rowBg);
					cellResult.setPadding(6f);
					movesTable.addCell(cellResult);

					rowIdx++;
				}
				document.add(movesTable);
			}

			// Estado Final da Frota
			Paragraph fleetHeader = new Paragraph("Estado Atual / Final da Frota", sectionFont);
			fleetHeader.setSpacingAfter(8f);
			document.add(fleetHeader);

			if (game.getMyFleet() != null) {
				PdfPTable fleetTable = new PdfPTable(new float[]{2.5f, 1.5f, 2.0f, 3.5f});
				fleetTable.setWidthPercentage(100);
				fleetTable.setSpacingAfter(16f);

				addTableHeader(fleetTable, "Tipo de Navio", boldFont);
				addTableHeader(fleetTable, "Tamanho", boldFont);
				addTableHeader(fleetTable, "Estado", boldFont);
				addTableHeader(fleetTable, "Posições Ocupadas", boldFont);

				int fleetRowIdx = 0;
				for (IShip ship : game.getMyFleet().getShips()) {
					Color rowBg = (fleetRowIdx % 2 == 0) ? Color.WHITE : ROW_ALT_BG;

					// Categoria
					PdfPCell cCategory = new PdfPCell(new Phrase(ship.getCategory(), normalFont));
					cCategory.setBackgroundColor(rowBg);
					cCategory.setPadding(5f);
					fleetTable.addCell(cCategory);

					// Tamanho
					PdfPCell cSize = new PdfPCell(new Phrase(String.valueOf(ship.getSize()), normalFont));
					cSize.setHorizontalAlignment(Element.ALIGN_CENTER);
					cSize.setBackgroundColor(rowBg);
					cSize.setPadding(5f);
					fleetTable.addCell(cSize);

					// Estado (A flutuar ou Afundado)
					boolean floating = ship.stillFloating();
					Phrase statusPhrase = new Phrase(floating ? "A Flutuar" : "Afundado",
							FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, floating ? WATER_COLOR : HIT_COLOR));
					PdfPCell cStatus = new PdfPCell(statusPhrase);
					cStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
					cStatus.setBackgroundColor(rowBg);
					cStatus.setPadding(5f);
					fleetTable.addCell(cStatus);

					// Posições
					StringBuilder posStr = new StringBuilder();
					for (IPosition pos : ship.getPositions()) {
						if (!posStr.isEmpty()) posStr.append(", ");
						posStr.append(pos.getClassicRow()).append(pos.getClassicColumn());
					}
					PdfPCell cPos = new PdfPCell(new Phrase(posStr.toString(), normalFont));
					cPos.setBackgroundColor(rowBg);
					cPos.setPadding(5f);
					fleetTable.addCell(cPos);

					fleetRowIdx++;
				}
				document.add(fleetTable);
			}

			document.close();
			return true;
		} catch (DocumentException | IOException e) {
			System.err.println("Erro ao gerar o ficheiro PDF: " + e.getMessage());
			return false;
		}
	}

	private static void addTableHeader(PdfPTable table, String headerTitle, Font font) {
		PdfPCell header = new PdfPCell(new Phrase(headerTitle, font));
		header.setBackgroundColor(HEADER_BG);
		header.setHorizontalAlignment(Element.ALIGN_CENTER);
		header.setPadding(6f);
		table.addCell(header);
	}

	private static void addSummaryCell(PdfPTable table, String label, String value, Font boldFont, Font normalFont) {
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(ROW_ALT_BG);
		cell.setPadding(8f);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);

		Paragraph pVal = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, PRIMARY_COLOR));
		pVal.setAlignment(Element.ALIGN_CENTER);
		Paragraph pLbl = new Paragraph(label, normalFont);
		pLbl.setAlignment(Element.ALIGN_CENTER);

		cell.addElement(pVal);
		cell.addElement(pLbl);
		table.addCell(cell);
	}
}
