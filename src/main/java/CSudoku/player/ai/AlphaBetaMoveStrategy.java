package CSudoku.player.ai;

import CSudoku.observers.AlphaBetaPruningObserver;
import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.MoveStrategy;
import CSudoku.player.Player;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaMoveStrategy implements MoveStrategy {

    private static final int MAX_DEPTH = 3; // Depth of the search tree
    private AlphaBetaPruningObserver observer;

    /**
     * Constructor for AlphaBetaMoveStrategy.
     * Initializes the observer for tracking the Alpha-Beta cuts and node visits.
     */
    public AlphaBetaMoveStrategy() {
        this.observer = new AlphaBetaPruningObserver(); // Initialize the observer
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
            long startTime = System.nanoTime();
            AlphaBetaResult bestResult = alphaBeta(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, (AIPlayer) player);
            double temps = (System.nanoTime()-startTime)/1000000000.0;
            // System.out.println("temps de calcul : "+temps+"secondes");
            // observer.printStats();
            return bestResult.bestMove;
        }
    }

    public Return selectMove2(CSudokuBoard board, Player player) {
        observer.reset();
        if (board == null) {
            return new Return(new Stats(), null);
        } else {
            long startTime = System.nanoTime();
            EvaluatedSimulatedBoard simulatedBoard = new EvaluatedSimulatedBoard(board, player);
            AlphaBetaResult bestResult = alphaBeta(simulatedBoard, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, (AIPlayer) player);
            double temps = (System.nanoTime()-startTime)/1000000000.0;
            // System.out.println("temps de calcul : "+temps+"secondes");
            observer.printStats();
            Stats stats = new Stats();
            stats.total_coups += 1;
            stats.total_noeuds += observer.getNodeCount();
            stats.total_temps += temps;
            stats.max_noeuds = observer.getNodeCount();
            stats.min_noeuds = observer.getNodeCount();
            stats.max_temps = temps;
            stats.min_temps = temps;
            return new Return(stats, bestResult.bestMove);
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

    public class AlphaBetaResult {
        public Move bestMove;
        public int bestValue;

        public AlphaBetaResult(Move bestMove, int bestValue) {
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
    public AlphaBetaResult alphaBeta(EvaluatedSimulatedBoard board, int depth, int alpha, int beta,
                                     boolean isMaximizingPlayer, AIPlayer player) {
        observer.incrementNodeCount();
        if (depth == 1) {
            return new AlphaBetaResult(null, board.getEval());
        } else {
            if (isMaximizingPlayer) {
                int bestValue = Integer.MIN_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new AlphaBetaResult(null, board.getEval());
                }
                for (Move move : moves) {
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, true, false);
                    AlphaBetaResult result = alphaBeta(newBoard, depth - 1, alpha, beta,false, player);
                    if (result.bestValue > bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                    if (bestValue >= beta) {
                        observer.incrementBetaCut();
                        return new AlphaBetaResult(bestMove, bestValue);
                    }
                    if (bestValue > alpha) {
                        alpha = bestValue;
                    }
                }
                return new AlphaBetaResult(bestMove, bestValue);
            } else {
                int bestValue = Integer.MAX_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new AlphaBetaResult(null, board.getEval());
                }
                for (Move move : moves) {
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, false, false);
                    AlphaBetaResult result = alphaBeta(newBoard, depth - 1, alpha, beta, true, player);
                    if (result.bestValue < bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                    if (bestValue <= alpha) {
                        observer.incrementAlphaCut();
                        return new AlphaBetaResult(bestMove, bestValue);
                    }
                    if (bestValue < beta) {
                        beta = bestValue;
                    }
                }
                return new AlphaBetaResult(bestMove, bestValue);
            }
        }
    }

    /**
     * Returns the name of the strategy, which in this case is "AlphaBeta".
     * <p>
     * This method is part of the {@link MoveStrategy} interface and is used to retrieve
     * the name of the strategy being implemented. In this case, it returns the string "AlphaBeta",
     * which indicates that the AlphaBeta Move strategy is used for selecting moves.
     * </p>
     *
     * @return The name of the strategy, which is {@code "AlphaBeta"}.
     */
    @Override
    public String getName() {
        return "AlphaBeta";
    }
}