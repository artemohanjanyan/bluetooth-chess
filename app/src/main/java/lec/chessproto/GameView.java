package lec.chessproto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import lec.chessproto.chess.Desk;
import lec.chessproto.chess.Figure;
import lec.chessproto.chess.FigureMoves;
import lec.chessproto.chess.Move;


public class GameView extends View {

    private static final String TAG = "GameView";

    public static final int FIGURES_COUNT = 12;

    public Desk desk;

    private Paint whiteFieldPaint, blackFieldPaint, whiteFieldPressedPaint, blackFieldPressedPaint, figurePaint;
    private int fieldSize, boardSize;
    private Rect dstRect, srcRect;

    private Bitmap[] figureBitmapMap;
    private int figureBitmapSize;


    private boolean fieldTouchDowned = false;
    private int pressedColumn = -1, pressedRow = -1;

    private Resources res;

    public List<Move> markerMoves;

    DeskListener deskListener;

    interface DeskListener {
        Figure  onFieldDrag(int row, int column);
        boolean onFiledSelect(int row, int column);
        void    onFieldDrop(int row, int column);
    }


    public GameView(Context context) {
        super(context);

        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }


    private void init() {

        whiteFieldPaint = new Paint();
        blackFieldPaint = new Paint();
        whiteFieldPressedPaint = new Paint();
        blackFieldPressedPaint = new Paint();
        figurePaint = new Paint();

        res = getResources();
        whiteFieldPaint.setColor(res.getColor(R.color.whiteField));
        blackFieldPaint.setColor(res.getColor(R.color.blackField));
        whiteFieldPressedPaint.setColor(res.getColor(R.color.whiteFieldPressed));
        blackFieldPressedPaint.setColor(res.getColor(R.color.blackFieldPressed));

        figureBitmapMap = new Bitmap[FIGURES_COUNT];

        loadFigureBitmap(Figure.WHITE_PAWN.getID()  , R.drawable.white_pawn);
        loadFigureBitmap(Figure.WHITE_KNIGHT.getID(), R.drawable.white_knight);
        loadFigureBitmap(Figure.WHITE_BISHOP.getID(), R.drawable.white_bishop);
        loadFigureBitmap(Figure.WHITE_ROOK.getID()  , R.drawable.white_rook);
        loadFigureBitmap(Figure.WHITE_QUEEN.getID() , R.drawable.white_queen);
        loadFigureBitmap(Figure.WHITE_KING.getID() , R.drawable.white_king);
        loadFigureBitmap(Figure.BLACK_PAWN.getID()  , R.drawable.black_pawn);
        loadFigureBitmap(Figure.BLACK_KNIGHT.getID(), R.drawable.black_knight);
        loadFigureBitmap(Figure.BLACK_BISHOP.getID(), R.drawable.black_bishop);
        loadFigureBitmap(Figure.BLACK_ROOK.getID()  , R.drawable.black_rook);
        loadFigureBitmap(Figure.BLACK_QUEEN.getID() ,R.drawable.black_queen);
        loadFigureBitmap(Figure.BLACK_KING.getID()  ,R.drawable.black_king);

        figureBitmapSize = figureBitmapMap[0].getWidth();
        srcRect = new Rect(0, 0, figureBitmapSize, figureBitmapSize);
        dstRect = new Rect();
    }

    public void loadFigureBitmap(int k, int id) {
        BitmapDrawable drawable = (BitmapDrawable) res.getDrawable(id);
        if (drawable != null)
            figureBitmapMap[k] =  drawable.getBitmap();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width  = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        boardSize = Math.min(width, height);
        fieldSize = boardSize / Desk.SIZE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                drawField(canvas, i, j, getFieldPaint(i, j));
            }
        }

        if (pressedColumn != -1 && pressedRow != -1) {
            drawField(canvas, pressedRow, pressedColumn, getFieldPressedPaint(pressedRow, pressedColumn));
        }
        if (markerMoves != null){
            for (Move m : markerMoves) {
                int row = m.endRow, column = m.endColumn;
                drawField(canvas, row, column, getFieldPressedPaint(row, column));
            }
        }

        if (isInEditMode())
            return;

        if (desk == null)
            return;

        dstRect.set(0, 0, fieldSize, fieldSize);


        for (int i = Desk.SIZE - 1; i >= 0; i--) {
            for (int  j = 0; j < Desk.SIZE; j++) {
                Figure figure = desk.getFigure(i, j);
                if (figure != null) {
                    Bitmap bitmap = figureBitmapMap[figure.getID()];
                    canvas.drawBitmap(bitmap, srcRect, dstRect, figurePaint);
                }
                dstRect.offset(fieldSize, 0);
            }
            dstRect.offset(-boardSize, fieldSize);
        }


    }

    Paint getFieldPressedPaint(int row, int column) {
        return (row + column) % 2 == 0 ? blackFieldPressedPaint : whiteFieldPressedPaint;
    }

    Paint getFieldPaint(int row, int column) {
        return (row + column) % 2 == 0 ? blackFieldPaint : whiteFieldPaint;
    }

    private void drawField(Canvas canvas, int row, int column, Paint paint) {
        canvas.drawRect(
                fieldSize * column,
                fieldSize * (Desk.SIZE - row - 1),
                fieldSize * (column + 1),
                fieldSize * (Desk.SIZE - row),
                paint
        );
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        Log.d(TAG, "touch event triggered with params" +
                ": x=" + Integer.toString(x) +
                ", y=" + Integer.toString(y) +
                ", action=" + MotionEvent.actionToString(event.getAction())
        );

        if (x < 0 || y < 0 || x >= boardSize || y >= boardSize)
            return super.onTouchEvent(event);

        int column = x / fieldSize;
        int row = Desk.SIZE - y / fieldSize - 1;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                fieldTouchDowned = true;
                break;
            case MotionEvent.ACTION_MOVE :
                if (fieldTouchDowned)
                    fieldDragged(row, column);
                fieldTouchDowned = false;
                break;
            case MotionEvent.ACTION_UP :
                if (fieldTouchDowned)
                    fieldSelected(row, column);
                else
                    fieldDropped(row, column);
                break;
            default:
                return super.onTouchEvent(event);
        }

        return true;
    }


    private void fieldSelected(int row, int column) {
        Log.d(TAG, "field selected" +
                ": row= " + Integer.toString(row) +
                ", column= " + Integer.toString(column)
        );
        if (deskListener == null)
            return;
        boolean marked = deskListener.onFiledSelect(row, column);
        if (marked) {
            pressedRow = row;
            pressedColumn = column;
        } else {
            pressedRow = -1;
            pressedColumn = -1;
        }
    }

    private void fieldDragged(int row, int column) {
        Log.d(TAG, "field dragged" +
                        ": row= " + Integer.toString(row) +
                        ", column= " + Integer.toString(column)
        );
    }

    private void fieldDropped(int row, int column) {
        Log.d(TAG, "field dropped" +
                        ": row= " + Integer.toString(row) +
                        ", column= " + Integer.toString(column)
        );
    }


}
