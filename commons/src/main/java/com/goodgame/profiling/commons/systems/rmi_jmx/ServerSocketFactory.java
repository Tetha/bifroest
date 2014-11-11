package com.goodgame.profiling.commons.systems.rmi_jmx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory for a ServerSocket which is required to create a rmi registry.
 * 
 * @author Jörn Ahlers, sglimm
 */
public class ServerSocketFactory implements RMIServerSocketFactory {
    private static final Logger log = LogManager.getLogger();

	private InetAddress address;
	private ServerSocket lastSocket = null;

	public ServerSocketFactory(InetAddress address) {
		this.address = address;
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		if (lastSocket != null) {
			log.warn("Creating multiple sockets with one ServerSocketFactory - is this right?");
		}
		
		lastSocket = new ServerSocket(port, 0, this.address);
		return lastSocket;
	}
	
	public ServerSocket getLast() {
		return lastSocket;
	}

	public boolean equals(Object obj) {
		if ((obj == null) || (super.getClass() != obj.getClass())) {
			return false;
		}
		ServerSocketFactory other = (ServerSocketFactory) obj;
		if (address == null) {
			return (other.address == null);
		}
		return address.equals(other.address);
	}

	public int hashCode() {
		return this.address == null ? 0 : this.address.hashCode();
	}
}
