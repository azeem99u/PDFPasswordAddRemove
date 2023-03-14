package stkmakers.pdfpasswordaddRemove;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import stkmakers.pdfpasswordaddRemove.ui.fragments.LockedFragment;
import stkmakers.pdfpasswordaddRemove.ui.fragments.UnlockedFragment;

public class StatusViewPagerAdapter extends FragmentStateAdapter {

    public StatusViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 0:{
                return new LockedFragment();
            }
            case 1:{
                return new UnlockedFragment();
            }
            default:{
                return null;
            }
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }



}
