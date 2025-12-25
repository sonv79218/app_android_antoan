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
import android.util.Log;
import com.example.ebook.api.ApiClient;

import org.json.JSONObject; // Cần thiết để bóc tách JSON lỗi

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
        setContentView(R.layout.activity_otp_verification);

        email = getIntent().getStringExtra("email");

        edtOTPCode = findViewById(R.id.edtOTPCode);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        tvDescriptionOTP = findViewById(R.id.tvDescriptionOTP);
        tvResendOTP = findViewById(R.id.tvResendOTP);

        if (email != null) {
            tvDescriptionOTP.setText("Mã xác thực đã được gửi đến:\n" + email);
        }

        authApi = ApiClient.getClient().create(AuthApi.class);

        btnVerifyOTP.setOnClickListener(v -> {
            String otp = edtOTPCode.getText().toString().trim();
            if (otp.length() < 6) {
                edtOTPCode.setError("Vui lòng nhập đủ 6 chữ số");
            } else {
                handleVerifyOTP(email, otp);
            }
        });

        tvResendOTP.setOnClickListener(v -> {
            // Bạn có thể thêm logic gọi authApi.forgotPassword tại đây nếu muốn gửi lại mã
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
                    Intent intent = new Intent(OTPVerificationActivity.this, ChangePassword2Activity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    // XỬ LÝ LỖI TỪ SERVER TRẢ VỀ
                    try {
                        // Bóc tách JSON từ errorBody (Ví dụ: {"message": "..."})
                        String errorBody = response.errorBody().string();
                        JSONObject jObjError = new JSONObject(errorBody);
                        String message = jObjError.getString("message");

                        // Hiển thị message trực tiếp từ server (đã bao gồm số lần còn lại hoặc thông báo khóa)
                        Toast.makeText(OTPVerificationActivity.this, message, Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        // Phòng hờ nếu server trả về lỗi không phải định dạng JSON
                        Toast.makeText(OTPVerificationActivity.this, "Mã OTP không chính xác hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                    }
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