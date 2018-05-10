package rf.androidovshchik.dosmthgreat.others;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.activities.MainActivity;
import rf.androidovshchik.dosmthgreat.adapters.YearAdapter;

public class CalendarFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.swipe)
    public SwipeRefreshLayout swipe;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private int year;

    private Unbinder unbinder;

    public static CalendarFragment newInstance(int year) {
        CalendarFragment fragment = new CalendarFragment();
        fragment.year = year;
        return fragment;
    }

    @Override
    @SuppressWarnings("all")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        swipe.setOnRefreshListener(this);
        swipe.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecorator(ContextCompat
            .getDrawable(getApplicationContext(), R.drawable.divider)));
        recyclerView.setAdapter(new YearAdapter(getApplicationContext(), year));
        return view;
    }

    @Override
    public void onRefresh() {
        getMainActivity().onSelectedPage(true, true, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @SuppressWarnings("all")
    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @SuppressWarnings("all")
    protected Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }
}