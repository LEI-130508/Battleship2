package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Shot
 */
public class Move implements IMove {

	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		Map<String, Integer> sunkBoatsCount = new HashMap<>();
		Map<String, Integer> hitsPerBoat = new HashMap<>();

		for (IGame.ShotResult result : this.shotResults) {
			if (!result.valid()) {
				continue;
			}

			if (result.repeated()) {
				repeatedShots++;
			} else {
				validShots++;
				if (result.ship() == null) {
					missedShots++;
				} else {
					String boatName = result.ship().getCategory();
					hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
					if (result.sunk()) {
						sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
					}
				}
			}
		}

		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		if (verbose) {
			printVerboseSummary(validShots, repeatedShots, missedShots, outsideShots, sunkBoatsCount, hitsPerBoat);
		}

		Map<String, Object> response = buildResponseMap(validShots, repeatedShots, missedShots, outsideShots, sunkBoatsCount, hitsPerBoat);
		String jsonString = serializeResponse(response);

		System.out.println(jsonString);
		System.out.println();

		return jsonString;
	}

	private void printVerboseSummary(int validShots, int repeatedShots, int missedShots, int outsideShots,
									 Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
		StringBuilder output = new StringBuilder();

		if (validShots == 0 && repeatedShots > 0) {
			output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
		} else {
			if (validShots > 0) {
				output.append(validShots).append(" tiro").append(validShots > 1 ? "s" : "").append(" válido").append(validShots > 1 ? "s" : "").append(": ");
			}

			if (!sunkBoatsCount.isEmpty()) {
				for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
					String boatName = entry.getKey();
					int count = entry.getValue();
					output.append(count).append(" ").append(boatName).append(count > 1 ? "s" : "").append(" ao fundo").append(" + ");
				}
			}

			if (!hitsPerBoat.isEmpty()) {
				for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
					String boatName = entry.getKey();
					int hits = entry.getValue();
					if (!sunkBoatsCount.containsKey(boatName)) {
						output.append(hits).append(" tiro").append(hits > 1 ? "s" : "").append(" num(a) ").append(boatName).append(" + ");
					}
				}
			}

			if (missedShots > 0) {
				output.append(missedShots).append(" tiro").append(missedShots > 1 ? "s" : "").append(" na água");
			} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
				output.setLength(output.length() - 2);
			}

			if (repeatedShots > 0) {
				if (validShots > 0) {
					output.append(", ");
				}
				output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
			}
		}

		if (outsideShots > 0) {
			if (!output.isEmpty()) {
				output.append(", ");
			}
			output.append(outsideShots).append(" tiro").append(outsideShots > 1 ? "s" : "").append(" exterior").append(outsideShots > 1 ? "es" : "");
		}

		System.out.println("Jogada nº" + this.number + " -> " + output);
	}

	private Map<String, Object> buildResponseMap(int validShots, int repeatedShots, int missedShots, int outsideShots,
												 Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
		Map<String, Object> response = new HashMap<>();
		response.put("validShots", validShots);
		response.put("outsideShots", outsideShots);
		response.put("repeatedShots", repeatedShots);
		response.put("missedShots", missedShots);

		List> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("count", entry.getValue());
			sunkBoats.add(boat);
		}
		response.put("sunkBoats", sunkBoats);

		List> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				Map<String, Object> boat = new HashMap<>();
				boat.put("type", entry.getKey());
				boat.put("hits", entry.getValue());
				boatHits.add(boat);
			}
		}
		response.put("hitsOnBoats", boatHits);
		return response;
	}

	private String serializeResponse(Map<String, Object> response) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {
			return objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}
	}
}
