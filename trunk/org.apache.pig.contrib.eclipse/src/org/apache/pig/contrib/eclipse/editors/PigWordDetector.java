package org.apache.pig.contrib.eclipse.editors;

import org.eclipse.jface.text.rules.IWordDetector;

public class PigWordDetector implements IWordDetector {

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
	
	public static void main(String[] args) {
		PigWordDetector detector=new PigWordDetector();
		System.out.println(detector.isWordStart('.'));
	}

}
