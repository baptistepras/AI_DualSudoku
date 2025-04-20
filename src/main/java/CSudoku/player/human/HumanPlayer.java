package CSudoku.player.human;

import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.Player;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;

import java.util.Scanner;

/**
 * Represents a human player in the Sudoku game.
 * This class allows a human player to input their move interactively through the console.
 */
public class HumanPlayer implements Player {

    /**
     * Default constructor.
     */
    public HumanPlayer() {
        // Aucun état particulier à initialiser
    }

    /**
     * Prompts the human player to input their move.
     * The player is asked to enter the row, column, and value of their move,
     * separated by spaces. The input is then used to create a {@link Move} object.
     *
     * @param board The current state of the Sudoku board.
     * @return A {@link Move} object containing the player's input.
     */
    @Override
    public Move getMove(CSudokuBoard board) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the row, column, and value (separated by spaces): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        int value = scanner.nextInt();
        return new Move(row, col, value);
    }

    /**
     * Prompts the human player to input their move and returns additional statistics.
     *
     * @param board The current state of the Sudoku board.
     * @return A {@link Return} object containing both the move and some basic statistics.
     */
    public Return getMove2(CSudokuBoard board) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the row, column, and value (separated by spaces): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
        int value = scanner.nextInt();
        Stats stats = new Stats();
        stats.total_coups += 1;
        stats.max_noeuds = 0;
        stats.min_noeuds = 0;
        stats.max_temps = 0;
        stats.min_temps = 0;
        return new Return(stats, new Move(row, col, value));
    }

    /**
     * Returns a clone of this HumanPlayer instance.
     * Since HumanPlayer does not maintain any mutable state, the cloning operation is trivial.
     *
     * @return A new HumanPlayer instance.
     */
    @Override
    public HumanPlayer clone() {
        return new HumanPlayer();
    }
}