package rf.androidovshchik.dosmthgreat.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Calendar;

import rf.androidovshchik.dosmthgreat.others.CalendarFragment;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;

public class PagerAdapter extends FragmentStatePagerAdapter {

    public final ArrayList<CalendarFragment> fragments = new ArrayList<>();

    public PagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        int yearX = AppUtil.getYearX(context), yearNow = Calendar.getInstance()
            .get(Calendar.YEAR);
        while (yearX <= yearNow) {
            fragments.add(CalendarFragment.newInstance(yearX));
            yearX++;
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}