package com.aris.templateapp.debug;

import com.aris.templateapp.ui.settings.DebugMenu;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/** Mengisi {@code Optional<DebugMenu>} di build debug. File ini tidak ada di build release. */
@Module
@InstallIn(SingletonComponent.class)
public abstract class DebugBindings {

    @Binds
    abstract DebugMenu debugMenu(SampleProjectsDebugMenu menu);
}
