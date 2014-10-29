/*******************************************************************************
 * Original copyright from org.eclipse.jface.text.rules.WordRule follows
 * 
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Doug Satchwell <doug.satchwell@ymail.com> - [implementation] Performance issue with jface text WordRule - http://bugs.eclipse.org/277299
 *******************************************************************************/
package org.apache.pig.contrib.eclipse;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;


/**
 * A changed version of {@link org.eclipse.jface.text.rules.WordRule} that can handle both case sensitive and insensitive words. It will first look for
 * a word in the insensitive list, and only if it fails there will it look in the case sensitive list.
 * 
 * @see IWordDetector
 */
public class WordRule implements IRule {

    /** Internal setting for the un-initialized column constraint. */
    protected static final int UNDEFINED= -1;

    /** The word detector used by this rule. */
    protected IWordDetector fDetector;
    /** The default token to be returned on success and if nothing else has been specified. */
    protected IToken fDefaultToken;
    /** The column constraint. */
    protected int fColumn= UNDEFINED;
    
    /** The table of predefined words and token for this rule (case sensitive). */
    protected Map<String, IToken> csWords= new HashMap<String, IToken>();
    /** The table of predefined words and token for this rule (case insensitive) */
    protected Map<String, IToken> ciWords= new HashMap<String, IToken>();

    /** Buffer used for pattern detection. */
    private StringBuilder fBuffer= new StringBuilder();
    /**
     * Tells whether this rule is case sensitive.
     * @since 3.3
     */
    private boolean fIgnoreCase= false;


    /**
     * Creates a rule which, with the help of an word detector, will return the token
     * associated with the detected word. If no token has been associated, the scanner
     * will be rolled back and an undefined token will be returned in order to allow
     * any subsequent rules to analyze the characters.
     *
     * @param detector the word detector to be used by this rule, may not be <code>null</code>
     * @see #addWord(String, IToken)
     */
    public WordRule(IWordDetector detector) {
        this(detector, Token.UNDEFINED, false);
    }

    /**
     * Creates a rule which, with the help of a word detector, will return the token
     * associated with the detected word. If no token has been associated, the
     * specified default token will be returned.
     *
     * @param detector the word detector to be used by this rule, may not be <code>null</code>
     * @param defaultToken the default token to be returned on success
     *            if nothing else is specified, may not be <code>null</code>
     * @see #addWord(String, IToken)
     */
    public WordRule(IWordDetector detector, IToken defaultToken) {
        this(detector, defaultToken, false);
    }

    /**
     * Creates a rule which, with the help of a word detector, will return the token
     * associated with the detected word. If no token has been associated, the
     * specified default token will be returned.
     *
     * @param detector the word detector to be used by this rule, may not be <code>null</code>
     * @param defaultToken the default token to be returned on success
     *          if nothing else is specified, may not be <code>null</code>
     * @param ignoreCase the default case sensitivity associated with this rule
     * @see #addWord(String, IToken)
     * @since 3.3
     */
    public WordRule(IWordDetector detector, IToken defaultToken, boolean ignoreCase) {
        Assert.isNotNull(detector);
        Assert.isNotNull(defaultToken);

        fDetector= detector;
        fDefaultToken= defaultToken;
        fIgnoreCase= ignoreCase;
    }

    /**
     * Adds a word and the token to be returned if it is detected. Uses the default case sensitivity setting
     *
     * @param word the word this rule will search for, may not be <code>null</code>
     * @param token the token to be returned if the word has been found, may not be <code>null</code>
     * @see #addWord(String, IToken, boolean)
     */
    public void addWord(String word, IToken token) {
        addWord(word, token, fIgnoreCase);
    }

    /**
     * Adds a word and the token to be returned if it is detected.
     *
     * @param word the word this rule will search for, may not be <code>null</code>
     * @param token the token to be returned if the word has been found, may not be <code>null</code>
     * @param ignoreCase whether this word is case-sensitive or not
     */
    public void addWord(String word, IToken token, boolean ignoreCase) {
        Assert.isNotNull(word);
        Assert.isNotNull(token);

        // If case-insensitive, convert to lower case before adding to the map
        if (ignoreCase) {
            word= word.toLowerCase();
            ciWords.put(word, token);
        } else {
            csWords.put(word, token);   
        }
    }

    /**
     * Sets a column constraint for this rule. If set, the rule's token
     * will only be returned if the pattern is detected starting at the
     * specified column. If the column is smaller then 0, the column
     * constraint is considered removed.
     *
     * @param column the column in which the pattern starts
     */
    public void setColumnConstraint(int column) {
        if (column < 0)
            column= UNDEFINED;
        fColumn= column;
    }

    /*
     * @see IRule#evaluate(ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
        int c= scanner.read();
        if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
            if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

                fBuffer.setLength(0);
                do {
                    fBuffer.append((char) c);
                    c= scanner.read();
                } while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
                scanner.unread();

                String buffer= fBuffer.toString();

                // first try to find a case-sensitive match
                IToken token= (IToken)csWords.get(buffer);

                if (token != null)
                    return token;
                // if no case-sensitive match is found, try for a case-insensitive match            
                buffer= buffer.toLowerCase();
            
                token= (IToken)ciWords.get(buffer);

                if (token != null)
                    return token;

                if (fDefaultToken.isUndefined())
                    unreadBuffer(scanner);

                return fDefaultToken;
            }
        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    /**
     * Returns the characters in the buffer to the scanner.
     *
     * @param scanner the scanner to be used
     */
    protected void unreadBuffer(ICharacterScanner scanner) {
        for (int i= fBuffer.length() - 1; i >= 0; i--)
            scanner.unread();
    }

    /**
     * Returns the default case sensitivity setting of this instance
     */
    public boolean getDefaultCaseSensitivity() {
        return fIgnoreCase;
    }
}