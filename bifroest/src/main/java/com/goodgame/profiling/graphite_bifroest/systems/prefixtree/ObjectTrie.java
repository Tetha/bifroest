package com.goodgame.profiling.graphite_bifroest.systems.prefixtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;

public final class ObjectTrie< K, V > {

    private final Map<K, ObjectTrie<K, V>> children;
    private V value;

    public ObjectTrie() {
        this.children = new HashMap<>();
        this.value = null;
    }

    private ObjectTrie( Map<K, ObjectTrie<K, V>> map ) {
        this.children = map;
        this.value = null;
    }

    public static < K, V > ObjectTrie<K, V> emptyTrie() {
        return new ObjectTrie<K, V>( Collections.<K, ObjectTrie<K, V>> emptyMap() );
    }

    public V value() {
        return value;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Set<Entry<K, ObjectTrie<K, V>>> children() {
        return children.entrySet();
    }

    public ObjectTrie<K, V> get( K key ) {
        return children.get( key );
    }

    public ObjectTrie<K, V> get( Queue<K> key ) {
        if ( key.isEmpty() ) {
            return this;
        } else if ( children.containsKey( key.peek() ) ) {
            return children.get( key.poll() ).get( key );
        } else {
            return null;
        }
    }

    public void add( Queue<K> key, V value ) {
        if ( key.isEmpty() ) {
            this.value = value;

        } else if ( children.containsKey( key.peek() ) ) {
            children.get( key.poll() ).add( key, value );

        } else {
            ObjectTrie<K, V> child = new ObjectTrie<>();
            children.put( key.poll(), child );
            child.add( key, value );
        }
    }

    @Deprecated
    public Collection<Queue<K>> collectKeys() {
        Collection<Queue<K>> accumulator = new ArrayList<>();
        Deque<K> stack = new ArrayDeque<>();
        collectKeys( stack, accumulator );
        return accumulator;
    }

    @Deprecated
    private void collectKeys( Deque<K> stack, Collection<Queue<K>> accumulator ) {
        if ( children.isEmpty() ) {
            // Reverse order
            LinkedList<K> result = new LinkedList<>();
            for ( K part : stack ) {
                result.addFirst( part );
            }
            accumulator.add( result );
        }
        for ( Entry<K, ObjectTrie<K, V>> child : children.entrySet() ) {
            stack.push( child.getKey() );
            child.getValue().collectKeys( stack, accumulator );
            stack.pop();
        }
    }

    public void forAllLeaves( BiConsumer<Collection<K>, V> consumer ) {
        Deque<K> deque = new ArrayDeque<>();
        forAllLeaves( consumer, deque );
    }

    private void forAllLeaves( BiConsumer<Collection<K>, V> consumer, Deque<K> deque ) {
        if ( children.isEmpty() ) {
            consumer.accept( deque, value );
        }
        for ( K childName : children.keySet() ) {
            deque.addLast( childName );
            children.get( childName ).forAllLeaves( consumer, deque );
            deque.removeLast();
        }
    }
}
