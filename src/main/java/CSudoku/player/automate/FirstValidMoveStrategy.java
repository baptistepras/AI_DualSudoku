package CSudoku.player.automate;

import CSudoku.player.Player;
import CSudoku.player.MoveStrategy;
import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;

import java.util.ArrayList;
import java.util.Random;

/**
 * Implements a simple strategy for selecting the first valid move on a Sudoku board.
 * This strategy iterates over the board cells and selects the first valid move that
 * can be made, considering the rules of Sudoku.
 */
public class FirstValidMoveStrategy implements MoveStrategy {

    Random randomGenerator = new Random();

    /**
     * Selects the first valid move available on the given Sudoku board.
     *
     * @param board  The current state of the Sudoku board.
     * @param player The AI player attempting to make a move.
     * @return A {@link Move} object representing the first valid move found,
     *         or {@code null} if no valid moves are available.
     */
    @Override
    public Move selectMove(CSudokuBoard board, Player player) {
        if (board.grid == null || board.isFull()) {
            return null;
        } else {
            // Choix de la case
            int N = board.getSize();
            var array = new ArrayList<Couple>();
            for (int i  = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (board.isCellEmpty(i, j)) {
                        array.add(new Couple(i, j));
                    }
                }
            }

            // Choix du nombre à placer
            for (int i = 0; i < array.size(); i++){
                Couple pos = array.get(i);
                ArrayList<Move> moves = player.coupsPossibles(board, pos.first, pos.second);
                if (!moves.isEmpty()) {
                    for (int candidate = N; candidate >= 1; candidate--) {
                        for (Move move : moves) {
                            // Renvoie la meilleure valeur possible dans la première case possible
                            if (move.getValue() == candidate) {
                                return move;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public Return selectMove2(CSudokuBoard board, Player player) {
        if (board.grid == null || board.isFull()) {
            return new Return(new Stats(), null);
        } else {
            // Choix de la case
            long startTime = System.nanoTime();
            int N = board.getSize();
            var array = new ArrayList<Couple>();
            for (int i  = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (board.isCellEmpty(i, j)) {
                        array.add(new Couple(i, j));
                    }
                }
            }

            // Choix du nombre à placer
            for (int i = 0; i < array.size(); i++){
                Couple pos = array.get(i);
                ArrayList<Move> moves = player.coupsPossibles(board, pos.first, pos.second);
                if (!moves.isEmpty()){
                    for (int candidate = N; candidate >= 1; candidate--) {
                        for (Move move : moves) {
                            // Renvoie la meilleure valeur possible dans la première case possible
                            if (move.getValue() == candidate) {
                                Stats stats = new Stats();
                                double temps = (System.nanoTime() - startTime) / 1000000000.0;
                                stats.total_coups += 1;
                                stats.total_temps += temps;
                                stats.max_noeuds = 0;
                                stats.min_noeuds = 0;
                                stats.max_temps = temps;
                                stats.min_temps = temps;
                                return new Return(stats, move);
                            }
                        }
                    }
                    Stats stats = new Stats();
                    double temps = (System.nanoTime() - startTime) / 1000000000.0;
                    stats.total_coups += 1;
                    stats.total_temps += temps;
                    stats.max_noeuds = 0;
                    stats.min_noeuds = 0;
                    stats.max_temps = temps;
                    stats.min_temps = temps;
                }
            }
        }
        return null;
    }

    /**
     * Returns the name of the strategy, which in this case is "First valid".
     * <p>
     * This method is part of the {@link MoveStrategy} interface and is used to retrieve
     * the name of the strategy being implemented. In this case, it returns the string "First valid",
     * which indicates that the First Valid Move strategy is used for selecting moves.
     * </p>
     *
     * @return The name of the strategy, which is {@code "First valid"}.
     */
    @Override
    public String getName() {
        return "First valid";
    }
}