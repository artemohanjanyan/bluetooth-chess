package itmo.courseproject.chess;


public class SimpleMove extends Move {

    protected Figure endFigure;

    static final byte MOVE_ID;
    static private final MoveFactory f = new MoveFactory() {
        @Override
        public Move create(byte... bytes) {
            Point s = byteToField(bytes[0]);
            Point f = byteToField(bytes[1]);
            Figure figure = (bytes[2] == -1) ? null : Figure.figures.get(bytes[2]);
            return new SimpleMove(s.row, s.column, f.row, f.column, figure);
        }
    };

    static {
        MOVE_ID = (byte) factoryMap.size();
        factoryMap.add(f);
    }

    public SimpleMove(int startRow, int startColumn, int endRow, int endColumn) {
        this(startRow, startColumn, endRow, endColumn, null);
    }

    public SimpleMove(int startRow, int startColumn, int endRow, int endColumn, Figure endFigure) {
        super(startRow, startColumn, endRow, endColumn);
        this.endFigure = endFigure;
    }

    @Override
    public void execute(Desk desk) {
        Figure f = (endFigure == null) ? desk.d[startRow][startColumn] : endFigure;
        desk.d[startRow][startColumn] = null;
        desk.d[endRow][endColumn] = f;
    }


    @Override
    public byte[] toBytes() {
        return new byte[]{
                MOVE_ID,
                fieldToByte(startRow, startColumn),
                fieldToByte(endRow, endColumn),
                (endFigure == null) ? -1 : endFigure.getID()
        };
    }
}
