package com.goodgame.profiling.commons.boot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.boot.interfaces.Environment;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class BootLoaderTest {
    @Test
    public void testBootOrder() {
        @SuppressWarnings("unchecked") Subsystem<Environment> logging = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> stats = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> cachingTree = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> commandServer = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> somethingElse = mock(Subsystem.class);

        when( logging.getSystemIdentifier() )      .thenReturn( "logging" );
        when( stats.getSystemIdentifier() )        .thenReturn( "stats" );
        when( cachingTree.getSystemIdentifier() )  .thenReturn( "caching tree" );
        when( commandServer.getSystemIdentifier() ).thenReturn( "command server" );
        when( somethingElse.getSystemIdentifier() ).thenReturn( "something else" );

        when( logging.getRequiredSystems() )      .thenReturn( Collections.<String>emptyList() );
        when( stats.getRequiredSystems() )        .thenReturn( Arrays.asList( "logging" ) );
        when( cachingTree.getRequiredSystems() )  .thenReturn( Arrays.asList( "logging", "stats" ) );
        when( commandServer.getRequiredSystems() ).thenReturn( Arrays.asList( "logging", "stats", "caching tree" ) );
        when( somethingElse.getRequiredSystems() ).thenReturn( Arrays.asList( "logging", "stats", "caching tree" ) );

        BootLoader<Environment> subject = new BootLoader<>();
        subject.addSubsystem( logging );
        subject.addSubsystem( stats );
        subject.addSubsystem( cachingTree );
        subject.addSubsystem( commandServer );
        subject.addSubsystem( somethingElse );

        List<Subsystem<Environment>> bootOrder = subject.bootOrder();
        assertEquals( 5, bootOrder.size() );
        assertBootOrder( bootOrder, logging, stats );
        assertBootOrder( bootOrder, stats, cachingTree );
        assertBootOrder( bootOrder, cachingTree, commandServer );
        assertBootOrder( bootOrder, cachingTree, somethingElse );
    }

    @Test
    public void testBootOrderTransitive() {
        @SuppressWarnings("unchecked") Subsystem<Environment> logging = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> stats = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> cachingTree = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> commandServer = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> somethingElse = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> somethingElse2 = mock(Subsystem.class);

        when( logging.getSystemIdentifier() )      .thenReturn( "logging" );
        when( stats.getSystemIdentifier() )        .thenReturn( "stats" );
        when( cachingTree.getSystemIdentifier() )  .thenReturn( "caching tree" );
        when( commandServer.getSystemIdentifier() ).thenReturn( "command server" );
        when( somethingElse.getSystemIdentifier() ).thenReturn( "something else" );
        when( somethingElse2.getSystemIdentifier() ).thenReturn( "something else2" );

        when( logging.getRequiredSystems() )      .thenReturn( Collections.<String>emptyList() );
        when( stats.getRequiredSystems() )        .thenReturn( Arrays.asList( "logging" ) );
        when( cachingTree.getRequiredSystems() )  .thenReturn( Arrays.asList( "logging", "stats" ) );
        when( commandServer.getRequiredSystems() ).thenReturn( Arrays.asList( "logging", "stats", "caching tree" ) );
        when( somethingElse.getRequiredSystems() ).thenReturn( Arrays.asList( "logging", "stats", "caching tree" ) );
        when( somethingElse2.getRequiredSystems() ).thenReturn( Arrays.asList( "something else" ) );

        BootLoader<Environment> subject = new BootLoader<>();
        subject.addSubsystem( logging );
        subject.addSubsystem( stats );
        subject.addSubsystem( cachingTree );
        subject.addSubsystem( commandServer );
        subject.addSubsystem( somethingElse );
        subject.addSubsystem( somethingElse2 );

        List<Subsystem<Environment>> bootOrder = subject.bootOrder();
        assertEquals( 6, bootOrder.size() );
        assertBootOrder( bootOrder, logging, stats );
        assertBootOrder( bootOrder, stats, cachingTree );
        assertBootOrder( bootOrder, cachingTree, commandServer );
        assertBootOrder( bootOrder, cachingTree, somethingElse );
        assertBootOrder( bootOrder, somethingElse, somethingElse2 );
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCyclesThrow() {
        @SuppressWarnings("unchecked") Subsystem<Environment> logging = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> stats = mock(Subsystem.class);

        when( logging.getSystemIdentifier() )      .thenReturn( "logging" );
        when( stats.getSystemIdentifier() )        .thenReturn( "stats" );

        when( logging.getRequiredSystems() )      .thenReturn( Arrays.asList( "stats" ) );
        when( stats.getRequiredSystems() )        .thenReturn( Arrays.asList( "logging" ) );

        BootLoader<Environment> loader = new BootLoader<Environment>();
        loader.addSubsystem( logging );
        loader.addSubsystem( stats );

        loader.bootOrder();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testUnknownSystemsThrow() {
        @SuppressWarnings("unchecked") Subsystem<Environment> logging = mock(Subsystem.class);
        @SuppressWarnings("unchecked") Subsystem<Environment> stats = mock(Subsystem.class);

        when( logging.getSystemIdentifier() )      .thenReturn( "logging" );
        when( stats.getSystemIdentifier() )        .thenReturn( "stats" );

        when( logging.getRequiredSystems() )      .thenReturn( Arrays.asList( "starts" ) );
        when( stats.getRequiredSystems() )        .thenReturn( Arrays.asList( "logging" ) );

        BootLoader<Environment> loader = new BootLoader<Environment>();
        loader.addSubsystem( logging );
        loader.addSubsystem( stats );

        loader.bootOrder();
    }

    private void assertBootOrder( List<Subsystem<Environment>> bootOrder, Subsystem<Environment> earlier, Subsystem<Environment> later ) {
        assertTrue( bootOrder.indexOf( earlier ) < bootOrder.indexOf( later ) );
    }
}
