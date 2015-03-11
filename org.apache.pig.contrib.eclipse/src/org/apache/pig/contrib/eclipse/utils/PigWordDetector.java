package org.apache.pig.contrib.eclipse.utils;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IWordDetector;

public class PigWordDetector implements IWordDetector {

	public static PigWordDetector INSTANCE = new PigWordDetector();
	
	@Override
	public boolean isWordPart(char c) {
		if (c=='.'){
			return true;
		}
		return Character.isJavaIdentifierPart(c);
	}

	@Override
	public boolean isWordStart(char c) {
		return Character.isJavaIdentifierStart(c);
	}
	
	/**
	 * Given a String representing a document and an offset, finds a word that is a valid pig relation, field, macro or udf
	 */
	public String getWord(IDocument doc, int offset) {
		
		int start = offset;
		int end = start;
		
		try {
			while (start >= 0 && isWordPart(doc.getChar(start))) {
				start--;
			}
			
		} catch (BadLocationException e) {}
		
		try {
			while (Character.isJavaIdentifierPart(doc.getChar(end))) {
				end++;
			}
		} catch (BadLocationException e) {}

		int length = end-start-1;
		
		try {
			return doc.get(start+1, length);
		} catch (BadLocationException ble) {
			PigLogger.warn("BadLocationException while getting word from region with start " + (start+1) + " and length " + length + " in a document with length " + doc.getLength(), ble); // this shouldn't happen
		}
		
		return null;
	}

}
