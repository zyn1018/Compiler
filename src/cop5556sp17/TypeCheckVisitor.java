package cop5556sp17;


import cop5556sp17.AST.*;

import static cop5556sp17.AST.Type.TypeName;
import static cop5556sp17.AST.Type.TypeName.*;
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

        return null;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
        binaryExpression.getE0().visit(this, null);
        binaryExpression.getE1().visit(this, null);
        TypeName e0 = binaryExpression.getE0().getTypeName();
        TypeName e1 = binaryExpression.getE1().getTypeName();
        Scanner.Token op = binaryExpression.getOp();
        if (e0.equals(INTEGER) && e1.equals(INTEGER)
                && (op.isKind(PLUS) || op.isKind(MINUS))) {
            binaryExpression.setTypeName(INTEGER);
            return binaryExpression.getTypeName();
        } else if (e0.equals(IMAGE) && e1.equals(IMAGE)
                && (op.isKind(PLUS) || op.isKind(MINUS))) {
            binaryExpression.setTypeName(IMAGE);
            return binaryExpression.getTypeName();
        } else if (op.isKind(LT, GT, LE, GE)
                && ((e0.equals(e1)
                && (e0.equals(INTEGER)
                || e0.equals(BOOLEAN))))) {
            binaryExpression.setTypeName(BOOLEAN);
            return binaryExpression.getTypeName();
        } else if (op.isKind(EQUAL, NOTEQUAL)
                && e0.equals(e1)) {
            binaryExpression.setTypeName(BOOLEAN);
            return binaryExpression.getTypeName();
        } else if (op.isKind(TIMES) &&
                ((e0.equals(INTEGER) && e1.equals(IMAGE))
                        || (e0.equals(IMAGE) && e1.equals(INTEGER)))
                ) {
            binaryExpression.setTypeName(IMAGE);
            return binaryExpression.getTypeName();
        } else if (e0.equals(INTEGER) && e1.equals(INTEGER)
                && (op.isKind(TIMES) || (op.isKind(DIV)))) {
            binaryExpression.setTypeName(INTEGER);
            return binaryExpression.getTypeName();
        } else throw new TypeCheckException("Encounterd a type error when visiting BinaryExpression");
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        symtab.enterScope();

        return null;
    }

    @Override
    public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitDec(Dec declaration, Object arg) throws Exception {

        if (!symtab.insert(declaration.getIdent().getText(), declaration)) {
            throw new TypeCheckException("Already declared " + declaration.getIdent().getText() + " in this scope");
        }

        return declaration.getType();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
        if (!symtab.insert(paramDec.getIdent().getText(), paramDec)) {
            throw new TypeCheckException("Already declared " + paramDec.getIdent().getText() + " at this scope");
        }

        return paramDec.getType();
    }

    @Override
    public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitTuple(Tuple tuple, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


}
