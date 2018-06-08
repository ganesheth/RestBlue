package com.manasaa.restblue;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class HTTPServer extends NanoHTTPD {
    public static final int PORT = 8080;
    private HTTPCallback mCallback;

    public HTTPServer(HTTPCallback callback) throws IOException {
        super(PORT);
        mCallback = callback;
    }

    @Override
    public NanoHTTPD.Response serve(IHTTPSession session) {

        String uri = session.getUri();
        String method = session.getMethod().name();
        String response = mCallback.handleRequest(uri, method, session.getInputStream(), session.getHeaders() );
        if(response == null)
            return null;
        Response httpResponse =  newFixedLengthResponse(response);
        httpResponse.addHeader("Content-Type", "application/json");
        return httpResponse;
    }
}
