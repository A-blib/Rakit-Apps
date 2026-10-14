package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.FolderRequestDto;
import com.aris.templateapp.data.remote.dto.MoveTemplateDto;
import com.aris.templateapp.data.remote.dto.ProviderDashboardDto;
import com.aris.templateapp.data.remote.dto.ProviderProfileDto;
import com.aris.templateapp.data.remote.dto.ProviderProfileUpdateDto;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Endpoint Dashboard Provider /providers/me/* (butuh access token & profil provider aktif). */
public interface ProviderApi {

    /** @param period "7d" atau "30d" */
    @GET("providers/me/dashboard")
    Call<ProviderDashboardDto> dashboard(@Query("period") String period);

    /**
     * @param folder null = semua template · "root" = hanya yang tanpa folder · id folder
     * @param query  null = tanpa pencarian (Retrofit tidak mengirim parameter yang null)
     */
    @GET("providers/me/templates")
    Call<TemplateListDto> templates(@Query("folder") String folder, @Query("status") String status,
                                    @Query("category") String category, @Query("sort") String sort,
                                    @Query("q") String query, @Query("page") int page);

    @PUT("providers/me/templates/{id}/folder")
    Call<Void> moveTemplate(@Path("id") String id, @Body MoveTemplateDto body);

    @GET("providers/me/template-folders")
    Call<List<TemplateFolderDto>> folders();

    @POST("providers/me/template-folders")
    Call<TemplateFolderDto> createFolder(@Body FolderRequestDto body);

    @PATCH("providers/me/template-folders/{id}")
    Call<TemplateFolderDto> renameFolder(@Path("id") String id, @Body FolderRequestDto body);

    @DELETE("providers/me/template-folders/{id}")
    Call<Void> deleteFolder(@Path("id") String id);

    @GET("providers/me/templates/{id}")
    Call<TemplateDetailDto> template(@Path("id") String id);

    @GET("providers/me/profile")
    Call<ProviderProfileDto> profile();

    @PATCH("providers/me/profile")
    Call<ProviderProfileDto> updateProfile(@Body ProviderProfileUpdateDto body);
}
