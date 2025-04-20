package CSudoku;

import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import CSudoku.player.Player;
import CSudoku.player.ai.AIPlayer;
import CSudoku.player.ai.AlphaBetaMoveStrategy;
import CSudoku.player.ai.MinimaxMoveStrategy;
import CSudoku.player.automate.AutomatePlayer;
import CSudoku.player.automate.FirstValidMoveStrategy;
import CSudoku.player.automate.RandomMoveNoValidationStrategy;
import CSudoku.player.automate.RandomMoveStrategy;
import CSudoku.player.human.HumanPlayer;
import CSudoku.referee.Referee;
import org.apache.commons.cli.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SudokuSimulation {
    private CSudokuBoard board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Referee referee;
    private  int size;

    public static String playerStr(int i){
        return switch (i) {
            case 1 -> "human";
            case 2 -> "Random No Validation";
            case 3 -> "Random Validation";
            case 4 -> "First Valid";
            case 5 -> "Minimax";
            case 6 -> "Alpha Beta";
            case 7 -> "IA Raphael et Baptiste";
            default -> "";
        };
    }

    public SudokuSimulation(int boardSize, Player p1, Player p2, String prefilledGridPath) {
        if (prefilledGridPath != null) {
            board = new CSudokuBoard(prefilledGridPath);
        } else {
            board = new CSudokuBoard(boardSize);
        }
        player1 = p1;
        player2 = p2;
        currentPlayer = player1;
        referee = Referee.getInstance();
        referee.init(player1, player2, board);
    }

    // Helper method to display player type and strategy
    private static String getPlayerInfo(Player player) {
        if (player instanceof HumanPlayer) {
            return "Human";
        } else if (player instanceof AIPlayer) {
            AIPlayer aiPlayer = (AIPlayer) player;
            return "AI (" + aiPlayer.getMoveStrategyName() + ")";
        } else if (player instanceof AutomatePlayer) {
            AutomatePlayer automatePlayer = (AutomatePlayer) player;
            return "Automate (" + automatePlayer.getMoveStrategyName() + ")";
        }
        return "Unknown Player";
    }
    /* Plays a new game and returns the winner
     * 0 if a tie 1 if player 1 2 if player 2
     */
    public int play(int boardSize) {
        board = new CSudokuBoard(boardSize);
        referee = Referee.getInstance();
        referee.init(player1, player2, board);
        while (!referee.isGameOver()) {



            if (referee.outOfMoves()) {

                currentPlayer = (currentPlayer == player1) ? player2 : player1;
                continue;
            }

            Move move = currentPlayer.getMove(board);

            if (move != null && referee.isValidMove(move)) {
                referee.applyMove(move);
                referee.addPoints(currentPlayer, move);
            } else {

                referee.applyPenalty(currentPlayer);
            }

            currentPlayer = (currentPlayer == player1) ? player2 : player1;

            /*try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        int score1 = referee.getScore(player1);
        int score2 = referee.getScore(player2);
        if (score1 == score2){

            return 0;
        }else{
            if (score1 > score2){

                return 1;

            }else{

                return 2;
            }
        }



    }

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(new Option("g", "grid-size", true, "Size of the board (e.g., 4, 9, 16, etc.)"));
        options.addOption(new Option("p1", "player1", true, "Player 1: (1: human, 2: Random No Validation, 3: Random, 4: First valid, 5: Minimax, 6: AlphaBeta)"));
        options.addOption(new Option("p2", "player2", true, "Player 2: (1: human, 2: Random No Validation, 3: Random, 4: First valid, 5: Minimax, 6: AlphaBeta)"));
        options.addOption(new Option("f", "file", true, "Path to a pre-filled grid in .txt format"));

        options.addOption(new Option("it" , "iterations",true, "Describes how many games must be simulated."));

        int winPlayer1 = 0;
        int winPLayer2 = 0;
        int ties = 0;





        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("SudokuGame", options);
            System.exit(1);
            return;
        }

        int gridSize = cmd.hasOption("g") ? Integer.parseInt(cmd.getOptionValue("g")) : 9;

        int nIter = cmd.hasOption("it") ? Integer.parseInt(cmd.getOptionValue("it")) : 100;




        String strP1 = playerStr(Integer.parseInt(cmd.getOptionValue("p1")));
        String strP2 = playerStr(Integer.parseInt(cmd.getOptionValue("p2")));

        String prefilledGridPath = cmd.getOptionValue("f");

        Player p1 = configurePlayer(cmd, "p1");
        Player p2 = configurePlayer(cmd, "p2");

        SudokuSimulation game = new SudokuSimulation(gridSize, p1, p2, prefilledGridPath);

        int d;
        for (int i = 0; i < nIter; i++){
            d = game.play(gridSize);
            if (d == 0){ties++;}
            else{
                if (d == 1){winPlayer1++;}
                else{winPLayer2++;}
            }
        }

        BigDecimal bd = BigDecimal.valueOf(100.0*(double) winPlayer1 / (double) nIter);
        BigDecimal bd2 =  BigDecimal.valueOf(100.0*(double) winPLayer2 / (double) nIter);
        BigDecimal rounded1 = bd.setScale(3, RoundingMode.HALF_UP);
        BigDecimal rounded2 = bd2.setScale(3, RoundingMode.HALF_UP);
        double pourcentage1 = Math.round(  (double)winPlayer1 / (double)nIter  );
        double pourcentage2 = Math.round((double)winPLayer2 / (double)nIter);


        System.out.println("Simulation avec "+nIter+" iterations sur une grille de taille "+gridSize);
        System.out.println("Victoires Joueur 1 "+strP1+" : "+ winPlayer1 + " - "+ rounded1 + "%");
        System.out.println("Victoires Joueur 2 : "+strP2+" : " + winPLayer2+ " - "+ rounded2 + "%");
        System.out.println("Egalites : " + ties);

    }

    private static Player configurePlayer(CommandLine cmd, String playerOption) {
        String playerType = cmd.hasOption(playerOption) ? cmd.getOptionValue(playerOption) : "1"; // Default to "1" (human)

        // Handle human player case
        if ("1".equals(playerType)) {
            return new HumanPlayer();
        }

        // Handle AI or automate player case with different strategies
        int aiStrategy = Integer.parseInt(playerType);

        return switch (aiStrategy) {
            case 2 -> // Random No Validation
                    new AutomatePlayer(new RandomMoveNoValidationStrategy());
            case 3 -> // Random
                    new AutomatePlayer(new RandomMoveStrategy());
            case 4 -> // First Valid
                    new AutomatePlayer(new FirstValidMoveStrategy());
            case 5 -> // Minimax
                    new AIPlayer(new MinimaxMoveStrategy());
            case 6 -> // AlphaBeta
                    new AIPlayer(new AlphaBetaMoveStrategy());
            default -> new HumanPlayer();  // Default to human player if an invalid option is given
        };
    }
}
