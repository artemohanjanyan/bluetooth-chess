package lec.chessproto.chess;

import java.util.ArrayList;
import java.util.List;


public abstract class Move {

    public final int startRow, startColumn, endRow, endColumn;
    public final boolean terminal;

    public Move(int startRow, int startColumn, int endRow, int endColumn, boolean terminal) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endRow = endRow;
        this.endColumn = endColumn;
        this.terminal = terminal;
    }

    public Move(int startRow, int startColumn, int endRow, int endColumn) {
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

    //should be override if move isn't terminal
    public List<Move> getPossibleFollowingMoves() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Move)) return false;

        Move other = (Move) o;
        return  this.startRow == other.startRow && this.startColumn == other.startColumn &&
                this.endRow == other.endRow && this.endColumn == other.endColumn;
    }
}