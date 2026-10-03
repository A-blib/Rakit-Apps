package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.UserDto;

import retrofit2.Call;
import retrofit2.http.GET;

/** Endpoint /users/me/* (butuh access token; dipasang otomatis oleh AuthInterceptor). */
public interface UserApi {

    @GET("users/me")
    Call<UserDto> me();
}
