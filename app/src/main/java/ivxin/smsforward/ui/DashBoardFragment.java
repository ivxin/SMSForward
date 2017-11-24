package ivxin.smsforward.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ivxin.smsforward.R;
import ivxin.smsforward.base.BaseFragment;
import ivxin.smsforward.db.DBService;
import ivxin.smsforward.entity.SMSEntity;
import ivxin.smsforward.view.MyAdapter;
import ivxin.smsforward.view.SMSItemView;

/**
 * Created by yaping.wang on 2017/9/14.
 */

public class DashBoardFragment extends BaseFragment {
    private boolean isShowForwarded = false;
    private DBService dbService;
    private SwitchCompat switch_show_forwarded;
    private ListView lv_forwarded;
    private MyAdapter<SMSItemView, SMSEntity> adapter;
    private List<SMSEntity> smsEntityList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        dbService = new DBService(getContext());
        View view = inflater.inflate(R.layout.fragment_dashboard, null, false);
        switch_show_forwarded = view.findViewById(R.id.switch_show_forwarded);
        lv_forwarded = view.findViewById(R.id.lv_forwarded);
        switch_show_forwarded.setChecked(isShowForwarded);
        switch_show_forwarded.setOnCheckedChangeListener(onCheckedChangeListener);
        adapter = new MyAdapter<>(getContext(), smsEntityList, SMSItemView.class, SMSEntity.class);
        lv_forwarded.setAdapter(adapter);
        lv_forwarded.setOnItemClickListener(onItemClickListener);
        lv_forwarded.setOnItemLongClickListener(onItemLongClickListener);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_item);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setInterpolator(new AccelerateDecelerateInterpolator());
        lv_forwarded.setLayoutAnimation(controller);
        refreshSMSData();
        return view;
    }

    public void refreshSMSData() {
        List<SMSEntity> list = dbService.selectAllSMS();
        if (list != null) {
            smsEntityList.clear();
            if (isShowForwarded) {
                for (SMSEntity smsEntity : list) {
                    if (smsEntity.isForwarded())
                        smsEntityList.add(smsEntity);
                }
            } else {
                smsEntityList.addAll(list);
            }
        }
        adapter.notifyDataSetChanged();
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            isShowForwarded = b;
            refreshSMSData();
        }
    };

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            SMSEntity smsEntity = smsEntityList.get(i);
            showConfirmDialog(smsEntity.getSender(), smsEntity.getContent(), "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
    };
    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
            showConfirmDialog("提示", "删除此项?", "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int witch) {
                    dbService.deleteSMSbyID(smsEntityList.get(i));
                    refreshSMSData();
                    dialogInterface.dismiss();
                }
            });
            return true;
        }
    };

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
