package com.goodgame.profiling.rewrite_framework.output.rewriter.modifier;

import java.util.ServiceLoader;

public class ModifierCreator {
    private static final ServiceLoader<ModifierFactory> modifierFactories = ServiceLoader.load( ModifierFactory.class );

    public static Modifier create( String modifierName, String modifierParam ) {
        for ( ModifierFactory factory : modifierFactories ) {
            if ( factory.handledType().equalsIgnoreCase( modifierName ) ) {
                return factory.create( modifierParam );
            }
        }
        throw new IllegalArgumentException( "Cannot handle modifier " + modifierName );
    }
}
