package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.AuthResponseDto;
import com.aris.templateapp.data.remote.dto.LoginRequestDto;
import com.aris.templateapp.data.remote.dto.RefreshTokenRequestDto;
import com.aris.templateapp.data.remote.dto.RegisterRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Endpoint /auth/* (publik, tanpa access token). Retrofit membuat implementasinya otomatis dari anotasi.
 * Path ditulis tanpa "/" di depan agar disambung ke API_BASE_URL (".../api/").
 */
public interface AuthApi {

    @POST("auth/register")
    Call<AuthResponseDto> register(@Body RegisterRequestDto body);

    @POST("auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto body);

    @POST("auth/refresh")
    Call<AuthResponseDto> refresh(@Body RefreshTokenRequestDto body);

    @POST("auth/logout")
    Call<Void> logout(@Body RefreshTokenRequestDto body);
}
