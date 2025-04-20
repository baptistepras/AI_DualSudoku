package CSudoku;

import CSudoku.player.Player;
import CSudoku.player.ai.AIPlayer;
import CSudoku.player.ai.AlphaBetaMoveStrategy;
import CSudoku.player.ai.MinimaxMoveStrategy;
import CSudoku.player.ai.IACompetitionStrategy;
import CSudoku.player.automate.FirstValidMoveStrategy;
import CSudoku.player.automate.RandomMoveNoValidationStrategy;
import CSudoku.player.automate.RandomMoveStrategy;
import CSudoku.player.automate.AutomatePlayer;
import CSudoku.player.human.HumanPlayer;
import CSudoku.player.student.StudentAI_theoutliertaskers;
import CSudoku.referee.Referee;
import CSudoku.board.CSudokuBoard;
import CSudoku.board.Move;
import org.apache.commons.cli.*;
import CSudoku.player.ai.IACompetitionStrategy.Return;
import CSudoku.player.ai.IACompetitionStrategy.Stats;


public class
CSudokuGame {

    private CSudokuBoard board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Referee referee;


    public CSudokuGame(int boardSize, Player p1, Player p2, String prefilledGridPath) {
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

    public void printStats(Stats j, int i) {
        System.out.println("\nStastistiques du joueur"+i+": ");
        System.out.println("Total de temps: "+j.total_temps+"secondes");
        System.out.println("Moyenne de temps: "+j.total_temps/j.total_coups+"secondes");
        System.out.println("Maximum de temps: "+j.max_temps+"secondes");
        System.out.println("Minimum de temps: "+j.min_temps+"secondes");
        System.out.println("Total de noeuds visités: "+j.total_noeuds);
        System.out.println("Moyenne de noeuds visités: "+j.total_noeuds/j.total_coups);
        System.out.println("Maximum de noeuds visités: "+j.max_noeuds);
        System.out.println("Minimum de noeuds visités: "+j.min_noeuds);
    }

    private void displayBoard() {
        System.out.println("\n===============================");
        System.out.println("          Game Board           ");
        System.out.println("===============================");
        board.printGrid();
        System.out.println("===============================");
    }

    private void displayScores() {
        System.out.println("\n===============================");
        System.out.println("            Scores              ");
        System.out.println("===============================");

        System.out.println("Player 1: " + getPlayerInfo(player1) + " | Score: " + referee.getScore(player1));
        System.out.println("Player 2: " + getPlayerInfo(player2) + " | Score: " + referee.getScore(player2));

        System.out.println("===============================");
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

    public void play(boolean s) {
        Stats statsJoueur1 = new Stats();
        Stats statsJoueur2 = new Stats();
        while (!referee.isGameOver()) {
            displayBoard();
            displayScores();

            System.out.println("\n=== " + (currentPlayer == player1 ? "Player 1's" : "Player 2's") + " turn ===");

            if (referee.outOfMoves()) {
                System.out.println("No valid moves available.");
                currentPlayer = (currentPlayer == player1) ? player2 : player1;
                continue;
            }

            Move move = null;
            if (s) {
                Return r = currentPlayer.getMove2(board);
                move = r.move;
                if (currentPlayer == player1) {
                    statsJoueur1.total_noeuds += r.stats.total_noeuds;
                    statsJoueur1.total_temps += r.stats.total_temps;
                    statsJoueur1.total_coups += r.stats.total_coups;
                    if (r.stats.max_noeuds > statsJoueur1.max_noeuds) {
                        statsJoueur1.max_noeuds = r.stats.max_noeuds;
                    }
                    if (r.stats.max_temps > statsJoueur1.max_temps) {
                        statsJoueur1.max_temps = r.stats.max_temps;
                    }
                    if (r.stats.min_noeuds < statsJoueur1.min_noeuds) {
                        statsJoueur1.min_noeuds = r.stats.min_noeuds;
                    }
                    if (r.stats.min_temps < statsJoueur1.min_temps) {
                        statsJoueur1.min_temps = r.stats.min_temps;
                    }
                } else {
                    statsJoueur2.total_noeuds += r.stats.total_noeuds;
                    statsJoueur2.total_temps += r.stats.total_temps;
                    statsJoueur2.total_coups += r.stats.total_coups;
                    if (r.stats.max_noeuds > statsJoueur2.max_noeuds) {
                        statsJoueur2.max_noeuds = r.stats.max_noeuds;
                    }
                    if (r.stats.max_temps > statsJoueur2.max_temps) {
                        statsJoueur2.max_temps = r.stats.max_temps;
                    }
                    if (r.stats.min_noeuds < statsJoueur2.min_noeuds) {
                        statsJoueur2.min_noeuds = r.stats.min_noeuds;
                    }
                    if (r.stats.min_temps < statsJoueur2.min_temps) {
                        statsJoueur2.min_temps = r.stats.min_temps;
                    }
                }
            } else {
                move = currentPlayer.getMove(board);
            }

            if (move != null && referee.isValidMove(move)) {
                referee.applyMove(move);
                referee.addPoints(currentPlayer, move);
            } else {
                System.out.println("Invalid move. Turn skipped.");
                referee.applyPenalty(currentPlayer);
            }

            currentPlayer = (currentPlayer == player1) ? player2 : player1;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        displayBoard();
        displayScores();
        referee.declareWinner();
        if (s) {
            printStats(statsJoueur1, 1);
            printStats(statsJoueur2, 2);
        }
    }

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(new Option("g", "grid-size", true, "Size of the board (e.g., 4, 9, 16, etc.)"));
        options.addOption(new Option("p1", "player1", true, "Player 1: (1: human, 2: Random No Validation, 3: Random, 4: First valid, 5: Minimax, 6: AlphaBeta, 7: IA Raphael & Baptiste)"));
        options.addOption(new Option("p2", "player2", true, "Player 2: (1: human, 2: Random No Validation, 3: Random, 4: First valid, 5: Minimax, 6: AlphaBeta, 7: IA Raphael & Baptiste)"));
        options.addOption(new Option("f", "file", true, "Path to a pre-filled grid in .txt format"));
        // Sert à afficher les statistiques de temps et visites des noeuds
        options.addOption(new Option("s", "stats", false, "Display time stats"));

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
        String prefilledGridPath = cmd.getOptionValue("f");

        Player p1 = configurePlayer(cmd, "p1");
        Player p2 = configurePlayer(cmd, "p2");

        CSudokuGame game = new CSudokuGame(gridSize, p1, p2, prefilledGridPath);
        game.play(cmd.hasOption("s"));
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
            case 7 -> // IA Raphael & Baptiste
                    new AIPlayer(new IACompetitionStrategy());
            case 8 -> // IA Raphael & Baptiste
                    new AIPlayer(new StudentAI_theoutliertaskers());
            default -> new HumanPlayer();  // Default to human player if an invalid option is given
        };
    }
}