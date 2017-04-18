package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;

import java.util.ArrayList;
import java.util.List;

import static cop5556sp17.Scanner.Kind.*;

public class Parser {

    /**
     * Exception to be thrown if a syntax error is detected in the input. You
     * will want to provide a useful error message.
     */
    @SuppressWarnings("serial")
    public static class SyntaxException extends Exception {
        public SyntaxException(String message) {
            super(message);
        }
    }

    /**
     * Useful during development to ensure unimplemented routines are not
     * accidentally called during development. Delete it when the Parser is
     * finished.
     */
    @SuppressWarnings("serial")
    public static class UnimplementedFeatureException extends RuntimeException {
        public UnimplementedFeatureException() {
            super();
        }
    }

    Scanner scanner;
    Token t;

    Parser(Scanner scanner) {
        this.scanner = scanner;
        t = scanner.nextToken();
    }

    /**
     * parse the input using tokens from the scanner. Check for EOF (i.e. no
     * trailing junk) when finished
     *
     * @throws SyntaxException
     */
    Program parse() throws SyntaxException {
        Program p = program();
        matchEOF();
        return p;
    }

    Expression expression() throws SyntaxException {
        Token firstToken = t;
        Expression e0 = term();
        Expression e1 = null;
        while (t.isKind(relOp)) {
            Token relop = match(relOp);
            e1 = term();
            e0 = new BinaryExpression(firstToken, e0, relop, e1);
        }
        return e0;
    }

    Expression term() throws SyntaxException {
        Token firstToken = t;
        Expression e0 = elem();
        Expression e1 = null;
        while (t.isKind(weakOp)) {
            Token weakop = match(weakOp);
            e1 = elem();
            e0 = new BinaryExpression(firstToken, e0, weakop, e1);
        }
        return e0;
    }

    Expression elem() throws SyntaxException {
        Token firstToken = t;
        Expression e0 = factor();
        Expression e1 = null;
        while (t.isKind(strongOp)) {
            Token strongop = match(strongOp);
            e1 = factor();
            e0 = new BinaryExpression(firstToken, e0, strongop, e1);
        }
        return e0;
    }

    Expression factor() throws SyntaxException {
        Kind kind = t.kind;
        Token firstToken = t;
        Expression e = null;
        switch (kind) {
            case IDENT: {
                consume();
                e = new IdentExpression(firstToken);
            }
            break;
            case INT_LIT: {
                consume();
                e = new IntLitExpression(firstToken);
            }
            break;
            case KW_TRUE:
            case KW_FALSE: {
                consume();
                e = new BooleanLitExpression(firstToken);
            }
            break;
            case KW_SCREENWIDTH:
            case KW_SCREENHEIGHT: {
                consume();
                e = new ConstantExpression(firstToken);
            }
            break;
            case LPAREN: {
                consume();
                e = expression();
                match(RPAREN);
            }
            break;
            default:
                // you will want to provide a more useful error message
                throw new SyntaxException("Not an expected Token at line" + scanner.returnLineNumber(t.pos) + ", "
                        + " at pos " + (t.pos - scanner.lineStartPos.get(scanner.returnLineNumber(t.pos))));
        }
        return e;
    }

    Block block() throws SyntaxException {
        List<Dec> decs = new ArrayList<>();
        List<Statement> statements = new ArrayList<Statement>();
        Token firstToken = t;
        if (t.isKind(LBRACE)) {
            match(LBRACE);
            while (true) {
                if (t.isKind(decfirst)) {
                    decs.add(dec());
                } else if (t.isKind(RBRACE)) {
                    break;
                } else {
                    statements.add(statement());
                }
            }
            match(RBRACE);
        }
        return new Block(firstToken, (ArrayList<Dec>) decs, (ArrayList<Statement>) statements);
    }

    Program program() throws SyntaxException {
        List<ParamDec> params = new ArrayList<ParamDec>();
        Token firstToken = t;
        match(IDENT);
        if (t.isKind(paramdecfirst)) {
            params.add(paramDec());
            while (t.isKind(COMMA)) {
                match(COMMA);
                params.add(paramDec());
            }
            Block b = block();
            return new Program(firstToken, (ArrayList<ParamDec>) params, b);
        } else {
            Block b = block();
            return new Program(firstToken, (ArrayList<ParamDec>) params, b);
        }
    }

    ParamDec paramDec() throws SyntaxException {
        Token firstToken = match(paramdecfirst);
        Token ident = match(IDENT);
        return new ParamDec(firstToken, ident);
    }

    Dec dec() throws SyntaxException {
        Token firstToken = match(decfirst);
        Token ident = match(IDENT);
        return new Dec(firstToken, ident);

    }

    Statement statement() throws SyntaxException {
        Token firstToken = t;

        if (t.isKind(OP_SLEEP)) {
            match(OP_SLEEP);
            Expression e = expression();
            match(SEMI);
            return new SleepStatement(firstToken, e);
        } else if (t.isKind(KW_WHILE)) {
            match(KW_WHILE);
            match(LPAREN);
            Expression e = expression();
            match(RPAREN);
            Block b = block();
            return new WhileStatement(firstToken, e, b);
        } else if (t.isKind(KW_IF)) {
            match(KW_IF);
            match(LPAREN);
            Expression e = expression();
            match(RPAREN);
            Block b = block();
            return new IfStatement(firstToken, e, b);
        } else if (t.isKind(IDENT)) {
            if (scanner.peek().isKind(ASSIGN)) {
                IdentLValue var = new IdentLValue(firstToken);
                match(IDENT);
                match(ASSIGN);
                Expression e = expression();
                match(SEMI);
                return new AssignmentStatement(firstToken, var, e);
            } else {
                Statement c = chain();
                match(SEMI);
                return c;
            }
        } else {
            Statement c = chain();
            match(SEMI);
            return c;
        }
    }

