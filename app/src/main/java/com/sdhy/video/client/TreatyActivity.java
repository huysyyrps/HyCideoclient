package com.sdhy.video.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 2019/12/16.
 */

public class TreatyActivity extends Activity {
    private ImageView ivBack;
    private TextView tvTitle,tvContent,tvContent1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treaty);
        Intent intent = getIntent();
        String tag = intent.getStringExtra("tag");
        ivBack = (ImageView) findViewById(R.id.ivBack);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent1 = (TextView) findViewById(R.id.tvContent1);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (tag.equals("1")){
            tvTitle.setText("用户协议");
            tvContent.setVisibility(View.VISIBLE);
            tvContent1.setVisibility(View.GONE);
        }else if (tag.equals("2")){
            tvTitle.setText("隐试政策");
            tvContent.setVisibility(View.GONE);
            tvContent1.setVisibility(View.VISIBLE);
        }
    }
}
