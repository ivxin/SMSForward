package ivxin.smsforward.mine.view;

import android.support.annotation.Nullable;
import android.text.Html;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ivxin.smsforward.mine.Constants;
import ivxin.smsforward.mine.R;
import ivxin.smsforward.mine.entity.MailEntity;

public class EmailAdapter extends BaseQuickAdapter<MailEntity, BaseViewHolder> {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.PATTERN, Locale.CHINA);

    public EmailAdapter(int layoutResId, @Nullable List<MailEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MailEntity item) {
        helper.setText(R.id.tv_receiver, item.getReceiver());
        helper.setText(R.id.tv_send_time, simpleDateFormat.format(item.getSendTime()));
        helper.setText(R.id.tv_subject, item.getSubject());
        helper.setText(R.id.tv_content, Html.fromHtml(item.getContent().replaceAll("\n", "<br>").replaceAll("\r", "<br>").trim()));
    }
}
