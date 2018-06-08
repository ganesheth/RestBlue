package com.manasaa.restblue;

import android.bluetooth.le.ScanResult;

import java.util.List;

public interface BLEClientCallback{
    public void onScanStart();

    public void onScanDeviceFound(ScanResult result);

    public void onScanEnd(List<ScanResult> results);

    public void onDataReadComplete(List<byte[]> data, int expected, long timeTaken);

    public void onDataReceived(byte[] data, int total, int current);

    public void onPeripheralConnectionStateChange(String message);
}