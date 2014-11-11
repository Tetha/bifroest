package com.goodgame.profiling.rewrite_framework.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HostnameInverter {
    private static final Logger log = LogManager.getLogger();

    private static Pattern IP_AND_MORE_PATTERN = Pattern.compile( "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}" );

    private HostnameInverter() {
        // Don't instantiate
    }

    public static String invertHostname( String host ) {
        log.entry( host );
        String[] chunks = host.split( "\\." );

        Matcher ipMatch = IP_AND_MORE_PATTERN.matcher( host );
        log.trace( ipMatch );
        if ( ipMatch != null && ipMatch.matches() ) {
            // IPs are already ordered low variability -> high variability
            return log.exit( host );
        } else {
            // dns names are high variability -> low variability
            CollectionUtils.reverseArray( chunks );
            return log.exit( String.join( ".", chunks ) );
        }
    }
}
