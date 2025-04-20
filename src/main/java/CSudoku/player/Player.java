package CSudoku.player;

import CSudoku.CSudokuGame;
import CSudoku.board.CSudokuBoard;;
import CSudoku.board.Move;
import CSudoku.player.ai.EvaluatedSimulatedBoard;
import CSudoku.player.ai.IACompetitionStrategy.Return;

import java.util.ArrayList;

/**
 * Represents a player in the Sudoku game.
 * This interface defines the behavior for any type of player,
 * whether human or AI, by specifying how they select their moves.
 * <p>
 * Implementing classes should provide the logic for how players make decisions during the game,
 * including how they generate and validate moves based on the current state of the board.
 * </p>
 */
public interface Player {

    /**
     * Retrieves the next move chosen by the player.
     * <p>
     * This method is used to ask the player for their next move given the current state of the Sudoku board.
     * The move is returned as a {@link Move} object, which contains the row, column, and value chosen by the player.
     * </p>
     *
     * @param board The current state of the Sudoku board, represented by the {@link CSudokuBoard} object.
     *              The player will use this board to determine their next move.
     * @return The move selected by the player as a {@link Move} object.
     *         The {@link Move} includes the row, column, and value that the player has chosen.
     * @see Move
     */
    Move getMove(CSudokuBoard board);
    Return getMove2(CSudokuBoard board);


    /**
     * Checks whether a move is valid on the given game board.
     *
     * @param board The current game board.
     * @param move  The move to validate.
     * @return {@code true} if the move is valid, {@code false} otherwise.
     */
    default boolean isValidMove(CSudokuBoard board, Move move) {
        int row = move.getRow();
        int col = move.getCol();
        int size = board.getSize();
        int val = move.getValue();

        // Vérifier que la case est vide
        if (!board.isCellEmpty(row, col)) {
            return false;
        }

        // Vérifier que la valeur n'est pas déjà présente dans la ligne
        for (int i = 0; i < size; i++) {
            if (board.getValue(row, i) == val) {
                return false;
            }
        }

        // Vérifier que la valeur n'est pas déjà présente dans la colonne
        for (int i = 0; i < size; i++) {
            if (board.getValue(i, col) == val) {
                return false;
            }
        }

        // Vérifier que la valeur n'est pas déjà présente dans la sous-grille
        int n = (int) Math.sqrt(size);
        int gridRow = row / n;
        int gridCol = col / n;
        int startRow = gridRow * n;
        int startCol = gridCol * n;
        for (int i = startRow; i < startRow + n; i++) {
            for (int j = startCol; j < startCol + n; j++) {
                if (board.getValue(i, j) == val) {
                    return false;
                }
            }
        }

        // Vérification des contraintes consécutives dans toutes les directions.
        // Direction vers le bas
        if (row + 1 < size && board.hasConsecutiveConstraint(row, col, row + 1, col)) {
            if (!board.isCellEmpty(row + 1, col)) {
                if (Math.abs(val - board.getValue(row + 1, col)) != 1) {
                    return false;
                }
            }
        }

        // Direction vers le haut
        if (row - 1 >= 0 && board.hasConsecutiveConstraint(row - 1, col, row, col)) {
            if (!board.isCellEmpty(row - 1, col)) {
                if (Math.abs(val - board.getValue(row - 1, col)) != 1) {
                    return false;
                }
            }
        }

        // Direction vers la droite
        if (col + 1 < size && board.hasConsecutiveConstraint(row, col, row, col + 1)) {
            if (!board.isCellEmpty(row, col + 1)) {
                if (Math.abs(val - board.getValue(row, col + 1)) != 1) {
                    return false;
                }
            }
        }

        // Direction vers la gauche
        if (col - 1 >= 0 && board.hasConsecutiveConstraint(row, col - 1, row, col)) {
            if (!board.isCellEmpty(row, col - 1)) {
                if (Math.abs(val - board.getValue(row, col - 1)) != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *  Returns the valid moves the player can play at (i,j)
     * @param board
     * @param i
     * @param j
     * @return
     */
    default ArrayList<Move> coupsPossibles(CSudokuBoard board, int i, int j) {
        ArrayList<Move> output= new ArrayList<>();
        for (int k = board.getSize(); k >= 1; k--){
            Move move = new Move(i, j, k);
            if (isValidMove(board, move)){
                output.add(move);
            }
        }
        return output;
    }
}