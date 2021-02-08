package com.del.delcontainer.ui.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.del.delcontainer.DelContainerActivity;
import com.del.delcontainer.R;
import com.del.delcontainer.database.entities.Auth;
import com.del.delcontainer.repositories.AuthRepository;
import com.del.delcontainer.ui.dialogs.MessageDialogFragment;

public class LoginActivity extends AppCompatActivity
        implements LoginViewModel.LoginFormView {

    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;
    private AuthRepository authRepository;
    private EditText emailId;
    private EditText password;
    private Button loginButton;
    private ProgressBar progressBar;
    private String token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = AuthRepository.getInstance(getApplicationContext());
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        loginViewModel.setLoginFormView(this);

        // Make activity full screen with no action bar
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        emailId = findViewById(R.id.email_id);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.login_progress);

        // Observe the LoginStateRepo object
        loginViewModel.getLoginStateRepo().observe(this, (loginStateRepo) -> {
            if (null != loginStateRepo.getToken() && null == loginStateRepo.getUserId()) {
                // Get user token details and then log in
                loginViewModel.getUserTokenDetails(loginStateRepo.getToken());
            } else if (null != loginStateRepo.getUserId()) {
                Log.d(TAG, "onCreate: Got successful login. Signing in.");
                // Add to repo only if it doesn't exist.
                if (authRepository.getAccessToken() == null) {
                    authRepository.addAuthInfo(new Auth("",
                            LoginStateRepo.getInstance().getToken(),
                            LoginStateRepo.getInstance().getUserId()));
                }
                loginApp();
            } else if(null == loginStateRepo.getToken()) {
                authRepository.clearAuthInfo();
                setFormVisible();
            }
        });

        loginButton.setOnClickListener((v) -> {
            Log.d(TAG, "onCreate: Logging in");
            loginViewModel.login(emailId.getText().toString(),
                    password.getText().toString());
        });

        loginViewModel.getStatusObserver().observe(this, (type) -> {
            //Call dialog
            MessageDialogFragment infoDialog = new MessageDialogFragment(type, loginViewModel.getStatusMessage());
            infoDialog.show(getSupportFragmentManager(), "login");
        });
    }

    /**
     * Called immediately after onCreate->onStart or when coming back into
     * focus. Check the token state here and set form visibility
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Validate tokens if they exist and login directly if valid
        token = authRepository.getAccessToken();
        if(null != token) {
            progressBar.setVisibility(View.VISIBLE);
            loginViewModel.getUserTokenDetails(token);
        } else {
            setFormVisible();
        }
    }

    /**
     * Set visibility of the login form
     */
    private void setFormVisible() {
        emailId.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Launch Main activity
     */
    private void loginApp() {
        Intent intent = new Intent(this.getApplicationContext(), DelContainerActivity.class);
        startActivity(intent);
    }

    @Override
    public void resetLoginForm() {
        Log.d(TAG, "resetLoginForm: Resetting login form due to token verification failure");
        authRepository.clearAuthInfo();
        setFormVisible();
    }

    @Override
    public void displayErrorToast(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}
