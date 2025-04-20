package CSudoku.player.ai;

import CSudoku.observers.AlphaBetaPruningObserver;
import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.MoveStrategy;
import CSudoku.player.Player;
import java.util.ArrayList;
import java.util.List;

public class IACompetitionStrategy implements MoveStrategy {

    private AlphaBetaPruningObserver observer;
    private static final int MAX_DEPTH = 3; // Depth of the search tree

    public static class Stats {
        public double total_temps;
        public int total_noeuds;
        public int total_coups;
        public double max_temps;
        public double min_temps;
        public int max_noeuds;
        public int min_noeuds;

        public Stats() {
            this.total_noeuds = 0;
            this.total_coups = 0;
            this.total_temps = 0;
            this.max_temps = Double.MIN_VALUE;
            this.min_temps = Double.MAX_VALUE;
            this.max_noeuds = Integer.MIN_VALUE;
            this.min_noeuds = Integer.MAX_VALUE;
        }
    }

    public static class Return {
        public Stats stats;
        public Move move;

        public Return(Stats stats, Move move) {
            this.stats = stats;
            this.move = move;
        }
    }

    /**
     * Constructor for AlphaBetaMoveStrategy.
     * Initializes the observer for tracking the Alpha-Beta cuts and node visits.
     */
    public IACompetitionStrategy()  {
        this.observer = new AlphaBetaPruningObserver(); // Initialize the observer
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
                    List<Move> moves = player.coupsPossibles(board, row, col);
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
        observer.reset();
        if (board == null) {
            return null;
        } else {
            EvaluatedSimulatedBoard simulatedBoard = new EvaluatedSimulatedBoard(board, player);
            IACompetitionResult bestResult;
            long startTime = System.nanoTime();
            int emptyCount = countEmptyCells(board);
            double rho = Rho(board, player);
            int depth = Depth(emptyCount, board.getSize(), rho, 2000000); // 2000000 (cas de base)
            // System.out.println(/*"rho:"+rho+"\n"+*/"depth:"+depth);
            if (board.getSize() == 4) {
                bestResult = iacompet(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, (AIPlayer) player);
            } else {
                bestResult = iacompet(simulatedBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, (AIPlayer) player);
            }
            double temps = (System.nanoTime()-startTime)/1000000000.0;
            // System.out.println("temps de calcul : "+temps+"secondes");
            // observer.printStats();
            // System.out.println("eval: "+bestResult.bestValue);
            if (bestResult.bestValue < -board.getSize()) {
                return null;  // Si prendre un malus coûte moins cher que ce coup, ne pas jouer
            } else {
                return bestResult.bestMove;
            }
        }
    }

    public Return selectMove2(CSudokuBoard board, Player player) {
        observer.reset();
        if (board == null) {
            return new Return(new Stats(), null);
        } else {
            long startTime = System.nanoTime();
            EvaluatedSimulatedBoard simulatedBoard = new EvaluatedSimulatedBoard(board, player);
            IACompetitionResult bestResult;
            int emptyCount = countEmptyCells(board);
            double rho = Rho(board, player);
            int depth = Depth(emptyCount, board.getSize(), rho, 2000000); // 2000000 (cas de base)
            // System.out.println(/*"rho:"+rho+"\n"+*/"depth:"+depth);
            if (board.getSize() == 4) {
                bestResult = iacompet(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, (AIPlayer) player);
            } else {
                bestResult = iacompet(simulatedBoard, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, (AIPlayer) player);
            }
            double temps = (System.nanoTime()-startTime)/1000000000.0;
            // System.out.println("temps de calcul : "+temps+"secondes");
            // observer.printStats();
            // System.out.println("eval: "+bestResult.bestValue);
            Stats stats = new Stats();
            stats.total_coups += 1;
            stats.total_noeuds += observer.getNodeCount();
            stats.total_temps += temps;
            stats.max_noeuds = observer.getNodeCount();
            stats.min_noeuds = observer.getNodeCount();
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
    private List<Move> getValidMoves(EvaluatedSimulatedBoard board, AIPlayer player) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++){
                if (board.isCellEmpty(row, col)){
                    moves.addAll(player.coupsPossibles(board, row, col));}
            }
        }
        return moves;
    }

    public class IACompetitionResult {
        public Move bestMove;
        public int bestValue;

        public IACompetitionResult(Move bestMove, int bestValue) {
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
    public IACompetitionResult iacompet(EvaluatedSimulatedBoard board, int depth, int alpha, int beta,
                                        boolean isMaximizingPlayer, AIPlayer player) {
        observer.incrementNodeCount();
        if (depth == 1) {
            // System.out.println("Board value fin: "+board.getEval());
            return new IACompetitionResult(null, board.getEval());
        } else {
            // System.out.println("Evaluation: "+board.getEval()+"\ndepth à: "+depth);
            if (isMaximizingPlayer){
                // System.out.println("Board value joueur maximisant: "+board.getEval());
                int bestValue = Integer.MIN_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new IACompetitionResult(null, board.getEval());
                }
                for (Move move : moves) {
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, true, true, depth);
                    IACompetitionResult result = iacompet(newBoard, depth - 1, alpha, beta,false, player);
                    if (result.bestValue > bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                    if (bestValue >= beta) {
                        observer.incrementBetaCut();
                        // System.out.println("Best Value joueur maximisant: "+bestValue);
                        return new IACompetitionResult(bestMove, bestValue);
                    }
                    if (bestValue > alpha) {
                        alpha = bestValue;
                    }
                }
                // System.out.println("Best Value joueur maximisant: "+bestValue);
                return new IACompetitionResult(bestMove, bestValue);
            } else {
                // System.out.println("Board value joueur minimisant: "+board.getEval());
                int bestValue = Integer.MAX_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new IACompetitionResult(null, board.getEval());
                }
                for (Move move : moves){
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, false, true, depth);
                    IACompetitionResult result = iacompet(newBoard, depth - 1, alpha, beta, true, player);
                    if (result.bestValue < bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                    if (bestValue <= alpha) {
                        observer.incrementAlphaCut();
                        // System.out.println("Best Value joueur minimisant: "+bestValue);
                        return new IACompetitionResult(bestMove, bestValue);
                    }
                    if (bestValue < beta) {
                        beta = bestValue;
                    }
                }
                // System.out.println("Best Value joueur minimisant: "+bestValue);
                return new IACompetitionResult(bestMove, bestValue);
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
        return "IA Raphael & Baptiste";
    }
}
