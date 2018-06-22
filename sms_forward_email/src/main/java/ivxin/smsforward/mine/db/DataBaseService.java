package ivxin.smsforward.mine.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ivxin.smsforward.mine.Constants;
import ivxin.smsforward.mine.entity.MailEntity;

public class DataBaseService {
    private Context context;

    public DataBaseService(Context context) {
        this.context = context;
    }

    public void insertMail(MailEntity mailEntity) {
        DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
        SQLiteDatabase db = dataBaseOpenHelper.getWritableDatabase();
        db.execSQL("insert into email (send_time,subject,content,receiver)values(?,?,?,?)",
                new String[]{String.valueOf(mailEntity.getSendTime()), mailEntity.getSubject(), mailEntity.getContent(), mailEntity.getReceiver()});
        db.close();
    }

    public void deleteMail(MailEntity mailEntity) {
        DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
        SQLiteDatabase db = dataBaseOpenHelper.getWritableDatabase();
        String id = String.valueOf(mailEntity.getId());
        db.execSQL("delete from email where id=?", new String[]{id});
        db.close();
    }

    public void deleteAllMail() {
        DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
        SQLiteDatabase db = dataBaseOpenHelper.getWritableDatabase();
        db.execSQL("delete from email where id>0");
        db.close();
    }

    public void updateMail(MailEntity mailEntity) {
        DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
        SQLiteDatabase db = dataBaseOpenHelper.getWritableDatabase();
        String id = String.valueOf(mailEntity.getId());
        db.execSQL("update email set send_time=?,subject=?,content=?,receiver=? where id=?",
                new String[]{String.valueOf(mailEntity.getSendTime()), mailEntity.getSubject(), mailEntity.getContent(), mailEntity.getReceiver(), id});
        db.close();
    }

    public List<MailEntity> selectMailList() {
        List<MailEntity> list = new ArrayList<>();
        DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
        SQLiteDatabase db = dataBaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from email order by id desc", null);
        while (c.moveToNext()) {
            MailEntity mailEntity = new MailEntity();
            mailEntity.setId(c.getInt(c.getColumnIndex("id")));
            mailEntity.setSendTime(c.getLong(c.getColumnIndex("send_time")));
            mailEntity.setSubject(c.getString(c.getColumnIndex("subject")));
            mailEntity.setContent(c.getString(c.getColumnIndex("content")));
            mailEntity.setReceiver(c.getString(c.getColumnIndex("receiver")));
            list.add(mailEntity);
        }
        c.close();
        db.close();
        return list;
    }

    public List<MailEntity> selectMailList(int pageIndex, int pageSize) {
        List<MailEntity> list = new ArrayList<>();
        DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
        SQLiteDatabase db = dataBaseOpenHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from email order by id desc limit ? offset ?",
                new String[]{String.valueOf(pageSize), String.valueOf(pageSize * pageIndex)});
        while (c.moveToNext()) {
            MailEntity mailEntity = new MailEntity();
            mailEntity.setId(c.getInt(c.getColumnIndex("id")));
            mailEntity.setSendTime(c.getLong(c.getColumnIndex("send_time")));
            mailEntity.setSubject(c.getString(c.getColumnIndex("subject")));
            mailEntity.setContent(c.getString(c.getColumnIndex("content")));
            mailEntity.setReceiver(c.getString(c.getColumnIndex("receiver")));
            list.add(mailEntity);
        }
        c.close();
        db.close();
        return list;
    }


    public class DataBaseOpenHelper extends SQLiteOpenHelper {
        private DataBaseOpenHelper(Context context) {
            super(context, Constants.DB_FILE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table email("
                    + "id integer primary key autoincrement,"
                    + "send_time varchar(30),"
                    + "subject varchar(20),"
                    + "content varchar(3000),"
                    + "receiver varchar(20))"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
