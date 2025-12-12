package com.example.ebook.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import com.example.ebook.R;
import com.example.ebook.model.LoginRequest;
import com.example.ebook.model.LoginResponse;
import com.example.ebook.repository.AuthRepository;
import com.example.ebook.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private AuthRepository repository;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = new AuthRepository();
        session = new SessionManager(this);

        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        Button btnLogin = findViewById(R.id.button3);
        TextView tvToRegister = findViewById(R.id.textViewNewUser);

        btnLogin.setOnClickListener(v -> doLogin());
        tvToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);
        repository.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String userId = response.body().getUser().getId();
                    Log.d("SESSION", "Received userId = " + userId);
                    // Lưu access token và refresh token
                    session.saveToken(response.body().getAccessToken());
                    if (response.body().getRefreshToken() != null) {
                        session.saveRefreshToken(response.body().getRefreshToken());
                    }
                    session.saveUsername(response.body().getUser().getName());
                    session.saveUserId(response.body().getUser().getId());
                    session.saveRole(response.body().getUser().getRole());
                    session.saveEmail(response.body().getUser().getEmail());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    if (response.code() == 403) {
                        Toast.makeText(LoginActivity.this, "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
