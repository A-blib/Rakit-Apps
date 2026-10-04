package com.aris.templateapp.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Query Room tab Project dijalankan sungguhan di HP, dengan database di memori (hilang setelah test).
 * Jalankan: {@code ./gradlew connectedDebugAndroidTest} saat HP tersambung.
 */
@RunWith(AndroidJUnit4.class)
public class ProjectDaoTest {

    /** Menjalankan pekerjaan LiveData langsung di thread test. */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutor = new InstantTaskExecutorRule();

    private AppDatabase database;
    private ProjectDao dao;

    @Before
    public void createDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).allowMainThreadQueries().build();
        dao = database.projectDao();
        dao.insertAll(List.of(
                project("1", "Toko Kue Bu Ani", ProjectStatus.DRAFT, 100, 500),
                project("2", "SMK Negeri 1", ProjectStatus.EXPORTED, 200, 400),
                project("3", "apotek sehat", ProjectStatus.READY, 300, 300),
                project("4", "Diskon 50% Toko", ProjectStatus.DRAFT, 400, 200)));
    }

    @After
    public void closeDatabase() {
        database.close();
    }

    @Test
    public void sortOrders() throws Exception {
        assertEquals(List.of("1", "2", "3", "4"), ids(dao.observeByUpdated(null, null)));
        assertEquals(List.of("4", "3", "2", "1"), ids(dao.observeByCreated(null, null)));
        // Nama A–Z tanpa membedakan huruf besar/kecil: "apotek" sebelum "Diskon".
        assertEquals(List.of("3", "4", "2", "1"), ids(dao.observeByName(null, null)));
    }

    @Test
    public void statusAndSearchFilter() throws Exception {
        assertEquals(List.of("1", "4"), ids(dao.observeByUpdated(ProjectStatus.DRAFT, null)));
        assertEquals(List.of("1", "4"), ids(dao.observeByUpdated(null, "%toko%")));
        // % dari user sudah di-escape oleh ProjectRepository: hanya "Diskon 50% Toko" yang cocok.
        assertEquals(List.of("4"), ids(dao.observeByUpdated(null, "%50\\%%")));
        assertEquals(List.of(), ids(dao.observeByUpdated(ProjectStatus.READY, "%toko%")));
    }

    @Test
    public void countsFollowSearchButNotStatus() throws Exception {
        ProjectCounts all = value(dao.observeCounts(null));
        assertEquals(4, all.total);
        assertEquals(2, all.draft);
        assertEquals(1, all.ready);
        assertEquals(1, all.exported);

        ProjectCounts toko = value(dao.observeCounts("%toko%"));
        assertEquals(2, toko.total);
        assertEquals(2, toko.draft);
        assertEquals(0, toko.exported);
    }

    @Test
    public void renameDeleteAndSamples() throws Exception {
        dao.rename("1", "Toko Roti");
        assertEquals("Toko Roti", dao.findById("1").name);
        // Ganti nama tidak mengubah waktu edit.
        assertEquals(500, dao.findById("1").updatedAt);

        dao.delete("2");
        assertNull(dao.findById("2"));

        ProjectEntity sample = project("9", "Contoh", ProjectStatus.READY, 0, 0);
        sample.sample = true;
        dao.insert(sample);
        assertEquals(1, dao.deleteSamples());
        assertEquals(3, value(dao.observeCounts(null)).total);
    }

    @Test
    public void recentIsLimited() throws Exception {
        assertEquals(List.of("1", "2"), ids(dao.observeRecent(2)));
    }

    private static ProjectEntity project(String id, String name, ProjectStatus status, long createdAt, long updatedAt) {
        return new ProjectEntity(id, name, ProjectMode.TEMPLATE, status, createdAt, updatedAt);
    }

    private static List<String> ids(LiveData<List<ProjectEntity>> live) throws InterruptedException {
        List<String> ids = new ArrayList<>();
        for (ProjectEntity project : value(live)) {
            ids.add(project.id);
        }
        return ids;
    }

    /** Menunggu nilai pertama LiveData (Room mengisinya di thread latar). */
    private static <T> T value(LiveData<T> live) throws InterruptedException {
        AtomicReference<T> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        live.observeForever(new androidx.lifecycle.Observer<>() {
            @Override
            public void onChanged(T value) {
                result.set(value);
                latch.countDown();
                live.removeObserver(this);
            }
        });
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new AssertionError("LiveData tidak memberi nilai");
        }
        return result.get();
    }
}
