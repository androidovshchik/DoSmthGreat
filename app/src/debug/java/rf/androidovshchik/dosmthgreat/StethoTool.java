package rf.androidovshchik.dosmthgreat;

import android.content.Context;

import com.facebook.stetho.Stetho;

public class StethoTool {

	public static void init(Context context) {
		Stetho.initializeWithDefaults(context);
	}
}