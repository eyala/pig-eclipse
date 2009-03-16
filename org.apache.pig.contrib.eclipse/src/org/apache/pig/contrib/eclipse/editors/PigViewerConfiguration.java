package org.apache.pig.contrib.eclipse.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class PigViewerConfiguration extends SourceViewerConfiguration {

	
	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		// TODO Auto-generated method stub
		return 4;
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {

		PigColorProvider provider= PigPlugin.getDefault().getColorProvider();
		PresentationReconciler reconciler= new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(PigPlugin.getDefault().getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		return reconciler;
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant= new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new PigContentAssistant(), IDocument.DEFAULT_CONTENT_TYPE);
		
		return assistant;
	}
}
