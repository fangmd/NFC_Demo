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

    public interface OnResultListener {
        void onResult(String resultStr);
    }

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
//                String s = Util.byteArrayToHexString(atqa);
//                Log.d(TAG, "nfca.getAtqa() : " + s);


                int le = nfca.getMaxTransceiveLength();
                byte[] bytes = new byte[le];
                byte[] transceive;
                try {
                    transceive = nfca.transceive(bytes);
                    ret = Util.byteArrayToHexString(transceive);
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
