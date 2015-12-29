package itmo.courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Random;

public class LocalGameConfigurationActivity extends GameConfigurationActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.non_local).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void launchGame() {
        Random random = new Random();
        Intent intent = new Intent(this, LocalGameActivity.class)
                .putExtra(GameActivity.SEED, random.nextLong())
                .putExtra(GameActivity.GAME, type)
                .putExtra(BtGameActivity.LOCAL_PLAYER_COLOR,
                        color != COLOR_WHITE);
        startActivity(intent);
        finish();
    }
}
