package per.learn.slidingdelete;

import java.util.ArrayList;
import java.util.Arrays;

import per.learn.slidingdelete.lib.SlidingDeleteListView;
import per.learn.slidingdelete.lib.SlidingDeleteListView.OnItemButtonShowingListener;
import per.learn.slidingdelete.util.Util;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SlidingDeleteListView mSlidingDeleteLv;
    private MyAdapter mAdapter;
    private ArrayList<String> arrays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrays = new ArrayList<String>(Arrays.asList(Util.arrays));

        mSlidingDeleteLv = (SlidingDeleteListView)findViewById(R.id.sliding_delete_lv);
        mAdapter = new MyAdapter(this, arrays);
        mSlidingDeleteLv.setButtonID(R.id.delete_btn);
        mSlidingDeleteLv.setAdapter(mAdapter);
        mSlidingDeleteLv.setOnItemButtonShowingListener(new OnItemButtonShowingListener() {

            @Override
            public void onShowButton(View button) {
            }

            @Override
            public void onHideButton(View button) {
            }

            @Override
            public void onButtonClick(View button, int position) {
                removeItem(position);
            }
        });
        mSlidingDeleteLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> view, View parent, int position,
                    long id) {
                Toast.makeText(MainActivity.this, "click item " + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
        mSlidingDeleteLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Toast.makeText(MainActivity.this, "long click",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void removeItem(int position) {
        arrays.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    public class MyAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<String> mArrays;

        public MyAdapter(Context c, ArrayList<String> data) {
            mContext = c;
            mArrays = data;
        }

        @Override
        public int getCount() {
            return mArrays.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrays.get(position);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.layout_list_item, null);
                holder.mContentTv = (TextView)convertView.findViewById(R.id.content_tv);
                holder.mDeleteBtn = (Button)convertView.findViewById(R.id.delete_btn);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.mContentTv.setText(mArrays.get(position));

            return convertView;
        }

    }

    private static class ViewHolder {
        TextView mContentTv;
        Button mDeleteBtn;
    }

}
