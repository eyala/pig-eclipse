package org.apache.pig.contrib.eclipse.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class PigPartitionScanner extends RuleBasedPartitionScanner {

	
	public PigPartitionScanner() {
		super();

//		IToken javaDoc= new Token(JAVA_DOC);
//		IToken comment= new Token(JAVA_MULTILINE_COMMENT);

		List<IRule> rules= new ArrayList<IRule>();

		rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add special case word rule.
//		rules.add(new WordPredicateRule(comment));

		// Add rules for multi-line comments and javadoc.
//		rules.add(new MultiLineRule("/*", "*/", comment, (char) 0, true)); //$NON-NLS-1$ //$NON-NLS-2$

		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
