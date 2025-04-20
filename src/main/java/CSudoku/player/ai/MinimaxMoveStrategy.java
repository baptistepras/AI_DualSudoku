package CSudoku.player.ai;

import CSudoku.observers.NodeCounterObserver;
import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.MoveStrategy;
import CSudoku.player.Player;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;

import java.util.ArrayList;
import java.util.List;



/**
 * Implements the Minimax algorithm for move selection in a Sudoku game.
 * The algorithm evaluates possible moves and selects the one with the best score,
 * taking into account the opponent's moves.
 */
public class MinimaxMoveStrategy implements MoveStrategy {

    public static final int MAX_DEPTH = 3; // Search depth for the Minimax algorithm
    private NodeCounterObserver nodeCounter;

    public int getCount(){
        return this.nodeCounter.getCount();
    }

    /**
     * Constructor for MinimaxMoveStrategy.
     * Initializes the node counter for tracking the number of visited nodes.
     */
    public MinimaxMoveStrategy() {
        this.nodeCounter = new NodeCounterObserver(); // Initialize the node counter
    }

    /**
     * Selects the best move using the Minimax algorithm.
     *
     * @param board  The current state of the Sudoku board.
     * @param player The AI/Automate player making the move.
     * @return The best {@link Move} according to the Minimax evaluation.
     */
    @Override
    public Move selectMove(CSudokuBoard board, Player player) {
        nodeCounter.reset();
        if (board == null) {
            return null;
        } else {
            EvaluatedSimulatedBoard simulatedBoard = new EvaluatedSimulatedBoard(board, player);
            MinimaxResult bestResult = minimax(simulatedBoard,MAX_DEPTH, true, (AIPlayer) player);
            //System.out.println("nombre noeuds visites : "+ getCount());
            return bestResult.bestMove;
        }
    }

    public Return selectMove2(CSudokuBoard board, Player player) {
        nodeCounter.reset();
        if (board == null) {
            return new Return(new Stats(), null);
        } else {
            long startTime = System.nanoTime();
            EvaluatedSimulatedBoard simulatedBoard = new EvaluatedSimulatedBoard(board, player);
            MinimaxResult bestResult = minimax(simulatedBoard,MAX_DEPTH, true, (AIPlayer) player);
            double temps = (System.nanoTime()-startTime)/1000000000.0;
            //System.out.println("nombre noeuds visites : "+ getCount());
            Stats stats = new Stats();
            stats.total_coups += 1;
            stats.total_noeuds += nodeCounter.getCount();
            stats.total_temps += temps;
            stats.max_noeuds = nodeCounter.getCount();
            stats.min_noeuds = nodeCounter.getCount();
            stats.max_temps = temps;
            stats.min_temps = temps;
            return new IACompetitionStrategy.Return(stats, bestResult.bestMove);
        }
    }

    /**
     * Returns the name of the strategy, which in this case is "Minimax".
     * <p>
     * This method is part of the {@link MoveStrategy} interface and is used to retrieve
     * the name of the strategy being implemented. In this case, it returns the string "Minimax",
     * which indicates that the Minimax algorithm is used for selecting moves.
     * </p>
     *
     * @return The name of the strategy, which is {@code "Minimax"}.
     */
    @Override
    public String getName() {
        return "Minimax";
    }

    /**
     * Retrieves all valid moves for the AI player.
     *
     * @param board  The current state of the Sudoku board.
     * @param player The AI/Automate player.
     * @return A list of all valid {@link Move} objects.
     */
    protected List<Move> getValidMoves(EvaluatedSimulatedBoard board, Player player) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int row = 0; row < board.getSize(); row++){
            for (int col = 0; col < board.getSize(); col++){
                if (board.isCellEmpty(row, col)){
                moves.addAll(player.coupsPossibles(board, row, col));}
            }
        }
        return moves;
    }

    public class MinimaxResult {
        public Move bestMove;
        public int bestValue;

        public MinimaxResult(Move bestMove, int bestValue) {
            this.bestMove = bestMove;
            this.bestValue = bestValue;
        }
    }

    /**
     * Implements the Minimax algorithm recursively to evaluate moves.
     *
     * @param board              The current state of the board.
     * @param depth              The remaining search depth.
     * @param isMaximizingPlayer True if the current player is the maximizing player.
     * @param player             The AI player.
     * @return The evaluation score of the board state.
     */
    public MinimaxResult minimax(EvaluatedSimulatedBoard board, int depth, boolean isMaximizingPlayer, AIPlayer player) {
        this.nodeCounter.increment();
        if (depth == 1) {
            //System.out.println(board.calculateHeuristic());
            return new MinimaxResult(null, board.getEval());
        } else {
            if (isMaximizingPlayer){
                int bestValue = Integer.MIN_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new MinimaxResult(null, board.getEval());
                }
                for (Move move : moves) {
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, true, false);
                    MinimaxResult result = minimax(newBoard, depth - 1, false, player);
                    if (result.bestValue > bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                }
                return new MinimaxResult(bestMove, bestValue);
            } else {
                int bestValue = Integer.MAX_VALUE;
                Move bestMove = null;
                List<Move> moves = getValidMoves(board, player);
                if (moves.isEmpty()) {
                    return new MinimaxResult(null, board.getEval());
                }
                for (Move move : moves) {
                    EvaluatedSimulatedBoard newBoard = new EvaluatedSimulatedBoard(board);
                    newBoard.setValue(move, false, false);
                    MinimaxResult result = minimax(newBoard, depth - 1, true, player);
                    if (result.bestValue < bestValue) {
                        bestValue = result.bestValue;
                        bestMove = move;
                    }
                }
                return new MinimaxResult(bestMove, bestValue);
            }
        }
    }
}