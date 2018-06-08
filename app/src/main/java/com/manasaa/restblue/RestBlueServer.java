package com.manasaa.restblue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.content.Context.BLUETOOTH_SERVICE;

public class RestBlueServer implements BLEClientCallback, HTTPCallback {

    private BLEClient mBleClient;
    private HTTPServer mHttpServer;
    private ScanResponse mLastScanResults;
    private Gson mJsonSerializer;

    public RestBlueServer(Context context) throws IOException{
        mLastScanResults = new ScanResponse();
        mJsonSerializer = new Gson();
        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        mBleClient = new BLEClient(this, adapter);
        mHttpServer = new HTTPServer(this);
        mHttpServer.start();
    }

    @Override
    public void onScanStart() {
        mLastScanResults.scanState = "started";
        mLastScanResults.scanResults = null;
    }

    @Override
    public void onScanDeviceFound(ScanResult result) {

    }

    @Override
    public void onScanEnd(List<ScanResult> results) {
        mLastScanResults.scanResults = results;
        mLastScanResults.scanState = "completed";
        mLastScanResults.resultCount = results.size();
    }

    @Override
    public void onDataReadComplete(List<byte[]> data, int expected, long timeTaken) {

    }

    @Override
    public void onDataReceived(byte[] data, int total, int current) {

    }

    @Override
    public void onPeripheralConnectionStateChange(String message) {

    }

    @Override
    public String handleRequest(String uri, String method, InputStream inputStream, Map<String, String> headers) {
        if(uri.equals("/startScan")) {
            mBleClient.startScan(null, 10000, false);
            return "started";
        }else if(uri.equals("/scanResults")){
            String json = mJsonSerializer.toJson(mLastScanResults).replace("\"m", "\"");
            return json;
        }
        else
            return null;
    }
}
