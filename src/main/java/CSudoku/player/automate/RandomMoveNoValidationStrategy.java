package CSudoku.player.automate;

import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.Player;
import CSudoku.player.MoveStrategy;
import CSudoku.player.ai.IACompetitionStrategy;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;

import java.util.ArrayList;
import java.util.Random;


class Couple {
    int first;
    int second;

    public Couple(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}

/**
 * Represents a strategy for selecting a random move in the Sudoku game.
 * The strategy selects an empty cell at random and assigns a random value.
 * No validation is performed to check if the move complies with Sudoku rules.
 */
public class RandomMoveNoValidationStrategy implements MoveStrategy {

    /**
     * Selects a random move for the AI player by choosing an empty cell
     * and assigning a random value, without checking for move validity.
     *
     * @param board The current state of the Sudoku board.
     * @param player The AI player who is making the move.
     * @return A randomly selected {@link Move} object, or null if no empty cells are available.
     */

    Random randomGenerator = new Random();


    @Override
    public Move selectMove(CSudokuBoard board, Player player) {
        if (board.getSize() == 0 || board.isFull()) {
            return null;
        } else {
            ArrayList<Couple> positions = new ArrayList<>();
            for (int i = 0; i < board.getSize(); i++){
                for (int j = 0; j < board.getSize(); j++){
                    if (board.isCellEmpty(i, j)){
                    positions.add(new Couple(i, j));}
                }
            }

            if (positions.isEmpty()) {
                return null;
            }

            Couple pos = positions.get(randomGenerator.nextInt(positions.size()));
            int v = randomGenerator.nextInt(1, board.getSize()+1);
            return new Move(pos.first, pos.second, v);}
    }

    public Return selectMove2(CSudokuBoard board, Player player) {
        if (board.getSize() == 0 || board.isFull()) {
            return new Return(new Stats(), null);
        } else {
            long startTime = System.nanoTime();
            ArrayList<Couple> positions = new ArrayList<>();
            for (int i = 0; i < board.getSize(); i++) {
                for (int j = 0; j < board.getSize(); j++) {
                    if (board.isCellEmpty(i, j)) {
                        positions.add(new Couple(i, j));
                    }
                }
            }
            if (positions.isEmpty()) {
                return null;
            }
            Couple pos = positions.get(randomGenerator.nextInt(positions.size()));
            int v = randomGenerator.nextInt(1, board.getSize() + 1);
            Stats stats = new Stats();
            double temps = (System.nanoTime() - startTime) / 1000000000.0;
            stats.total_coups += 1;
            stats.total_temps += temps;
            stats.max_noeuds = 0;
            stats.min_noeuds = 0;
            stats.max_temps = temps;
            stats.min_temps = temps;
            return new Return(stats, new Move(pos.first, pos.second, v));
        }
    }

    /**
     * Returns the name of the strategy, which in this case is "Random No Validation".
     * <p>
     * This method is part of the {@link MoveStrategy} interface and is used to retrieve
     * the name of the strategy being implemented. In this case, it returns the string "Random No Validation",
     * which indicates that the Random No Validation Move strategy is used for selecting moves.
     * </p>
     *
     * @return The name of the strategy, which is {@code "Random"}.
     */
    @Override
    public String getName() {
        return "Random No Validation";
    }
}