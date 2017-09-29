/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.komodo.modeshape.teiid.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.komodo.modeshape.teiid.Messages;
import org.komodo.modeshape.teiid.TeiidClientException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.runtime.version.MetadataVersion;
import org.komodo.utils.StringUtils;
/**
 * Abstract implemention of {@link TeiidSeqParser}.
 * Generated parser implementations extend this class.
 */
public abstract class AbstractTeiidParser {

    /**
     * A model to encapsulate a parsing error. Used to hold errors, allowing
     * the parser to try and continue as much as possible before returning
     * with the errors.
     */
    public static class ParsingError implements StringConstants {

        private final String token;

        private final int line;

        private final int column;

        private final String message;

        /**
         * Create a new instance
         *
         * @param token the token where the error occurred
         * @param line the text line where the error occurred
         * @param column the text column where the error occurred
         * @param message an additional message to convey with the error
         */
        public ParsingError(String token, int line, int column, String message) {
            this.token = token;
            this.line = line;
            this.column = column;
            this.message = message;
        }

        /**
         * @return the token
         */
        public String getToken() {
            return this.token;
        }

        /**
         * @return the line
         */
        public int getLine() {
            return this.line;
        }

        /**
         * @return the column
         */
        public int getColumn() {
            return this.column;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return Messages.getString(Messages.TeiidParser.parsing_error, token, line, column, message);
        }
    }

    protected List<ParsingError> errors = new ArrayList<ParsingError>();

    protected Pattern udtPattern = Pattern.compile("(\\w+)\\s*\\(\\s*(\\d+),\\s*(\\d+),\\s*(\\d+)\\)"); //$NON-NLS-1$
    
    protected Pattern SOURCE_HINT = Pattern.compile("\\s*sh(\\s+KEEP ALIASES)?\\s*(?::((?:'[^']*')+))?\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //$NON-NLS-1$
    
    protected Pattern SOURCE_HINT_ARG = Pattern.compile("\\s*([^: ]+)(\\s+KEEP ALIASES)?\\s*:((?:'[^']*')+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL); //$NON-NLS-1$

    protected MetadataVersion version;

    /**
     * @return teiid instance version
     */
    public MetadataVersion getVersion() {
        return version;
    }

    public List<ParsingError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    protected abstract void ReInit(Reader reader);

    public void reset(Reader reader) {
        errors.clear();
        ReInit(reader);
    }

    /**
     * @param teiidVersion version of teiid
     */
    public void setVersion(MetadataVersion teiidVersion) {
        this.version = teiidVersion;
    }

	protected String prependSign(String sign, String literal) {
		if (sign != null && sign.charAt(0) == '-') {
			return sign + literal;
		}
		return literal;
	}

    private int parseNumericValue(CharSequence string, StringBuilder sb, int i, int value,
                                                                         int possibleDigits, int radixExp) {
        for (int j = 0; j < possibleDigits; j++) {
            if (i + 1 == string.length()) {
                break;
            }
            char digit = string.charAt(i + 1);
            int val = Character.digit(digit, 1 << radixExp);
            if (val == -1) {
                break;
            }
            i++;
            value = (value << radixExp) + val;
        }
        sb.append((char)value);
        return i;
    }

	/**
     * Unescape the given string
     * @param string
     * @param quoteChar
     * @param useAsciiExcapes
     * @param sb a scratch buffer to use
     * @return
     */
    private String unescape(CharSequence string, int quoteChar, boolean useAsciiEscapes, StringBuilder sb) {
        boolean escaped = false;
        
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (escaped) {
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    i = parseNumericValue(string, sb, i, 0, 4, 4);
                    //TODO: this should probably be strict about needing 4 digits
                    break;
                default:
                    if (c == quoteChar) {
                        sb.append(quoteChar);
                    } else if (useAsciiEscapes) {
                        int value = Character.digit(c, 8);
                        if (value == -1) {
                            sb.append(c);
                        } else {
                            int possibleDigits = value < 3 ? 2:1;
                            int radixExp = 3;
                            i = parseNumericValue(string, sb, i, value, possibleDigits, radixExp);
                        }
                    }
                }
                escaped = false;
            } else {
                if (c == '\\') {
                    escaped = true;
                } else if (c == quoteChar) {
                    break;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

	private String unescape(String string) {
        return unescape(string, -1, true, new StringBuilder());
    }

	private String removeEscapeChars(String str, String tickChar) {
        return StringUtils.replaceAll(str, tickChar + tickChar, tickChar);
    }

	protected String normalizeStringLiteral(String s) {
	    if (s == null)
	        return null;

		int start = 1;
		boolean unescape = false;
  		if (s.charAt(0) == 'N') {
  			start++;
  		} else if (s.charAt(0) == 'E') {
  			start++;
  			unescape = true;
  		}
  		char tickChar = s.charAt(start - 1);
  		s = s.substring(start, s.length() - 1);
  		String result = removeEscapeChars(s, String.valueOf(tickChar));
  		if (unescape) {
  			result = unescape(result);
  		}
  		return result;
	}
	
	/**
	 * @param s
	 * @return normalized string id
	 */
	protected String normalizeId(String s) {
		if (s == null || s.indexOf('"') == -1) {
			return s;
		}

		List<String> nameParts = new LinkedList<String>();
		while (s.length() > 0) {
			if (s.charAt(0) == '"') {
				boolean escape = false;
				for (int i = 1; i < s.length(); i++) {
					if (s.charAt(i) != '"') {
						continue;
					}
					escape = !escape;
					boolean end = i == s.length() - 1;
					if (end || (escape && s.charAt(i + 1) == '.')) {
				  		String part = s.substring(1, i);
				  		s = s.substring(i + (end?1:2));
				  		nameParts.add(removeEscapeChars(part, "\"")); //$NON-NLS-1$
				  		break;
					}
				}
			} else {
				int index = s.indexOf('.');
				if (index == -1) {
					nameParts.add(s);
					break;
				} 
				nameParts.add(s.substring(0, index));
				s = s.substring(index + 1);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> i = nameParts.iterator(); i.hasNext();) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append('.');
			}
		}
		return sb.toString();
	}
	
    /**
     * Check if this is a valid string literal
     * @param id Possible string literal
     */
    protected boolean isStringLiteral(String str, ParseInfo info) {
    	if (info.useAnsiQuotedIdentifiers() || str.charAt(0) != '"' || str.charAt(str.length() - 1) != '"') {
    		return false;
    	}
    	int index = 1;
    	while (index < str.length() - 1) {
    		index = str.indexOf('"', index);
    		if (index == -1 || index + 1 == str.length()) {
    			return true;
    		}
    		if (str.charAt(index + 1) != '"') {
    			return false;
    		}
    		index += 2;
    	}
    	return true;
    }    

    protected String validateName(String id, boolean element) throws Exception {
        if(id.indexOf('.') != -1) { 
            Messages.TeiidParser key = Messages.TeiidParser.Invalid_alias;
            if (element) {
                key = Messages.TeiidParser.Invalid_short_name;
            }
            throw new TeiidClientException(Messages.getString(key, id)); 
        }
        return id;
    }

	protected boolean isTrue(final String text) {
        return Boolean.valueOf(text);
    }    
}
