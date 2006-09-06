//----------------------------------------------------------------------------
// $Id$
// $Source$
//----------------------------------------------------------------------------

package net.sf.gogui.twogtp;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import net.sf.gogui.game.GameInformation;
import net.sf.gogui.game.GameTree;
import net.sf.gogui.game.Node;
import net.sf.gogui.game.NodeUtil;
import net.sf.gogui.go.Board;
import net.sf.gogui.go.BoardUtil;
import net.sf.gogui.go.GoColor;
import net.sf.gogui.go.Move;
import net.sf.gogui.go.MoveUtil;
import net.sf.gogui.go.GoPoint;
import net.sf.gogui.sgf.SgfReader;

//----------------------------------------------------------------------------

/** Find duplicates in games. */
public final class Compare
{
    /** Check if game already exists in game collection.
        All games must have the same board size.
        Also finds rotated duplicates.
        @param board Board with the correct size (only used for
        Board.rotate).
        @param moves Moves of game to check.
        @param games ArrayList containing games (ArrayList of moves) in
        collection.
        @param useAlternate If true, assume that players are exchanged every
        second game. Only check games where player played the same color.
        @param isAlternated If useAlternate, indicate if game to check
        had players exchanged.
        @return String containing number of first identical game in
        collection or "-" if no duplicate was found. If a nearly identical
        game is found (<= 20% identical moves comparing moves by number),
        the game number is returned with a question mark appended.
    */
    public static String checkDuplicate(Board board, ArrayList moves,
                                        ArrayList games, boolean useAlternate,
                                        boolean isAlternated)
    {
        String result = "-";
        for (int numberGame = 0; numberGame < games.size(); ++numberGame)
        {
            if (useAlternate && ((numberGame % 2 != 0) != isAlternated))
                continue;
            ArrayList gameMoves = (ArrayList)games.get(numberGame);
            for (int rot = 0; rot < BoardUtil.NUMBER_ROTATIONS; ++rot)
            {
                int numberDifferent = 0;
                int moveNumber = moves.size();
                int maxDifferent = moveNumber / 5;
                if (gameMoves.size() != moveNumber)
                {
                    numberDifferent = Math.abs(gameMoves.size() - moveNumber);
                    moveNumber = Math.min(gameMoves.size(), moveNumber);
                }
                for (int i = 0;
                     numberDifferent <= maxDifferent && i < moveNumber; ++i)
                {
                    Move move = (Move)moves.get(i);
                    GoPoint point = move.getPoint();
                    GoColor color = move.getColor();
                    Move gameMove = (Move)gameMoves.get(i);
                    GoPoint gamePoint = BoardUtil.rotate(rot,
                                                          gameMove.getPoint(),
                                                          board.getSize());
                    GoColor gameColor = gameMove.getColor();
                    if (! color.equals(gameColor) || point != gamePoint)
                        ++numberDifferent;
                }
                if (numberDifferent == 0)
                    return Integer.toString(numberGame);
                else if (numberDifferent < maxDifferent)
                    result = Integer.toString(numberGame) + "?";
            }
        }
        return result;
    }

    /** Compare a set of SGF files.
        Prints the results to standard output, one line per game
        with the filename and the duplicate information as returned by
        Compare.checkDuplicate.
    */
    public static void compare(ArrayList filenames) throws Exception
    {
        Board board = null;
        ArrayList games = new ArrayList();
        for (int gameNumber = 0; gameNumber < filenames.size(); ++gameNumber)
        {
            String filename = (String)filenames.get(gameNumber);
            File file = new File(filename);
            FileInputStream fileStream = new FileInputStream(file);
            SgfReader reader
                = new SgfReader(fileStream, file.toString(), null, 0);
            GameTree gameTree = reader.getGameTree();
            GameInformation gameInformation = gameTree.getGameInformation();
            int size = gameInformation.m_boardSize;
            if (board == null)
                board = new Board(size);
            else if (size != board.getSize())
                throw new Exception("Board size in " + filename +
                                    " does not match other games");
            ArrayList moves = getAllAsMoves(gameTree.getRoot());
            String duplicate =
                checkDuplicate(board, moves, games, false, false);
            System.out.println(Integer.toString(gameNumber) + " " +
                               filename + " " + duplicate);
            games.add(moves);
        }
    }

    /** Return moves in main variation from node.
        All setup stones are translated to moves and passes are filled in
        to ensure that moves are alternating beginning with black.
    */
    public static ArrayList getAllAsMoves(Node node)
    {
        ArrayList moves = new ArrayList(128);
        ArrayList nodeMoves = new ArrayList(128);
        while (node != null)
        {
            NodeUtil.getAllAsMoves(node, nodeMoves);
            moves.addAll(nodeMoves);
            node = node.getChild();
        }
        moves = MoveUtil.fillPasses(moves, GoColor.BLACK);
        return moves;
    }

    /** Make constructor unavailable; class is for namespace only. */
    private Compare()
    {
    }
}

//----------------------------------------------------------------------------
