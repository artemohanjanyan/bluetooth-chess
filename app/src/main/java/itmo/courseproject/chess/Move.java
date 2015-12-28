package itmo.courseproject.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class Move {

    protected static final ArrayList<MoveFactory> factoryMap = new ArrayList<>();

    protected interface MoveFactory {
        Move create(byte... bytes);
    }


    private static class EmptyMove extends Move {
        protected EmptyMove() {
            super(0, 0, 0, 0);
        }

        @Override
        void execute(Desk desk) {
        }

        @Override
        public byte[] toBytes() {
            return new byte[0];
        }

        @Override
        public String getNotation(Desk desk) {
            return "";
        }
    }

    public static final Move EMPTY_MOVE = new EmptyMove();

    public final int startRow, startColumn, endRow, endColumn;
    public final boolean terminal;

    protected Move(int startRow, int startColumn, int endRow, int endColumn, boolean terminal) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endRow = endRow;
        this.endColumn = endColumn;
        this.terminal = terminal;
    }

    protected Move(int startRow, int startColumn, int endRow, int endColumn) {
        this(startRow, startColumn, endRow, endColumn, true);
    }

    abstract void execute(Desk desk);

    public List<Point> getChangedFields() {
        List<Point> ret = new ArrayList<>();
        ret.add(new Point(startRow, startColumn));
        if (startRow != endRow || startColumn != endColumn)
            ret.add(new Point(endRow, endColumn));
        return ret;
    }

    //should be overridden if move isn't terminal
    public List<Move> getPossibleFollowingMoves() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) return false;

        Move other = (Move) o;
        return this.startRow == other.startRow && this.startColumn == other.startColumn &&
                this.endRow == other.endRow && this.endColumn == other.endColumn;
    }

    public boolean equals(int sRow, int sColumn, int eRow, int eColumn) {
        return startColumn == sColumn && startRow == sRow && endColumn == eColumn && endRow == eRow;
    }

    public abstract byte[] toBytes();

    public static byte fieldToByte(int row, int column) {
        return (byte) ((row << 3) | column);
    }

    public static Point byteToField(byte b) {
        return new Point((b >> 3) & 0b111, b & 0b111);
    }


    public static Move getMove(byte[] buffer) {
        return factoryMap.get(buffer[0]).create(Arrays.copyOfRange(buffer, 1, buffer.length));
    }

    public abstract String getNotation(Desk desk);
}
