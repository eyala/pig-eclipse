package org.apache.pig.contrib.eclipse.editors;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pig.contrib.eclipse.PigLogger;
import org.apache.pig.contrib.eclipse.utils.RegexUtils;
import org.apache.pig.contrib.eclipse.utils.VisitorWorkspaceSearcher;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class PigTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {
	
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return getHoverInfo2(textViewer, hoverRegion).toString();
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		 Point selection = textViewer.getSelectedRange();
         if (selection.x <= offset && offset < selection.x + selection.y) {
                 return new Region(selection.x, selection.y);
         }
         // if no text is selected then we return a region of the size 0 (a single character)
         return new Region(offset, 0);
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		IDocument doc = textViewer.getDocument();
		
		int start = hoverRegion.getOffset();
		int end = start;

		// 1. Find the word we're on
		try {
			while (start >= 0 && Character.isJavaIdentifierPart(doc.getChar(start))) {
				start--;
			}
		} catch (BadLocationException e) {}
		
		try {
			while (Character.isJavaIdentifierPart(doc.getChar(end))) {
				end++;
			}
		} catch (BadLocationException e) {}
			
		String word = "";
		
		try {
			word = doc.get(start+1, end-start-1);
		} catch (BadLocationException ble) {
			PigLogger.warn("BadLocationException while getting word from region with start " + start + " and end " + end, ble); // this shouldn't happen
		}
		
		if (!word.isEmpty()) {

			PigLogger.debug("Searching for macro definition for '" + word + "'");

			// 2. Prepare a regular expression for finding macro definitions for the current word 
			Pattern macro_defines = RegexUtils.findMacroDefinesForHoverInfoPattern(word);

			// 3. Get all of the current document (up to this point)
			String mostOfDoc = "";
			
			try {
				mostOfDoc = doc.get(0, start);
			} catch (BadLocationException ble) {
				PigLogger.warn("BadLocationException while getting document from start to " + start, ble); // this shouldn't happen
			}

			Matcher m = macro_defines.matcher(mostOfDoc);
			
			if (m.find()) {
				return m.group(1);
			}

			// 4. Prepare a regular expression for finding non macro definitions and use it 
			Pattern local_defines = RegexUtils.findNonMacroDefinesForHoverInfoPattern(word);

			Matcher m2 = local_defines.matcher(mostOfDoc);
			
			if (m2.find()) {
				return m2.group();
			}

			// 5. Scan all of the current document (up to this point) for import statements, to prune the list of pig files to read
			Set<String> imports = RegexUtils.findImports(mostOfDoc);


			// 6. Try to find a matching macro definition elsewhere in the workspace
			return new VisitorWorkspaceSearcher().findInFiles(imports, macro_defines, false);
		}
		
		return "";
	}
	
	/**
	 * This makes the hover dialog resizable, which solves the problem that it is sometimes too small
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
		
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, true);
			}
		};
	}
}
