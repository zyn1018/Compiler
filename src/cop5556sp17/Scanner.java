package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    /**
     * Kind enum
     */

    public enum Kind {
        IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), KW_IMAGE("image"), KW_URL("url"),
        KW_FILE("file"), KW_FRAME("frame"), KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"),
        SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), RBRACE("}"), ARROW("->"), BARARROW("|->"),
        OR("|"), AND("&"), EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), PLUS("+"), MINUS("-"),
        TIMES("*"), DIV("/"), MOD("%"), NOT("!"), ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"),
        KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"),
        KW_YLOC("yloc"), KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), KW_SCALE("scale"), EOF("eof");

        Kind(String text) {
            this.text = text;
        }

        final String text;

        String getText() {
            return text;
        }
    }

    // Handling reserved words
    public static final Map<String, Kind> reservedWords = new HashMap<String, Kind>();

    static {
        for (Kind k : Kind.values()) {
            String str = k.getText();
            if (str.matches("^[a-z]+$")) {
                reservedWords.put(str, k);
            }
        }
    }

    // if the string is one of the reserved words,return 1
    public boolean isReserved(String str) {
        return reservedWords.containsKey(str);

    }

    // According to the String, to determine what kinds of keyword it is
    public Kind getKind(String str) {
        return reservedWords.get(str);
    }

    // State
    public static enum State {
        START, IN_DIGIT, IN_IDENT, AFTER_EQ, AFTER_EXCLA/* ! */, AFTER_MINUS, AFTER_LT/* < */, AFTER_GT/* > */, AFTER_OR, START_COMMENT, END_COMMENT, AFTER_DIV, OR_MINUS;
    }

    /**
     * Thrown by Scanner when an illegal character is encountered
     */
    @SuppressWarnings("serial")
    public static class IllegalCharException extends Exception {
        public IllegalCharException(String message) {
            super(message);
        }
    }

    /**
     * Thrown by Scanner when an int literal is not a value that can be
     * represented by an int.
     */
    @SuppressWarnings("serial")
    public static class IllegalNumberException extends Exception {
        public IllegalNumberException(String message) {
            super(message);
        }
    }

    public List<Integer> lineStartPos = new ArrayList<Integer>();

    /**
     * Holds the line and position in the line of a token.
     */
    static class LinePos {
        public final int line;
        public final int posInLine;

        public LinePos(int line, int posInLine) {
            super();
            this.line = line;
            this.posInLine = posInLine;
        }

        @Override
        public String toString() {
            return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
        }
    }

    public class Token {
        public final Kind kind;
        public final int pos; // position in input array
        public final int length;

        // returns the text of this Token
        public String getText() {
            if (kind == Kind.EOF) {
                return Kind.EOF.getText();

            }
            return chars.substring(pos, pos + length);

        }

        // returns a LinePos object representing the line and column of this
        // Token
        public LinePos getLinePos() {
            return new LinePos(returnLineNumber(pos), (pos - lineStartPos.get(returnLineNumber(pos))));
        }

        Token(Kind kind, int pos, int length) {
            this.kind = kind;
            this.pos = pos;
            this.length = length;
        }

        // Determine if the token's kind is corresponding to the given kind
        public boolean isKind(Kind kindExpected) {
            return (kind == kindExpected);
        }

        public boolean isKind(Kind... kinds) {
            for (Kind kindall : kinds) {
                if (kind == kindall) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Precondition: kind = Kind.INT_LIT, the text can be represented with a
         * Java int. Note that the validity of the input should have been
         * checked when the Token was created. So the exception should never be
         * thrown.
         *
         * @return int value of this token, which should represent an INT_LIT
         * @throws NumberFormatException
         */
        public int intVal() throws NumberFormatException {
            return Integer.parseInt(chars.substring(pos, pos + length));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((kind == null) ? 0 : kind.hashCode());
            result = prime * result + length;
            result = prime * result + pos;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Token)) {
                return false;
            }
            Token other = (Token) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (kind != other.kind) {
                return false;
            }
            if (length != other.length) {
                return false;
            }
            if (pos != other.pos) {
                return false;
            }
            return true;
        }


        private Scanner getOuterType() {
            return Scanner.this;
        }
    }

    // Skip white spaces
    private int skipWhiteSpace(int pos) {
        if (pos < chars.length()) {
            while (Character.isSpaceChar(chars.charAt(pos))) {
                pos++;
                if (pos == chars.length()) {
                    break;
                }
            }

        }
        return pos;

    }

    Scanner(String chars) {
        this.chars = chars;
        tokens = new ArrayList<Token>();

    }

    // According to char's position, to determine which line it is at
    public int returnLineNumber(int pos) {
        int linepos = 0;
        int i;
        for (i = 0; i < lineStartPos.size(); i++) {
            linepos = lineStartPos.get(i);
            if (pos > linepos) {
                continue;
            } else if (pos == linepos) {
                return i;
            } else {
                break;
            }
        }
        return i - 1;

    }

    /**
     * Initializes Scanner object by traversing chars and adding tokens to
     * tokens list.
     *
     * @return this scanner
     * @throws IllegalCharException
     * @throws IllegalNumberException
     */
    public Scanner scan() throws IllegalCharException, IllegalNumberException {
        int pos = 0;
        int length = chars.length();
        State state = State.START;
        int startPos = 0;
        int ch;
        lineStartPos.add(0);
        while (pos <= length) {
            ch = pos < length ? chars.charAt(pos) : -1;
            switch (state) {
                case START: {
                    pos = skipWhiteSpace(pos);
                    ch = pos < length ? chars.charAt(pos) : -1;
                    startPos = pos;
                    switch (ch) {
                        case -1: {
                            tokens.add(new Token(Kind.EOF, pos, 0));
                            pos++;
                        }
                        break;
                        case '+': {
                            tokens.add(new Token(Kind.PLUS, startPos, 1));
                            pos++;
                        }
                        break;
                        case '&': {
                            tokens.add(new Token(Kind.AND, startPos, 1));
                            pos++;
                        }
                        break;
                        case '*': {
                            tokens.add(new Token(Kind.TIMES, startPos, 1));
                            pos++;
                        }
                        break;
                        case '0': {
                            tokens.add(new Token(Kind.INT_LIT, startPos, 1));
                            pos++;
                        }
                        break;
                        case '%': {
                            tokens.add(new Token(Kind.MOD, startPos, 1));
                            pos++;
                        }
                        break;
                        case '{': {
                            tokens.add(new Token(Kind.LBRACE, startPos, 1));
                            pos++;

                        }
                        break;
                        case '}': {
                            tokens.add(new Token(Kind.RBRACE, startPos, 1));
                            pos++;

                        }
                        break;
                        case '(': {
                            tokens.add(new Token(Kind.LPAREN, startPos, 1));
                            pos++;

                        }
                        break;
                        case ')': {
                            tokens.add(new Token(Kind.RPAREN, startPos, 1));
                            pos++;

                        }
                        break;
                        case ';': {
                            tokens.add(new Token(Kind.SEMI, startPos, 1));
                            pos++;
                        }
                        break;
                        case ',': {
                            tokens.add(new Token(Kind.COMMA, startPos, 1));
                            pos++;
                        }
                        break;
                        case '=': {
                            state = State.AFTER_EQ;
                            pos++;
                        }
                        break;
                        case '>': {
                            state = State.AFTER_GT;
                            pos++;
                        }
                        break;
                        case '<': {
                            state = State.AFTER_LT;
                            pos++;
                        }
                        break;
                        case '/': {
                            state = State.AFTER_DIV;
                            pos++;
                        }
                        break;
                        case '!': {
                            state = State.AFTER_EXCLA;
                            pos++;
                        }
                        break;
                        case '-': {
                            state = State.AFTER_MINUS;
                            pos++;

                        }
                        break;
                        case '|': {
                            state = State.AFTER_OR;
                            pos++;
                        }
                        break;
                        case '\n': {
                            pos++;
                            lineStartPos.add(pos);
                        }
                        break;
                        case '\r': {
                            pos++;
                        }
                        break;

                        default: {
                            if (Character.isDigit(ch)) {
                                state = State.IN_DIGIT;
                                pos++;
                            } else if (Character.isJavaIdentifierStart(ch)) {
                                state = State.IN_IDENT;
                                pos++;
                            } else {
                                throw new IllegalCharException("illegal char at Line: " + returnLineNumber(pos) + ", "
                                        + " at pos " + (pos - lineStartPos.get(returnLineNumber(pos))));
                            }
                        }
                    } // switch (ch)
                }
                break;
                case IN_DIGIT: {

                    if (Character.isDigit(ch)) {
                        pos++;
                    } else {
                        // String numString = chars.substring(startPos, pos);
                        try {
                            Token testToken = new Token(Kind.INT_LIT, startPos, pos - startPos);
                            testToken.intVal();
                            tokens.add(testToken);
                            state = State.START;
                        } catch (NumberFormatException e) {
                            // TODO: handle exception
                            throw new IllegalNumberException("The number at Line: " + returnLineNumber(pos) + ", "
                                    + " at pos " + (pos - lineStartPos.get(returnLineNumber(pos)))
                                    + "is out of the range of a java int");
                        }

                    }
                }
                break;
                case IN_IDENT: {
                    if (Character.isJavaIdentifierPart(ch)) {
                        pos++;
                    } else {
                        String str = chars.substring(startPos, pos);
                        if (isReserved(str)) {
                            tokens.add(new Token(getKind(str), startPos, pos - startPos));
                            state = State.START;
                        } else {
                            tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
                            state = State.START;
                        }
                    }
                }
                break;
                case AFTER_EQ: {
                    if (ch == '=') {
                        tokens.add(new Token(Kind.EQUAL, startPos, 2));
                        pos++;
                        state = State.START;

                    } else
                        throw new IllegalCharException("illegal char at Line: " + returnLineNumber(pos) + ", " + " at pos "
                                + (pos - lineStartPos.get(returnLineNumber(pos))));
                }
                break;
                case AFTER_DIV: {
                    if (ch == '*') {
                        pos++;
                        state = State.START_COMMENT;
                    } else {
                        tokens.add(new Token(Kind.DIV, startPos, 1));
                        state = State.START;
                    }
                }
                break;
                case AFTER_EXCLA: {
                    if (ch == '=') {
                        tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
                        pos++;
                        state = State.START;

                    } else {
                        tokens.add(new Token(Kind.NOT, startPos, 1));
                        state = State.START;
                    }
                }
                break;
                case AFTER_GT: {
                    if (ch == '=') {
                        tokens.add(new Token(Kind.GE, startPos, 2));
                        pos++;
                        state = State.START;
                    } else {
                        tokens.add(new Token(Kind.GT, startPos, 1));
                        state = State.START;
                    }
                }
                break;
                case AFTER_LT: {
                    if (ch == '=') {
                        tokens.add(new Token(Kind.LE, startPos, 2));
                        pos++;
                        state = State.START;
                    } else if (ch == '-') {
                        tokens.add(new Token(Kind.ASSIGN, startPos, 2));
                        pos++;
                        state = State.START;
                    } else {
                        tokens.add(new Token(Kind.LT, startPos, 1));
                        state = State.START;

                    }
                }
                break;
                case AFTER_MINUS: {
                    if (ch == '>') {
                        tokens.add(new Token(Kind.ARROW, startPos, 2));
                        pos++;
                        state = State.START;
                    } else {
                        tokens.add(new Token(Kind.MINUS, startPos, 1));
                        state = State.START;
                    }

                }
                break;
                case AFTER_OR: {
                    if (ch == '-') {
                        pos++;
                        state = State.OR_MINUS;

                    } else {
                        tokens.add(new Token(Kind.OR, startPos, 1));
                        state = State.START;
                    }

                }
                break;

                case OR_MINUS: {
                    if (ch == '>') {
                        pos++;
                        tokens.add(new Token(Kind.BARARROW, startPos, 3));
                        state = State.START;

                    } else {
                        tokens.add(new Token(Kind.OR, startPos, 1));
                        tokens.add(new Token(Kind.MINUS, startPos + 1, 1));
                        state = State.START;

                    }

                }
                break;
                case START_COMMENT: {
                    if (ch == '*') {
                        state = State.END_COMMENT;
                        pos++;
                    } else if (ch == -1) {
                        state = State.START;

                    } else if (ch == '\n') {
                        pos++;
                        lineStartPos.add(pos);
                    } else {
                        pos++;
                    }
                }
                break;
                case END_COMMENT: {
                    if (ch == '/') {
                        state = State.START;
                        pos++;
                    } else {
                        state = State.START_COMMENT;
                    }
                }
                break;
                default:
                    assert false;
            }// switch(state)
        } // while
        tokens.add(new Token(Kind.EOF, pos, 0));
        return this;
    }

    final ArrayList<Token> tokens;
    final String chars;
    int tokenNum;

    /*
     * Return the next token in the token list and update the state so that the
     * next call will return the Token..
     */
    public Token nextToken() {
        if (tokenNum >= tokens.size())
            return null;
        return tokens.get(tokenNum++);
    }

    /*
     * Return the next token in the token list without updating the state. (So
     * the following call to next will return the same token.)
     */
    public Token peek() {
        if (tokenNum >= tokens.size())
            return null;
        return tokens.get(tokenNum);
    }

    /**
     * Returns a LinePos object containing the line and position in line of the
     * given token.
     * <p>
     * Line numbers start counting at 0
     *
     * @param t
     * @return
     */
    public LinePos getLinePos(Token t) {
        return t.getLinePos();
    }

}
