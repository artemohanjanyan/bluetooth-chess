package lec.chessproto.chess;

final public class Point {
    int row, column;

    public Point() {
    }

    public Point(int row, int column) {
        set(row, column);
    }

    void set(int x, int y) {
        this.row = x;
        this.column = y;
    }

    void offset(int drow, int dcolumn) {
        row += drow;
        column += dcolumn;
    }
}
