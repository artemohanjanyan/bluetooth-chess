package itmo.courseproject.chess;


import java.util.List;

public final class PieceTakenMove extends SimpleMove {

    private int pieceTakenRow, pieceTakenColumn;

    static final byte MOVE_ID;
    static private final MoveFactory f = new MoveFactory() {
        @Override
        public Move create(byte... bytes) {
            Point s = byteToField(bytes[0]);
            Point f = byteToField(bytes[1]);
            Point p = byteToField(bytes[2]);
            Figure figure = (bytes[3] == -1) ? null : Figure.figures.get(bytes[3]);
            return new PieceTakenMove(s.row, s.column, f.row, f.column, p.row, p.column, figure);
        }
    };

    static {
        MOVE_ID = (byte) factoryMap.size();
        factoryMap.add(f);
    }

    public PieceTakenMove(int startRow, int startColumn, int endRow, int endColumn, int pieceTakenRow, int pieceTakenColumn) {
        this(startRow, startColumn, endRow, endColumn, pieceTakenRow, pieceTakenColumn, null);
    }

    public PieceTakenMove(int startRow, int startColumn, int endRow, int endColumn, int pieceTakenRow, int pieceTakenColumn, Figure endFigure) {
        super(startRow, startColumn, endRow, endColumn, endFigure);
        this.pieceTakenRow = pieceTakenRow;
        this.pieceTakenColumn = pieceTakenColumn;
    }

    @Override
    public void execute(Desk desk) {
        super.execute(desk);
        desk.d[pieceTakenRow][pieceTakenColumn] = null;
    }

    @Override
    public byte[] toBytes() {
        return new byte[]{
                MOVE_ID,
                fieldToByte(startRow, startColumn),
                fieldToByte(endRow, endColumn),
                fieldToByte(pieceTakenRow, pieceTakenColumn),
                (endFigure == null) ? -1 : endFigure.getID()
        };
    }

    @Override
    public List<Point> getChangedFields() {
        List<Point> ret = super.getChangedFields();
        ret.add(new Point(pieceTakenRow, pieceTakenColumn));
        return ret;
    }
}
