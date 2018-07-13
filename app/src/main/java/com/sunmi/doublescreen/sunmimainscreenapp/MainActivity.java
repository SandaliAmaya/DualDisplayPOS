package com.sunmi.doublescreen.sunmimainscreenapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sunmi.doublescreen.sunmimainscreenapp.utils.DataModel;
import com.sunmi.doublescreen.sunmimainscreenapp.utils.SharePreferenceUtil;
import com.sunmi.doublescreen.sunmimainscreenapp.utils.UPacketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import cat.xojan.numpad.NumPadButton;
import cat.xojan.numpad.NumPadView;
import cat.xojan.numpad.OnNumPadClickListener;
import sunmi.ds.DSKernel;
import sunmi.ds.SF;
import sunmi.ds.callback.ICheckFileCallback;
import sunmi.ds.callback.IConnectionCallback;
import sunmi.ds.callback.IReceiveCallback;
import sunmi.ds.callback.ISendCallback;
import sunmi.ds.callback.ISendFilesCallback;
import sunmi.ds.callback.QueryCallback;
import sunmi.ds.data.DSData;
import sunmi.ds.data.DSFile;
import sunmi.ds.data.DSFiles;
import sunmi.ds.data.DataPacket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private DSKernel mDSKernel = null;
    private MyHandler myHandler;

    private Button btnWelcom; //(show welcome)
    private Button btnWXPay;//(weixin pay)
    //private Button btnImg; //显示单张图片(show picture)

    private EditText editAmount;
    private String amount;
    NumPadView customNumberPad;

    private final String imgKey = "MAINSCREENIMG";
    private final String imgKey2 = "MAINSCREENING";

    public static final String QR_CODE_CONTENT_TEXT = "QR_CODE_CONTENT";

    private String orderID = "ORDER13244321";
    private ProgressDialog dialog;

    private IConnectionCallback mIConnectionCallback = new IConnectionCallback() {
        @Override
        public void onDisConnect() {
            Message message = new Message();
            message.what = 1;
            message.obj = "\n" + "Interrupted connection to remote service";
            myHandler.sendMessage(message);
        }

        @Override
        public void onConnected(ConnState state) {
            Message message = new Message();
            message.what = 1;
            switch (state) {
                case AIDL_CONN:
                    message.obj = "Successfully bound to the remote service";
                    break;
                case VICE_SERVICE_CONN:
                    message.obj = "\n" + "Normal communication with the secondary screen service";
                    break;
                case VICE_APP_CONN:
                    message.obj = "Communicate normally with the secondary screen app";
                    break;
                default:
                    break;
            }
            myHandler.sendMessage(message);
        }
    };

    private IReceiveCallback mIReceiveCallback = new IReceiveCallback() {
        @Override
        public void onReceiveData(DSData data) {

        }

        @Override
        public void onReceiveFile(DSFile file) {

        }

        @Override
        public void onReceiveFiles(DSFiles files) {

        }

        @Override
        public void onReceiveCMD(DSData cmd) {

        }
    };
    private IReceiveCallback mIReceiveCallback2 = new IReceiveCallback() {
        @Override
        public void onReceiveData(DSData data) {

        }

        @Override
        public void onReceiveFile(DSFile file) {

        }

        @Override
        public void onReceiveFiles(DSFiles files) {

        }

        @Override
        public void onReceiveCMD(DSData cmd) {

        }
    };

    public void requestStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");

            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        } else {
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getActionBar().setTitle("POS");

        myHandler = new MyHandler(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter("Parameter.FCM_PUSH_NOTIFICATION"));



        //capture topic
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        initSdk();
        initView();
        initAction();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ------------>" + (mDSKernel == null));
        if (mDSKernel != null) {
            mDSKernel.checkConnection();
        } else {
            initSdk();
        }
    }

    private void initSdk() {
        mDSKernel = DSKernel.newInstance();
        mDSKernel.init(this, mIConnectionCallback);
        mDSKernel.addReceiveCallback(mIReceiveCallback);
        requestStoragePermissionGranted();
        //mDSKernel.addReceiveCallback(mIReceiveCallback2);
        //mDSKernel.removeReceiveCallback(mIReceiveCallback);
        //mDSKernel.removeReceiveCallback(mIReceiveCallback2);
    }

    private void initView() {
        btnWelcom = (Button) findViewById(R.id.btn_welcome);
        btnWXPay = (Button) findViewById(R.id.btn_wxpay);
        //btnImg = (Button) findViewById(R.id.btn_img);
        editAmount = (EditText) findViewById(R.id.amount);
        hideSoftKeyboard(editAmount);
        customNumberPad = (NumPadView) findViewById(R.id.custom_number_pad);
    }


    private void initAction() {

        btnWelcom.setOnClickListener(this);
        btnWXPay.setOnClickListener(this);
        //btnImg.setOnClickListener(this);

        customNumberPad.setNumberPadClickListener(new OnNumPadClickListener() {

            @Override
            public void onPadClicked(NumPadButton numButton) {
                String clickedPad = genNumberFromName(numButton.name());
                if (clickedPad.equals("DEL")) {
                    String currentText = editAmount.getText().toString();
                    if ((currentText != null) && (currentText.length() > 0)) {
                        currentText = currentText.substring(0, (currentText.length() - 1));
                        editAmount.setText(currentText);
                    }

                    //If  only numbers are clicked
                } else if ((!clickedPad.equals("OK")) && (!clickedPad.equals("no"))) {
                    editAmount.append(clickedPad);
                }
            }
        });


    }

    private void showQR() {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            NumberFormat formatter = new DecimalFormat("####.00");

            String formattedNumber = formatter.format(Double.valueOf(amount));
            String qrContent = "da842c8e-1d08-4e43-bdb0-e2a25ec30dee " + formattedNumber + " main " +
                    orderID;
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrContent, BarcodeFormat.QR_CODE, 600, 600);
            showToast("QR Successfully sent---->");

            File file = UtilHelper.createImage(bitmap);
            if (file != null) {
                JSONObject json = new JSONObject();
                try {
                    json.put("title", "Pay for Merchant");
                    json.put("content", "LKR "+ amount + ".00");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "file created[" + file.getAbsolutePath() + "] ");
                mDSKernel.sendFile(DSKernel.getDSDPackageName(), json.toString(), file.getAbsolutePath(), new
                        ISendCallback() {
                            @Override
                            public void onSendSuccess(long l) {
                                dismissDialog();
                                //显示图片
                                try {
                                    JSONObject json = new JSONObject();
                                    json.put("dataModel", "QRCODE");
                                    json.put("data", "default");
                                    mDSKernel.sendCMD(SF.DSD_PACKNAME, json.toString(), l, null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onSendFail(int i, String s) {
                                dismissDialog();
                            }

                            @Override
                            public void onSendProcess(long l, long l1) {

                            }
                        });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_welcome:


                long imgTaskId = (long) SharePreferenceUtil.getParam(MainActivity.this, imgKey2,
                        0L);
                checkImgFileExistWelcome(imgTaskId);
                //alertDialog.setMessage("Payment Successful.");

//
//                try {
//                    JSONObject json = new JSONObject();
//                    json.put("dataModel", "SHOW_IMG_WELCOME");
//                    json.put("data", "gaolulin");
//                    mDSKernel.sendCMD(SF.DSD_PACKNAME, json.toString(), -1, null);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                break;

            case R.id.btn_wxpay:// WeChat payment

                showDialog("Sending QR Code");
                amount = editAmount.getText().toString();
                showQR();

                break;

//            case R.id.btn_img:// WeChat payment
//
//             showDialog("Sending Payment Success Message");
//                long imgTaskId = (long) SharePreferenceUtil.getParam(this, imgKey, 0L);
//               checkImgFileExist(imgTaskId);
//
//                break;


            default:
                break;
        }
    }

    private void checkImgFileExist(final long taskID) {
        Log.d(TAG, "checkImgFileExist: ---------->" + taskID);
        if (taskID < 0) {
            sendPicture();
            return;
        }
        checkFileExist(taskID, new ICheckFileCallback() {
            @Override
            public void onCheckFail() {

                Log.d(TAG, "onCheckFail: ------------>file not exist");
                sendPicture();
            }

            @Override
            public void onResult(boolean exist) {
                if (exist) {

                    Log.d(TAG, "onResult: --------->file is exist");
                    dismissDialog();
                    showPicture(taskID);
                } else {

                    Log.d(TAG, "onResult: --------->file is not exist");
                    sendPicture();
                }
            }
        });
    }

    private void checkImgFileExistWelcome(final long taskID) {
        Log.d(TAG, "checkImgFileExist: ---------->" + taskID);
        if (taskID < 0) {
            sendPictureWelcome();
            return;
        }
        checkFileExist(taskID, new ICheckFileCallback() {
            @Override
            public void onCheckFail() {

                Log.d(TAG, "onCheckFail: ------------>file not exist");
                sendPictureWelcome();
            }

            @Override
            public void onResult(boolean exist) {
                if (exist) {

                    Log.d(TAG, "onResult: --------->file is exist");
                    dismissDialog();
                    showPicture(taskID);
                } else {

                    Log.d(TAG, "onResult: --------->file is not exist");
                    sendPictureWelcome();
                }
            }
        });
    }


    private void checkFileExist(long fileId, final ICheckFileCallback mICheckFileCallback) {
        DataPacket packet = new DataPacket.Builder(DSData.DataType.CHECK_FILE).data("def").
                recPackName(DSKernel.getDSDPackageName()).addCallback(new ISendCallback() {
            @Override
            public void onSendSuccess(long taskId) {

            }

            @Override
            public void onSendFail(int errorId, String errorInfo) {
                if (mICheckFileCallback != null) {
                    mICheckFileCallback.onCheckFail();
                }
            }

            @Override
            public void onSendProcess(long totle, long sended) {

            }
        }).isReport(true).build();
        packet.getData().fileId = fileId;
        mDSKernel.sendQuery(packet, new QueryCallback() {
            @Override
            public void onReceiveData(DSData data) {
                boolean exist = TextUtils.equals("true", data.data);
                if (mICheckFileCallback != null) {
                    mICheckFileCallback.onResult(exist);
                }
            }
        });
    }

    private void sendPicture() {
        Log.d(TAG, "sendPicture: --------->1111111111111111111");
        mDSKernel.sendFile(DSKernel.getDSDPackageName(), Environment.getExternalStorageDirectory().getPath() + "/PSuccess.png", new ISendCallback() {
            @Override
            public void onSendSuccess(long taskId) {
                dismissDialog();
                SharePreferenceUtil.setParam(MainActivity.this, imgKey, taskId);
                Log.d(TAG, "sendPicture: --------->222222222222222222");
                showPicture(taskId);
            }

            @Override
            public void onSendFail(int errorId, String errorInfo) {
                Log.d("TAG", "onSendFail: -------------------->" + errorId + "  " + errorInfo);
                showToast("Failed to send a single image---->" + errorInfo);
                dismissDialog();

            }

            @Override
            public void onSendProcess(long totle, long sended) {
                Log.d(TAG, "sendPicture: --------->"+totle+"  "+sended);
            }
        });
    }


    private void sendPictureWelcome() {
        Log.d(TAG, "sendPicture: --------->1111111111111111111");
        mDSKernel.sendFile(DSKernel.getDSDPackageName(), Environment.getExternalStorageDirectory
                ().getPath() + "/welcome.png", new ISendCallback() {
            @Override
            public void onSendSuccess(long taskId) {
                dismissDialog();
                SharePreferenceUtil.setParam(MainActivity.this, imgKey2, taskId);
                Log.d(TAG, "sendPicture: --------->222222222222222222");
                showPicture(taskId);
            }

            @Override
            public void onSendFail(int errorId, String errorInfo) {
                Log.d("TAG", "onSendFail: -------------------->" + errorId + "  " + errorInfo);
                showToast("Failed to send a single image---->" + errorInfo);
                dismissDialog();

            }

            @Override
            public void onSendProcess(long totle, long sended) {
                Log.d(TAG, "sendPicture: --------->"+totle+"  "+sended);
            }
        });
    }




    private void showPicture(long taskId) {
        //显示图片
        try {
            JSONObject json = new JSONObject();
            json.put("dataModel", "SHOW_IMG_WELCOME");
            json.put("data", "default");
            mDSKernel.sendCMD(DSKernel.getDSDPackageName(), json.toString(), taskId, new ISendCallback() {
                @Override
                public void onSendSuccess(long taskId) {
                    Log.d(TAG, "sendPicture: --------->33333333333333");
                }

                @Override
                public void onSendFail(int errorId, String errorInfo) {

                }

                @Override
                public void onSendProcess(long totle, long sended) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private static class MyHandler extends Handler {
        private WeakReference<Activity> mActivity;

        public MyHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() != null && !mActivity.get().isFinishing()) {
                switch (msg.what) {
                    case 1://Message prompt use
                        Toast.makeText(mActivity.get(), msg.obj + "", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }

    }


    @Override
    protected void onPause() { //If there is an activity jump, you need to do the cleanup operation
        super.onPause();
        mDSKernel.onDestroy();
        mDSKernel = null;
    }


    private synchronized void showDialog(String title) {
        Log.d(TAG, "showDialog: ----------------->");
        if (dialog != null && !dialog.isShowing()) {
            dialog.setTitle(title);
            dialog.show();
        }
    }

    private synchronized void dismissDialog() {
        Log.d(TAG, "dismissDialog: ------------->");
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    //hide the soft keyboard
    private void hideSoftKeyboard(EditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private String genNumberFromName(String name) {

        switch (name) {
            case "NUM_0":
                return "0";
            case "NUM_1":
                return "1";
            case "NUM_2":
                return "2";
            case "NUM_3":
                return "3";
            case "NUM_4":
                return "4";
            case "NUM_5":
                return "5";
            case "NUM_6":
                return "6";
            case "NUM_7":
                return "7";
            case "NUM_8":
                return "8";
            case "NUM_9":
                return "9";
            case "CUSTOM_BUTTON_1":
                return "DEL";
            case "CUSTOM_BUTTON_2":
                return "OK";
            default:
                return "no";
        }
    }


    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Parameter.FCM_PUSH_NOTIFICATION")) {

                Log.d("intent","Message Received "+intent.getStringExtra("fcmData"));

//                Intent sendSuccess = new Intent(MainActivity.this,Success.class);
//                startActivity(sendSuccess);

//                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage("Successfully Received");
//                builder.setIcon(R.drawable.ic_sucess);
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                });

                if(intent.getStringExtra("fcmData").contains(orderID)) {

                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

                    //alertDialog.setTitle("Alert");

                    long imgTaskId = (long) SharePreferenceUtil.getParam(MainActivity.this, imgKey, 0L);
                    checkImgFileExist(imgTaskId);
                    alertDialog.setMessage("Payment Successful.");

                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
//                                showDialog("Sending Payment Success Message");
//                                long imgTaskId = (long) SharePreferenceUtil.getParam(this, imgKey, 0L);
//                                checkImgFileExist(imgTaskId);


                        }
                    });
                    alertDialog.show();
                }else{
                    Log.d("intent","unrelated message");

                }

            }
        }
    };

}
