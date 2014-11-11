package com.goodgame.profiling.rewrite_framework.drain.carbon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.rewrite_framework.core.drain.AbstractBasicDrain;
import com.goodgame.profiling.rewrite_framework.drain.statistics.DrainMetricOutputEvent;

public class CarbonPlainTextDrain  extends AbstractBasicDrain {

	private final String carbonHost;
	private final int carbonPort;

	public CarbonPlainTextDrain( String carbonHost, int carbonPort ) {
		this.carbonHost = carbonHost;
		this.carbonPort = carbonPort;
	}

	@Override
	public void output(List<Metric> metrics) throws IOException {

		StringBuilder buffer = new StringBuilder();

		for (Metric metric : metrics) {
			String line = metric.name() + " " + metric.value() + " " + metric.timestamp() + "\n";
			buffer.append(line);
		}

		try ( Socket carbonSocket = new Socket( carbonHost, carbonPort ) ) {
			BufferedWriter toCarbon = new BufferedWriter( new OutputStreamWriter( carbonSocket.getOutputStream() ) );
			toCarbon.write( buffer.toString() );
			toCarbon.flush();
		}
		
		EventBusManager.fire( new DrainMetricOutputEvent( ( new CarbonPlainTextDrainFactory<>().handledType() ), metrics.size() ) );
		buffer = new StringBuilder(buffer.length());
	}
}
