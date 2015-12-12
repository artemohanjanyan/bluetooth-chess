package lec.chessproto.chess;



import java.util.LinkedList;
import java.util.List;

public  class Figure {

    private boolean color;
    private MoveFinder m;
    private int ID;

    public Figure(boolean color, int ID, MoveFinder m) {
        this.color = color;
        this.m = m;
        m.figure = this;
        this.ID = ID;
    }

    public List<Move> getMoves(Desk desk, int row, int column) {
            return  m.getMoves(desk, row, column);
    }

    public boolean getColor() {
        return color;
    }

    public int getID() {
        return ID;
    }

    abstract static class MoveFinder {
        Figure figure;

        abstract List<Move> getMoves(Desk desk, int row, int column);

    }

    public static boolean isFieldCordsCorrect(int row, int column) {
        return row >= 0 && column >= 0 && row < Desk.SIZE && column < Desk.SIZE;
    }

    static class DirectMoveFinder extends  MoveFinder {

        boolean line, diag;

        public DirectMoveFinder(boolean line, boolean diag) {
            this.line = line;
            this.diag = diag;
        }

        @Override
        public List<Move> getMoves(Desk desk, int row, int column) {
            List<Move> ret = new LinkedList<>();
            if (line) {
                addMovesByDirection(desk, row, column,  1,  0, ret);
                addMovesByDirection(desk, row, column, -1,  0, ret);
                addMovesByDirection(desk, row, column,  0, -1, ret);
                addMovesByDirection(desk, row, column,  0,  1, ret);
            }
            if (diag) {
                addMovesByDirection(desk, row, column,  1,  1, ret);
                addMovesByDirection(desk, row, column, -1,  1, ret);
                addMovesByDirection(desk, row, column,  1, -1, ret);
                addMovesByDirection(desk, row, column, -1, -1, ret);
            }
            return ret;
        }

        void addMovesByDirection(Desk desk, int row, int column, int drow, int dcolumn, List<Move> ret) {
            int srow = row, scolumn = column;
            row += drow;
            column += dcolumn;
            while (isFieldCordsCorrect(row, column) && desk.d[row][column] == null) {
                ret.add(new SimpleMove(srow, scolumn, row, column));
                row += drow;
                column += dcolumn;
            }
            if (isFieldCordsCorrect(row, column) && desk.d[row][column].color ^ figure.color) {
                ret.add(new SimpleMove(srow, scolumn, row, column));
            }
        }
    }

    static class PawnMoveFinder extends  MoveFinder {
        int drow, startrow;

        public PawnMoveFinder(int drow, int startrow) {
            this.drow = drow;
            this.startrow = startrow;
        }

        @Override
        List<Move> getMoves(Desk desk, int row, int column) {
            LinkedList<Move> ret = new LinkedList<>();
            int nrow = row + drow;
            if (isFieldCordsCorrect(nrow, column) && desk.d[nrow][column] == null) {
                ret.add(new SimpleMove(row, column, nrow, column));
            }
            if (isFieldCordsCorrect(nrow, column - 1) && desk.d[nrow][column - 1] != null && desk.d[nrow][column - 1].color ^ figure.color) {
                ret.add(new SimpleMove(row, column, nrow, column - 1));
            }
            if (isFieldCordsCorrect(nrow, column + 1) && desk.d[nrow][column + 1] != null && desk.d[nrow][column + 1].color ^ figure.color) {
                ret.add(new SimpleMove(row, column, nrow, column + 1));
            }
            if (row == startrow && desk.d[row + 2 * drow][column] == null) {
                ret.add(new SimpleMove(row, column, row + 2 * drow, column));
            }
            return ret;
        }
    }

    static class FieldCheckMoveFinder extends MoveFinder {

        int[] drow, dcolumn;

        public FieldCheckMoveFinder(int[] drow, int[] dcolumn) {
            this.drow = drow;
            this.dcolumn = dcolumn;
    }

        void checkMoveField(Desk desk, int row, int column, int drow, int dcolumn, List<Move> ret) {
            int nrow = row + drow, ncolumn = column + dcolumn;
            if (isFieldCordsCorrect(nrow, ncolumn) && (desk.d[nrow][ncolumn] == null || desk.d[nrow][ncolumn].color ^ figure.color)) {
                ret.add(new SimpleMove(row, column, nrow, ncolumn));
            }
        }

        @Override
        List<Move> getMoves(Desk desk, int row, int column) {
            LinkedList<Move> ret = new LinkedList<>();
            for (int i = 0; i < drow.length; i++) {
                checkMoveField(desk, row, column, drow[i], dcolumn[i], ret);
            }
            return ret;
        }
    }

    static final int[] KNIGHT_DROW    = new int[] {2, 1, -2, -1, 2, 1, -2, -1};
    static final int[] KNIGHT_DCOLUMN = new int[] {1, 2, 1, 2, -1, -2, -1, -2};

    static final int[] KING_DROW    = new int[] {1, 0, -1, 0, 1, -1, 1, -1};
    static final int[] KING_DCOLUMN = new int[] {0, 1, 0, -1, 1, 1, -1, -1};

    public static final Figure WHITE_PAWN = new Figure(Chess.WHITE, 0, new PawnMoveFinder( 1, 1));
    public static final Figure BLACK_PAWN = new Figure(Chess.BLACK, 1, new PawnMoveFinder(-1, 6));

    public static final Figure WHITE_ROOK = new Figure(Chess.WHITE, 2, new DirectMoveFinder(true, false));
    public static final Figure BLACK_ROOK = new Figure(Chess.BLACK, 3, new DirectMoveFinder(true, false));

    public static final Figure WHITE_BISHOP = new Figure(Chess.WHITE, 4, new DirectMoveFinder(false, true));
    public static final Figure BLACK_BISHOP = new Figure(Chess.BLACK, 5, new DirectMoveFinder(false, true));

    public static final Figure WHITE_KNIGHT = new Figure(Chess.WHITE, 6, new FieldCheckMoveFinder(KNIGHT_DROW, KNIGHT_DCOLUMN));
    public static final Figure BLACK_KNIGHT = new Figure(Chess.BLACK, 7, new FieldCheckMoveFinder(KNIGHT_DROW, KNIGHT_DCOLUMN));

    public static final Figure WHITE_QUEEN = new Figure(Chess.WHITE, 8, new DirectMoveFinder(true, true));
    public static final Figure BLACK_QUEEN = new Figure(Chess.BLACK, 9, new DirectMoveFinder(true, true));

    public static final Figure WHITE_KING = new Figure(Chess.WHITE, 10, new FieldCheckMoveFinder(KING_DROW, KING_DCOLUMN));
    public static final Figure BLACK_KING = new Figure(Chess.BLACK, 11, new FieldCheckMoveFinder(KING_DROW, KING_DCOLUMN));
}
