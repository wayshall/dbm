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

	public DbmMySqlLexer(String input) {
		super(input);
	}

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
    }

    public boolean isValidVarChar(char c) {
        return !Character.isWhitespace(c) // 空白符包含：空格、tab 键、换行符
        		&& c!=EOI 
        		&& c!=',' 
        		&& c!=';'
        		&& c != '　' && c != '，'; // 中文空格和逗号
    }

}
