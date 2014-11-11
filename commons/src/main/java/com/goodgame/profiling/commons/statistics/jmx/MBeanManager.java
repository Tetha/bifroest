package com.goodgame.profiling.commons.statistics.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MBeanManager {

	private static final Logger log = LogManager.getLogger();

	public static < T > void registerStandardMBean( T object, Class<T> mbeanInterface ) {
		String name = "" + object.getClass().getPackage().getName() + ":type=" + object.getClass().getSimpleName();
		registerStandardMBean( object, name, mbeanInterface );
	}

	public static < T > void registerStandardMBean( T object, String name, Class<T> mbeanInterface ) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		StandardMBean mBean = null;

		if ( object == null ) {
			log.warn( "The object with the object name <" + name + "> which should be registered is null." );
			return;
		}

		try {
			mBean = new StandardMBean( object, mbeanInterface );
		} catch ( NotCompliantMBeanException e1 ) {
			log.warn( e1 );
			return;
		}

		ObjectName objectName;
		try {
			objectName = new ObjectName( name );
		} catch ( MalformedObjectNameException e ) {
			log.warn( e );
			return;
		} catch ( NullPointerException e ) {
			log.warn( e );
			return;
		}

		if ( !mbs.isRegistered( objectName ) ) {
			try {
				mbs.registerMBean( mBean, objectName );

				if ( log.isDebugEnabled() ) {
					log.debug( "The MBean with the given name <" + name + "> is registered." );
				}
			} catch ( InstanceAlreadyExistsException e ) {
				log.warn( e );
				return;
			} catch ( MBeanRegistrationException e ) {
				log.warn( e );
				return;
			} catch ( NotCompliantMBeanException e ) {
				log.warn( e );
				return;
			}
		}
	}

}
