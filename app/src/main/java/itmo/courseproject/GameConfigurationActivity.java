package itmo.courseproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

public abstract class GameConfigurationActivity extends AppCompatActivity {

    protected static final String TAG = "GameConfiguration";

    public static final int COLOR_RANDOM = 0;
    public static final int COLOR_WHITE = 1;
    public static final int COLOR_BLACK = 2;

    protected int type = GameActivity.CHESS_CLASSIC;
    protected int color = COLOR_RANDOM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_configuration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_configuration, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        launchGame();
        finish();
        return super.onOptionsItemSelected(item);
    }

    protected abstract void launchGame();

    public void onTypeSelected(View view) {
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        switch (view.getId()) {
            case R.id.type_classic:
                type = GameActivity.CHESS_CLASSIC;
                break;

            case R.id.type_fisher:
                type = GameActivity.CHESS_960;
                break;
        }
    }

    public void onColorSelected(View view) {
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        switch (view.getId()) {
            case R.id.color_random:
                color = COLOR_RANDOM;
                break;

            case R.id.color_white:
                color = COLOR_WHITE;
                break;

            case R.id.color_black:
                color = COLOR_BLACK;
                break;
        }
    }
}
