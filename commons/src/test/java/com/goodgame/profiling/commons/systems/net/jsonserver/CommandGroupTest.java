package com.goodgame.profiling.commons.systems.net.jsonserver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.statistics.eventbus.EventBus;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.util.Either;

public final class CommandGroupTest {

	@Mock
	private Environment environment;

	@Mock
	private EventBus eventBus;

	@Mock
	private Command<Environment> commandA;
	@Mock
	private Command<Environment> commandB;
	@Mock
	private Command<Environment> commandC;

	@Mock
	private JSONObject resultA;
	@Mock
	private JSONObject resultB;
	@Mock
	private JSONObject resultC;

	private CommandGroup<Environment> subject;

	@Before
	public void setupTestEnvironment() {
		MockitoAnnotations.initMocks( this );
		
		EventBusManager.setEventBus(eventBus);

		when( commandA.getJSONCommand() ).thenReturn( "A" );
		when( commandB.getJSONCommand() ).thenReturn( "B" );
		when( commandC.getJSONCommand() ).thenReturn( "C" );

		when( commandA.execute( any( JSONObject.class ), any( Environment.class ) ) ).thenReturn( resultA );
		when( commandB.execute( any( JSONObject.class ), any( Environment.class ) ) ).thenReturn( resultB );
		when( commandC.execute( any( JSONObject.class ), any( Environment.class ) ) ).thenReturn( resultC );

		subject = new CommandGroup<Environment>( "someName" );
		subject.add( commandA );
		subject.add( commandB );
		subject.add( commandC );
	}

	@Test
	public void testCommandSelectionAndExecution() {
		JSONObject input = new JSONObject();
		input.put( "command", "B" );

		assertEquals( Either.ofGoodValue( resultB ),
                              subject.executeJSON( input, environment ) );

		verify( commandB ).execute( input, environment );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testHandlingOfJSONObjectWithoutCommand() {
		JSONObject input = new JSONObject();
		input.put( "cmomand", "B" );

		subject.executeJSON( input, environment );
	}

}
