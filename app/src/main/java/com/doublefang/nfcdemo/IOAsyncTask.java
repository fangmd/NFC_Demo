package com.doublefang.nfcdemo;

import android.nfc.tech.IsoDep;
import android.os.AsyncTask;

import java.io.IOException;

/**
 * Created by double on 16-6-10.
 * Project: NFCDemo
 */
public class IOAsyncTask extends AsyncTask<IsoDep, Void, String> {

    private static final String TAG = IOAsyncTask.class.getName();
    private OnResultListener mOnResultListener;


    public IOAsyncTask(OnResultListener onResultListener) {
        mOnResultListener = onResultListener;
    }

    @Override
    protected String doInBackground(IsoDep... params) {
        IsoDep nfca = null;
        String ret = "";
        if (params.length > 0) {
            nfca = params[0];
            try {
                if (!nfca.isConnected()) {
                    nfca.connect();
                } else {
                    nfca.close();
                    nfca.connect();
                }

//                byte[] atqa = nfca.getAtqa();
//                String s = MyUtils.byteArrayToHexString(atqa);
//                Log.d(TAG, "nfca.getAtqa() : " + s);


                final byte[] cmd = { (byte) 0x80, // CLA Class
                        (byte) 0x5C, // INS Instruction
                        (byte) 0x00, // P1 Parameter 1
                        (byte) 0x02, // P2 Parameter 2
                        (byte) 0x04, // Le
                };

                byte[] transceive;
                try {
                    transceive = nfca.transceive(cmd);
                    ret = MyUtils.byteArrayToHexString(transceive);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (nfca.isConnected()) {
                    try {
                        nfca.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ret;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mOnResultListener.onResult(s);
    }


}
