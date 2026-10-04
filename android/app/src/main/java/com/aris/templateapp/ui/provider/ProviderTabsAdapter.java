package com.aris.templateapp.ui.provider;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Isi 4 tab Dashboard Provider untuk ViewPager2. Urutan halaman = urutan menu bottom navigation
 * (lihat {@link ProviderDashboardFragment#TABS}).
 */
class ProviderTabsAdapter extends FragmentStateAdapter {

    ProviderTabsAdapter(@NonNull Fragment shell) {
        // Fragment anak dikelola childFragmentManager shell dan mengikuti siklus hidup VIEW shell,
        // sehingga tab ikut dibersihkan saat view shell dihancurkan (mis. membuka detail template).
        super(shell.getChildFragmentManager(), shell.getViewLifecycleOwner().getLifecycle());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new ProviderUploadFragment();
            case 2:
                return new ProviderTemplatesFragment();
            case 3:
                return new ProviderProfileFragment();
            case 0:
            default:
                return new ProviderHomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return ProviderDashboardFragment.TABS.length;
    }
}
