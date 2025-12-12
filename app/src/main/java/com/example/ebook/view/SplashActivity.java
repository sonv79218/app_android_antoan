package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook.R;
import com.example.ebook.model.LoginResponse;
import com.example.ebook.model.RefreshTokenRequest;
import com.example.ebook.repository.AuthRepository;
import com.example.ebook.utils.JwtUtils;
import com.example.ebook.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 1500; // 1.5 giây
    private SessionManager sessionManager;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        authRepository = new AuthRepository();

        // Kiểm tra auto login sau một khoảng thời gian ngắn
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAutoLogin();
            }
        }, SPLASH_DELAY);
    }

    private void checkAutoLogin() {
        String token = sessionManager.getToken();
        String refreshToken = sessionManager.getRefreshToken();

        // Nếu không có token, chuyển đến Login
        if (token == null || token.isEmpty()) {
            Log.d(TAG, "No token found, navigating to Login");
            navigateToLogin();
            return;
        }

        // Kiểm tra token có hợp lệ không
        if (JwtUtils.isTokenValid(token)) {
            Log.d(TAG, "Token is valid, navigating to Home");
            navigateToHome();
        } else {
            // Token đã hết hạn, thử refresh token
            Log.d(TAG, "Token expired, attempting to refresh");
            if (refreshToken != null && !refreshToken.isEmpty()) {
                refreshAccessToken(refreshToken);
            } else {
                // Không có refresh token, chuyển đến Login
                Log.d(TAG, "No refresh token, navigating to Login");
                sessionManager.clearSession();
                navigateToLogin();
            }
        }
    }

    private void refreshAccessToken(String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        authRepository.refreshToken(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lưu token mới
                    LoginResponse loginResponse = response.body();
                    sessionManager.saveToken(loginResponse.getAccessToken());
                    if (loginResponse.getRefreshToken() != null) {
                        sessionManager.saveRefreshToken(loginResponse.getRefreshToken());
                    }
                    
                    // Lưu lại thông tin user nếu có
                    if (loginResponse.getUser() != null) {
                        sessionManager.saveUsername(loginResponse.getUser().getName());
                        sessionManager.saveUserId(loginResponse.getUser().getId());
                        sessionManager.saveRole(loginResponse.getUser().getRole());
                        sessionManager.saveEmail(loginResponse.getUser().getEmail());
                    }
                    
                    Log.d(TAG, "Token refreshed successfully, navigating to Home");
                    navigateToHome();
                } else {
                    // Refresh token không hợp lệ, xóa session và chuyển đến Login
                    Log.d(TAG, "Refresh token failed, clearing session and navigating to Login");
                    sessionManager.clearSession();
                    navigateToLogin();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Error refreshing token: " + t.getMessage());
                // Lỗi kết nối, vẫn thử điều hướng với token cũ (có thể đã hết hạn)
                // Hoặc có thể chuyển đến Login để user đăng nhập lại
                sessionManager.clearSession();
                navigateToLogin();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

