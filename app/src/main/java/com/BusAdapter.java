package com;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sdhy.video.client.R;

import java.util.List;

/**
 * Created by Administrator on 2019/8/30.
 */

public class BusAdapter extends ArrayAdapter<String> {

    private int resoureId;
    private List<String> objects;
    private Context context;


    public BusAdapter(Context context, int resourceId, List<String> objects) {
        super(context, resourceId, objects);
        // TODO Auto-generated constructor stub
        this.objects = objects;
        this.context = context;

    }

    private static class ViewHolder {
        TextView title;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return objects.size();
    }

    @Override
    public String getItem(int position) {
        // TODO Auto-generated method stub
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.adapter_bus, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tvText);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String person = objects.get(position);
        if (null != person) {
//            viewHolder.title.setHt(person);
            viewHolder.title.setText(Html.fromHtml(person));
        }

        return convertView;
    }
}