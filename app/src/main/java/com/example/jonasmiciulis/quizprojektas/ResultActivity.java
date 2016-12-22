package com.example.jonasmiciulis.quizprojektas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView scoreLabel = (TextView) findViewById(R.id.current_score_label);
        TextView highScoreLabel = (TextView) findViewById(R.id.highscore_label);
        TextView displayLabel = (TextView) findViewById(R.id.end_label);

        int score = getIntent().getIntExtra("SCORE", 0);
        scoreLabel.setText(score + "");

        String display = getIntent().getStringExtra("LABEL");
        displayLabel.setText(display);

        SharedPreferences settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        int highScore = settings.getInt("HIGH_SCORE", 0);

        if (score > highScore) {
            highScoreLabel.setText("Highest score : " + score);
            Toast.makeText(ResultActivity.this, "You beat your highest score!", Toast.LENGTH_LONG).show();
            // Saugoju
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("HIGH_SCORE", score);
            editor.commit();
        }
        else {
            highScoreLabel.setText("Highest score : " + highScore);
        }
    }

    public void mainMenu(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public void onBackPressed() {
    }
}
