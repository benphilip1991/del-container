package com.del.delcontainer.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.del.delcontainer.MainActivity;
import com.del.delcontainer.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make activity full screen with no action bar
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        final EditText emailId = findViewById(R.id.emailId);
        final EditText password = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login_button);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        // Lambda implements the Observer method OnChanged
        loginViewModel.getLoginStateRepo().observe(this, (loginStateRepo) -> {
            if(null != loginStateRepo.getToken() && null == loginStateRepo.getUserId()) {

                // Get user token details and then log in
                loginViewModel.getUserTokenDetails(loginStateRepo.getToken());
            }

            // Move to the main activity only if a valid token was used and the user logged in
            if(null != loginStateRepo.getUserId()) {
                Log.d(TAG, "onCreate: Got successful login. Signing in.");
                Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Lambda for View.OnClickListener interface
        loginButton.setOnClickListener((v) -> {
            Log.d(TAG, "onCreate: Logging in");
            loginViewModel.login(emailId.getText().toString(),
                    password.getText().toString());
        });
    }
}
