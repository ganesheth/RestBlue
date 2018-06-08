package com.manasaa.restblue;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class ScanResponse {
    public String scanState = "unknown";
    public int resultCount = 0;
    public List<ScanResult> scanResults = new ArrayList<>();
}
