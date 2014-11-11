package com.goodgame.profiling.commons.util.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONClient {
    private final String host;
    private final int port;

    public JSONClient( String host, int port ) {
        this.host = host;
        this.port = port;
    }

    public JSONObject request( JSONObject request ) throws IOException {
        try( Socket socket = new Socket( host, port );
                Writer writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) ) ) {
            request.write( writer );
            writer.flush();
            return new JSONObject( new JSONTokener( new InputStreamReader( socket.getInputStream() ) ) );
        }
    }
}
