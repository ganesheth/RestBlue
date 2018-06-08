package com.manasaa.restblue;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by Ganesh on 23-Mar-18.
 */

public class BLEClient {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    //private Context mContext;
    private BLEClientCallback mCallback;
    private List<ScanResult> mScanResults = new ArrayList<>();
    private int mTotalExpectedNotifications = 0;
    private int mExpectedNotifications = 0;
    private List<byte[]> mLeechedData = new ArrayList<>();
    //private boolean mConnecting = false;
    private Date mLastConnection;
    private long mTimeTakenForDataExtraction = 0;
    private boolean mReportInvidualResults = false;
    private String TAG_BLE = "BLE";
    private String TAG_BLE_SCANNING = "BLE_SCANNING";


    public BLEClient(BLEClientCallback callback, BluetoothAdapter adapter){
        mBluetoothAdapter = adapter;
        mCallback = callback;
        //mContext = context;

        mHandler = new Handler();
    }

    public void startScan(List<String> serviceUUIDs, int duration, boolean sendIndividualResults){

        ScanSettings scanSettings;
        scanSettings = new ScanSettings.Builder().setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)//.build();
                //.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE).build();
                .setCallbackType(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT).build();

        List<ScanFilter> scanFilters = null;
        if(serviceUUIDs != null) {
            scanFilters = new ArrayList<>();
            for (String filterStr : serviceUUIDs) {
                ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(filterStr))).build();
                scanFilters.add(scanFilter);
            }
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {stopScan();}
        }, duration);

        mScanning = true;
        mScanResults.clear();
        try {
            Log.i(TAG_BLE_SCANNING, "0. Starting ble scan");
            mReportInvidualResults = sendIndividualResults;
            if(scanFilters == null)
                mBluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, mScanCallback);
            else
                mBluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, mScanCallback);
            mCallback.onScanStart();
        } catch (Exception e) {
            Log.e(TAG_BLE_SCANNING, e.getMessage());
        }
    }

    public void stopScan(){
        mHandler.removeCallbacks(null);
        mScanning = false;
        Log.i(TAG_BLE_SCANNING, String.format("0. Stopping ble scan. Got %d tags", mScanResults.size()));
        try {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        } catch (Exception e) {
            Log.e(TAG_BLE_SCANNING, e.getMessage());
        }
        mCallback.onScanEnd(mScanResults);
    }

    public boolean isScanning(){
        return mScanning;
    }

    private boolean enoughTimeSinceLastConnection(){
        long secondsSinceLastConnection = 60;
        if(mLastConnection != null){
            Date now = new Date();
            long diff = now.getTime() - mLastConnection.getTime();
            secondsSinceLastConnection = diff / 1000 % 60;
        }
        return secondsSinceLastConnection > 20;
    }


    //------------------------------------------- Scanning Callbacks ----------------------------
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i("BLE", "0.1 Got scan result");
            for(ScanResult r : mScanResults){
                if(r.getDevice().getAddress().compareTo(result.getDevice().getAddress())==0) {
                    Log.i("BLE", "0.1. Result is a duplicate. Ignoring.");
                    return;
                }
            }
            /*
            if(okToConnect()) //Need to connect?
            {
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                BluetoothDevice device = result.getDevice();
                mConnecting = true;
                Log.i("BLE", "1. Invoking connection");
                mCallback.onPeripheralConnectionStateChange("Connecting");
                mLeechedData.clear();
                mTimeTakenForDataExtraction = SystemClock.elapsedRealtime();
                device.connectGatt(mContext, false, mBluetoothGattCallback);
            }
            */
            mScanResults.add(result);
            if(mReportInvidualResults){
                mCallback.onScanDeviceFound(result);
            }
        }


        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            mScanResults.addAll(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("BLE", String.format("0.1 Scan failed with code %d", errorCode));
            mBluetoothAdapter.disable();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.enable();
                }
            }, 2000);
        }
    };

    //----------------------------- Connected mode callbacks ---------------------------------
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i("BLE", String.format("On connection state change %d new state %d", status, newState));
            if(newState == BluetoothGatt.STATE_CONNECTED){
                if(!enoughTimeSinceLastConnection()){
                    Log.i("BLE", "2. Connected, but too not enough time since last connection. Disconnecting!");
                    //mCallback.onPeripheralConnectionStateChange("Disconnecting");
                    //gatt.disconnect();
                    //return;
                }
                mLastConnection = new Date();
                Log.i("BLE", "2. Connected");
                mCallback.onPeripheralConnectionStateChange("Connected to aggregator");
                boolean res = gatt.discoverServices();
            } else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                Log.i("BLE", "8. Disconnected");
                //mConnecting = false;
                mTimeTakenForDataExtraction = SystemClock.elapsedRealtime() - mTimeTakenForDataExtraction;
                mCallback.onDataReadComplete(mLeechedData, mTotalExpectedNotifications, mTimeTakenForDataExtraction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i("BLE", "3. ervices discovered. Reading characterstic");
            //BluetoothGattCharacteristic c = gatt.getService(BLEDevice.UART_SERVICE_ID).getCharacteristic(BLEDevice.TX_READ_CHAR_ID);
            //boolean res = gatt.readCharacteristic(c);
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i("BLE", "4. Read characterstic. Writing descriptor");
            byte[] data = characteristic.getValue();

            if(data.length == 1)
                mExpectedNotifications = data[0] & 0xff;
            else if(data.length == 2)
                mExpectedNotifications = data[0] & 0xff + (data[1] << 8);
            else
                mExpectedNotifications = -1;

            mTotalExpectedNotifications = mExpectedNotifications;
            boolean res = gatt.setCharacteristicNotification(characteristic, true);
            //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEDevice.TX_READ_CHAR_DESC_ID);
            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            //boolean success = gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("BLE", "6. Received notification");
            byte[] data = characteristic.getValue();
            mLeechedData.add(data);
            mExpectedNotifications--;
            mCallback.onDataReceived(data, mTotalExpectedNotifications, mLeechedData.size());
            mHandler.removeCallbacksAndMessages(null);
            int wait = mExpectedNotifications <= 0 ? 10 : 1000;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLEDevice.TX_READ_CHAR_DESC_ID);
                    //descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    Log.i("BLE", mExpectedNotifications <= 0 ? "6b. No more notifications expected" : "6c. Notification timeout");
                    //boolean success = gatt.writeDescriptor(descriptor);
                    gatt.setCharacteristicNotification(characteristic, false);
                    mCallback.onPeripheralConnectionStateChange("Notifications ended.");
                    gatt.disconnect();
                }
            }, wait);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i("BLE", "5. Written to descriptor");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
}