    Chain chain() throws SyntaxException {
        Token firstToken = t;
        Chain e0 = chainElem();
        Token arrow = match(arrowOp);
        ChainElem e1 = chainElem();
        e0 = new BinaryChain(firstToken, e0, arrow, e1);
        while (t.isKind(arrowOp)) {
            arrow = match(arrowOp);
            e1 = chainElem();
            e0 = new BinaryChain(firstToken, e0, arrow, e1);

        }

        return e0;
    }

    ChainElem chainElem() throws SyntaxException {
        Token firstToken = t;

        if (t.isKind(IDENT)) {
            match(IDENT);
            return new IdentChain(firstToken);

        } else if (t.isKind(filterOp)) {
            match(filterOp);
            Tuple arg = arg();
            return new FilterOpChain(firstToken, arg);
        } else if (t.isKind(frameOp)) {
            match(frameOp);
            Tuple arg = arg();
            return new FrameOpChain(firstToken, arg);
        } else if (t.isKind(imageOp)) {
            match(imageOp);
            Tuple arg = arg();
            return new ImageOpChain(firstToken, arg);
        } else
            throw new SyntaxException("Not an expected Token at line" + scanner.returnLineNumber(t.pos) + ", " + " at pos "
                    + (t.pos - scanner.lineStartPos.get(scanner.returnLineNumber(t.pos))));
    }

    Tuple arg() throws SyntaxException {
        Token firstToken = t;
        List<Expression> exprList = new ArrayList<Expression>();
        if (t.isKind(LPAREN)) {
            match(LPAREN);
            exprList.add(expression());
            while (t.isKind(COMMA)) {
                match(COMMA);
                exprList.add(expression());
            }
            match(RPAREN);
        }
        return new Tuple(firstToken, exprList);
    }

    /**
     * Checks whether the current token is the EOF token. If not, a
     * SyntaxException is thrown.
     *
     * @return
     * @throws SyntaxException
     */
    private Token matchEOF() throws SyntaxException {
        if (t.isKind(EOF)) {
            return t;
        }
        throw new SyntaxException("expected EOF at line" + scanner.returnLineNumber(t.pos) + ", " + " at pos "
                + (t.pos - scanner.lineStartPos.get(scanner.returnLineNumber(t.pos))));
    }

    static final Kind[] strongOp = {TIMES, DIV, AND, MOD};
    static final Kind[] weakOp = {PLUS, MINUS, OR};
    static final Kind[] relOp = {LT, LE, GT, GE, EQUAL, NOTEQUAL};
    static final Kind[] imageOp = {OP_WIDTH, OP_HEIGHT, KW_SCALE};
    static final Kind[] frameOp = {KW_SHOW, KW_HIDE, KW_MOVE, KW_XLOC, KW_YLOC};
    static final Kind[] filterOp = {OP_BLUR, OP_GRAY, OP_CONVOLVE};
    static final Kind[] decfirst = {KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_FRAME};
    static final Kind[] paramdecfirst = {KW_URL, KW_FILE, KW_INTEGER, KW_BOOLEAN};
    static final Kind[] arrowOp = {ARROW, BARARROW};

    /**
     * Checks if the current token has the given kind. If so, the current token
     * is consumed and returned. If not, a SyntaxException is thrown.
     * <p>
     * Precondition: kind != EOF
     *
     * @param kind
     * @return
     * @throws SyntaxException
     */
    private Token match(Kind kind) throws SyntaxException {
        if (t.isKind(kind)) {
            return consume();
        }
        throw new SyntaxException("Not an expected Token at line" + scanner.returnLineNumber(t.pos) + ", " + " at pos "
                + (t.pos - scanner.lineStartPos.get(scanner.returnLineNumber(t.pos))));
    }

    /**
     * Checks if the current token has one of the given kinds. If so, the
     * current token is consumed and returned. If not, a SyntaxException is
     * thrown.
     * <p>
     * * Precondition: for all given kinds, kind != EOF
     *
     * @param kinds list of kinds, matches any one
     * @return
     * @throws SyntaxException
     */
    private Token match(Kind... kinds) throws SyntaxException {
        for (Kind kind : kinds) {
            if (t.isKind(kind)) {
                return consume();
            }
        }
        throw new SyntaxException("Not an expected Token at line" + scanner.returnLineNumber(t.pos) + ", " + " at pos "
                + (t.pos - scanner.lineStartPos.get(scanner.returnLineNumber(t.pos))));
    }

    /**
     * Gets the next token and returns the consumed token.
     * <p>
     * Precondition: t.kind != EOF
     *
     * @return
     */
    private Token consume() throws SyntaxException {
        Token tmp = t;
        t = scanner.nextToken();
        return tmp;
    }
}
