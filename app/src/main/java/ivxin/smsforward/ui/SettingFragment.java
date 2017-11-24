package ivxin.smsforward.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ivxin.smsforward.Constants;
import ivxin.smsforward.R;
import ivxin.smsforward.base.BaseFragment;
import ivxin.smsforward.base.OnPermissionCheckedListener;

/**
 * Created by yaping.wang on 2017/9/14.
 */

public class SettingFragment extends BaseFragment implements View.OnClickListener {
    private static final int CONTACTS = 91;
    private TextView tv_number_rex;
    private Button btn_delete_number_rex;
    private Button btn_add_number_rex;
    private TextView tv_content_rex;
    private Button btn_delete_content_rex;
    private Button btn_add_content_rex;
    private TextView tv_receiver_number;
    private Button btn_delete_receiver_number;
    private Button btn_add_receiver_number;
    private Button btn_choose_receiver_number;
    private SwitchCompat tb_switcher;
    private SharedPreferences sp;
    private ArrayList<CharSequence> contactPhoneNumbers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sp = getContext().getSharedPreferences(Constants.SP_FILE_NAME, Context.MODE_PRIVATE);
        View view = inflater.inflate(R.layout.fragment_settings, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        tb_switcher = view.findViewById(R.id.tb_switcher);
        tv_number_rex = view.findViewById(R.id.tv_number_rex);
        btn_delete_number_rex = view.findViewById(R.id.btn_delete_number_rex);
        btn_add_number_rex = view.findViewById(R.id.btn_add_number_rex);
        tv_content_rex = view.findViewById(R.id.tv_content_rex);
        btn_delete_content_rex = view.findViewById(R.id.btn_delete_content_rex);
        btn_add_content_rex = view.findViewById(R.id.btn_add_content_rex);
        tv_receiver_number = view.findViewById(R.id.tv_receiver_number);
        btn_delete_receiver_number = view.findViewById(R.id.btn_delete_receiver_number);
        btn_add_receiver_number = view.findViewById(R.id.btn_add_receiver_number);
        btn_choose_receiver_number = view.findViewById(R.id.btn_choose_receiver_number);

        setDataFromSP();
        tb_switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sp.edit().putBoolean(Constants.STARTED_KEY, b).apply();
            }
        });
        btn_add_number_rex.setOnClickListener(this);
        btn_add_content_rex.setOnClickListener(this);
        btn_add_receiver_number.setOnClickListener(this);
        btn_choose_receiver_number.setOnClickListener(this);

        btn_delete_number_rex.setOnClickListener(this);
        btn_delete_content_rex.setOnClickListener(this);
        btn_delete_receiver_number.setOnClickListener(this);

        tv_number_rex.setOnClickListener(this);
        tv_content_rex.setOnClickListener(this);
        tv_receiver_number.setOnClickListener(this);
    }

    private void setDataFromSP() {
        tb_switcher.setChecked(sp.getBoolean(Constants.STARTED_KEY, false));
        tv_number_rex.setText(sp.getString(Constants.NUM_REX_KEY, ""));
        tv_content_rex.setText(sp.getString(Constants.REX_KEY, ""));
        tv_receiver_number.setText(sp.getString(Constants.TARGET_KEY, ""));

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_number_rex:
                showInputDialog("添加号码过滤规则", true, new InputDialogCallback() {
                    @Override
                    public void onClick(View view, String text) {
                        String newRex = addRex(Constants.NUM_REX_KEY, text);
                        tv_number_rex.setText(newRex);
                    }
                });
                break;
            case R.id.btn_add_content_rex:
                showInputDialog("添加内容过滤规则", false, new InputDialogCallback() {
                    @Override
                    public void onClick(View view, String text) {
                        String newRex = addRex(Constants.REX_KEY, text);
                        tv_content_rex.setText(newRex);
                    }
                });
                break;
            case R.id.btn_add_receiver_number:
                showInputDialog("添加收信人", true, new InputDialogCallback() {
                    @Override
                    public void onClick(View view, String text) {
                        String newRex = addRex(Constants.TARGET_KEY, text);
                        tv_receiver_number.setText(newRex);
                    }
                });
                break;
            case R.id.btn_choose_receiver_number:
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), CONTACTS);
                break;
            case R.id.btn_delete_number_rex:
                String new_number_rex = removeRex(Constants.NUM_REX_KEY);
                tv_number_rex.setText(new_number_rex);
                break;
            case R.id.btn_delete_content_rex:
                String new_content_rex = removeRex(Constants.REX_KEY);
                tv_content_rex.setText(new_content_rex);
                break;
            case R.id.btn_delete_receiver_number:
                String new_receiver_number = removeRex(Constants.TARGET_KEY);
                tv_receiver_number.setText(new_receiver_number);
                break;
            case R.id.tv_number_rex:
                showContentDialog(1);
                break;
            case R.id.tv_content_rex:
                showContentDialog(2);
                break;
            case R.id.tv_receiver_number:
                showContentDialog(3);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case CONTACTS:
                    checkPermissions(new OnPermissionCheckedListener() {
                        @Override
                        public void onPermissionGranted(String permission) {
                            contactPhoneNumbers.clear();
                            String username = "";
                            ContentResolver reContentResolverol = getActivity().getContentResolver();
                            Uri contactData = data.getData();
                            @SuppressWarnings("deprecation")
                            Cursor cursor = getActivity().managedQuery(contactData, null, null, null, null);
                            cursor.moveToFirst();
                            int phoneCount = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                            if (phoneCount > 0) {
                                username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                                Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null,
                                        null);
                                if (phone != null) {
                                    while (phone.moveToNext()) {
                                        String phoneNumber = phone
                                                .getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        contactPhoneNumbers.add(phoneNumber.replaceAll(" ", "").replace("-", ""));
                                    }
                                }
                                if (contactPhoneNumbers.size() == 1) {
                                    String newRex = addRex(Constants.TARGET_KEY, contactPhoneNumbers.get(0));
                                    tv_receiver_number.setText(newRex);
                                } else if (contactPhoneNumbers.size() > 0) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle(username);
                                    CharSequence[] charSequences = new CharSequence[contactPhoneNumbers.size()];
                                    builder.setItems(contactPhoneNumbers.toArray(charSequences), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            String newRex = addRex(Constants.TARGET_KEY, contactPhoneNumbers.get(i));
                                            tv_receiver_number.setText(newRex);
                                        }
                                    });
                                    builder.create().show();
                                }
                            } else {
                                Toast.makeText(getContext(), "联系人没有号码", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionDenied(String permission) {
                            Toast.makeText(getContext(), "没有权限", Toast.LENGTH_SHORT).show();
                        }
                    }, Manifest.permission.READ_CONTACTS);
                    break;
                default:
                    break;
            }
        }
    }

    private String addRex(String key, CharSequence text) {
        String org = sp.getString(key, "");
        String newRex = org + ";" + text;
        if (newRex.startsWith(";"))
            newRex = newRex.substring(1, newRex.length());
        newRex = newRex.replaceAll(" ", "").replaceAll("-", "").replaceAll(";;", ";");
        sp.edit().putString(key, newRex).apply();
        return newRex;
    }

    private String removeRex(String key) {
        String new_number_rex;
        try {
            String org_number_rex = sp.getString(key, "");
            new_number_rex = org_number_rex.substring(0, org_number_rex.lastIndexOf(";"));
            sp.edit().putString(key, new_number_rex).apply();
            return new_number_rex;
        } catch (Exception ignore) {
            sp.edit().putString(key, "").apply();
            return "";
        }
    }

    private void showContentDialog(int what) {
        final StringBuffer key = new StringBuffer();
        switch (what) {
            case 1:
                key.append(Constants.NUM_REX_KEY);
                break;
            case 2:
                key.append(Constants.REX_KEY);
                break;
            case 3:
                key.append(Constants.TARGET_KEY);
                break;
        }
        final String text = sp.getString(key.toString(), "");
        if (TextUtils.isEmpty(text)) return;
        try {
            final String[] texts = text.split(";");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("点击一项删除");
            builder.setItems(texts, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        String newText = text.replace(texts[i], "").replace(";;", ";");
                        if (newText.endsWith(";")) {
                            newText = newText.substring(0, newText.length() - 1);
                        }
                        sp.edit().putString(key.toString(), newText).apply();
                    } catch (Exception ignore) {
                    } finally {
                        setDataFromSP();
                    }
                }
            });
            builder.create().show();
        } catch (Exception ignore) {
        }
    }

    private void showInputDialog(String title, boolean isNumber, final InputDialogCallback inputDialogCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = View.inflate(getContext(), R.layout.layout_input_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        TextView tv_dialog_title = view.findViewById(R.id.tv_dialog_title);
        final EditText et_input_filed = view.findViewById(R.id.et_input_filed);
        et_input_filed.setInputType(isNumber ? InputType.TYPE_CLASS_PHONE : InputType.TYPE_CLASS_TEXT);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);
        tv_dialog_title.setText(title);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_input_filed.getText().toString();
                if (inputDialogCallback != null && !TextUtils.isEmpty(text)) {
                    inputDialogCallback.onClick(view, text);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    interface InputDialogCallback {
        void onClick(View view, String text);
    }
}
