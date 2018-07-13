package com.sunmi.doublescreen.sunmimainscreenapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Parameter;

import sunmi.ds.DSKernel;
import sunmi.ds.SF;
import sunmi.ds.callback.ISendCallback;

public class QRCode extends AppCompatActivity {

    private static final String TAG = "QR_CDE";
    TextView mainTextView;
    private DSKernel mDSKernel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        mainTextView = (TextView) findViewById(R.id.textView8);

        try {
            Intent amountIntent = getIntent();
            String amountPassed = amountIntent.getStringExtra(MainActivity.QR_CODE_CONTENT_TEXT);
            Log.d("QRAmount", amountPassed);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(amountPassed, BarcodeFormat.QR_CODE, 400, 400);

            mainTextView.setText("Amount : " + amountPassed);
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.qrCode);
            Log.d("QRBitMapData", bitmap.toString());
            imageViewQrCode.setImageBitmap(bitmap);
            Log.d("QR generated","Successfully Sent the QR");

//            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
//                    new IntentFilter("Parameter.FCM_PUSH_NOTIFICATION"));
//

            File file = UtilHelper.createImage(bitmap);


//            boolean isSaved = UtilHelper.saveImage(bitmap, "qR_code.jpg");

//            if (isSaved) {
//                Log.d("QRImageSaved", "Saved Image");
//            } else {
//                Log.d("QRImageFailed", "Could Not Save Image");
//            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("QRCodeError", "To check if there is an error");
        }

    }
//
//    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals("Parameter.FCM_PUSH_NOTIFICATION")) {
//
//                Log.d("intent","Message Received "+intent.getExtras());
//
//                Intent sendSuccess = new Intent(QRCode.this,Success.class);
//                startActivity(sendSuccess);
//
//
////                BottomNavigationMenuView bottomNavigationMenuView =
////                        (BottomNavigationMenuView) navigationx.getChildAt(0);
////                View v = bottomNavigationMenuView.getChildAt(1);
////                BottomNavigationItemView itemView = (BottomNavigationItemView) v;
////                View badge = LayoutInflater.from(ActivityNotification.this)
////                        .inflate(R.layout.layout_notification_badge, bottomNavigationMenuView, false);
////                itemView.addView(badge);
////
////                getAvailableNotifications();
//
//
//            }
//        }
//    };
}
