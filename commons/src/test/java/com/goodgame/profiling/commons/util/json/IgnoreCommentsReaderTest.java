package com.goodgame.profiling.commons.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class IgnoreCommentsReaderTest {
	@Test
	public void testKeepBasicFunctionality() throws IOException {
		try (BufferedReader r = new BufferedReader(new IgnoreCommentsReader(
		        '#', new StringReader("foo\nbar\nbaz\n")))) {

			assertEquals("foo", r.readLine());
			assertEquals("bar", r.readLine());
			assertEquals("baz", r.readLine());
			assertNull(r.readLine());
		}
	}

	@Test
	public void testIgnoreCommentSingleLine() throws IOException {
		try (BufferedReader r = new BufferedReader(new IgnoreCommentsReader(
		        '#', new StringReader("foo#bar")))) {

			assertEquals("foo", r.readLine());
			assertNull(r.readLine());
		}
	}

	@Test
	public void testIgnoreCommentMultiLine() throws IOException {
		try (BufferedReader r = new BufferedReader(new IgnoreCommentsReader(
		        '#', new StringReader("foo#bar\nbaz#qux\n")))) {

			assertEquals("foo", r.readLine());
			assertEquals("baz", r.readLine());
			assertNull(r.readLine());
		}
	}

	@Test
	public void testOffset() throws IOException {
		try (Reader r = new IgnoreCommentsReader('#', new StringReader(
		        "foo#bar\nbaz#qux"))) {
			char[] buffer = new char[20];

			int bytesRead = r.read(buffer, 10, 5);

			for (int i = 0; i < 10; i++) {
				assertEquals((char) 0, buffer[i]);
			}
			for (int i = 0; i < bytesRead; i++) {
				assertEquals("foo\nbaz".charAt(i), buffer[10 + i]);
			}
			for (int i = 10 + bytesRead; i < 20; i++) {
				assertEquals((char) 0, buffer[i]);
			}
			assertEquals(3, bytesRead);
		}
	}

	@Test
	public void testKeepState() throws IOException {
		try (Reader r = new IgnoreCommentsReader('#', new StringReader(
		        "foo#bar\nbaz#qux"))) {
			char[] buffer1 = new char[20];
			char[] buffer2 = new char[20];

			int bytesRead1 = r.read(buffer1, 0, 5);
			int bytesRead2 = r.read(buffer2, 0, 10);

			for (int i = 0; i < 3; i++) {
				assertEquals("foo".charAt(i), buffer1[i]);
			}
			for (int i = 3; i < 20; i++) {
				assertEquals((char) 0, buffer1[i]);
			}
			assertEquals(3, bytesRead1);

			for (int i = 0; i < 4; i++) {
				assertEquals("\nbaz".charAt(i), buffer2[i]);
			}
			for (int i = 4; i < 20; i++) {
				assertEquals((char) 0, buffer2[i]);
			}
			assertEquals(4, bytesRead2);
		}
	}
}
