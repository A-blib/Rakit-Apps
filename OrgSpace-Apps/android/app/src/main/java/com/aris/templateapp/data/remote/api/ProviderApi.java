package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.ProviderDashboardDto;
import com.aris.templateapp.data.remote.dto.ProviderProfileDto;
import com.aris.templateapp.data.remote.dto.ProviderProfileUpdateDto;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Endpoint Dashboard Provider /providers/me/* (butuh access token & profil provider aktif). */
public interface ProviderApi {

    /** @param period "7d" atau "30d" */
    @GET("providers/me/dashboard")
    Call<ProviderDashboardDto> dashboard(@Query("period") String period);

    /** @param query null = tanpa pencarian (Retrofit tidak mengirim parameter yang null) */
    @GET("providers/me/templates")
    Call<TemplateListDto> templates(@Query("status") String status, @Query("category") String category,
                                    @Query("sort") String sort, @Query("q") String query, @Query("page") int page);

    @GET("providers/me/templates/{id}")
    Call<TemplateDetailDto> template(@Path("id") String id);

    @GET("providers/me/profile")
    Call<ProviderProfileDto> profile();

    @PATCH("providers/me/profile")
    Call<ProviderProfileDto> updateProfile(@Body ProviderProfileUpdateDto body);
}
