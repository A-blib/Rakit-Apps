package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.CreateUploadSessionDto;
import com.aris.templateapp.data.remote.dto.DeviceWarningsDto;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.DraftInfoDto;
import com.aris.templateapp.data.remote.dto.DraftStepDto;
import com.aris.templateapp.data.remote.dto.HelpArticleDto;
import com.aris.templateapp.data.remote.dto.ReportIssueDto;
import com.aris.templateapp.data.remote.dto.UploadCheckDto;
import com.aris.templateapp.data.remote.dto.UploadOverviewDto;
import com.aris.templateapp.data.remote.dto.UploadSessionDto;
import com.aris.templateapp.data.remote.dto.UploadSettingsDto;
import com.aris.templateapp.data.remote.dto.UploadStartedDto;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/** Endpoint Upload provider (docs/rancangan/alur-fitur-upload.md) + artikel Panduan. */
public interface UploadApi {

    @GET("providers/me/uploads")
    Call<UploadOverviewDto> overview();

    @GET("providers/me/uploads/settings")
    Call<UploadSettingsDto> settings();

    @POST("providers/me/uploads/sessions")
    Call<UploadSessionDto> createSession(@Body CreateUploadSessionDto body);

    @GET("providers/me/uploads/sessions/{id}")
    Call<UploadSessionDto> session(@Path("id") String sessionId);

    /** Body = byte mentah satu potongan (Content-Type application/octet-stream). */
    @PUT("providers/me/uploads/sessions/{id}")
    Call<UploadSessionDto> appendChunk(@Path("id") String sessionId, @Query("offset") long offset, @Body RequestBody chunk);

    @POST("providers/me/uploads/sessions/{id}/complete")
    Call<UploadStartedDto> complete(@Path("id") String sessionId);

    @GET("providers/me/uploads/{id}/check")
    Call<UploadCheckDto> check(@Path("id") String templateId);

    @GET("providers/me/uploads/{id}")
    Call<DraftDto> draft(@Path("id") String templateId);

    @PATCH("providers/me/uploads/{id}/info")
    Call<DraftDto> updateInfo(@Path("id") String templateId, @Body DraftInfoDto body);

    @PATCH("providers/me/uploads/{id}/step")
    Call<DraftDto> updateStep(@Path("id") String templateId, @Body DraftStepDto body);

    /** @param source auto · section · custom; @param view mobile · desktop */
    @PUT("providers/me/uploads/{id}/thumbnail")
    Call<DraftDto> updateThumbnail(@Path("id") String templateId, @Query("source") String source,
                                   @Query("view") String view, @Body RequestBody image);

    /** ZIP template; dibaca sebagai stream agar tidak ditampung utuh di memori. */
    @Streaming
    @GET("providers/me/uploads/{id}/source")
    Call<ResponseBody> source(@Path("id") String templateId);

    @PUT("providers/me/uploads/{id}/device-warnings")
    Call<DraftDto> deviceWarnings(@Path("id") String templateId, @Body DeviceWarningsDto body);

    @DELETE("providers/me/uploads/{id}")
    Call<Void> delete(@Path("id") String templateId);

    @POST("providers/me/uploads/issues/{id}/report")
    Call<Void> report(@Path("id") String issueId, @Body ReportIssueDto body);

    @GET("help/articles/{code}")
    Call<HelpArticleDto> helpArticle(@Path("code") String code);
}
