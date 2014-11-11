package com.goodgame.profiling.rewrite_framework.core.source.timestamp;

import org.json.JSONObject;

public interface TimestampFactory {

    String handledType();

    Timestamp create( Timestamp oldTimestamp, JSONObject config );

}
