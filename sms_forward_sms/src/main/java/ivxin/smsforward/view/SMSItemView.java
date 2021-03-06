package ivxin.smsforward.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import ivxin.smsforward.Constants;
import ivxin.smsforward.R;
import ivxin.smsforward.db.DBService;
import ivxin.smsforward.entity.SMSEntity;
import ivxin.smsforward.utils.StringUtils;

/**
 * Created by yaping.wang on 2017/9/15.
 */

public class SMSItemView extends MyAdapterItemLayout<SMSEntity> {
    private Context mContext;
    private SMSEntity data;
    private TextView tv_sender;
    private TextView tv_content;
    private TextView tv_time;
    private CheckBox cb_is_forwarded;
    private CheckBox cb_is_star;

    public SMSItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public SMSItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SMSItemView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        View.inflate(context, R.layout.item_sms, this);
        tv_sender = findViewById(R.id.tv_sender);
        tv_content = findViewById(R.id.tv_content);
        tv_time = findViewById(R.id.tv_time);
        cb_is_forwarded = findViewById(R.id.cb_is_forwarded);
        cb_is_star = findViewById(R.id.cb_is_star);
        cb_is_star.setOnClickListener(v -> {
            DBService dbs = new DBService(mContext);
            dbs.starSMS(data, cb_is_star.isChecked() ? "1" : "0");
        });
    }

    @Override
    public void setData(SMSEntity bean, int position, ViewGroup parent) {
        data = bean;
        tv_sender.setText(bean.getSender());
        tv_content.setText(bean.getContent());
        tv_time.setText(StringUtils.getDateFomated(Constants.PATTERN, bean.getReceivedTime()));
        cb_is_forwarded.setChecked(bean.isForwarded());
        cb_is_star.setChecked("1".equals(bean.isStar()));
    }
}
