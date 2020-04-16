package com.example.arduino.gameScreen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.arduino.R;
import com.example.arduino.initGame.Member;
import com.example.arduino.loby.PopWindow;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class GameScreenActivity extends AppCompatActivity {
    private Game game;
    private ProgressBar pbLife;
    private TextView txtLife;
    private TextView txtAmmo;
    private TextView txtScore;
    private  TextView txtKeys;
    private TextView txtMines;
    private TextView txtDefuse;
    private Button btnShot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
        btnShot = (Button) findViewById(R.id.btGameShoot);
        pbLife = (ProgressBar) findViewById(R.id.progressBar2);
        txtLife = (TextView) findViewById(R.id.txtLife);
        txtAmmo = (TextView) findViewById(R.id.txtAmmouLeft);
        txtScore = (TextView) findViewById(R.id.txtGamePoint);
        txtKeys = (TextView) findViewById(R.id.txtGameKeys);
        txtMines = (TextView) findViewById(R.id.txtGameMines);
        txtDefuse = (TextView) findViewById(R.id.txtGameDefuse);
        getDataFromSetting();
        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
            }
        });

        btnShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer num =Integer.parseInt(game.getAmmuo());
                num = num -1 ;
                game.setAmmuo(num.toString());
                txtAmmo.setText("Ammuo: - " + game.getAmmuo());

            }
        });


        ListnerForChangeInGame();
    }

    /**
     * get the data from the user that set the game
     */
    private void getDataFromSetting(){
        Bundle data = getIntent().getExtras();
        assert data != null;
        Member member = (Member) data.getParcelable("MyMember");
        assert member != null;
        game = new Game(member);
        txtAmmo.setText("Ammuo Left:" + game.getAmmuo());
        txtLife.setText("LIFE- 100");
        txtScore.setText("SCORE- 0");
        txtKeys.setText("KEYS - 0");
        txtMines.setText("MINES - 0");
        txtDefuse.setText("DEFUSE - 0");

    }

    private void ListnerForChangeInGame() {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = fstore.collection("Game").document("firstgame");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String life_left = documentSnapshot.getString("LifePlayer1");
                assert life_left != null;
                if(life_left.equals("0")){
                    gameOver();
                }
                pbLife.setProgress(Integer.parseInt(life_left));
                txtLife.setText("LIFE: - " +life_left);

            }
        });
    }

    private void gameOver() {
    }
}
