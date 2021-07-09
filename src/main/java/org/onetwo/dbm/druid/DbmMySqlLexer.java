package org.onetwo.dbm.druid;

import static com.alibaba.druid.sql.parser.LayoutCharacters.EOI;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlLexer;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;

/**
 * @author weishao zeng
 * <br/>
 */

public class DbmMySqlLexer extends MySqlLexer {
	
//	private Map<String, LexerVar> vars = Maps.newHashMap();
//	private int varIndex = 0;

	public DbmMySqlLexer(String input) {
		super(input);
	}

	/****
	 * 整个方法复制自父类，主要替换了 isIdentifierChar(ch)的实现，因为isIdentifierChar为静态方法，无法覆盖
	 */
    public void scanVariable() {
        if (ch != '@' && ch != ':' && ch != '#' && ch != '$') {
            throw new ParserException("illegal variable");
        }

        mark = pos;
        bufPos = 1;

        if (charAt(pos + 1) == '@') {
            ch = charAt(++pos);
            bufPos++;
        }

        if (charAt(pos + 1) == '`') {
            ++pos;
            ++bufPos;
            char ch;
            for (;;) {
                ch = charAt(++pos);

                if (ch == '`') {
                    bufPos++;
                    ch = charAt(++pos);
                    break;
                } else if (ch == EOI) {
                    throw new ParserException("illegal identifier");
                }

                bufPos++;
                continue;
            }

            this.ch = charAt(pos);

            stringVal = subString(mark, bufPos);
            token = Token.VARIANT;
        } else if (charAt(pos + 1) == '{') {
            ++pos;
            ++bufPos;
            char ch;
            for (;;) {
                ch = charAt(++pos);

                if (ch == '}') {
                    bufPos++;
                    ch = charAt(++pos);
                    break;
                } else if (ch == EOI) {
                    throw new ParserException("illegal identifier");
                }

                bufPos++;
                continue;
            }

            this.ch = charAt(pos);

            stringVal = subString(mark, bufPos);
            token = Token.VARIANT;
        } else {
            for (;;) {
                ch = charAt(++pos);

                //主要替换druid的这一行实现
                if (!isValidVarChar(ch)) {
                    break;
                }

                bufPos++;
                continue;
            }
        }

        this.ch = charAt(pos);

        stringVal = subString(mark, bufPos);
        token = Token.VARIANT;
        
//        this.putVar(stringVal);
    }

    public boolean isValidVarChar(char c) {
        return !Character.isWhitespace(c) // 空白符包含：空格、tab 键、换行符
        		&& c != EOI 
        		&& c != ',' 
        		&& c != ';'
        		&& c != '　' && c != '，'; // 中文空格和逗号
    }
    
//    private void putVar(String varName) {
//    	LexerVar lexer = new LexerVar(varName, varIndex);
//    	this.vars.put(varName, lexer);
//    	varIndex++;
//    }
//
//	public Map<String, LexerVar> getVars() {
//		return vars;
//	}
    
}
