package com.example.studybuddy.data;

import com.example.studybuddy.model.GeminiRequest;
import com.example.studybuddy.model.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface NetworkService {

    @POST("v1beta/models/gemini-3.1-flash-lite:generateContent")
    Call<GeminiResponse> generateContent(
            @Header("x-goog-api-key") String apiKey,
            @Body GeminiRequest request
    );
}