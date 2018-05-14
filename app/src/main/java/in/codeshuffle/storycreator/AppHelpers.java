package in.codeshuffle.storycreator;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by stpl on 12/4/18.
 */

public class AppHelpers {

    public static int offX;
    public static int offY;

    public static void getToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
