package CSudoku.player.automate;

import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.Player;
import CSudoku.player.MoveStrategy;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a strategy for selecting a random valid move in the Sudoku game.
 * The strategy generates a list of all valid moves and randomly selects one.
 */
public class RandomMoveStrategy implements MoveStrategy {

    Random randomGenerator = new Random();

    /**
     * Selects a valid move for the AI player by randomly choosing from all possible valid moves.
     * A valid move is one that satisfies the Sudoku rules.
     *
     * @param board The current state of the Sudoku board.
     * @param player The AI player who is making the move.
     * @return A randomly selected {@link Move} object, or null if no valid moves are available.
     */
    @Override
    public Move selectMove(CSudokuBoard board, Player player) {
        if (board.getSize() == 0 || board.isFull()) {
            return null;
        } else {
            // Cherche les cases vides
            int N = board.getSize();
            var emptyCells = new ArrayList<Couple>();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (board.isCellEmpty(i, j)) {
                        emptyCells.add(new Couple(i, j));
                    }
                }
            }

            // Si aucune cellule vide n'existe, on retourne null
            if (emptyCells.isEmpty()) {
                return null;
            }

            // Liste pour stocker tous les coups valides trouvés dans toutes les cellules vides
            ArrayList<Move> validMoves = new ArrayList<>();
            for (Couple pos : emptyCells) {
                for (int k = 1; k <= board.getSize(); k++){
                    Move move = new Move(pos.first, pos.second, k);
                    if (player.isValidMove(board, move)) {
                        validMoves.add(move);
                    }
                }
            }

            // Si aucune coup n'est valide sur aucune cellule, retourner null
            if (validMoves.isEmpty()) {
                return null;
            }

            // Choisir aléatoirement un coup valide parmi la liste globale.
            int index = randomGenerator.nextInt(validMoves.size());
            return validMoves.get(index);
        }
    }

    public Return selectMove2(CSudokuBoard board, Player player) {
        if (board.getSize() == 0 || board.isFull()) {
            return new Return(new Stats(), null);
        } else {
            // Cherche les cases vides
            long startTime = System.nanoTime();
            int N = board.getSize();
            var emptyCells = new ArrayList<Couple>();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (board.isCellEmpty(i, j)) {
                        emptyCells.add(new Couple(i, j));
                    }
                }
            }

            // Si aucune cellule vide n'existe, on retourne null
            if (emptyCells.isEmpty()) {
                return null;
            }

            // Liste pour stocker tous les coups valides trouvés dans toutes les cellules vides
            ArrayList<Move> validMoves = new ArrayList<>();
            for (Couple pos : emptyCells) {
                for (int k = 1; k <= board.getSize(); k++){
                    Move move = new Move(pos.first, pos.second, k);
                    if (player.isValidMove(board, move)) {
                        validMoves.add(move);
                    }
                }
            }

            // Si aucune coup n'est valide sur aucune cellule, retourner null
            if (validMoves.isEmpty()) {
                return null;
            }

            // Choisir aléatoirement un coup valide parmi la liste globale
            int index = randomGenerator.nextInt(validMoves.size());
            Stats stats = new Stats();
            double temps = (System.nanoTime() - startTime) / 1000000000.0;
            stats.total_coups += 1;
            stats.total_temps += temps;
            stats.max_noeuds = 0;
            stats.min_noeuds = 0;
            stats.max_temps = temps;
            stats.min_temps = temps;
            return new Return(stats, validMoves.get(index));
        }
    }

    /**
     * Returns the name of the strategy, which in this case is "Random".
     * <p>
     * This method is part of the {@link MoveStrategy} interface and is used to retrieve
     * the name of the strategy being implemented. In this case, it returns the string "Random",
     * which indicates that the Random Move strategy is used for selecting moves.
     * </p>
     *
     * @return The name of the strategy, which is {@code "Random"}.
     */
    @Override
    public String getName() {
        return "Random";
    }
}