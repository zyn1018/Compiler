package cop5556sp17;


import cop5556sp17.AST.*;

import java.util.List;

import static cop5556sp17.AST.Type.TypeName;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.AST.Type.getTypeName;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

    @SuppressWarnings("serial")
    public static class TypeCheckException extends Exception {
        TypeCheckException(String message) {
            super(message);
        }
    }

    SymbolTable symtab = new SymbolTable();


    @Override
    public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
        binaryChain.getE0().visit(this, arg);
        binaryChain.getE1().visit(this, arg);
        Chain e0 = binaryChain.getE0();
        Chain e1 = binaryChain.getE1();
        TypeName e0TypeName = binaryChain.getE0().getTypeName();
        TypeName e1TypemName = binaryChain.getE1().getTypeName();
        Scanner.Token op = binaryChain.getArrow();
        if (e0TypeName.isType(URL) && e1TypemName.isType(IMAGE) && op.isKind(ARROW)) {
            binaryChain.setTypeName(IMAGE);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(FILE) && e1TypemName.isType(IMAGE) && op.isKind(ARROW)) {
            binaryChain.setTypeName(IMAGE);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(FRAME) && e1 instanceof FrameOpChain && e1.getFirstToken().isKind(KW_XLOC, KW_YLOC) && op.isKind(ARROW)) {
            binaryChain.setTypeName(INTEGER);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(FRAME) && e1 instanceof FrameOpChain && e1.getFirstToken().isKind(KW_SHOW, KW_HIDE, KW_MOVE) && op.isKind(ARROW)) {
            binaryChain.setTypeName(FRAME);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(IMAGE) && e1 instanceof ImageOpChain && e1.getFirstToken().isKind(OP_WIDTH, OP_HEIGHT) && op.isKind(ARROW)) {
            binaryChain.setTypeName(INTEGER);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(IMAGE) && e1TypemName.isType(FRAME) && op.isKind(ARROW)) {
            binaryChain.setTypeName(FRAME);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(IMAGE) && e1TypemName.isType(FILE) && op.isKind(ARROW)) {
            binaryChain.setTypeName(NONE);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(IMAGE) && e1 instanceof FilterOpChain && e1.getFirstToken().isKind(OP_GRAY, OP_BLUR, OP_CONVOLVE) && op.isKind(ARROW, BARARROW)) {
            binaryChain.setTypeName(IMAGE);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(IMAGE) && e1 instanceof ImageOpChain && e1.getFirstToken().isKind(KW_SCALE) && op.isKind(ARROW)) {
            binaryChain.setTypeName(IMAGE);
            return binaryChain.getTypeName();
        } else if (e0TypeName.isType(IMAGE) && e1 instanceof IdentChain && op.isKind(ARROW)) {
            binaryChain.setTypeName(IMAGE);
            return binaryChain.getTypeName();
        } else
            throw new TypeCheckException("Encounterd a type error at " + binaryChain.getFirstToken().getLinePos() + " when visiting BinaryChain");
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
        binaryExpression.getE0().visit(this, arg);
        binaryExpression.getE1().visit(this, arg);
        TypeName e0 = binaryExpression.getE0().getTypeName();
        TypeName e1 = binaryExpression.getE1().getTypeName();
        Scanner.Token op = binaryExpression.getOp();
        if (e0.isType(INTEGER) && e1.isType(INTEGER) && (op.isKind(PLUS) || op.isKind(MINUS))) {
            binaryExpression.setTypeName(INTEGER);
            return binaryExpression.getTypeName();
        } else if (e0.isType(IMAGE) && e1.isType(IMAGE) && (op.isKind(PLUS) || op.isKind(MINUS))) {
            binaryExpression.setTypeName(IMAGE);
            return binaryExpression.getTypeName();
        } else if (op.isKind(LT, GT, LE, GE) && ((e0.isType(e1) && (e0.isType(INTEGER) || e0.isType(BOOLEAN))))) {
            binaryExpression.setTypeName(BOOLEAN);
            return binaryExpression.getTypeName();
        } else if (op.isKind(EQUAL, NOTEQUAL) && e0.isType(e1)) {
            binaryExpression.setTypeName(BOOLEAN);
            return binaryExpression.getTypeName();
        } else if (op.isKind(TIMES) && ((e0.isType(INTEGER) && e1.isType(IMAGE)) || (e0.isType(IMAGE) && e1.isType(INTEGER)))) {
            binaryExpression.setTypeName(IMAGE);
            return binaryExpression.getTypeName();
        } else if (e0.isType(INTEGER) && e1.isType(INTEGER) && (op.isKind(TIMES) || (op.isKind(DIV)))) {
            binaryExpression.setTypeName(INTEGER);
            return binaryExpression.getTypeName();
        } else
            throw new TypeCheckException("Encounterd a type error at " + binaryExpression.getFirstToken().getLinePos() + " when visiting BinaryExpression");
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        symtab.enterScope();
        List<Dec> decList = block.getDecs();
        List<Statement> statementList = block.getStatements();
        for (Dec dec : decList) {
            dec.visit(this, arg);
        }
        for (Statement stmt : statementList) {
            stmt.visit(this, arg);
        }
        symtab.leaveScope();
        return null;
    }

    @Override
    public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
        booleanLitExpression.setTypeName(BOOLEAN);
        return null;
    }

    @Override
    public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
        Tuple tuple = filterOpChain.getArg();
        List<Expression> exprList = tuple.getExprList();
        if (exprList.size() == 0) {
            filterOpChain.getArg().visit(this, arg);
            filterOpChain.setTypeName(IMAGE);
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + filterOpChain.getFirstToken().getLinePos() + " when visiting filterOpChain");

    }

    @Override
    public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
        Scanner.Token frameOP = frameOpChain.getFirstToken();
        int tupleLength = frameOpChain.getArg().getExprList().size();
        frameOpChain.getArg().visit(this, arg);
        if (frameOP.isKind(KW_SHOW, KW_HIDE) && tupleLength == 0) {
            frameOpChain.setTypeName(NONE);
        } else if (frameOP.isKind(KW_XLOC, KW_YLOC) && tupleLength == 0) {
            frameOpChain.setTypeName(INTEGER);
        } else if (frameOP.isKind(KW_MOVE) && tupleLength == 2) {
            frameOpChain.setTypeName(NONE);
        } else
            throw new TypeCheckException("Encounterd a type error at " + frameOpChain.getFirstToken().getLinePos() + " when visiting FrameOpChain");
        return null;
    }

    @Override
    public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
        String ident = identChain.firstToken.getText();
        Dec dec = symtab.lookup(ident);
        if (dec != null) {
            identChain.setTypeName(getTypeName(dec.getFirstToken()));
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + identChain.getFirstToken().getLinePos() + " when visiting identChain");
    }

    @Override
    public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
        String ident = identExpression.getFirstToken().getText();
        Dec temp = symtab.lookup(ident);
        if (temp != null) {
            identExpression.setTypeName(getTypeName(temp.getFirstToken()));
            identExpression.setDec(temp);
        } else
            throw new TypeCheckException("Encounterd a type error at " + identExpression.getFirstToken().getLinePos() + " when visiting identExpression");
        return null;
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
        ifStatement.getE().visit(this, arg);
        ifStatement.getB().visit(this, arg);
        if (ifStatement.getE().getTypeName().isType(BOOLEAN)) {
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + ifStatement.getFirstToken().getLinePos() + " when visiting ifStatement");
    }

    @Override
    public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
        intLitExpression.setTypeName(INTEGER);
        return null;
    }

    @Override
    public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
        sleepStatement.getE().visit(this, arg);
        if (sleepStatement.getE().getTypeName().isType(INTEGER)) {
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + sleepStatement.getFirstToken().getLinePos() + " when visiting sleepstatement");
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
        whileStatement.getE().visit(this, arg);
        whileStatement.getB().visit(this, arg);
        if (whileStatement.getE().getTypeName().isType(BOOLEAN)) {
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + whileStatement.getFirstToken().getLinePos() + " when visiting whilestatement");
    }

    @Override
    public Object visitDec(Dec declaration, Object arg) throws Exception {

        if (symtab.insert(declaration.getIdent().getText(), declaration)) {
            Dec temp = symtab.lookup(declaration.getIdent().getText());
            declaration.setTypeName(getTypeName(temp.getFirstToken()));
        } else throw new TypeCheckException("Already declared " + declaration.getIdent().getText() + " at this scope");
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        List<ParamDec> paramDecList = program.getParams();
        for (ParamDec paramDec : paramDecList) {
            paramDec.visit(this, arg);
        }
        program.getB().visit(this, arg);
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
        Expression expr = assignStatement.getE();
        IdentLValue identLValue = assignStatement.getVar();
        identLValue.visit(this, arg);
        expr.visit(this, arg);
        TypeName identLvalueType = assignStatement.getVar().getDec().getTypeName();
        TypeName exprTypeName = expr.getTypeName();
        if (exprTypeName.isType(identLvalueType)) {
            expr.visit(this, arg);
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + assignStatement.getFirstToken().getLinePos() + " when visiting assignstatement");
    }

    @Override
    public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
        String ident = identX.getFirstToken().getText();
        Dec temp = symtab.lookup(ident);
        if (temp != null) {
            identX.setDec(temp);
            identX.getDec().setTypeName(getTypeName(identX.getDec().getFirstToken()));
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + identX.getFirstToken().getLinePos() + " when visiting identLValue");
    }

    @Override
    public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
        if (symtab.insert(paramDec.getIdent().getText(), paramDec)) {
            Dec temp = symtab.lookup(paramDec.getIdent().getText());
            paramDec.setTypeName(getTypeName(temp.getFirstToken()));
        } else throw new TypeCheckException("Already declared " + paramDec.getIdent().getText() + " at this scope");
        return paramDec.getType();
    }

    @Override
    public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
        constantExpression.setTypeName(INTEGER);
        return null;
    }

    @Override
    public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
        Scanner.Token imageOp = imageOpChain.getFirstToken();
        int tupleLength = imageOpChain.getArg().getExprList().size();
        imageOpChain.getArg().visit(this, arg);
        if (imageOp.isKind(OP_WIDTH, OP_HEIGHT) && tupleLength == 0) {
            imageOpChain.setTypeName(INTEGER);
        } else if (imageOp.isKind(KW_SCALE) && tupleLength == 1) {
            imageOpChain.setTypeName(IMAGE);
        } else
            throw new TypeCheckException("Encounterd a type error at " + imageOpChain.getFirstToken().getLinePos() + " when visiting ImageOpChain");
        return null;
    }

    @Override
    public Object visitTuple(Tuple tuple, Object arg) throws Exception {
        List<Expression> exprList = tuple.getExprList();
        int flag = 0;
        if (exprList.size() == 0) {
            return null;
        } else {
            for (Expression expr : exprList) {
                expr.visit(this, arg);
                if (expr.getTypeName().isType(INTEGER)) {
                    flag++;
                }
            }
        }
        if (flag == exprList.size()) {
            for (Expression expr : exprList) {
                expr.visit(this, arg);
            }
            return null;
        } else
            throw new TypeCheckException("Encounterd a type error at " + tuple.getFirstToken().getLinePos() + " when visiting tuple");
    }


}
