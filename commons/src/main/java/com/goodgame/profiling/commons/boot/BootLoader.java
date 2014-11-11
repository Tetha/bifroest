package com.goodgame.profiling.commons.boot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.util.ProfilingUtils;

public class BootLoader<E extends Environment> implements InitD {

    private final List<Subsystem<E>> systems = new ArrayList<Subsystem<E>>();
    private E env;
    private List<Subsystem<E>> bootOrder;
    private int numBootedSystems = 0;

    public void addSubsystem( Subsystem<E> system ) {
        systems.add( system );
    }

    public void boot( E env ) throws Exception {
        if ( numBootedSystems != 0 ) {
            throw new IllegalStateException( "Cannot start the systems twice!" );
        }
        this.env = env;
        Runtime.getRuntime().addShutdownHook( new Shutdown() );
        try {
            for( Subsystem<E> system : bootOrder() ) {
                System.out.println( ProfilingUtils.getCurrentTimeStamp() + " Starting " + system.getSystemIdentifier() );
                system.boot( env );
                numBootedSystems++;
            }
            System.out.println( ProfilingUtils.getCurrentTimeStamp() + " Service startup successsful. " );
        } catch( Exception e ) {
            // If a system can throw during boot, but it can handle the
            // exceptions, it should not allow the exceptions to reach the boot
            // loader. Hence, if an exception is severe enough to get to this
            // point, we just shut everything down again.
            System.out.println( ProfilingUtils.getCurrentTimeStamp() + " Error while startup: " );
            e.printStackTrace();
            System.exit( 1 );
        }
    }

    public void shutdown() {
        System.out.println( ProfilingUtils.getCurrentTimeStamp() + " Shutting down..." );
        for( ; numBootedSystems > 0; numBootedSystems-- ) {
            Subsystem<E> currentSystem = bootOrder().get( numBootedSystems - 1 );
            System.out.println( ProfilingUtils.getCurrentTimeStamp() + " Shutting down " + currentSystem.getSystemIdentifier() );
            currentSystem.shutdown( env );
        }
        System.out.println( ProfilingUtils.getCurrentTimeStamp() + " Shutdown complete" );
    }

    private class Shutdown extends Thread {
        @Override
        public void run() {
            shutdown();
        }
    }

    private void computeBootOrder() {
        Collection<String> systemNames = new ArrayList<>( systems.size() );
        for( Subsystem<E> system : systems ) {
            systemNames.add( system.getSystemIdentifier() );
        }

        // Check for unknown systems
        for( Subsystem<E> system : systems ) {
            if ( !systemNames.containsAll( system.getRequiredSystems() ) ) {
                List<String> requirements = new ArrayList<>( system.getRequiredSystems() );
                requirements.removeAll( systemNames );
                throw new IllegalArgumentException( " Unknown systems " + StringUtils.join( requirements, "," ) + " requested" );
            }
        }

        Collection<String> initializedSystems = new ArrayList<>( systems.size() );
        List<Subsystem<E>> uninitializedSystems = new ArrayList<>( systems );
        //
        // Shuffle so no one can get lucky often with broken
        // system requirements.
        Collections.shuffle( uninitializedSystems );

        Map<String, List<String>> systemToRequirement = new HashMap<>();
        for( Subsystem<E> system : systems ) {
            List<String> actualRequirements = new ArrayList<>();
            actualRequirements.addAll( system.getWeaklyRequiredSystems() );
            actualRequirements.retainAll( systemNames );
            actualRequirements.addAll( system.getRequiredSystems() );
            systemToRequirement.put( system.getSystemIdentifier(), actualRequirements );
        }

        bootOrder = new ArrayList<Subsystem<E>>( systems.size() );
        while( uninitializedSystems.size() > 0 ) {
            boolean added = false;
            for( Subsystem<E> system : uninitializedSystems ) {
                if ( initializedSystems.containsAll( systemToRequirement.get( system.getSystemIdentifier() ) ) ) {
                    bootOrder.add( system );
                    uninitializedSystems.remove( system );
                    initializedSystems.add( system.getSystemIdentifier() );
                    added = true;
                    break;
                }
            }
            if ( !added ) {
                StringBuilder order = new StringBuilder();
                for( Subsystem<E> system : bootOrder ) {
                    order.append( system.getSystemIdentifier() ).append( ">" );
                }
                throw new IllegalArgumentException( " Cannot find bootable system after partial order: " + order.toString() );
            }
        }
    }

    // package private for tests
    List<Subsystem<E>> bootOrder() {
        if ( bootOrder == null ) {
            computeBootOrder();
            StrBuilder bootOrderString = new StrBuilder( " Figured out bootOrder: " );
            for( Subsystem<E> system : bootOrder ) {
                bootOrderString.appendSeparator( " > " ).append( system.getSystemIdentifier() );
            }
            System.out.println( bootOrderString );
        }
        return bootOrder;
    }

}
