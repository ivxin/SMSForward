package ivxin.smsforward.lib.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.Locale;

public class ContactsUtil {

    public static String getDisplayNameByNumber(Context context, String number) {
        String[] projection = new String[]{android.provider.ContactsContract.Contacts.DISPLAY_NAME};
        String selection = String.format(Locale.CHINA, "where %s=%s", ContactsContract.CommonDataKinds.Phone.NUMBER, number);
        String displayName = null;
        Cursor cursor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(number).build();
            cursor = resolver.query(uri, projection, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndexName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                displayName = cursor.getString(columnIndexName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return displayName;
    }
}
