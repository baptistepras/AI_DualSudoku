package CSudoku.player.student;

import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.MoveStrategy;
import CSudoku.player.Player;
import java.util.ArrayList;
import java.util.List;

import CSudoku.player.ai.AIPlayer;
import CSudoku.player.ai.IACompetitionStrategy;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;
import java.util.ArrayList;
import java.util.List;

public class StudentAI_theoutliertaskers implements MoveStrategy {
    private static final int MAX_DEPTH = 3; // Depth of the search tree

    /**
     * Checks whether a move is valid on the given game board.
     *
     * @param board The current game board.
     * @param move  The move to validate.
     * @return {@code true} if the move is valid, {@code false} otherwise.
     */
    public static boolean isValidMove(CSudokuBoard board, Move move) {
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
    public static ArrayList<Move> coupsPossibles(CSudokuBoard board, int i, int j) {
        ArrayList<Move> output= new ArrayList<>();
        for (int k = board.getSize(); k >= 1; k--){
            Move move = new Move(i, j, k);
            if (isValidMove(board, move)){
                output.add(move);
            }
        }
        return output;
    }

    /**
     * Calcule le nombre de cases vides
     */
    private int countEmptyCells(CSudokuBoard board) {
        int count = 0;
        int N = board.getSize();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (board.isCellEmpty(i, j)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Calcule rho, le ratio de coups réellement possibles par rapport
     * aux coups théoriquement possibles (en prenant en compte les contraintes)
     */
    public static double Rho(CSudokuBoard board, Player player) {
        int n = board.getSize();
        int emptyCount = 0;
        double totalValidMoves = 0;

        // Parcours de toutes les cellules
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (board.isCellEmpty(row, col)) {
                    emptyCount++;
                    // On récupère la liste des coups possibles pour cette case
                    List<Move> moves = coupsPossibles(board, row, col);
                    totalValidMoves += moves.size();
                }
            }
        }

        // Si aucune case vide n'est trouvée, on retourne 1
        if (emptyCount == 0) {
            return 1.0;
        }

        // Le nombre théorique maximum de coups pour toutes les cases vides est: emptyCount * n
        double rho = totalValidMoves / (emptyCount * n);
        return rho;
    }

    /**
     * Calcule la profondeur atteignable en moins de `threshold` noeuds visités
     * pour adapter dynamiquement la profondeur max en fonction de l'état de la grille
     */
    private int Depth(int f, int n, double rho, double threshold) {
        double product = 1.0;
        int moves = 0;
        while (true) {
            if (f - moves <= 0) break;
            double nextLevel = (product * rho * n * (f - moves)) / 1.6;
            if (nextLevel > threshold) break;
            product = nextLevel;
            moves++;
        }
        // On impose une profondeur minimale de 3, et maximale du nombre de cases vides restantes
        return Math.min(Math.max(moves + 1, 3), f);
    }

    /**
     * Selects the best move using the Alpha-Beta pruning algorithm.
     *
     * @param board  The current state of the Sudoku board.
     * @param player The AI player making the move.
     * @return The best {@link Move} according to the Alpha-Beta evaluation.
     */
    @Override
    public Move selectMove(CSudokuBoard board, Player player) {
        if (board == null) {
            return null;
        } else {
            EvaluatedSimulatedBoard simulatedBoard = new EvaluatedSimulatedBoard(board, player);
            StudentAI_theoutliertaskersResult bestResult;
            int emptyCount = countEmptyCells(board);
            double rho = Rho(board, player);
            int depth = Depth(emptyCount, board.getSize(), rho, 2000000); // 2000000 (cas de base)
            if (board.getSize() == 4) {
                bestResult = iacompet(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, player);
            } else {
                bestResult = iacompet(simulatedBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, player);
            }
            if (bestResult.bestValue < -board.getSize()) {
                return null;  // Si prendre un malus coûte moins cher que ce coup, ne pas jouer
            } else {
                return bestResult.bestMove;
            }
        }
    }

    public Return selectMove2(CSudokuBoard board, Player player) {
        if (board == null) {
            return new Return(new Stats(), null);
        } else {
            long startTime = System.nanoTime();
            EvaluatedSimulatedBoard simulatedBoard = new EvaluatedSimulatedBoard(board, player);
            StudentAI_theoutliertaskersResult bestResult;
            int emptyCount = countEmptyCells(board);
            double rho = Rho(board, player);
            int depth = Depth(emptyCount, board.getSize(), rho, 2000000); // 2000000 (cas de base)
            // System.out.println(/*"rho:"+rho+"\n"+*/"depth:"+depth);
            if (board.getSize() == 4) {
                bestResult = iacompet(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, player);
            } else {
                bestResult = iacompet(simulatedBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, player);
            }
            double temps = (System.nanoTime()-startTime)/1000000000.0;
            // System.out.println("temps de calcul : "+temps+"secondes");
            // observer.printStats();
            // System.out.println("eval: "+bestResult.bestValue);
            Stats stats = new Stats();
            stats.total_coups += 1;
            stats.total_noeuds += 0;
            stats.total_temps += temps;
            stats.max_noeuds = 0;
            stats.min_noeuds = 0;
            stats.max_temps = temps;
            stats.min_temps = temps;
            if (bestResult.bestValue < -board.getSize()) {
                return new Return(stats, null);
            } else {
                return new Return(stats, bestResult.bestMove);
            }
        }
    }

    /**
     * Retrieves all valid moves for the AI player.
     *
     * @param board  The current state of the Sudoku board.
     * @param player The AI player.
     * @return A list of all valid {@link Move} objects.
     */
    private List<Move> getValidMoves(EvaluatedSimulatedBoard board, Player player) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++){
                if (board.isCellEmpty(row, col)){
                    moves.addAll(coupsPossibles(board, row, col));}
            }
        }
        return moves;
    }

    public class StudentAI_theoutliertaskersResult {
        public Move bestMove;
        public int bestValue;

        public StudentAI_theoutliertaskersResult(Move bestMove, int bestValue) {
            this.bestMove = bestMove;
            this.bestValue = bestValue;
        }
    }

    /**
     * Implements the Alpha-Beta pruning algorithm recursively to evaluate moves.
     *
     * @param board              The current state of the board.
     * @param depth              The remaining search depth.
     * @param alpha              The best value for the maximizing player so far.
     * @param beta               The best value for the minimizing player so far.
     * @param isMaximizingPlayer True if the current player is the maximizing player.
     * @param player             The AI player.
     * @return The evaluation score of the board state.
     */
    public StudentAI_theoutliertaskersResult iacompet(EvaluatedSimulatedBoard board, int depth, int alpha, int beta,
                    boolean isMaximizingPlayer, Player player) {
        if (depth == 1) {
            // System.out.println("Board value fin: "+board.getEval());
            return new StudentAI_theoutliertaskersResult(null, board.getEval());
        } else {
            // System.out.println("Evaluation: "+board.getEval()+"\ndepth à: "+depth);
            if (isMaximizingPlayer){
                // System.out.println("Board value joueur maximisant: "+board.getEval());
                int bestValue = Integer.MIN_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new StudentAI_theoutliertaskersResult(null, board.getEval());
                }
                for (Move move : moves) {
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, true, true, depth);
                    StudentAI_theoutliertaskersResult result = iacompet(newBoard, depth - 1, alpha, beta,false, player);
                    if (result.bestValue > bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                    if (bestValue >= beta) {
                        // System.out.println("Best Value joueur maximisant: "+bestValue);
                        return new StudentAI_theoutliertaskersResult(bestMove, bestValue);
                    }
                    if (bestValue > alpha) {
                        alpha = bestValue;
                    }
                }
                // System.out.println("Best Value joueur maximisant: "+bestValue);
                return new StudentAI_theoutliertaskersResult(bestMove, bestValue);
            } else {
                // System.out.println("Board value joueur minimisant: "+board.getEval());
                int bestValue = Integer.MAX_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new StudentAI_theoutliertaskersResult(null, board.getEval());
                }
                for (Move move : moves){
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, false, true, depth);
                    StudentAI_theoutliertaskersResult result = iacompet(newBoard, depth - 1, alpha, beta, true, player);
                    if (result.bestValue < bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                    if (bestValue <= alpha) {
                        // System.out.println("Best Value joueur minimisant: "+bestValue);
                        return new StudentAI_theoutliertaskersResult(bestMove, bestValue);
                    }
                    if (bestValue < beta) {
                        beta = bestValue;
                    }
                }
                // System.out.println("Best Value joueur minimisant: "+bestValue);
                return new StudentAI_theoutliertaskersResult(bestMove, bestValue);
            }
        }
    }

    /**
     * Returns the name of the strategy, which in this case is "IA Raphael & Baptiste".
     * <p>
     * This method is part of the {@link MoveStrategy} interface and is used to retrieve
     * the name of the strategy being implemented. In this case, it returns the string "IA Raphael & Baptiste",
     * which indicates that the IA Raphael & Baptiste Move strategy is used for selecting moves.
     * </p>
     *
     * @return The name of the strategy, which is {@code "IA Raphael & Baptiste"}.
     */
    @Override
    public String getName() {
        return "The Outlier Taskers, Raphael LEONARDI & Baptiste PRAS";
    }
}

/**
 * A class representing an evaluated and simulated Sudoku board.
 * Extends the functionality of {@link CSudokuBoard} by adding evaluation capabilities
 * for AI strategies and maintaining the state of rows, columns, and subgrids.
 */
class EvaluatedSimulatedBoard extends CSudokuBoard {

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
                    output.addAll(StudentAI_theoutliertaskers.coupsPossibles(this, i, j));
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
                ArrayList<Move> coups = StudentAI_theoutliertaskers.coupsPossibles(this, i, col);
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
                ArrayList<Move> coups = StudentAI_theoutliertaskers.coupsPossibles(this, row, i);
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
                    coups = StudentAI_theoutliertaskers.coupsPossibles(this, i, j);
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
                    bonus += StudentAI_theoutliertaskers.coupsPossibles(this, row, col).size();
                }
            }
        }
        return bonus;
    }
}



