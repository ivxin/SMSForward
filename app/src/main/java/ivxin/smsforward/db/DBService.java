package ivxin.smsforward.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ivxin.smsforward.entity.SMSEntity;
import ivxin.smsforward.utils.StringUtils;

/**
 * Created by yaping.wang on 2017/9/18.
 */

public class DBService {
    DBconnHelper dbh;

    public DBService(Context context) {
        dbh = new DBconnHelper(context);
    }

    public List<SMSEntity> selectAllSMS() {
        List<SMSEntity> list = new ArrayList<SMSEntity>();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("select * from sms order by id desc", null);
        while (c.moveToNext()) {
            SMSEntity sms = new SMSEntity();
            sms.setId(c.getString(c.getColumnIndex("id")));
            sms.setReceivedTime(c.getLong(c.getColumnIndex("received_time")));
            sms.setSender(StringUtils.decryptBASE64(c.getString(c.getColumnIndex("sender_address"))));
            sms.setContent(StringUtils.decryptBASE64(c.getString(c.getColumnIndex("content"))));
            sms.setReceiver(StringUtils.decryptBASE64(c.getString(c.getColumnIndex("receiver"))));
            sms.setForwarded(Boolean.parseBoolean(c.getString(c.getColumnIndex("is_forwarded"))));
            list.add(sms);
        }
        db.close();
        return list;
    }

    public List<SMSEntity> selectTransedSMS() {
        List<SMSEntity> list = new ArrayList<SMSEntity>();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("select * from sms order by id desc", null);
        while (c.moveToNext()) {
            SMSEntity sms = new SMSEntity();
            sms.setId(c.getString(c.getColumnIndex("id")));
            sms.setReceivedTime(c.getLong(c.getColumnIndex("received_time")));
            sms.setSender(StringUtils.decryptBASE64(c.getString(c.getColumnIndex("sender_address"))));
            sms.setContent(StringUtils.decryptBASE64(c.getString(c.getColumnIndex("content"))));
            sms.setReceiver(StringUtils.decryptBASE64(c.getString(c.getColumnIndex("receiver"))));
            boolean isSent = Boolean.parseBoolean(c.getString(c.getColumnIndex("is_forwarded")));
            sms.setForwarded(isSent);
            if (isSent) {
                list.add(sms);
            }
        }
        db.close();
        return list;
    }

    public void insertSMS(SMSEntity sms) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        String receivedTime = sms.getReceivedTime() + "";
        String sender = StringUtils.encryptBASE64(sms.getSender());
        String content = StringUtils.encryptBASE64(sms.getContent());
        String receiver = StringUtils.encryptBASE64(sms.getReceiver());
        boolean isForwarded = sms.isForwarded();
        db.execSQL("insert into sms (received_time,sender_address,content,receiver,is_forwarded)values(?,?,?,?,?)",
                new String[] { receivedTime, sender, content, receiver, isForwarded + "" });
        db.close();
    }

    public void deleteSMSbyID(SMSEntity sms) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        String id = sms.getId();
        db.execSQL("delete from sms where id=?", new String[] { id });
        db.close();
    }

    private List<SMSEntity> selectAllOrgSMS() {
        List<SMSEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("select * from sms order by id desc", null);
        while (c.moveToNext()) {
            SMSEntity sms = new SMSEntity();
            sms.setId(c.getString(c.getColumnIndex("id")));
            sms.setReceivedTime(c.getLong(c.getColumnIndex("received_time")));
            sms.setSender(c.getString(c.getColumnIndex("sender_address")));
            sms.setContent(c.getString(c.getColumnIndex("content")));
            sms.setReceiver(c.getString(c.getColumnIndex("receiver")));
            sms.setForwarded(Boolean.parseBoolean(c.getString(c.getColumnIndex("is_forwarded"))));
            list.add(sms);
        }
        db.close();
        return list;
    }

}
