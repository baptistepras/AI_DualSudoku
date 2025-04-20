package CSudoku.player.ai;

import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.Player;
import CSudoku.referee.Referee;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class representing an evaluated and simulated Sudoku board.
 * Extends the functionality of {@link CSudokuBoard} by adding evaluation capabilities
 * for AI strategies and maintaining the state of rows, columns, and subgrids.
 */
public class EvaluatedSimulatedBoard extends CSudokuBoard {

    private int eval; // Evaluation score of the board.
    private Move lastMove; // The last move played on the board.
    private int[] zerosInRows; // Number of empty cells (zeros) in each row.
    private int[] zerosInColumns; // Number of empty cells (zeros) in each column.
    private Player player;

    /**
     * Creates an empty evaluated simulated board with the given size.
     *
     * @param size The size of the board (e.g., 4, 9, 16, etc.).
     */
    public EvaluatedSimulatedBoard(int size) {
        super(size);
        this.player = null;
        /* Score initialisé à 0 */
        this.eval = 0;
        this.lastMove = null;
        this.zerosInRows = new int[size];
        this.zerosInColumns = new int[size];

        for (int i = 0; i < size; i++) {
            this.zerosInRows[i] = size;
            this.zerosInColumns[i] = size;
        }
    }

    /**
     * Creates a deep copy of the given evaluated simulated board.
     *
     * @param board The board to copy.
     */
    public EvaluatedSimulatedBoard(EvaluatedSimulatedBoard board) {
        this(board.getSize());
        this.lastMove = board.getLastMove();
        this.player = board.player;

        // Copie des zéros
        for (int i = 0; i < board.getSize(); i++) {
            this.zerosInColumns[i] = board.getZerosInColumn(i);
            this.zerosInRows[i] = board.getZerosInRow(i);
        }

        this.eval = board.getEval();
        // Copie des valeurs
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                this.setValue(i, j, board.getValue(i, j));
            }
        }

        // Copie des contraintes
        for (Constraint c : board.getConstraints()) {
            this.addConstraint(c);
        }
    }

    /**
     * Creates a simulated board from an existing board, initializing it for evaluation.
     *
     * @param board  The original board.
     * @param player The AI/Automate player making the evaluation.
     */
    public EvaluatedSimulatedBoard(CSudokuBoard board, Player player) {
        this(board.getSize());
        this.player = player;

        // Copie des valeurs
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                this.setValue(i, j, board.getValue(i, j));
                // Copie des zéros
                if (!isCellEmpty(i, j)) {
                    decreaseZerosInRows(i);
                    decreaseZerosInColumns(j);
                }
            }
        }

        // Copie des contraintes
        for (Constraint c : board.getConstraints()) {
            this.addConstraint(c);
        }

    }

    /**
     * Returns the number of empty cells in a specified row.
     *
     * @param row The row index.
     * @return The number of empty cells in the row.
     */
    public int getZerosInRow(int row) {
        return this.zerosInRows[row];
    }

    /**
     * Returns the number of empty cells in a specified column.
     *
     * @param col The column index.
     * @return The number of empty cells in the column.
     */
    public int getZerosInColumn(int col) {
        return this.zerosInColumns[col];
    }

    /**
     * Checks if the board is completely filled.
     *
     * @return {@code true} if there are no empty cells, {@code false} otherwise.
     */
    public boolean isFull() {
        for (int i = 0; i < this.getSize(); i++) {
            if (getZerosInRow(i) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the evaluation score of the board.
     *
     * @return The current evaluation score.
     */
    public int getEval() {
        return this.eval;
    }

    /**
     * Sets the evaluation score of the board.
     *
     * @param eval The new evaluation score.
     */
    public void setEval(int eval) {
        this.eval = eval;
    }

    /**
     * Gets the last move played on the board.
     *
     * @return The last move.
     */
    public Move getLastMove() {
        return lastMove;
    }

    /**
     * Sets the last move played on the board.
     *
     * @param move The move to set.
     */
    public void setLastMove(Move move) {
        lastMove = move;
    }

    /**
     * Sets a value on the board and updates the evaluation score.
     *
     * @param move               The move to apply.
     * @param isMaximisingPlayer {@code true} if the move is by the maximizing player.
     */
    public void setValue(Move move, boolean isMaximisingPlayer, boolean ia, int depth) {
        // On récupère les informations du coup
        int row = move.getRow();
        int col = move.getCol();
        int value = move.getValue();
        // Pose le chiffre sur la grille
        this.setValue(row, col, value);
        this.setLastMove(move);
        int n = this.getSize();

        // Mise à jour des compteurs et mémorisation du coup joué
        this.decreaseZerosInRows(row);
        this.decreaseZerosInColumns(col);
        this.lastMove = move;
        // System.out.println("Zéros dans la ligne "+row+": "+getZerosInRow(row));
        // System.out.println("Zéros dans la colonne "+col+": "+getZerosInColumn(col));

        // Définir un multiplicateur en fonction du joueur : +1 pour maximisant, -1 pour minimisant
        int sign = isMaximisingPlayer ? 1 : -1;
        // Mise à jour de l'évaluation de base
        this.increaseScore(sign * value);
        // Si la colonne est complétée, on ajoute un bonus (ou pénalité) égal à n*n
        if (this.isColumnFilled(col)) {
            this.increaseScore(sign * n * n);
        }
        // Si la ligne est complétée, on ajoute un bonus (ou pénalité) égal à n*n
        if (this.isRowFilled(row)) {
            this.increaseScore(sign * n * n);
        }
        // Si la sous-grille est complétée, on ajoute un bonus (ou pénalité) égal à n*n
        if (this.isSubgridFilled(row, col)) {
            this.increaseScore(sign * n * n);
        }

        // Si la ligne, colonne ou sous-grille est presque complétée et que c'est le dernier tour
        // simulé, on ajoute une pénalité (ou un bonus) égale & n*n car le prochain joueur va
        // obtenir le bonus de complétion
        int isRow = this.isRowAlmostFilled(row);
        int isCol = this.isColumnAlmostFilled(col);
        int isSub = this.isSubgridAlmostFilled(row, col);
        if (isRow != -1 && ia && n > 4) {
            this.increaseScore(-sign * (n * n + isRow));
        }
        if (isCol != -1 && ia && n > 4) {
            this.increaseScore(-sign * (n * n + isCol));
        }
        if (isSub != -1 && ia && n > 4) {
            this.increaseScore(-sign * (n * n + isSub));
        }

        // Favorise les coups qui ne ferment pas la grille
        if (ia && isMaximisingPlayer && n>4) {
            this.increaseScore(evaluateMobility()/2);
        }
    }

    // Pour la compatibilité lorsque la méthode est utilisée ailleurs
    public void setValue(Move move, boolean isMaximisingPlayer, boolean ia) {
        setValue(move, isMaximisingPlayer, ia, -1);
    }

    /**
     * Retrieves all the moves the player can do.
     *
     * @return A list of all valid {@link Move} objects.
     */
    public ArrayList<Move> getAllMoves() {
        ArrayList<Move> output = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                if (isCellEmpty(i, j) && this.player == null) {
                    output.addAll(this.player.coupsPossibles(this, i, j));
                }
            }
        }
        return output;
    }

    /**
     * Checks if a row is completely filled.
     *
     * @param row The row index.
     * @return {@code true} if the row is filled, {@code false} otherwise.
     */
    public boolean isRowFilled(int row) {
        return getZerosInRow(row) == 0;
    }

    /**
     * Checks if a column is completely filled.
     *
     * @param col The column index.
     * @return {@code true} if the column is filled, {@code false} otherwise.
     */
    public boolean isColumnFilled(int col) {
        return getZerosInColumn(col) == 0;
    }

    /**
     * Checks if the subgrid (bloc) containing a cell is completely filled.
     *
     * @param row The row index.
     * @param col The column index.
     * @return {@code true} if the subgrid is completely filled, {@code false} otherwise.
     */
    public boolean isSubgridFilled(int row, int col) {
        int gridSize = (int) Math.sqrt(getSize());
        // Calculer le début de la sous-grille
        int startRow = (row / gridSize) * gridSize;
        int startCol = (col / gridSize) * gridSize;
        // Vérifier chaque case dans le bloc
        for (int i = startRow; i < startRow + gridSize; i++) {
            for (int j = startCol; j < startCol + gridSize; j++) {
                if (this.isCellEmpty(i, j)) {  // Si une case est vide, la sous-grille n'est pas remplie
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Decreases the count of empty cells in a row.
     *
     * @param row The row index.
     */
    public void decreaseZerosInRows(int row) {
        this.zerosInRows[row] = this.getZerosInRow(row) - 1;
    }

    /**
     * Decreases the count of empty cells in a column.
     *
     * @param col The column index.
     */
    public void decreaseZerosInColumns(int col) {
        this.zerosInColumns[col] = this.getZerosInColumn(col) - 1;
    }

    /**
     * Increases the evaluation score by a given value.
     *
     * @param val The amount to add to the score.
     */
    public void increaseScore(int val) {
        this.setEval(this.getEval() + val);
    }

    /**
     * Checks if a cell is empty.
     *
     * @param row The row index.
     * @param col The column index.
     * @return {@code true} if the cell is empty, {@code false} otherwise.
     */
    public boolean isCellEmpty(int row, int col) {
        return super.isCellEmpty(row, col);
    }

    /**
     * Checks if the column containing a cell is almost filled.
     *
     * @param col The column index.
     * @return best move value if the column can be filled in one move, -1 otherwise.
     */
    public int isColumnAlmostFilled(int col) {
        if (getZerosInColumn(col) != 1 || this.player == null) {
            return -1;
        }
        for (int i = 0; i < getSize(); i++) {
            if (isCellEmpty(i, col)) {
                ArrayList<Move> coups = this.player.coupsPossibles(this, i, col);
                if (coups.size() > 0) {
                    return coups.get(0).getValue(); // Renvoie le premier coup jouable
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Checks if the row containing a cell is almost filled.
     *
     * @param row The row index.
     * @return best move value if the row can be filled in one move, -1 otherwise.
     */
    public int isRowAlmostFilled(int row) {
        if (getZerosInRow(row) != 1 || this.player == null) {
            return -1;
        }
        for (int i = 0; i < getSize(); i++) {
            if (isCellEmpty(row, i)) {
                ArrayList<Move> coups = this.player.coupsPossibles(this, row, i);
                if (coups.size() > 0) {
                    return coups.get(0).getValue(); // Renvoie le premier coup jouable
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Checks if the subgrid containing a cell is almost filled.
     *
     * @param row The row index.
     * @param col The column index.
     * @return best move value if the subgrid can be filled in one move, -1 otherwise.
     */
    public int isSubgridAlmostFilled(int row, int col) {
        int gridSize = (int) Math.sqrt(getSize());
        int casesVides = 0;
        ArrayList<Move> coups = new ArrayList<>();
        // Calculer le début de la sous-grille
        int startRow = (row / gridSize) * gridSize;
        int startCol = (col / gridSize) * gridSize;
        // Vérifier chaque case dans le bloc
        for (int i = startRow; i < startRow + gridSize; i++) {
            for (int j = startCol; j < startCol + gridSize; j++) {
                if (this.isCellEmpty(i, j) && this.player != null) {
                    casesVides++;
                    coups = this.player.coupsPossibles(this, i, j);
                }
            }
        }
        if (casesVides == 1) {  // Il y a exactement une case vide
            if (!coups.isEmpty()) {  // Et un coup peut être joué dedans
                return coups.get(0).getValue();  // Return le premier coup disponible dans la sous-grille
            }
        }
        return -1;
    }

    /**
     * Évalue le bonus de mobilité en sommant, pour chaque case vide, le nombre de coups possibles
     */
    private int evaluateMobility() {
        int bonus = 0;
        int n = getSize();
        // Pour chaque case vide du plateau, ajouter le nombre de coups possibles
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (isCellEmpty(row, col) && this.player != null) {
                    bonus += player.coupsPossibles(this, row, col).size();
                }
            }
        }
        return bonus;
    }
}