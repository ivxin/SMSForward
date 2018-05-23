package ivxin.smsforward.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ivxin.smsforward.Constants;

/**
 * Created by yaping.wang on 2017/9/18.
 */

public class DBconnHelper extends SQLiteOpenHelper {
    public DBconnHelper(Context context) {
        super(context, Constants.DB_FILE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table sms("
                + "id integer primary key autoincrement,"
                + "received_time varchar(30),"
                + "sender_address varchar(20),"
                + "content varchar(3000),"
                + "receiver varchar(20),"
                + "is_forwarded boolean,"
                + "is_star varchar(10),"
                + "receiver_email varchar(50))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                db.execSQL("alter table sms add column is_star varchar(10);");
                db.execSQL("alter table sms add column receiver_email varchar(50);");
                break;
            case 2:
                db.execSQL("alter table sms add column receiver_email varchar(50);");
                break;
                default:
        }
    }
}
