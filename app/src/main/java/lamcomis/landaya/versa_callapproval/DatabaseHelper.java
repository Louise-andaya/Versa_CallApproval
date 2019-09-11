package lamcomis.landaya.versa_callapproval;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by landaya on 5/21/2019.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ADMINDATABASE";
    public static final String ADMIN_TABLE = "ADMIN";
    public static final String ADMIN_NAME_COL = "ADMIN_NAME";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ADMIN_TABLE + "('" + ADMIN_NAME_COL + "' TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ADMIN_TABLE);
        onCreate(db);
    }

    public boolean insertAdminData(String admin_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ADMIN_NAME_COL, admin_name);
        long result = db.insert(ADMIN_TABLE, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }
    public Cursor getLastAminLogin() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT ADMIN_NAME FROM " + ADMIN_TABLE, null);

        return data;
    }
    public void deleteAdmin()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ ADMIN_TABLE);
        db.close();
    }
}
