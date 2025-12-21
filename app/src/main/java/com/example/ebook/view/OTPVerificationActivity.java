package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ebook.R;
import com.example.ebook.model.VerifyOtpRequest;
import com.example.ebook.api.AuthApi;
import com.example.ebook.model.ResetPasswordRequest;
import android.util.Log;
import com.example.ebook.api.ApiClient;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OTPVerificationActivity extends AppCompatActivity {

    private EditText edtOTPCode;
    private Button btnVerifyOTP;
    private TextView tvDescriptionOTP, tvResendOTP;
    private String email;
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification); // Thay bằng tên file xml của bạn

        // 1. Lấy email từ Intent gửi sang
        email = getIntent().getStringExtra("email");

        // 2. Ánh xạ view
        edtOTPCode = findViewById(R.id.edtOTPCode);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        tvDescriptionOTP = findViewById(R.id.tvDescriptionOTP);
        tvResendOTP = findViewById(R.id.tvResendOTP);

        if (email != null) {
            tvDescriptionOTP.setText("Mã xác thực đã được gửi đến:\n" + email);
        }

        // Khởi tạo API (Giả sử bạn đã có lớp RetrofitClient)
        authApi = ApiClient.getClient().create(AuthApi.class);

        // 3. Xử lý sự kiện nút Xác thực
        btnVerifyOTP.setOnClickListener(v -> {
            String otp = edtOTPCode.getText().toString().trim();
            if (otp.length() < 6) {
                edtOTPCode.setError("Vui lòng nhập đủ 6 chữ số");
            } else {
                handleVerifyOTP(email, otp);
            }
        });

        // 4. Xử lý gửi lại mã (Nếu cần)
        tvResendOTP.setOnClickListener(v -> {
            // Gọi lại API forgotPassword tại đây để gửi lại mã
            Toast.makeText(this, "Đang gửi lại mã...", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleVerifyOTP(String email, String otp) {
        VerifyOtpRequest request = new VerifyOtpRequest(email, otp);

        authApi.verifyOtp(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OTPVerificationActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình Reset Password
                    Intent intent = new Intent(OTPVerificationActivity.this, ChangePassword2Activity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    // Thường là lỗi 400 do sai OTP
                    Toast.makeText(OTPVerificationActivity.this, "Mã OTP không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(OTPVerificationActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}