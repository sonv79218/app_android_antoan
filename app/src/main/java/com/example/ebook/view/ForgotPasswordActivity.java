package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ebook.R;
import com.example.ebook.model.ForgotPasswordRequest;
import com.example.ebook.repository.AuthRepository;
import org.json.JSONObject; // Thêm thư viện này
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private AuthRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        repository = new AuthRepository();
        etEmail = findViewById(R.id.edtEmailForgot);
        Button btnSend = findViewById(R.id.btnSendRequest);
        TextView tvBack = findViewById(R.id.tvBackToLogin);

        btnSend.setOnClickListener(v -> doCheckEmail());
        tvBack.setOnClickListener(v -> finish());
    }

    private void doCheckEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        repository.forgotPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, OTPVerificationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                } else {
                    try {
                        // Đọc thông báo lỗi chi tiết từ Server
                        String errorBody = response.errorBody().string();
                        JSONObject jObjError = new JSONObject(errorBody);
                        String serverMessage = jObjError.getString("message");

                        if (response.code() == 404) {
                            Toast.makeText(ForgotPasswordActivity.this, "Email không tồn tại!", Toast.LENGTH_LONG).show();
                        } else if (response.code() == 403) {
                            // Hiển thị thông báo khóa: "Tài khoản đang bị khóa. Thử lại sau X phút"
                            Toast.makeText(ForgotPasswordActivity.this, serverMessage, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, serverMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ForgotPasswordActivity.this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}