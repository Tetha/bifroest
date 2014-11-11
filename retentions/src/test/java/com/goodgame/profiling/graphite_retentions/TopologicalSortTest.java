package com.goodgame.profiling.graphite_retentions;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TopologicalSortTest {
	
	MutableRetentionConfiguration retentions;

	@Before
	public void initConfig(){
		retentions = new MutableRetentionConfiguration();
	}
	
	@Test
	public void testEmptyLevels() {
		List<RetentionLevel> sort = retentions.getTopologicalSort();
		assertTrue(sort.isEmpty());
	}
	
	@Test
	public void testOneLevel(){
		RetentionLevel level01 = new RetentionLevel("level01", 1, 1, 1, null);
		retentions.addLevel(level01);
		List<RetentionLevel> sort = retentions.getTopologicalSort();
		
		assertTrue(sort.contains(level01));
		assertEquals(1, sort.size());
	}
	
	@Test
	public void testSomeLevels(){
		RetentionLevel level01 = new RetentionLevel("level01", 1 ,1 ,1, null);
		RetentionLevel level02 = new RetentionLevel("level02", 1 ,1 ,1, "level01");
		RetentionLevel level03 = new RetentionLevel("level03", 1 ,1 ,1, "level01");
		RetentionLevel level04 = new RetentionLevel("level04", 1 ,1 ,1, "level03");
		
		retentions.addLevel(level01);
		retentions.addLevel(level02);
		retentions.addLevel(level03);
		retentions.addLevel(level04);
		
		List<RetentionLevel> sort = retentions.getTopologicalSort();
		
		assertTrue(sort.contains(level01));
		assertTrue(sort.contains(level02));
		assertTrue(sort.contains(level03));
		assertTrue(sort.contains(level04));
		assertTrue(sort.indexOf(level02) < sort.indexOf(level01));
		assertTrue(sort.indexOf(level04) < sort.indexOf(level03));
		assertTrue(sort.indexOf(level03) < sort.indexOf(level01));
		assertEquals(4, sort.size());
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCycle(){
		RetentionLevel level01 = new RetentionLevel("level01", 1 ,1 ,1, "level02");
		RetentionLevel level02 = new RetentionLevel("level02", 1 ,1 ,1, "level01");
		
		retentions.addLevel(level01);
		retentions.addLevel(level02);
		
		retentions.getTopologicalSort();
	}

}
