package CSudoku;

import CSudoku.board.CSudokuBoard;
import CSudoku.player.ai.AIPlayer;
import CSudoku.player.ai.EvaluatedSimulatedBoard;
import CSudoku.player.ai.MinimaxMoveStrategy;

public class TestA {

    public  static void main(String[] args){
        MinimaxMoveStrategy minimaxStrategy = new MinimaxMoveStrategy();

        // Initialize a real instance of CSudokuBoard with a size of 9 (standard 9x9 Sudoku)
         CSudokuBoard board = new CSudokuBoard(9);

        // Initialize the AI player (you may need to pass a MoveStrategy if necessary)
        AIPlayer aiPlayer = new AIPlayer(minimaxStrategy);
        EvaluatedSimulatedBoard boa = new EvaluatedSimulatedBoard(board, aiPlayer);

        MinimaxMoveStrategy.MinimaxResult bestMove = minimaxStrategy.minimax(boa, 3,true,aiPlayer );
        System.out.println("best val : "+bestMove.bestValue +" nombre noeuds visites : "+minimaxStrategy.getCount());
    }
}
