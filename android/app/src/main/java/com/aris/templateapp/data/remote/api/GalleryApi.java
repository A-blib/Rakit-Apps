package com.aris.templateapp.data.remote.api;

import com.aris.templateapp.data.remote.dto.GalleryPageDto;
import com.aris.templateapp.data.remote.dto.TemplateEventDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Galeri Template untuk pembuat website. Keduanya boleh dipanggil tamu. */
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
}
