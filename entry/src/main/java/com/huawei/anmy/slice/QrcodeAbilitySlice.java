package com.huawei.anmy.slice;

import com.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import com.huawei.anmy.ResourceTable;
import com.huawei.anmy.log.HmLog;
import com.huawei.watch.kit.hiwear.HiWear;
import com.huawei.watch.kit.hiwear.p2p.*;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Image;
import ohos.media.image.PixelMap;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class QrcodeAbilitySlice extends AbilitySlice {
    // image size of barcode
    private static final int SIZE = 280;
    // content of image barcode
    private String CONTENT = "HC1:XXXXXXXX-H6SKJPT.-7G2TZ978S8SFBXEJW.TFJTXG41UQR$TTSJSQKX9SAN9I6T5XH-G2%E3EV4*2DYFPU*0CEBQ/GXQFY735LBJU0OJJYT0139%2H8JFII7HH5V.40ATPHN7Y47%S7Y48YIZ73423ZQTZABLD3XW43%2..P*PP:+P*.1D9R+Q6646S%0.LJB/S7-SN2H N37J3JFTULJ5CB8X2+36D-I/2DBAJDAJCNB-43 X45X2DPF1BJ3X8I235AL5:4A93/IBZQT.EJFG3025RZ4E%5MK9 O99TUT+U31AC9Q+8IRCEFSHY*UB/9MM5G118AL--I2$8QJAZGA+1V2:U2E4VG0-YFCO5AUN4*CO1RTAVYB3IUQ LB3I8S*65HRB3O*TT+XK41I02SFW5E-3*1F3EAYHT9VUKK3CB7JOAR5B9BH1JF4:52 XXXXXXXXXX";
    private Image mImage;
    private String peerPkgName = "com.huawei.anmywatchdemo01";
    private String peerFinger = "XXXXX8DE205EC95AC98AA4DA45FC6779213196AFDF23E63744F0D770657XXXXX";
    private Receiver mReceiver;
    private P2pClient mP2pClient;
    private PixelMap pixelMap;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_qrcode);
        mImage = (Image) findComponentById(ResourceTable.Id_image);

        // Step 1: Obtain the P2P communication object.
        mP2pClient = HiWear.getP2pClient(this);

        // Step 2: Set the package name of your phone app to be communicated with.
        mP2pClient.setPeerPkgName(peerPkgName);

        // Step 3: Set the fingerprint information for the phone app
        mP2pClient.setPeerFingerPrint(peerFinger);

        startPing();
        generateBarcode();
        /*try {
            sendP2pMessage();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

    }

    /**
     * Check whether your app on the phone is running by using the ping function
     */
    private void startPing() {
        // Step 3: Check that your phone app is running.
        mP2pClient.ping(new PingCallback() {
            @Override
            public void onResult(int i) {
                HmLog.d("####ping result is:" + i);
                if (i == 205) {
                    registerReceiver();
                }
            }
        });
    }

    /**
     * Send a short message to your app on the phone
     */
    private void sendP2pMessage() throws UnsupportedEncodingException {
        String messageStr = "Hello, Wear Engine!";
        HiWearMessage.Builder builder = new HiWearMessage.Builder();
        builder.setPayload(messageStr.getBytes("UTF-8"));
        HiWearMessage sendMessage = builder.build();
        // Create the callback method.
        SendCallback sendCallback = new SendCallback() {
            @Override
            public void onSendResult(int resultCode) {
                HmLog.d("####SendP2PMessage, resultCode is: " + resultCode);
            }

            @Override
            public void onSendProgress(long progress) {
                HmLog.d("####SendP2PMessage, progress is: " + progress);
            }
        };
        if (sendMessage != null && sendCallback != null) {
            mP2pClient.send(sendMessage, sendCallback);
        }
    }

    /**
     * Receive the message or file from your app on the phone
     */
    private void registerReceiver() {
        mReceiver = message -> {
            switch (message.getType()) {
                // Receive the short message
                case HiWearMessage.MESSAGE_TYPE_DATA: {
                    HmLog.d("####Receive P2P Message from Phone");
                    if (message.getData() != null) {
                        String msg = new String(message.getData(), StandardCharsets.UTF_8);
                        CONTENT = msg;
                    }
                    generateBarcode();
                    break;
                }
                // Receive the file
                case HiWearMessage.MESSAGE_TYPE_FILE: {
                    HmLog.d("####Receive P2P file from Phone");
                    File file = message.getFile();
                    if (file != null && file.exists()) {
                        HmLog.d("####Receive P2P file name: " + file.getName());
                    }
                    break;
                }
                default:
                    HmLog.i("unsupported message type");
            }
        };
        mP2pClient.registerReceiver(mReceiver);
    }

    /**
     * Cancel the reception of the message or file from your app on the phone
     */
    private void unregisterReceiver() {
        if (mP2pClient != null && mReceiver != null) {
            mP2pClient.unregisterReceiver(mReceiver);
        }
    }

    private void generateBarcode() {
        pixelMap = QRCodeEncoder.syncEncodeQRCode(CONTENT, SIZE, SIZE);
        mImage.setPixelMap(pixelMap);
        unregisterReceiver();
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
