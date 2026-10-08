package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.GalleryPageDto;
import com.aris.templateapp.data.remote.dto.GalleryTemplateDetailDto;
import com.aris.templateapp.data.remote.dto.TemplateEventDto;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/** Galeri Template untuk pembuat website. Semuanya boleh dipanggil tamu. */
public interface GalleryApi {

    /**
     * @param category null/"all" = semua kategori
     * @param query    null = tanpa pencarian (Retrofit tidak mengirim parameter yang null)
     * @param sort     "popular" atau "newest"
     */
    @GET("templates")
    Call<GalleryPageDto> templates(@Query("category") String category, @Query("q") String query,
                                   @Query("sort") String sort, @Query("page") int page, @Query("size") int size);

    @POST("templates/{id}/events")
    Call<Void> recordEvent(@Path("id") String templateId, @Body TemplateEventDto body);

    /** Detail untuk layar Unduh (alur-buat-website-via-template.md bagian 5). */
    @GET("templates/{id}")
    Call<GalleryTemplateDetailDto> detail(@Path("id") String templateId);

    /** Paket ZIP template. @Streaming: isi dibaca sedikit demi sedikit, tidak dimuat utuh ke memori. */
    @Streaming
    @GET("templates/{id}/package")
    Call<ResponseBody> templatePackage(@Path("id") String templateId);
}
