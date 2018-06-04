package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import android.content.SharedPreferences;
import android.widget.SimpleCursorAdapter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_MULTI_PROCESS;


/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 *
 * Please read:
 *
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 *
 * before you start to get yourself familiarized with ContentProvider.
 *
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 *
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    public static String GGROUPMESSENGER = "gGroupMessenger";
    public static String KEY = "key";
    public static String VALUE = "value";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    /** References -
     * https://developer.android.com/training/basics/data-storage/shared-preferences.html
     * http://stackoverflow.com/questions/7528470/how-to-access-getsharedpreferences-from-another-class
     */

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         *
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        Log.v("GMProvider-insert",values.toString());

//        Log.v("key",values.get("key").toString());
//        Log.v("Value",values.get("value").toString());
        SharedPreferences.Editor editor = this.getContext().getSharedPreferences(GGROUPMESSENGER,MODE_MULTI_PROCESS).edit();
        editor.putString(values.get(KEY).toString(),values.get(VALUE).toString());
        editor.commit();
//        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.

        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    /** References -
     * https://developer.android.com/training/basics/data-storage/shared-preferences.html
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         * http://stackoverflow.com/questions/7528470/how-to-access-getsharedpreferences-from-another-class
         */

        //int defaultValue = getResources().getInteger(R.string.saved_high_score_default);
        String returnedValue = this.getContext().getSharedPreferences(GGROUPMESSENGER,MODE_MULTI_PROCESS).getString(selection,"");
        // Log.v("query key",selection);
        // Log.v("query value",returnedValue);
        String[] columns = new String[] {KEY, VALUE };
        MatrixCursor matrixCursor= new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] {selection,returnedValue});
        // Log.v("query", selection);
        return matrixCursor;
    }
}
