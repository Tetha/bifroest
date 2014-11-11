package com.goodgame.profiling.commons.systems.rmi_jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;

/**
 * Service class to start and stop JMX during runtime
 * 
 * @author Jörn Ahlers, sglimm
 */
public class JMXSystem<E extends EnvironmentWithJSONConfiguration> implements Subsystem<E> {
	private static final Logger log = LogManager.getLogger();

	private Registry rmiRegistry;
	private JMXConnectorServer connector;
	private ServerSocketFactory serverSocketFactory;

	@Override
	public String getSystemIdentifier() {
		return SystemIdentifiers.RMIJMX;
	}

	@Override
	public Collection<String> getRequiredSystems() {
		return Arrays.asList(
				SystemIdentifiers.LOGGING,
				SystemIdentifiers.CONFIGURATION);
	}

	/**
	 * Starts the jmx server with specified hostname, ports and access/password files
	 */
	@Override
	public void boot(E environment) throws IOException {
		JSONObject config = environment.getConfiguration().getJSONObject("rmi-jmx");
		int rmiPort = config.getInt("rmiport");
		int jmxPort = config.getInt("jmxport");
		String jmxAccessFile = config.getString("accessfile");
		String jmxPasswordFile = config.getString("passwordfile");
		String hostname = config.getString("hostname");

		if (rmiPort <= 0 || jmxPort <= 0) {
			throw new IllegalArgumentException("Ports must be positive.");
		}

		if (connector != null) {
			return; // there is already an existing connector
		}

		InetAddress address = InetAddress.getByName(hostname);
		serverSocketFactory = new ServerSocketFactory(address);
		if (rmiRegistry == null) {
			rmiRegistry = LocateRegistry.createRegistry(rmiPort, null, serverSocketFactory);
		}

		String serviceUrl = "service:jmx:rmi://"
				+ address.getHostAddress() + ":" + jmxPort
				+ "/jndi/rmi://"
				+ address.getHostAddress() + ":" + rmiPort
				+ "/jmxrmi";
		JMXServiceURL url = new JMXServiceURL(serviceUrl);
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

		Map<String, Object> env = new HashMap<>();
		env.put("jmx.remote.x.authenticate", true);
		env.put("jmx.remote.x.local.only", false);
		env.put("jmx.remote.x.access.file", jmxAccessFile);
		env.put("jmx.remote.x.password.file", jmxPasswordFile);

		connector = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbeanServer);
		connector.start();

		log.info("Created JMX connection on " + serviceUrl);
	}

	/**
	 * Stops the jmx server and all connections that are currently active.
	 * Frees up the bound port.
	 */
	@Override
	public void shutdown(E environment) {
		if (connector != null) {
			try {
				connector.stop();
			} catch (IOException e) { /* We have done what we could */
			}
			connector = null;
		}
		if (rmiRegistry != null) {
			try {
				UnicastRemoteObject.unexportObject(rmiRegistry, true);
			} catch (NoSuchObjectException e) {
				log.warn(e);
			}
			rmiRegistry = null;
		}
		if (serverSocketFactory != null
				&& serverSocketFactory.getLast() != null) {
			try {
				serverSocketFactory.getLast().close();
			} catch (IOException e) {
				log.catching(e);
			}
			serverSocketFactory = null;
		}
	}
}
