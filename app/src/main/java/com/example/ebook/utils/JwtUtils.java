package com.example.ebook.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

public class JwtUtils {
    private static final String TAG = "JwtUtils";

    /**
     * Decode JWT token và lấy payload
     */
    public static JSONObject decodeJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.decode(parts[1], Base64.DEFAULT));
                return new JSONObject(payload);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decoding JWT: " + e.getMessage());
        }
        return null;
    }

    /**
     * Kiểm tra xem token có hết hạn chưa
     * @return true nếu token còn hợp lệ, false nếu đã hết hạn hoặc không hợp lệ
     */
    public static boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            JSONObject payload = decodeJWT(token);
            if (payload == null) {
                return false;
            }

            // Kiểm tra expiry (exp)
            if (payload.has("exp")) {
                long exp = payload.getLong("exp");
                long currentTime = System.currentTimeMillis() / 1000; // Convert to seconds
                
                // Thêm buffer 60 giây để tránh edge case
                if (exp < (currentTime + 60)) {
                    Log.d(TAG, "Token expired. Exp: " + exp + ", Current: " + currentTime);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy expiry time của token (milliseconds)
     */
    public static long getTokenExpiry(String token) {
        try {
            JSONObject payload = decodeJWT(token);
            if (payload != null && payload.has("exp")) {
                return payload.getLong("exp") * 1000; // Convert to milliseconds
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting token expiry: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Lấy user ID từ token
     */
    public static String getUserIdFromToken(String token) {
        try {
            JSONObject payload = decodeJWT(token);
            if (payload != null && payload.has("id")) {
                return payload.getString("id");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID from token: " + e.getMessage());
        }
        return null;
    }
}

