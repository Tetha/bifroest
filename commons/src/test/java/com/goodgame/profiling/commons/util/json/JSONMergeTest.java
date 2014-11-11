package com.goodgame.profiling.commons.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JSONMergeTest {

	private JSONObject obj1;
	private JSONObject obj2;

	@Before
	public void createObjects() {
		obj1 = new JSONObject();
		obj1.put( "okay-a", true );
		obj1.put( "primitive", 5 );
		JSONArray array1 = new JSONArray();
		obj1.put( "array", array1 );
		array1.put( "hello" );
		JSONObject deep1 = new JSONObject();
		obj1.put( "deep", deep1 );
		deep1.put( "okay-c", true );
		deep1.put( "primitive", 0.4 );

		obj2 = new JSONObject();
		obj2.put( "okay-b", false );
		obj2.put( "primitive", 3 );
		JSONArray array2 = new JSONArray();
		obj2.put( "array", array2 );
		array2.put( "world" );
		JSONObject deep2 = new JSONObject();
		obj2.put( "deep", deep2 );
		deep2.put( "okay-d", true );
		deep2.put( "primitive", "foo" );
	}

	@Test
	public void testSimpleFirst() {
		JSONObject merged = JSONUtils.simpleMergeObjectsUseFirst( obj1, obj2 );
		assertTrue( merged.has( "okay-a" ) );
		assertTrue( merged.has( "okay-b" ) );
		assertTrue( merged.has( "primitive" ) );
		assertTrue( merged.has( "array" ) );
		assertTrue( merged.has( "deep" ) );
		assertEquals( 5, merged.getInt( "primitive" ) );
		JSONArray array = merged.getJSONArray( "array" );
		assertEquals( 1, array.length() );
		assertEquals( "hello", array.get( 0 ) );
		JSONObject deep = merged.getJSONObject( "deep" );
		assertTrue( deep.has( "okay-c" ) );
		assertFalse( deep.has( "okay-d" ) );
		assertTrue( deep.has( "primitive" ) );
		assertEquals( 0.4, deep.getDouble( "primitive" ), 0.0d );
	}

	@Test
	public void testSimpleLast() {
		JSONObject merged = JSONUtils.simpleMergeObjectsUseLast( obj1, obj2 );
		assertTrue( merged.has( "okay-a" ) );
		assertTrue( merged.has( "okay-b" ) );
		assertTrue( merged.has( "primitive" ) );
		assertTrue( merged.has( "array" ) );
		assertTrue( merged.has( "deep" ) );
		assertEquals( 3, merged.getInt( "primitive" ) );
		JSONArray array = merged.getJSONArray( "array" );
		assertEquals( 1, array.length() );
		assertEquals( "world", array.get( 0 ) );
		JSONObject deep = merged.getJSONObject( "deep" );
		assertFalse( deep.has( "okay-c" ) );
		assertTrue( deep.has( "okay-d" ) );
		assertTrue( deep.has( "primitive" ) );
		assertEquals( "foo", deep.getString( "primitive" ) );
	}

	@Test
	public void testDeepFirst() {
		JSONObject merged = JSONUtils.deepMergeObjectsUseFirst( obj1, obj2 );
		assertTrue( merged.has( "okay-a" ) );
		assertTrue( merged.has( "okay-b" ) );
		assertTrue( merged.has( "primitive" ) );
		assertTrue( merged.has( "array" ) );
		assertTrue( merged.has( "deep" ) );
		assertEquals( 5, merged.getInt( "primitive" ) );
		JSONArray array = merged.getJSONArray( "array" );
		assertEquals( 2, array.length() );
		assertEquals( "hello", array.get( 0 ) );
		assertEquals( "world", array.get( 1 ) );
		JSONObject deep = merged.getJSONObject( "deep" );
		assertTrue( deep.has( "okay-c" ) );
		assertTrue( deep.has( "okay-d" ) );
		assertTrue( deep.has( "primitive" ) );
		assertEquals( 0.4, deep.getDouble( "primitive" ), 0.0d );
	}

	@Test
	public void testDeepLast() {
		JSONObject merged = JSONUtils.deepMergeObjectsUseLast( obj1, obj2 );
		assertTrue( merged.has( "okay-a" ) );
		assertTrue( merged.has( "okay-b" ) );
		assertTrue( merged.has( "primitive" ) );
		assertTrue( merged.has( "array" ) );
		assertTrue( merged.has( "deep" ) );
		assertEquals( 3, merged.getInt( "primitive" ) );
		JSONArray array = merged.getJSONArray( "array" );
		assertEquals( 2, array.length() );
		assertEquals( "hello", array.get( 0 ) );
		assertEquals( "world", array.get( 1 ) );
		JSONObject deep = merged.getJSONObject( "deep" );
		assertTrue( deep.has( "okay-c" ) );
		assertTrue( deep.has( "okay-d" ) );
		assertTrue( deep.has( "primitive" ) );
		assertEquals( "foo", deep.getString( "primitive" ) );
	}

}
