package com.del.delcontainer.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.del.delcontainer.MainActivity;
import com.del.delcontainer.R;
import com.del.delcontainer.database.entities.Auth;
import com.del.delcontainer.repositories.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = AuthRepository.getInstance(getApplicationContext());
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        // Validate tokens if they exist and login directly if valid
        validateTokenIfExists();

        // Make activity full screen with no action bar
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        final EditText emailId = findViewById(R.id.emailId);
        final EditText password = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login_button);

        // Observe the LoginStateRepo object
        loginViewModel.getLoginStateRepo().observe(this, (loginStateRepo) -> {
            if (null != loginStateRepo.getToken() && null == loginStateRepo.getUserId()) {
                // Get user token details and then log in
                loginViewModel.getUserTokenDetails(loginStateRepo.getToken());
            }

            // Move to the main activity only if a valid token was used and the user logged in
            if (null != loginStateRepo.getUserId()) {
                Log.d(TAG, "onCreate: Got successful login. Signing in.");
                // Add to repo only if it doesn't exist.
                if (authRepository.getAccessToken() == null) {
                    authRepository.addAuthInfo(new Auth("",
                            LoginStateRepo.getInstance().getToken(),
                            LoginStateRepo.getInstance().getUserId()));
                }

                loginApp();
            }
        });

        // Lambda for View.OnClickListener interface
        loginButton.setOnClickListener((v) -> {
            Log.d(TAG, "onCreate: Logging in");
            loginViewModel.login(emailId.getText().toString(),
                    password.getText().toString());
        });
    }

    /**
     * Validate stored tokens and if valid, bypass the login screen
     */
    private void validateTokenIfExists() {
        String token = authRepository.getAccessToken(); // call waits for token
        if (null != token) {
            Log.d(TAG, "validateTokenIfExists: Previous token found. Validating.");
            // Observer already attached to the state repo and will launch the main
            // activity if the token is set in the following call.
            loginViewModel.getUserTokenDetails(token);
        }
    }

    /**
     * Launch Main activity
     */
    private void loginApp() {
        Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
