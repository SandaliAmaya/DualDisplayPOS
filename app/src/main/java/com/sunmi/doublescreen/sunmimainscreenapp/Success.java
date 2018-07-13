package com.sunmi.doublescreen.sunmimainscreenapp;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunmi.doublescreen.sunmimainscreenapp.utils.SharePreferenceUtil;

import sunmi.ds.DSKernel;
import sunmi.ds.callback.ICheckFileCallback;
import sunmi.ds.callback.ISendCallback;

public class Success extends AppCompatActivity {

    private final String TAG = Success.class.getSimpleName();
    private ImageView imageView;
    private TextView text;
    private Button ok; //show picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        imageView = (ImageView) findViewById(R.id.successImage);
        text = (TextView) findViewById(R.id.successMsg);
        ok = (Button) findViewById(R.id.btnOk);

        imageView.setImageResource(R.drawable.success);
        //text.setText("Success");

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent backToMainscreen = new Intent(Success.this,MainActivity.class);
                startActivity(backToMainscreen);

            }
        });

    }


}
