package com.goodgame.profiling.commons.util.json;

import java.io.IOException;
import java.io.Reader;

public class IgnoreCommentsReader extends Reader {

	private enum Modes {
		COPYING, SKIPPING
	};

	private Modes currentMode;

	private final char commentChar;
	private final Reader innerReader;

	public IgnoreCommentsReader( char commentChar, Reader innerReader ) {
		this.commentChar = commentChar;
		this.innerReader = innerReader;
		this.currentMode = Modes.COPYING;
	}

	@Override
	public int read( char[] cbuf, int off, int len ) throws IOException {
		char[] newcbuf = new char[cbuf.length];

		int bytesRead = innerReader.read( newcbuf, off, len );

		if ( bytesRead == -1 ) {
			return -1;
		}

		int readIndex = 0;
		int writeIndex = 0;
		while ( readIndex < bytesRead ) {
			if ( newcbuf[off + readIndex] == commentChar ) {
				currentMode = Modes.SKIPPING;
			} else if ( newcbuf[off + readIndex] == '\n' ) {
				currentMode = Modes.COPYING;
			}

			if ( currentMode == Modes.COPYING ) {
				cbuf[off + writeIndex] = newcbuf[off + readIndex];
				writeIndex++;
			}
			readIndex++;
		}

		return writeIndex;
	}

	@Override
	public void close() throws IOException {
		innerReader.close();
	}

}
