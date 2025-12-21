package com.example.ebook.repository;

import com.example.ebook.api.ApiClient;
import com.example.ebook.api.AuthApi;
import com.example.ebook.model.ForgotPasswordRequest;
import com.example.ebook.model.LoginRequest;
import com.example.ebook.model.RegisterRequest;
import com.example.ebook.model.LoginResponse;
import com.example.ebook.model.RefreshTokenRequest;


import retrofit2.Call;

public class AuthRepository {
    private AuthApi authApi;

    public AuthRepository() {
        authApi = ApiClient.getClient().create(AuthApi.class);
    }

    public Call<LoginResponse> login(LoginRequest request) {
        return authApi.login(request);
    }

    public Call<Void> register(RegisterRequest request) {
        return authApi.register(request);
    }
    public Call<LoginResponse> refreshToken(RefreshTokenRequest request) {
        return authApi.refreshToken(request);
    }

    public Call<Void> forgotPassword(ForgotPasswordRequest request) {
        return authApi.forgotPassword(request);
    }
}
