package battleship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a position on the game board.
 */
public class Position implements IPosition {

	private static final int[][] DIRECTIONS = {
			{-1, 0},  // north
			{0, 1},   // east
			{1, 0},   // south
			{0, -1},  // west
			{1, 1},   // northeast
			{1, -1},  // northwest
			{-1, 1},  // southeast
			{-1, -1}  // southwest
	};

	private final int row;
	private final int column;
	private boolean isOccupied;
	private boolean isHit;

	public static Position randomPosition() {
		int row = (int) (Math.random() * Game.BOARD_SIZE);
		int col = (int) (Math.random() * Game.BOARD_SIZE);
		return new Position(row, col);
	}

	public Position(char classicRow, int classicColumn) {
		this.row = Character.toUpperCase(classicRow) - 'A';
		this.column = classicColumn - 1;
		this.isOccupied = false;
		this.isHit = false;
	}

	public Position(int row, int column) {
		this.row = row;
		this.column = column;
		this.isOccupied = false;
		this.isHit = false;
	}

	@Override
	public int getRow() {
		return row;
	}

	@Override
	public int getColumn() {
		return column;
	}

	public char getClassicRow() {
		return (char) ('A' + row);
	}

	public int getClassicColumn() {
		return column + 1;
	}

	@Override
	public boolean isInside() {
		return row >= 0 && column >= 0 && row < Game.BOARD_SIZE && column < Game.BOARD_SIZE;
	}

	@Override
	public boolean isAdjacentTo(IPosition other) {
		return Math.abs(this.row - other.getRow()) <= 1 && Math.abs(this.column - other.getColumn()) <= 1;
	}

	@Override
	public List<IPosition> adjacentPositions() {
		List<IPosition> adjacents = new ArrayList<>();

		for (int[] dir : DIRECTIONS) {
			Position newPosition = new Position(row + dir[0], column + dir[1]);
			if (newPosition.isInside()) {
				adjacents.add(newPosition);
			}
		}

		return adjacents;
	}

	@Override
	public boolean isOccupied() {
		return isOccupied;
	}

	@Override
	public boolean isHit() {
		return isHit;
	}

	@Override
	public void occupy() {
		isOccupied = true;
	}

	@Override
	public void shoot() {
		isHit = true;
	}

	@Override
	public boolean equals(Object otherPosition) {
		if (this == otherPosition) {
			return true;
		}
		if (otherPosition instanceof IPosition) {
			IPosition other = (IPosition) otherPosition;
			return this.row == other.getRow() && this.column == other.getColumn();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(row, column, isOccupied, isHit);
	}

	@Override
	public String toString() {
		return (char) ('A' + row) + "" + (column + 1);
	}
}
