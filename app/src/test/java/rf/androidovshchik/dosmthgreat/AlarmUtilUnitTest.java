package rf.androidovshchik.dosmthgreat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import rf.androidovshchik.dosmthgreat.models.Task;
import rf.androidovshchik.dosmthgreat.utils.AlarmUtil;

import static org.junit.Assert.*;

public class AlarmUtilUnitTest {

    private static final SimpleDateFormat TEST_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

    @Test
    @SuppressWarnings("all")
    public void fixedDate() {
        Date date0 = AlarmUtil.fixedDate("00:00", "Mon");
        System.out.println("date0: " + date0.getTime());
        assertTrue(date0.getTime() == 0L);
        Date date24 = AlarmUtil.fixedDate("24:00", "Sun");
        System.out.println("date24: " + date24.getTime());
        assertTrue(date24.getTime() == AlarmUtil.MAX_DELAY);
    }

    @Test
    public void nextDelay() {
        // 2018-05-11 is Friday
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(newFakeTask(null, "12:00"));
        assertEquals(AlarmUtil.delay(null, tasks, getFakeCalendar("2018-05-11 15:00")),
            21 * AlarmUtil.HOUR);
        tasks.clear();
        tasks.add(newFakeTask("Fri", "12:00"));
        assertEquals(AlarmUtil.delay(null, tasks, getFakeCalendar("2018-05-11 15:00")),
            6 * AlarmUtil.HOURS_24 + 21 * AlarmUtil.HOUR);
        tasks.clear();
    }

    private Task newFakeTask(@Nullable String day, @NonNull String time) {
        Task task = new Task();
        task.day = day;
        task.time = time;
        return task;
    }

    private Calendar getFakeCalendar(String dateTime) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(TEST_FORMAT.parse(dateTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }
}
