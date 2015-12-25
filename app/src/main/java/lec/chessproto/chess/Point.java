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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return row == p.row && column == p.column;
    }

    public boolean equals(int row, int column) {
        return this.row == row && this.column == column;
    }

    static public boolean equals(int aRow, int aColumn, int bRow, int bColumn){
        return aRow == bRow && aColumn == bColumn;
    }
}
