package com.example.arduino.ui.login;


import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.arduino.R;
import com.example.arduino.defines.LogDefs;
import com.example.arduino.menu.MenuActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LogDefs.tagLogin;
    private FirebaseAuth mAuth;

    //xml fields
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private ProgressBar loadingProgressBar;


    @Override
    public void onStart() {
        super.onStart();


        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        UpdateButtonsVisibility(false);

        if (user != null) {
            //user logged in, change to main menu screen
            changeScreen(MenuActivity.class);
            finish();
        }

    }

    //add stats document for new registered user
    static public void CreateStatsDocument(FirebaseUser user) {
        //TODO impl, consider move this to utilities
        //return;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById((R.id.register));
        loadingProgressBar = findViewById(R.id.loading);

        mAuth = FirebaseAuth.getInstance();

        //ensure user not logged in when app start
        mAuth.signOut();

        ///Start of Login part
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!ValidateEmail(LoginActivity.this, email)) {
                    usernameEditText.setError(email);
                    return;
                }
                if(!ValidatePassword(LoginActivity.this,password)) {
                    passwordEditText.setError(password);
                    return;
                }

                UpdateButtonsVisibility(true);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, LogDefs.emailLoginSucMsg);
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, LogDefs.emailLoginFailMsg, task.getException());
                                    String errorMsg = LoginErrorCodeConvert(task.getException());
                                    Toast.makeText(LoginActivity.this, errorMsg,
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                            }
                        });
            }
        });
        ///End of Login part

        ///Start of Register part
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!ValidateEmail(LoginActivity.this, email)) {
                    usernameEditText.setError(email);
                    return;
                }
                if(!ValidatePassword(LoginActivity.this,password)) {
                    passwordEditText.setError(password);
                    return;
                }
                UpdateButtonsVisibility(true);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, LogDefs.emailRegisterSucMsg);
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    assert(user != null);
                                    CreateStatsDocument(user);
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.

                                    Log.w(TAG, LogDefs.emailRegisterFailMsg, task.getException());
                                    String errorMsg = LoginErrorCodeConvert(task.getException());
                                    Toast.makeText(LoginActivity.this, errorMsg ,
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });
        ///End of Register part


    }



private void UpdateButtonsVisibility(Boolean isHideButtons) {
    if(isHideButtons) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        registerButton.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.INVISIBLE);
    }
    else {
        loadingProgressBar.setVisibility(View.INVISIBLE);
        registerButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);
    }
}

/******************* Utilities methods *******************/
//TODO  - change this method to utility file for reusable across files
private void changeScreen(Class screen){
    Intent intent = new Intent(this, screen);
    startActivity(intent);
}
    static public Boolean ValidateEmail(Context context, String email)
    {
        if( TextUtils.isEmpty(email) ) {
            Toast.makeText(context, "email field is empty", Toast.LENGTH_LONG).show();
            Log.e(TAG, LogDefs.emailInvalidlMsg);
            return false;
        }
        if (!email.contains("@") || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "email value is in invalid format", Toast.LENGTH_LONG).show();
            Log.e(TAG, LogDefs.emailInvalidlMsg);
            return false;
        }

        return true;
    }

    static public Boolean ValidatePassword(Context context, String password) {
        if (TextUtils.isEmpty(password) ) {
            Toast.makeText(context, "password field is empty", Toast.LENGTH_LONG).show();
            Log.e(TAG, LogDefs.passwordInvalidlMsg);
            return false;
        }

        if (password.length() < 5) {
            Toast.makeText(context, "password must contain at least 6 chars", Toast.LENGTH_LONG).show();
            Log.e(TAG, LogDefs.passwordInvalidlMsg);
            return false;
        }

        return true;
    }

    static public String LoginErrorCodeConvert(Exception exception) {
        String errorCode = ((FirebaseAuthException)(exception)).getErrorCode();
        String errorResult;
        switch (errorCode) {

            case "ERROR_WRONG_PASSWORD":
            case "ERROR_USER_NOT_FOUND":
                errorResult = "Wrong user name or password";
                break;
            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
            case "ERROR_EMAIL_ALREADY_IN_USE":
                errorResult = "The user name is already in use by another account.";
                break;
            default:
                errorResult = exception.getMessage();
        }

        return errorResult;
    }
}
