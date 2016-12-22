package com.example.jonasmiciulis.quizprojektas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private TextView quizQuestion;

    private RadioGroup radioGroup;
    private RadioButton optionOne;
    private RadioButton optionTwo;
    private RadioButton optionThree;
    private RadioButton optionFour;

    private TextView lifeLabel;
    private TextView scoreLabel;

    private Button timer;

    private int currentQuizQuestion;
    private int quizCount;


    boolean[] timerProcessing = { false };
    boolean[] timerStarts = { false };
    private MyCount counter;

    long remainingtime;

    private int score = 0;
    private int lives = 3;

    private int radioSelected = 0;
    private int userSelection = 0;

    private QuizEntry firstQuestion;
    private List<QuizEntry> parsedObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        counter = new MyCount(60000, 1);
        if (!timerStarts[0]) {
            counter.start();
            timerStarts[0] = true;
            timerProcessing[0] = true;
        }

        quizQuestion = (TextView)findViewById(R.id.quiz_question);
        timer = (Button)findViewById(R.id.time);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        optionOne = (RadioButton)findViewById(R.id.radio0);
        optionTwo = (RadioButton)findViewById(R.id.radio1);
        optionThree = (RadioButton)findViewById(R.id.radio2);
        optionFour = (RadioButton)findViewById(R.id.radio3);
        scoreLabel = (TextView)findViewById(R.id.score_label);
        lifeLabel = (TextView)findViewById(R.id.life_label);

        Button nextButton = (Button)findViewById(R.id.nextquiz);

        scoreLabel.setText("Questions answered : 0 ");
        lifeLabel.setText("Lives : 3");

        AsyncJsonObject asyncObject = new AsyncJsonObject();
        asyncObject.execute("");

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                radioSelected = radioGroup.getCheckedRadioButtonId();
                userSelection = getSelectedAnswer(radioSelected);
                int correctAnswerForQuestion = firstQuestion.getCorrectAnswer();

                if(userSelection == correctAnswerForQuestion){
                    // Teisingas atsakymas
                    Toast.makeText(QuizActivity.this, "Correct!", Toast.LENGTH_SHORT).show();

                    currentQuizQuestion++;
                    score++;
                    scoreLabel.setText("Questions answered : " + score);
                    if(currentQuizQuestion >= quizCount){
                        // atsakė į visus klausimus
                        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                        intent.putExtra("SCORE", score);
                        intent.putExtra("LABEL", "Quiz Completed!");
                        startActivity(intent);
                    }
                    else {
                        firstQuestion = parsedObject.get(currentQuizQuestion);
                        quizQuestion.setText(firstQuestion.getQuestion());
                        String[] possibleAnswers = firstQuestion.getAnswers().split(",");
                        uncheckedRadioButton();
                        optionOne.setText(possibleAnswers[0]);
                        optionTwo.setText(possibleAnswers[1]);
                        optionThree.setText(possibleAnswers[2]);
                        optionFour.setText(possibleAnswers[3]);
                        counter.cancel();
                        counter = new MyCount(remainingtime + 15000, 1);
                        counter.start();
                    }
                }
                else {
                    // Neteisingas atsakymas
                    if (lives > 0 && userSelection != 0) {
                        lives--;
                        Toast.makeText(QuizActivity.this, "Wrong answer! -life", Toast.LENGTH_SHORT).show();
                        lifeLabel.setText("Lives : " + lives);
                    }
                    // liko 1 gyvybė
                    if (lives == 1) {
                        lifeLabel.setTextColor(Color.parseColor("#ff0000"));
                    }
                    // liko 0 gyvybių
                    if(lives <= 0){
                        counter.cancel();
                        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                        intent.putExtra("SCORE", score);
                        intent.putExtra("LABEL", "GAME OVER!");
                        startActivity(intent);
                    }
                    // nepasirinktas atsakymas
                    if (userSelection == 0) {
                        Toast.makeText(QuizActivity.this, "You haven't checked anything!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_quiz, menu);
        return true;
    }

    @Override
    public void onBackPressed () {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class AsyncJsonObject extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {

            String jsonResult = "";

            try {
                URL url = new URL("http://jonmic.stud.if.ktu.lt/Android_Quiz/index.php");
                HttpURLConnection con;
                con = (HttpURLConnection) url.openConnection();
                InputStream in;
                in = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                jsonResult = reader.readLine();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonResult;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(QuizActivity.this, "Downloading Quiz","Wait....", true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            System.out.println("Rasta reiksme: " + result);
            try {
                parsedObject = returnParsedJsonObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(parsedObject == null){
                return;
            }
            quizCount = parsedObject.size();
            firstQuestion = parsedObject.get(0);

            quizQuestion.setText(firstQuestion.getQuestion());
            String[] possibleAnswers = firstQuestion.getAnswers().split(",");
            optionOne.setText(possibleAnswers[0]);
            optionTwo.setText(possibleAnswers[1]);
            optionThree.setText(possibleAnswers[2]);
            optionFour.setText(possibleAnswers[3]);
        }

    }
    private List<QuizEntry> returnParsedJsonObject(String result) throws JSONException {

        List<QuizEntry> jsonObject = new ArrayList<QuizEntry>();
        JSONObject resultObject = null;
        JSONArray jsonArray = null;
        QuizEntry newItemObject = null;

        try {
            resultObject = new JSONObject(result);
            System.out.println("Isbandome " + resultObject.toString());
            jsonArray = resultObject.optJSONArray("quiz_questions");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Random rnd = new Random();
        for (int i = jsonArray.length() - 1; i >= 0; i--)
        {
            int j = rnd.nextInt(i + 1);
            // Simple swap
            Object object = jsonArray.get(j);
            jsonArray.put(j, jsonArray.get(i));
            jsonArray.put(i, object);
        }


        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonChildNode = null;
            try {
                jsonChildNode = jsonArray.getJSONObject(i);
                int id = jsonChildNode.getInt("id");
                String question = jsonChildNode.getString("question");
                String answerOptions = jsonChildNode.getString("possible_answers");
                int correctAnswer = jsonChildNode.getInt("correct_answer");
                newItemObject = new QuizEntry(id, question, answerOptions, correctAnswer);
                jsonObject.add(newItemObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    private int getSelectedAnswer(int radioSelected){

        int answerSelected = 0;
        if(radioSelected == R.id.radio0){
            answerSelected = 1;
        }
        if(radioSelected == R.id.radio1){
            answerSelected = 2;
        }
        if(radioSelected == R.id.radio2){
            answerSelected = 3;
        }
        if(radioSelected == R.id.radio3){
            answerSelected = 4;
        }
        return answerSelected;
    }
    private void uncheckedRadioButton(){
        radioGroup.clearCheck();
    }

    public class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            timer.setText("" + millisUntilFinished / 1000);
            remainingtime = millisUntilFinished;
            if (remainingtime <= 10000) {
                timer.setTextColor(Color.parseColor("#ff0000"));
            }
            else {
                timer.setTextColor(Color.parseColor("#ffffff"));
            }
        }

        @Override
        public void onFinish() {
            if (remainingtime <= 1000) {
            timerProcessing[0] = false;
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("SCORE", score);
                intent.putExtra("LABEL", "Time is up!");
                startActivity(intent);
            }
        }
    }
}
