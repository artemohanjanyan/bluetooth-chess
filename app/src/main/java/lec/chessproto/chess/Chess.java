package lec.chessproto.chess;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Chess extends Game {

    int cfRow, cfColumn;


    private List<Move>[][] deskMoves;

    public Chess(Figure[][] d, boolean turn, Player whitePlayer, Player blackPlayer) {
        super(d, turn, whitePlayer, blackPlayer);

        cfRow = -1;
        cfColumn = -1;

        deskMoves = genMoves(desk, WHITE);
    }


    public Desk getDesk() {
        return desk;
    }

    synchronized List<Move> chooseFigure(Player player, int row, int column) {
        if (!(player == whitePlayer || player == blackPlayer) ||
                player.color ^ desk.turn ||
                desk.d[row][column] == null ||
                player.color ^ desk.d[row][column].getColor()) {
            return new LinkedList<>();
        }

        cfRow = row;
        cfColumn = column;
        List<Move> moves = desk.d[row][column].getMoves(desk, row, column);

        //TODO :: check are moves valid

        if (listener != null) {
            listener.onFigureChosen(moves);
        }

        return moves;
    }

    synchronized boolean  moveFigure(Player player, Move move) {
        if (!(player == whitePlayer || player == blackPlayer)
                || player.color ^ desk.turn
                || deskMoves[move.startRow][move.startColumn] == null
                ||!deskMoves[move.startRow][move.startColumn].contains(move)) {
            return false;
        }
        boolean isExecuted = tryExecute(move);
        if (isExecuted) onMoveExecution(player, move);
        return isExecuted;
    }

    @SuppressWarnings("unchecked")
    static List<Move>[][] genMoves(Desk desk, boolean color) {
        List<Move>[][] ret = new LinkedList[Desk.SIZE][Desk.SIZE];

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (desk.d[i][j] != null  && desk.d[i][j].color == color) {
                    ret[i][j] = desk.d[i][j].getMoves(desk, i, j);
                }
            }
        }

        return ret;
    }

    private boolean tryExecute(Move move) {
        List<Point> changed = move.getChangedFields();
        Figure[] savedState = new Figure[changed.size()];
        for (int i = 0; i < savedState.length; i++) {
            Point p = changed.get(i);
            savedState[i] = desk.d[p.row][p.column];
        }
        Figure king = desk.turn ? Figure.BLACK_KING : Figure.WHITE_KING;
        move.execute(desk);
        List<Move>[][] moves = genMoves(desk, !desk.turn);

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (moves[i][j] == null) {
                    continue;
                }
                for (Move pmove : moves[i][j]) {
                    if (desk.d[pmove.endRow][pmove.endColumn] == king) {
                        for (int k = 0; k < savedState.length; k++) {
                            desk.d[changed.get(k).row][changed.get(k).column] = savedState[k];
                        }
                        return false;
                    }
                }
            }
        }
        deskMoves = moves;
        return true;
    }

}
