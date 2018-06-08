package com.manasaa.restblue;

import java.io.InputStream;
import java.util.Map;

public interface HTTPCallback {

    public String handleRequest(String uri, String method, InputStream inputStream, Map<String, String> headers);
}
