package com.greaterjasol.complaints.network;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class Models {
    public static class RequestOtp { public String phone; }
    public static class VerifyOtp { public String phone; public String otp; }
    public static class TokenResponse { public String token; }
    public static class PresignResponse { public String uploadUrl; public String fileUrl; }
    public static class ComplaintCreate { public int ward_id; public String title; public String description; public java.util.List<String> photo_urls; }
    public static class ComplaintCreated { public String id; }
}

interface ApiService {
    @POST("auth/request-otp")
    Call<Void> requestOtp(@Body Models.RequestOtp body);

    @POST("auth/verify-otp")
    Call<Models.TokenResponse> verifyOtp(@Body Models.VerifyOtp body);

    @GET("uploads/presign")
    Call<Models.PresignResponse> presign(@Query("filename") String filename, @Query("filetype") String filetype);

    @POST("complaints")
    Call<Models.ComplaintCreated> createComplaint(@Body Models.ComplaintCreate body);
}
