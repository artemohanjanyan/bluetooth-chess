package itmo.courseproject.chess;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Figure {

    static final ArrayList<Figure> figures = new ArrayList<>();

    public final boolean color;
    private final MoveFinder m;
    private final byte ID;

    private Figure(boolean color, MoveFinder m) {
        this.color = color;
        this.m = m;
        m.figure = this;
        this.ID = (byte) figures.size();
        figures.add(this);
    }

    public List<Move> getMoves(Desk desk, int row, int column) {
        return m.getMoves(desk, row, column);
    }

    public boolean getColor() {
        return color;
    }

    public byte getID() {
        return ID;
    }

    abstract static class MoveFinder {
        Figure figure;

        abstract List<Move> getMoves(Desk desk, int row, int column);

    }

    private static boolean isFieldCordsCorrect(int row, int column) {
        return row >= 0 && column >= 0 && row < Desk.SIZE && column < Desk.SIZE;
    }

    static class DirectMoveFinder extends MoveFinder {

        final boolean line;
        final boolean diagonal;

        public DirectMoveFinder(boolean line, boolean diagonal) {
            this.line = line;
            this.diagonal = diagonal;
        }

        @Override
        public List<Move> getMoves(Desk desk, int row, int column) {
            List<Move> ret = new LinkedList<>();
            if (line) {
                addMovesByDirection(desk, row, column, 1, 0, ret);
                addMovesByDirection(desk, row, column, -1, 0, ret);
                addMovesByDirection(desk, row, column, 0, -1, ret);
                addMovesByDirection(desk, row, column, 0, 1, ret);
            }
            if (diagonal) {
                addMovesByDirection(desk, row, column, 1, 1, ret);
                addMovesByDirection(desk, row, column, -1, 1, ret);
                addMovesByDirection(desk, row, column, 1, -1, ret);
                addMovesByDirection(desk, row, column, -1, -1, ret);
            }
            return ret;
        }

        void addMovesByDirection(Desk desk, int row, int column, int dRow, int dColumn, List<Move> ret) {
            int sRow = row, sColumn = column;
            row += dRow;
            column += dColumn;
            while (isFieldCordsCorrect(row, column) && desk.d[row][column] == null) {
                ret.add(new SimpleMove(sRow, sColumn, row, column));
                row += dRow;
                column += dColumn;
            }
            if (isFieldCordsCorrect(row, column) && desk.d[row][column].color ^ figure.color) {
                ret.add(new SimpleMove(sRow, sColumn, row, column));
            }
        }
    }

    static class PawnMoveFinder extends MoveFinder {
        final int dRow;
        final int startRow;
        final int endRow;
        final int enPassantRow;

        int row, column, nRow, nnRow;
        Figure opPawn, f;

        public PawnMoveFinder(int dRow) {
            this.dRow = dRow;
            this.startRow =     dRow == 1 ? 1 : 6;
            this.endRow =       dRow == 1 ? 6 : 1;
            this.enPassantRow = dRow == 1 ? 4 : 3;
        }

        public void checkPieceTakenMove(Desk desk, int dColumn, List<Move> ret) {
            int nColumn = column + dColumn;
            if (isFieldCordsCorrect(nRow, nColumn)) {
                if (desk.d[nRow][nColumn] != null && desk.d[nRow][nColumn].color ^ figure.color) {
                    ret.add(new SimpleMove(row, column, nRow, nColumn, f));
                }
                if (row == enPassantRow && desk.d[row][nColumn] == opPawn && desk.d[nnRow][nColumn] == null) {
                    desk.undoMove();
                    if (desk.d[row][nColumn] == null && desk.d[nnRow][nColumn] == opPawn) {
                        ret.add(new PieceTakenMove(row, column, nRow, nColumn, row, nColumn));
                    }
                    desk.redoMove();
                }
            }
        }

        @Override
        List<Move> getMoves(Desk desk, int row, int column) {
            LinkedList<Move> ret = new LinkedList<>();

            this.row = row;
            this.column = column;
            nRow = row + dRow;
            nnRow = nRow + dRow;
            opPawn = figure.color ? WHITE_PAWN : BLACK_PAWN;
            f = row == endRow ? (figure.color ? Figure.BLACK_QUEEN : Figure.WHITE_QUEEN) : null;

            if (isFieldCordsCorrect(nRow, column) && desk.d[nRow][column] == null) {
                ret.add(new SimpleMove(row, column, nRow, column, f));
                if (row == startRow && desk.d[nnRow][column] == null) {
                    ret.add(new SimpleMove(row, column, nnRow, column));
                }
            }
            checkPieceTakenMove(desk, -1, ret);
            checkPieceTakenMove(desk, +1, ret);
            return ret;
        }
    }

    static class FieldCheckMoveFinder extends MoveFinder {

        final int[] dRow;
        final int[] dColumn;

        public FieldCheckMoveFinder(int[] dRow, int[] dColumn) {
            this.dRow = dRow;
            this.dColumn = dColumn;
        }

        void checkMoveField(Desk desk, int row, int column, int dRow, int dColumn, List<Move> ret) {
            int nRow = row + dRow, nColumn = column + dColumn;
            if (isFieldCordsCorrect(nRow, nColumn) && (desk.d[nRow][nColumn] == null || desk.d[nRow][nColumn].color ^ figure.color)) {
                ret.add(new SimpleMove(row, column, nRow, nColumn));
            }
        }

        @Override
        List<Move> getMoves(Desk desk, int row, int column) {
            LinkedList<Move> ret = new LinkedList<>();
            for (int i = 0; i < dRow.length; i++) {
                checkMoveField(desk, row, column, dRow[i], dColumn[i], ret);
            }
            return ret;
        }
    }

    static class KingMoveFinder extends FieldCheckMoveFinder {

        Figure king, rook;

        public KingMoveFinder() {
            super(KING_D_ROW, KING_D_COLUMN);
        }

        private Move checkCastling(Desk desk, int row, int column, int dColumn) {
            int startKingColumn = column, startRookColumn = -1;
            boolean rookFind = false;

            for (column += dColumn; column < 8 && column >= 0; column += dColumn) {
                Figure f = desk.d[row][column];
                if (f != null) {
                    if (f == rook) {
                        rookFind = true;
                        startRookColumn = column;
                        break;
                    } else {
                        return null;
                    }
                }
            }
            for (column += dColumn; column <= 6 && column >= 2; column += dColumn) {
                Figure f = desk.d[row][column];
                if (f != null) {
                    return null;
                }
            }
            return rookFind ? new Castling(king.getColor(), startKingColumn, startRookColumn) : null;
        }

        @Override
        List<Move> getMoves(Desk desk, int row, int column) {
            Chess chess = (Chess) desk.game;
            List<Move> ret = super.getMoves(desk, row, column);
            king = desk.d[row][column];
            rook = king.color ? BLACK_ROOK : WHITE_ROOK;
            if (king == BLACK_KING && row == 7) {
                if (chess.isBlackLongCastlingEnabled) {
                    Move longCastling = checkCastling(desk, row, column, -1);
                    if (longCastling != null) ret.add(longCastling);

                }
                if (chess.isBlackShortCastlingEnabled) {
                    Move shortCastling = checkCastling(desk, row, column, 1);
                    if (shortCastling != null) ret.add(shortCastling);
                }
            } else if (king == WHITE_KING && row == 0) {
                if (chess.isWhiteLongCastlingEnabled) {
                    Move longCastling = checkCastling(desk, row, column, -1);
                    if (longCastling != null) ret.add(longCastling);
                }
                if (chess.isWhiteShortCastlingEnabled) {
                    Move shortCastling = checkCastling(desk, row, column, 1);
                    if (shortCastling != null) ret.add(shortCastling);
                }
            }
            return ret;
        }
    }

    private static final int[] KNIGHT_D_ROW = new int[]{2, 1, -2, -1, 2, 1, -2, -1};
    private static final int[] KNIGHT_D_COLUMN = new int[]{1, 2, 1, 2, -1, -2, -1, -2};

    private static final int[] KING_D_ROW = new int[]{1, 0, -1, 0, 1, -1, 1, -1};
    private static final int[] KING_D_COLUMN = new int[]{0, 1, 0, -1, 1, 1, -1, -1};

    public static final Figure WHITE_PAWN = new Figure(Chess.WHITE, new PawnMoveFinder(1));
    public static final Figure BLACK_PAWN = new Figure(Chess.BLACK, new PawnMoveFinder(-1));

    public static final Figure WHITE_ROOK = new Figure(Chess.WHITE, new DirectMoveFinder(true, false));
    public static final Figure BLACK_ROOK = new Figure(Chess.BLACK, new DirectMoveFinder(true, false));

    public static final Figure WHITE_BISHOP = new Figure(Chess.WHITE, new DirectMoveFinder(false, true));
    public static final Figure BLACK_BISHOP = new Figure(Chess.BLACK, new DirectMoveFinder(false, true));

    public static final Figure WHITE_KNIGHT = new Figure(Chess.WHITE, new FieldCheckMoveFinder(KNIGHT_D_ROW, KNIGHT_D_COLUMN));
    public static final Figure BLACK_KNIGHT = new Figure(Chess.BLACK, new FieldCheckMoveFinder(KNIGHT_D_ROW, KNIGHT_D_COLUMN));

    public static final Figure WHITE_QUEEN = new Figure(Chess.WHITE, new DirectMoveFinder(true, true));
    public static final Figure BLACK_QUEEN = new Figure(Chess.BLACK, new DirectMoveFinder(true, true));

    public static final Figure WHITE_KING = new Figure(Chess.WHITE, new KingMoveFinder());
    public static final Figure BLACK_KING = new Figure(Chess.BLACK, new KingMoveFinder());
}
