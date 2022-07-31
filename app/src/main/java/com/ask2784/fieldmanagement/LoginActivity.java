package com.ask2784.fieldmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.ask2784.fieldmanagement.databinding.ActivityLoginBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);

        if (FirebaseAuth.getInstance().getUid() != null) {
            startMainActivity();
        }

        binding.login.setOnClickListener(v -> {
            List<AuthUI.IdpConfig> provider = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build());
            Intent intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAlwaysShowSignInMethodScreen(true)
                    .setAvailableProviders(provider)
                    .build();
            launcher.launch(intent);
        });
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                if (user.getMetadata() != null) {
                    if (user.getMetadata().getCreationTimestamp() != user.getMetadata().getLastSignInTimestamp())
                        Toast.makeText(this, "Welcome Back", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                }
            }
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
