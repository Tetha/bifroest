package com.goodgame.profiling.graphite_bifroest.systems.prefixtree;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class PrefixTreeTest {

    @Test
    public void test() {
        PrefixTree tree = new PrefixTree();
        tree.addEntry("server.test-graphite-backup01.services.de.ggs-net.com.System.Entropy");
        tree.addEntry("server.test-graphite-carbon01.services.de.ggs-net.com.System.Entropy");
        tree.addEntry("server.test-graphite-web01.services.de.ggs-net.com.System.Entropy");
        
        Collection<Pair<String, Boolean>> result = new HashSet<>(tree.getPrefixesMatching("server.{test-graphite-backup01,test-graphite-carbon01,test-graphite-web01}.services.de.ggs-net.com.System.Entropy"));
        
        Collection<Pair<String, Boolean>> expected = new HashSet<>(Arrays.<Pair<String, Boolean>>asList(
                new ImmutablePair<String, Boolean>("server.test-graphite-backup01.services.de.ggs-net.com.System.Entropy", true),
                new ImmutablePair<String, Boolean>("server.test-graphite-carbon01.services.de.ggs-net.com.System.Entropy", true),
                new ImmutablePair<String, Boolean>("server.test-graphite-web01.services.de.ggs-net.com.System.Entropy", true)
                ));
        
        assertEquals(expected, result);
    }

}
