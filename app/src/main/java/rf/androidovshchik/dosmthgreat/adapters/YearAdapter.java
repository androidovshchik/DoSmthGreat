package rf.androidovshchik.dosmthgreat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import rf.androidovshchik.dosmthgreat.R;
import rf.androidovshchik.dosmthgreat.month.HighlightStyle;
import rf.androidovshchik.dosmthgreat.month.MonthView;
import rf.androidovshchik.dosmthgreat.utils.AppUtil;

public class YearAdapter extends RecyclerView.Adapter<YearAdapter.ViewHolder> {

    private static final Calendar today = Calendar.getInstance();

    private final Date dateX;

    private int year;

    public YearAdapter(Context context, int year) {
        this.year = year;
        this.dateX = AppUtil.getDateX(context);
    }

    @Override
    public int getItemCount() {
        return 12;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int month) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month,
            parent, false);
        return new ViewHolder(itemView, month);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date now = today.getTime();
        Calendar calendar = holder.monthView.getCalendar();
        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 1; d <= days; d++) {
            calendar.set(Calendar.DAY_OF_MONTH, d);
            Date time = calendar.getTime();
            if (time.after(dateX) && time.before(now)) {
                holder.monthView.setHighlight(d, HighlightStyle.SOLID_CIRCLE);
            } else if (year == today.get(Calendar.YEAR) && d == today.get(Calendar.DAY_OF_MONTH) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                holder.monthView.setHighlight(d, HighlightStyle.TOP_SEMICIRCLE);
            } else {
                holder.monthView.setHighlight(d, HighlightStyle.NO_HIGHLIGHT);
            }
        }
        holder.monthView.invalidate();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.itemMonth)
        TextView itemMonthView;
        @BindView(R.id.itemYear)
        TextView itemYearView;
        @BindView(R.id.month)
        MonthView monthView;

        @SuppressWarnings("all")
        public ViewHolder(@NonNull View itemView, int month) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemMonthView.setText(String.format("%02d", month + 1));
            itemYearView.setText(String.valueOf(year));
            monthView.setMode(MonthView.Mode.DISPLAY_ONLY);
            monthView.setFirstDayOfWeek(Calendar.MONDAY);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            monthView.setCalendar(calendar);
            SparseArray<HighlightStyle> array = new SparseArray<>();
            int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int d = 1; d <= days; d++) {
                array.append(d, HighlightStyle.NO_HIGHLIGHT);
            }
            monthView.setDayStyleArray(array);
        }

        @SuppressWarnings("unused")
        public Context getApplicationContext() {
            return itemView.getContext().getApplicationContext();
        }

        @SuppressWarnings("unused")
        public String getString(@StringRes int id, Object... params) {
            return itemView.getContext().getString(id, params);
        }
    }
}
