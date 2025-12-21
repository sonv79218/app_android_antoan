package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook.R;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.AuthApi;
import com.example.ebook.model.ResetPasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword2Activity extends AppCompatActivity {

    private EditText edtNewPassword, edtConfirmPassword;
    private Button btnUpdatePassword;
    private String email;
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password2);

        // 1. Lấy email được truyền từ màn hình OTP sang
        email = getIntent().getStringExtra("email");

        // 2. Ánh xạ View
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Khởi tạo API (Dùng getClient() không cần token)
        authApi = ApiClient.getClient().create(AuthApi.class);

        // 3. Xử lý nút bấm
        btnUpdatePassword.setOnClickListener(v -> handleResetPassword());
    }

    private void handleResetPassword() {
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra hợp lệ
        if (newPass.isEmpty() || newPass.length() < 6) {
            edtNewPassword.setError("Mật khẩu phải từ 6 ký tự");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        // Gửi API Reset Password
        ResetPasswordRequest request = new ResetPasswordRequest(email, newPass);

        btnUpdatePassword.setEnabled(false); // Chống nhấn nhiều lần

        authApi.resetPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnUpdatePassword.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePassword2Activity.this,
                            "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();

                    // Quay về màn hình Đăng nhập
                    Intent intent = new Intent(ChangePassword2Activity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ChangePassword2Activity.this, "Lỗi: Không thể đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnUpdatePassword.setEnabled(true);
                Toast.makeText(ChangePassword2Activity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

//test xem push đc chưa