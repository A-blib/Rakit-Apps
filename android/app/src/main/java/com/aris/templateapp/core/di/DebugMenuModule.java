package com.aris.templateapp.core.di;

import com.aris.templateapp.ui.settings.DebugMenu;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Menyatakan bahwa {@link DebugMenu} BOLEH tidak ada. Build debug menyediakannya lewat module di
 * {@code src/debug/}; build release tidak, sehingga {@code Optional<DebugMenu>} di Pengaturan kosong.
 */
@Module
@InstallIn(SingletonComponent.class)
public abstract class DebugMenuModule {

    @BindsOptionalOf
    abstract DebugMenu debugMenu();
}
