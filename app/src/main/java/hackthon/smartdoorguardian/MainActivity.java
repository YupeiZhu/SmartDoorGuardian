package hackthon.smartdoorguardian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity implements OnClickListener {

    private static final int NOSELECT_STATE = -1;// 表示未选中任何CheckBox

    private ListView listView;
    private Button bt_cancel, bt_delete;
    private TextView tv_sum;
    private LinearLayout linearLayout;
    private LinearLayout linearLayout2;

    private List<entry> list = new ArrayList<entry>();// 数据
    private List<String> list_delete = new ArrayList<String>();// 需要删除的数据
    private boolean isMultiSelect = false;// 是否处于多选状态

    private MyAdapter adapter;// ListView的Adapter
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Firebase fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread notification = new Thread() {
            public void run() {

                Intent notification = new Intent(MainActivity.this, MyService.class);

                startService(notification);
            }
        };
        notification.start();
        Toast.makeText(getApplicationContext(), "Please wait for loading", Toast.LENGTH_LONG).show();
        Firebase.setAndroidContext(getApplicationContext());
        fb = new Firebase("https://test-110.firebaseio.com/SB");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list = new ArrayList<entry>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    entry newEntry = new entry();
                    newEntry.hash = data.getKey();
                    newEntry.str = data.child("str").getValue(String.class);
                    String base64 = data.child("image").getValue(String.class);

                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    newEntry.bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    list.add(newEntry);
                }
                Log.i("hhh", "ssss");
                listView = (ListView) findViewById(R.id.listView1);
                bt_cancel = (Button) findViewById(R.id.bt_cancel);
                bt_delete = (Button) findViewById(R.id.bt_delete);
                tv_sum = (TextView) findViewById(R.id.tv_sum);
                linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
                linearLayout2 = (LinearLayout) findViewById(R.id.linearLayout1);
                bt_cancel.setOnClickListener(MainActivity.this);
                bt_delete.setOnClickListener(MainActivity.this);

                // 未选中任何Item，position设置为-1
                adapter = new MyAdapter(MainActivity.this, list, NOSELECT_STATE);
                listView.setAdapter(adapter);
                //fb.removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        fb.addValueEventListener(listener);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class MyAdapter extends BaseAdapter {

        private List<entry> list;

        private LayoutInflater inflater;

        private HashMap<Integer, Integer> isCheckBoxVisible;// 用来记录是否显示checkBox
        private HashMap<Integer, Boolean> isChecked;// 用来记录是否被选中

        @SuppressLint("UseSparseArrays")
        public MyAdapter(Context context, List<entry> list, int position) {
            inflater = LayoutInflater.from(context);
            this.list = list;
            isCheckBoxVisible = new HashMap<Integer, Integer>();
            isChecked = new HashMap<Integer, Boolean>();
            // 如果处于多选状态，则显示CheckBox，否则不显示
            if (isMultiSelect) {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.VISIBLE);
                    isChecked.put(i, false);
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    isCheckBoxVisible.put(i, CheckBox.INVISIBLE);
                    isChecked.put(i, false);
                }
            }

            // 如果长按Item，则设置长按的Item中的CheckBox为选中状态
            if (isMultiSelect && position >= 0) {
                isChecked.put(position, true);
            }
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_layout, null);
                viewHolder.tv_Name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb_select);
                viewHolder.iv = (ImageView) convertView.findViewById(R.id.img);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final String str = list.get(position).str;
            final Bitmap bitmap = list.get(position).bitmap;
            viewHolder.tv_Name.setText(str);
            viewHolder.iv.setImageBitmap(bitmap);
            // 根据position设置CheckBox是否可见，是否选中
            viewHolder.cb.setChecked(isChecked.get(position));
            viewHolder.cb.setVisibility(isCheckBoxVisible.get(position));
            // ListView每一个Item的长按事件
            convertView.setOnLongClickListener(new onMyLongClick(position, list));
            /*
             * 在ListView中点击每一项的处理
			 * 如果CheckBox未选中，则点击后选中CheckBox，并将数据添加到list_delete中
			 * 如果CheckBox选中，则点击后取消选中CheckBox，并将数据从list_delete中移除
			 */
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 处于多选模式
                    if (isMultiSelect) {
                        if (viewHolder.cb.isChecked()) {
                            viewHolder.cb.setChecked(false);
                            list_delete.remove(list.get(position).hash);
                        } else {
                            viewHolder.cb.setChecked(true);
                            list_delete.add(list.get(position).hash);
                        }
                        tv_sum.setText("Total: " + list_delete.size());
                    } else {
                        Dialog builder = new Dialog(MainActivity.this);

                        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);

                        builder.getWindow().setBackgroundDrawable(

                                new ColorDrawable(android.graphics.Color.TRANSPARENT));

                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override

                            public void onDismiss(DialogInterface dialogInterface) {

                                //nothing;

                            }

                        });


                        ImageView imageView = new ImageView(MainActivity.super.getApplicationContext());
                        imageView.setImageBitmap(big(list.get(position).bitmap));

                        builder.addContentView(imageView, new RelativeLayout.LayoutParams(

                                ViewGroup.LayoutParams.MATCH_PARENT,

                                ViewGroup.LayoutParams.MATCH_PARENT));

                        builder.show();
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            public TextView tv_Name;
            public ImageView iv;
            public CheckBox cb;
        }

        // 自定义长按事件
        class onMyLongClick implements OnLongClickListener {

            private int position;
            private List<entry> list;

            // 获取数据，与长按Item的position
            public onMyLongClick(int position, List<entry> list) {
                this.position = position;
                this.list = list;
            }

            // 在长按监听时候，切记将监听事件返回ture
            @Override
            public boolean onLongClick(View v) {
                isMultiSelect = true;
                list_delete.clear();
                // 添加长按Item到删除数据list中
                list_delete.add(list.get(position).hash);
                linearLayout.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.GONE);
                tv_sum.setText("Total: " + list_delete.size());
                for (int i = 0; i < list.size(); i++) {
                    adapter.isCheckBoxVisible.put(i, CheckBox.VISIBLE);
                }
                // 根据position，设置ListView中对应的CheckBox为选中状态
                adapter = new MyAdapter(MainActivity.this, list, position);
                listView.setAdapter(adapter);
                return true;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 取消选择
            case R.id.bt_cancel:
                isMultiSelect = false;// 退出多选模式
                list_delete.clear();// 清楚选中的数据
                // 重新加载Adapter
                adapter = new MyAdapter(MainActivity.this, list, NOSELECT_STATE);
                listView.setAdapter(adapter);
                linearLayout.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.VISIBLE);
                break;
            // 删除
            case R.id.bt_delete:
                isMultiSelect = false;
                // 将数据从list中移除
                for (int i = 0; i < list.size(); i++) {
                    for (int j = 0; j < list_delete.size(); j++) {
                        if (list.get(i).hash.equals(list_delete.get(j))) {
                            String hash = list.get(i).hash;
                            fb.child(hash).setValue(null);
                        }
                    }
                }
                list_delete.clear();
                // 重新加载Adapter
                adapter = new MyAdapter(MainActivity.this, list, NOSELECT_STATE);
                listView.setAdapter(adapter);
                linearLayout.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }
    private Bitmap big(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = d.getWidth();
        float f = width/bitmap.getWidth();
        matrix.postScale(f,f);//长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }
}