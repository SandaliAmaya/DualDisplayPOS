package com.sunmi.doublescreen.sunmimainscreenapp;

import android.content.Context;

import sunmi.ds.DSKernel;
import sunmi.ds.callback.IReceiveCallback;
import sunmi.ds.data.DSData;
import sunmi.ds.data.DSFile;
import sunmi.ds.data.DSFiles;

/**
 * Created by highsixty on 2017/11/3.
 * mail  gaolulin@sunmi.com
 */

public class Test {

    private DSKernel dsKernel;
    private Context context;
    private IReceiveCallback iReceiveCallback = new IReceiveCallback() {
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

    public Test(Context mContext) {
        dsKernel = DSKernel.newInstance();
    }

    public void test() {
        dsKernel.addReceiveCallback(iReceiveCallback);
        dsKernel.removeReceiveCallback(iReceiveCallback);
    }

}
