package com.goodgame.profiling.rewrite_framework.output.rewriter.replace;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.replace.Replacer;

public class ReplacerTest {
	@Test
	public void testSimpleReplacement() {
		Map<String, String> replacements = new LinkedHashMap<String, String>();
		replacements.put("foo", "oof");
		replacements.put("bar", "baz");

		Replacer subject = new Replacer( replacements );
		assertEquals("oof baz", subject.modify( "foo bar" ) );
	}

	@Test
	public void testReplacementIsInOrder() {
		Map<String, String> replacements = new LinkedHashMap<String, String>();
		replacements.put( "foo", "bar" );
		replacements.put( "bar", "baz" );

		Replacer subject = new Replacer( replacements );
		assertEquals( "baz", subject.modify( "foo" ) );
	}
}
