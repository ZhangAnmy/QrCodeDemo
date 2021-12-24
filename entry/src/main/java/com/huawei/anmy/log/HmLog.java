package com.huawei.anmy.log;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class HmLog {
    private static final String TAG = "QrCodeDemo";
    private static final int DOMAIN = 0x00000;
    private static final HiLogLabel LOG_LABLE = new HiLogLabel(HiLog.LOG_APP, DOMAIN, TAG);

    public static void e(String message) {
        HiLog.error(LOG_LABLE, "%{public}s", message);
    }

    public static void i(String message) {
        HiLog.error(LOG_LABLE, "%{public}s", message);
    }

    public static void d(String message) {
        HiLog.debug(LOG_LABLE, "%{public}s", message);
    }

    public static boolean isDebug() {
        return HiLog.isLoggable(DOMAIN, TAG, HiLog.DEBUG);
    }
}
