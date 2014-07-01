package com.appspot.afnf4199ga.twawm.ctl;

import java.util.ArrayList;

import net.afnf.and.twawm2.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class CustomizeActionsActivity extends ListActivity {

    private IconicAdapter adapter = null;
    private ArrayList<ListItem> array = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ctl_customize_actions_main);

        array = constructListItemArrayFromCustomizedData(
                getResources().getStringArray(R.array.entries_menu_widget_click_action_on_choose),
                Const.getPrefWidgetClickActionCustomizedData(this));

        adapter = new IconicAdapter();
        setListAdapter(adapter);

        CwacTouchListView tlv = (CwacTouchListView) getListView();
        tlv.setDropListener(onDrop);
        tlv.setRemoveListener(onRemove);
    }

    public void onSave(View view) {

        StringBuilder sb = new StringBuilder();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            ListItem item = adapter.getItem(i);
            sb.append(item.value);
            sb.append(item.checked ? 1 : 0);
            sb.append(',');
        }
        Const.updatePrefWidgetClickActionCustomizedData(this, sb.toString());

        finish();
    }

    public void onCancel(View view) {
        finish();
    }

    public static ArrayList<ListItem> constructListItemArrayFromCustomizedData(String[] labels, String customizedData) {
        try {
            ArrayList<ListItem> array = new ArrayList<ListItem>();
            int len = labels.length;

            // 初期化
            if (MyStringUtlis.isEmpty(customizedData)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < len; i++) {
                    sb.append((char) ('a' + i));
                    sb.append("1,");
                }
                customizedData = sb.toString();
            }

            // customizedDataのパース
            String[] datas = customizedData.split(",");
            int datalen = datas.length;

            for (int i = 0; i < len; i++) {
                String data = "";

                // customizedDataが足りない場合の対応
                if (datalen <= i) {
                    data = ((char) ('a' + i)) + "1";
                }
                else {
                    data = datas[i];
                }

                // ListItemの構築
                char value = data.charAt(0);
                int index = value - 'a';
                boolean checked = data.charAt(1) == '1';
                ListItem item = new ListItem(value, labels[index], checked);
                array.add(item);
            }

            return array;
        }
        catch (Throwable e) {

            // 失敗した場合はcustomizedDataを空にして再構築
            Logger.w("customizedData broken : " + customizedData, e);
            return constructListItemArrayFromCustomizedData(labels, null);
        }
    }

    private CwacTouchListView.DropListener onDrop = new CwacTouchListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            ListItem item = adapter.getItem(from);
            adapter.remove(item);
            adapter.insert(item, to);
        }
    };

    private CwacTouchListView.RemoveListener onRemove = new CwacTouchListView.RemoveListener() {
        @Override
        public void remove(int which) {
            adapter.remove(adapter.getItem(which));
        }
    };

    class IconicAdapter extends ArrayAdapter<ListItem> {
        IconicAdapter() {
            super(CustomizeActionsActivity.this, R.layout.ctl_customize_actions_row, array);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.ctl_customize_actions_row, parent, false);
            }

            TextView v = (TextView) row.findViewById(R.id.textView1);
            ListItem item = array.get(position);
            v.setText(item.label);

            CheckBox c = (CheckBox) row.findViewById(R.id.checkBox1);
            c.setOnCheckedChangeListener(new CheckBoxListener(item));
            c.setChecked(item.checked);

            return row;
        }
    }

    class CheckBoxListener implements CompoundButton.OnCheckedChangeListener {
        ListItem item;

        CheckBoxListener(ListItem item) {
            this.item = item;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            item.checked = isChecked;
        }
    }
}
