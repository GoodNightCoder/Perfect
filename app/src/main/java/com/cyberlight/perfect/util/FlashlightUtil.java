package com.cyberlight.perfect.util;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class FlashlightUtil {

    private static boolean sRunning = false;
    private static String sCanFlashCameraId;

    public static void flash(Context context, long[] timings) {
        if (!sRunning) {
            sRunning = true;
            try {
                // 获取相机管理器
                CameraManager cameraManager =
                        (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                // 获取摄像头
                String[] cameraIds = cameraManager.getCameraIdList();
                // 遍历摄像头，找有闪光灯的摄像头
                sCanFlashCameraId = null;
                if (cameraIds != null && cameraIds.length > 0) {
                    for (String cId : cameraIds) {
                        // getCameraCharacteristics获取摄像头的特性
                        CameraCharacteristics cCs = cameraManager.getCameraCharacteristics(cId);
                        boolean canFlash = cCs.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        if (canFlash) {
                            sCanFlashCameraId = cId;
                            break;
                        }
                    }
                }
                if (sCanFlashCameraId != null) {
                    // 调用闪光灯
                    new Thread(() -> {
                        boolean isOpen = false;
                        try {
                            // 按照timings数组交替闪烁
                            for (long timing : timings) {
                                cameraManager.setTorchMode(sCanFlashCameraId, isOpen);
                                Thread.sleep(timing);
                                isOpen = !isOpen;
                            }
                            // 最后确保关闭闪光灯
                            cameraManager.setTorchMode(sCanFlashCameraId, false);
                        } catch (InterruptedException | CameraAccessException e) {
                            e.printStackTrace();
                        }
                        sRunning = false;
                    }).start();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}