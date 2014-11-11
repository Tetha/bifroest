package com.goodgame.profiling.graphite_bifroest.metric_cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BackendPool {
    private static final Logger log = LogManager.getLogger();

    private final Deque<OneMetricCacheBackend> freeBackends;
    private final ReferenceQueue<OneMetricCache> refQueue;
    private final Set<Reference<OneMetricCache>> references;

    private final int minimumRemaining;

    public BackendPool( int visibleCacheSize, int totalCacheSize, int cacheLineWidth ) {
        this.freeBackends = new LinkedList<>();
        for ( int i = 0; i < totalCacheSize; i++ ) {
            this.freeBackends.addLast( new OneMetricCacheBackend( cacheLineWidth ) );
        }

        this.minimumRemaining = totalCacheSize - visibleCacheSize;

        this.refQueue = new ReferenceQueue<>();
        this.references = new HashSet<>();
    }

    private void reclaimElementsFromRefQueue() {
        Reference<? extends OneMetricCache> current;
        while ( ( current = refQueue.poll() ) != null ) {
            if ( current instanceof FrontendWeakReference ) {
                OneMetricCacheBackend backend = ( (FrontendWeakReference)current ).backend();
                backend.reset();
                freeBackends.addFirst( backend );
            } else {
                log.error( "Reference Queue contained something that wasn't a FrontendWeakReference!" );
            }
        }
    }

    private void cleanUpReferences() {
        references.removeIf( ref -> ref.get() == null );
    }

    synchronized public boolean isEmpty() {
        return freeBackends.size() <= minimumRemaining;
    }

    synchronized public Optional<OneMetricCacheBackend> getNextFree() {
        reclaimElementsFromRefQueue();
        cleanUpReferences();
        return Optional.ofNullable( freeBackends.pollFirst() );
    }

    synchronized public void notifyFrontendCreated( OneMetricCache frontend ) {
        references.add( new FrontendWeakReference( frontend, refQueue ) );
    }

    private static class FrontendWeakReference extends WeakReference<OneMetricCache> {
        private final OneMetricCacheBackend backend;

        private FrontendWeakReference( OneMetricCache frontend, ReferenceQueue<? super OneMetricCache> q ) {
            super( frontend, q );
            this.backend = frontend.getBackend();
        }

        public OneMetricCacheBackend backend() {
            return backend;
        }
    }
}
