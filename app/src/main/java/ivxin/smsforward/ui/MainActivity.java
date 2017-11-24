package ivxin.smsforward.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import java.util.ArrayList;

import ivxin.smsforward.Constants;
import ivxin.smsforward.R;
import ivxin.smsforward.base.BaseActivity;
import ivxin.smsforward.base.BaseFragment;
import ivxin.smsforward.base.OnPermissionCheckedListener;

public class MainActivity extends BaseActivity {
    private final ArrayList<BaseFragment> fragmentList = new ArrayList<>();
    private FragmentManager fragmentManager;
    private int currentPageIndex;

    {
        fragmentList.add(new HomeFragment());
        fragmentList.add(new DashBoardFragment());
        fragmentList.add(new SettingFragment());
        fragmentList.add(new AboutFragment());
    }

    BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DashBoardFragment dashBoardFragment = (DashBoardFragment) fragmentList.get(1);
            dashBoardFragment.refreshSMSData();
        }
    };

    BottomNavigationBar.OnTabSelectedListener onTabSelectedListener
            = new BottomNavigationBar.OnTabSelectedListener() {
        @Override
        public void onTabSelected(int position) {
            changeFragment(position);
        }

        @Override
        public void onTabUnselected(int position) {

        }

        @Override
        public void onTabReselected(int position) {
            onTabSelected(position);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                changeFragment(3);
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(refreshReceiver, new IntentFilter(Constants.ACTION));
        fragmentManager = getSupportFragmentManager();
        BottomNavigationBar bottom_navigation_bar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        bottom_navigation_bar
                .addItem(new BottomNavigationItem(R.drawable.ic_home_black_24dp, "主页"))
                .addItem(new BottomNavigationItem(R.drawable.ic_dashboard_black_24dp, "信息"))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings_black_24dp, "设置"))
                .initialise();
        bottom_navigation_bar.setTabSelectedListener(onTabSelectedListener);
        changeFragment(0);
        checkPermissions(new OnPermissionCheckedListener() {
            @Override
            public void onPermissionGranted(String permission) {
                Constants.HAVE_PERMISSION = true;
            }

            @Override
            public void onPermissionDenied(String permission) {
                Constants.HAVE_PERMISSION = false;
            }
        }, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS);
    }

    private void changeFragment(int index) {
        BaseFragment fragment = fragmentList.get(index);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
        currentPageIndex = index;
    }

    /**
     * 返回键退出时间标记
     */
    public static long lastPressed;

    @Override
    public void onBackPressed() {
        if (fragmentList.get(currentPageIndex).onBackPressed()) return;//执行fragment监听

        if (System.currentTimeMillis() - lastPressed < 2000) {
            finish();
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            lastPressed = System.currentTimeMillis();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(refreshReceiver);
    }
}
