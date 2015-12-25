package lec.chessproto.chess;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Chess extends Game {



    private List<MoveTree>[][] deskMoves;

    boolean isBlackLongCastlingEnabled ,
            isBlackShortCastlingEnabled,
            isWhiteLongCastlingEnabled ,
            isWhiteShortCastlingEnabled;
    int lwrColumn = -1, rwrColumn = -1, lbrColumn = -1, rbrColumn = -1;
    Point blackKing, whiteKing;

    @SuppressWarnings("unchecked")
    public Chess(Figure[][] d, boolean turn, Player whitePlayer, Player blackPlayer) {
        super(d, turn, whitePlayer, blackPlayer);



        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (desk.d[i][j] == Figure.WHITE_KING) whiteKing = new Point(i, j);
                if (desk.d[i][j] == Figure.BLACK_KING) blackKing = new Point(i, j);
            }
        }
        if (whiteKing == null || blackKing == null)
            throw new RuntimeException("Not enough kings on the board");
        if (whiteKing.row == 0) {
            for (int i = 0; i < whiteKing.column; i++) {
                if (desk.d[0][i] == Figure.WHITE_ROOK) {
                    lwrColumn = i;
                    isWhiteLongCastlingEnabled = true;
                }
            }
            for (int i = whiteKing.column + 1; i < Desk.SIZE; i++) {
                if (desk.d[0][i] == Figure.WHITE_ROOK) {
                    rwrColumn = i;
                    isWhiteShortCastlingEnabled = true;
                }
            }
        }
        if (blackKing.row == 7) {
            for (int i = 0; i < blackKing.column; i++) {
                if (desk.d[7][i] == Figure.BLACK_ROOK) {
                    lbrColumn = i;
                    isBlackLongCastlingEnabled = true;
                }
            }
            for (int i = blackKing.column + 1; i < Desk.SIZE; i++) {
                if (desk.d[7][i] == Figure.BLACK_ROOK) {
                    rbrColumn = i;
                    isBlackShortCastlingEnabled = true;
                }
            }
        }

        List<Move>[][] firstMoves = genMoves();
        deskMoves = new List[Desk.SIZE][Desk.SIZE];
        genDeskMoves(firstMoves);
    }

    private static class MoveTree {
        Move move;

        List<Move>[][] nextMoves;

        public MoveTree(Move move) {
            this.move = move;
        }

        public MoveTree(Move move, List<Move>[][] nextMoves) {
            this.move = move;
            this.nextMoves = nextMoves;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MoveTree moveTree = (MoveTree) o;

            return move.equals(moveTree.move);
        }
    }


    synchronized List<Move> chooseFigure(Player player, int row, int column) {
        List<MoveTree> moveTrees = deskMoves[row][column];
        if (moveTrees == null ||
                !(player == whitePlayer || player == blackPlayer) ||
                player.color ^ desk.turn ||
                desk.d[row][column] == null ||
                player.color ^ desk.d[row][column].getColor()) {
            return new ArrayList<>();
        }
        List<Move> moves = new ArrayList<>(moveTrees.size());

        for (MoveTree m : moveTrees) {
            moves.add(m.move);
        }

        if (listener != null) {
            listener.onFigureChosen(moves);
        }

        return moves;
    }


    boolean  moveFigure(Player player, Move move) {
        if (deskMoves[move.startRow][move.startColumn] == null) {
            return false;
        }
        int index = deskMoves[move.startRow][move.startColumn].indexOf(new MoveTree(move));
        if (!(player == whitePlayer || player == blackPlayer)
                || player.color ^ desk.turn
                || index == -1) {
            return false;
        }
        move = deskMoves[move.startRow][move.startColumn].get(index).move;
        Point king =(desk.turn ? blackKing : whiteKing);
        if (king.equals(move.startRow, move.startColumn)) {
            king.set(move.endRow, move.endColumn);
            if (desk.turn) {
                isBlackLongCastlingEnabled = false;
                isBlackShortCastlingEnabled = false;
            } else {
                isWhiteLongCastlingEnabled = false;
                isWhiteShortCastlingEnabled = false;
            }
        }
        int aRow = desk.turn ? move.endRow : move.startRow;
        int aColumn = desk.turn ? move.endColumn : move.startColumn;
        int bRow = desk.turn ? move.startRow : move.endRow;
        int bColumn = desk.turn ? move.startColumn : move.endColumn;
        if (Point.equals(0, lwrColumn, aRow, aColumn))
            isWhiteLongCastlingEnabled = false;
        if (Point.equals(0, rwrColumn, aRow, aColumn))
            isWhiteShortCastlingEnabled = false;
        if (Point.equals(7, lbrColumn, bRow, bColumn))
            isBlackLongCastlingEnabled = false;
        if (Point.equals(7, rbrColumn, bRow, bColumn))
            isBlackShortCastlingEnabled = false;


        desk.executeMove(move);
        List<Move>[][] nextMoves = deskMoves[move.startRow][move.startColumn].get(index).nextMoves;
        boolean isMate = !genDeskMoves(nextMoves);

        onMoveExecution(player, move);

        if (isMate) {
            gameOver(!desk.turn);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private List<Move>[][] genMoves() {
        List<Move>[][] ret = new LinkedList[Desk.SIZE][Desk.SIZE];

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (desk.d[i][j] != null  && desk.d[i][j].color == desk.turn) {
                    ret[i][j] = desk.d[i][j].getMoves(desk, i, j);
                }
            }
        }

        return ret;
    }

    private boolean isCorrect;

    private List<Move>[][] checkNonTarget(Point... pointToCheck) {
        List<Move>[][] moves = genMoves();

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (moves[i][j] == null) {
                    continue;
                }
                for (Move m : moves[i][j]) {
                    for (Point p : pointToCheck) {
                        if (p.equals(m.endRow, m.endColumn)) {
                            isCorrect =  false;
                            return moves;
                        }
                    }
                }
            }
        }
        isCorrect =  true;
        return moves;
    }

    private boolean genDeskMoves(List<Move>[][] moves) {
        boolean hasMoves = false;
        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (moves[i][j] == null) {
                    deskMoves[i][j] = null;
                    continue;
                }
                deskMoves[i][j] = new ArrayList<>(moves[i][j].size());
                for (Move m : moves[i][j]) {
                    desk.executeMove(m);
                    Point[] points;
                    if (m instanceof Castling) {
                        int sC = Math.min(m.startColumn, m.endColumn);
                        int eC = Math.max(m.startColumn, m.endColumn);
                        points = new Point[eC - sC + 1];
                        for (int k = sC; k <= eC; k++) {
                            points[k - sC] = new Point(m.startRow, k);
                        }
                    } else {
                        points = new Point[]{desk.turn ? whiteKing : blackKing};
                    }
                    List<Move>[][] generatedMoves = checkNonTarget(points);
                    if (isCorrect) {
                        deskMoves[i][j].add(new MoveTree(m, generatedMoves));
                        hasMoves = true;
                    }
                    desk.undoMove();
                }
            }
        }
        return hasMoves;
    }

}
