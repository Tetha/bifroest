package com.goodgame.profiling.graphite_retentions;

import java.util.List;

public class RetentionLevelIterator {
    private final List<RetentionLevel> levels;
    private int i;

    RetentionLevelIterator( List<RetentionLevel> levels ) {
        this.levels = levels;
        this.i = 0;
    }

    public RetentionLevel sourceLevel() {
        return levels.get( i );
    }

    public RetentionLevel targetLevel() {
        return i+1 < levels.size() ? levels.get( i+1 ) : null;
    }

    public void advance() {
        i++;
    }

    public boolean isValid() {
        return i < levels.size();
    }
}
