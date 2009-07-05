package org.apache.pig.contrib.eclipse.editors;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class PigWhiteSpaceDetector implements IWhitespaceDetector {

	@Override
	public boolean isWhitespace(char c) {
		return Character.isWhitespace(c);
	}

}
