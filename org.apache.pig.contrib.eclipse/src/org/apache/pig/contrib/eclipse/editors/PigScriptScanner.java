package org.apache.pig.contrib.eclipse.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pig.contrib.eclipse.PigActivator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

public class PigScriptScanner extends RuleBasedScanner {

	private static Set<String> KEYWORDS = new HashSet<String>();

	private static Set<String> BUILTIN_FUN = new HashSet<String>();

	static {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					PigActivator.getDefault().openStream(
							new Path("data/keywords.txt"))));

			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() != 0) {
					KEYWORDS.add(line);
				}
			}
			reader.close();

			reader = new BufferedReader(new InputStreamReader(PigActivator
					.getDefault().openStream(new Path("data/builtin_fun.txt"))));
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() != 0) {
					BUILTIN_FUN.add(line);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PigScriptScanner(PigColorProvider colorProvider) {

		IToken keywordToken = new Token(new TextAttribute(colorProvider
				.getColor(PigColorProvider.KEYWORD), null, SWT.BOLD));
		IToken commentToken = new Token(new TextAttribute(colorProvider
				.getColor(PigColorProvider.COMMNET)));
		IToken builtinFunToken = new Token(new TextAttribute(colorProvider
				.getColor(PigColorProvider.BUILTIN_FUN)));
		IToken stringToken = new Token(new TextAttribute(colorProvider
				.getColor(PigColorProvider.RAWSTRING)));
		IToken defaultToken = new Token(new TextAttribute(colorProvider
				.getColor(PigColorProvider.DEFAULT)));

		List<IRule> rules = new ArrayList<IRule>();

		rules.add(new EndOfLineRule("--", commentToken)); //$NON-NLS-1$

		rules.add(new SingleLineRule("'", "'", stringToken, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new PigWhiteSpaceDetector()));

		// Add word rule for keywords, types, and constants.
		WordRule wordRule = new WordRule(new PigWordDetector(), defaultToken,
				true);
		for (String keyword : KEYWORDS)
			wordRule.addWord(keyword, keywordToken);

		for (String func : BUILTIN_FUN)
			wordRule.addWord(func, builtinFunToken);

		rules.add(wordRule);
		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
