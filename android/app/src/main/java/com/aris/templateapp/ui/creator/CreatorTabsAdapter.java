package com.aris.templateapp.ui.creator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/** Isi 4 tab Dashboard Pembuat Website untuk ViewPager2 (tombol + bukan tab, jadi tidak punya halaman). */
class CreatorTabsAdapter extends FragmentStateAdapter {

    CreatorTabsAdapter(@NonNull Fragment shell) {
        super(shell.getChildFragmentManager(), shell.getViewLifecycleOwner().getLifecycle());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new ProjectsFragment();
            case 2:
                return new GalleryFragment();
            case 3:
                return new CreatorProfileFragment();
            case 0:
            default:
                return new CreatorHomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return CreatorDashboardFragment.TABS.length;
    }
}
