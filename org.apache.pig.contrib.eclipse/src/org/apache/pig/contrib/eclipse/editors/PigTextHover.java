package org.apache.pig.contrib.eclipse.editors;

import org.apache.pig.contrib.eclipse.OpenDeclarationHandler;
import org.apache.pig.contrib.eclipse.utils.SearchResult;
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
		
		int offset = hoverRegion.getOffset();
		
		SearchResult result = OpenDeclarationHandler.findDeclaration(doc, offset);
			
		if (result != null && result.getText() != null) {
			return result.getText();
		} else {
			return "";
		}
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
