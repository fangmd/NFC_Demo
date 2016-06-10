package com.doublefang.nfcdemo;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements IOAsyncTask.OnResultListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Tag tag;
    NfcAdapter nfcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取默认的NFC控制器
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Log.d(TAG, "onCreate: 设备不支持NFC");
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            Log.d(TAG, "onCreate: 请在系统设置中先启用NFC功能");
            finish();
            return;
        }

        Intent intent = getIntent();
        if (intent != null) {
//            handleIntent(intent);
//            processIntent(intent);
            processIntentIsoDep(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
//            handleIntent(intent);
//            processIntent(intent);
            processIntentIsoDep(intent);
        }
    }

    /**
     * 查看 nfc 卡的基本信息
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {
        //获取到Intent的Action，注意多打Log
        Log.d(TAG, "handleIntent: " + intent.getAction());
        if (!intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            Log.d(TAG, "handleIntent: no valid action");
            return;
        }
        //获取Tag对象
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //获取卡ID，这个ID一般没什么用，有可能是卡自动生成的
        Log.d(TAG, "Id:" + Util.byteArrayToHexString(tag.getId()));
        //NFC卡片所支持的技术标准
        Log.d(TAG, "TechList:" + Arrays.toString(tag.getTechList()));
    }

    /**
     * 解析 NfcA 技术标准的数据
     *
     * @param intent
     */
    private void processIntentNFCA(Intent intent) {
        //取出封装在intent中的TAG
        Log.d(TAG, "handleIntent: " + intent.getAction());
        if (!intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            Log.d(TAG, "handleIntent: no valid action");
            return;
        }

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for (String tech : tagFromIntent.getTechList()) {
            System.out.println(tech);
        }
        //读取TAG
        NfcA nfca = NfcA.get(tagFromIntent);
//        new IOAsyncTask(this).execute(nfca);
    }

    /**
     * 解析 IsoDep 技术标准的数据
     *
     * @param intent
     */
    private void processIntentIsoDep(Intent intent) {
        //取出封装在intent中的TAG
        Log.d(TAG, "handleIntent: " + intent.getAction());
        if (!intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            Log.d(TAG, "handleIntent: no valid action");
            return;
        }

        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for (String tech : tagFromIntent.getTechList()) {
            System.out.println(tech);
        }
        //读取TAG
        IsoDep nfca = IsoDep.get(tagFromIntent);
        new IOAsyncTask(this).execute(nfca);
    }

    /**
     * 解析 MifareClassic 技术标准的数据
     *
     * @param intent
     */
    private void processIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for (String tech : tagFromIntent.getTechList()) {
            System.out.println(tech);
        }
        boolean auth = false;
        //读取TAG
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + Util.byteArrayToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            Log.d(TAG, "processIntent: " + metaInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResult(String resultStr) {
        Log.d(TAG, "onResult: " + resultStr);
    }
}
