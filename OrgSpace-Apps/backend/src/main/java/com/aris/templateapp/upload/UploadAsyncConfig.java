package com.aris.templateapp.upload;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.upload.check.TemplateChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Pengecekan upload berjalan di thread pool kecil sendiri (bukan thread request), dan pembersihan draft berjalan
 * terjadwal tiap malam.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class UploadAsyncConfig {

    public static final String CHECK_EXECUTOR = "uploadCheckExecutor";

    @Bean(name = CHECK_EXECUTOR)
    public TaskExecutor uploadCheckExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Pengecekan memakan memori (isi ZIP dibaca ke memori), jadi dibatasi 2 sekaligus; sisanya antre.
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("cek-template-");
        executor.initialize();
        return executor;
    }

    @Bean
    public TemplateChecker templateChecker(AppProperties properties) {
        return new TemplateChecker(properties.upload());
    }
}
