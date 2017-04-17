package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.AST.Type;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cop5556sp17.AST.Type.TypeName;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;


public class CodeGenVisitor implements ASTVisitor, Opcodes {

    /**
     * @param DEVEL          used as parameter to genPrint and genPrintTOS
     * @param GRADE          used as parameter to genPrint and genPrintTOS
     * @param sourceFileName name of source file, may be null.
     */
    public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
        super();
        this.DEVEL = DEVEL;
        this.GRADE = GRADE;
        this.sourceFileName = sourceFileName;
    }

    ClassWriter cw;
    String className;
    String classDesc;
    String sourceFileName;
    int slot_Number = 1;
    int count = 0;
    Map<Dec, Label> startLabelMap = new HashMap<>();
    Map<Dec, Label> endLabelMap = new HashMap<>();

    MethodVisitor mv; // visitor of method currently under construction

    /**
     * Indicates whether genPrint and genPrintTOS should generate code.
     */
    final boolean DEVEL;
    final boolean GRADE;

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        className = program.getName();
        classDesc = "L" + className + ";";
        String sourceFileName = (String) arg;
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
                new String[]{"java/lang/Runnable"});
        cw.visitSource(sourceFileName, null);

        // generate constructor code
        // get a MethodVisitor
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
                null);
        mv.visitCode();
        // Create label at start of code
        Label constructorStart = new Label();
        mv.visitLabel(constructorStart);
        // this is for convenience during development--you can see that the code
        // is doing something.
        CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
        // generate code to call superclass constructor
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        // visit parameter decs to add each as field to the class
        // pass in mv so decs can add their initialization code to the
        // constructor.
        ArrayList<ParamDec> params = program.getParams();
        for (ParamDec dec : params)
            dec.visit(this, mv);
        mv.visitInsn(RETURN);
        // create label at end of code
        Label constructorEnd = new Label();
        mv.visitLabel(constructorEnd);
        // finish up by visiting local vars of constructor
        // the fourth and fifth arguments are the region of code where the local
        // variable is defined as represented by the labels we inserted.
        mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
        // indicates the max stack size for the method.
        // because we used the COMPUTE_FRAMES parameter in the classwriter
        // constructor, asm
        // will do this for us. The parameters to visitMaxs don't matter, but
        // the method must
        // be called.
        mv.visitMaxs(1, 1);
        // finish up code generation for this method.
        mv.visitEnd();
        // end of constructor

        // create main method which does the following
        // 1. instantiate an instance of the class being generated, passing the
        // String[] with command line arguments
        // 2. invoke the run method.
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
                null);
        mv.visitCode();
        Label mainStart = new Label();
        mv.visitLabel(mainStart);
        // this is for convenience during development--you can see that the code
        // is doing something.
        CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
        mv.visitInsn(RETURN);
        Label mainEnd = new Label();
        mv.visitLabel(mainEnd);
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
        mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // create run method
        mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        mv.visitCode();
        Label startRun = new Label();
        mv.visitLabel(startRun);
        CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
        program.getB().visit(this, null);
        mv.visitInsn(RETURN);
        Label endRun = new Label();
        mv.visitLabel(endRun);
        mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
        // visit the local variables
        List<Dec> decList = program.getB().getDecs();
        for (Dec dec : decList) {
            if (startLabelMap.get(dec) == null || endLabelMap.get(dec) == null) {
                Label slabel = new Label();
                Label elabel = new Label();
                mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, slabel, elabel, dec.slotNum);
            } else {
                mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null, startLabelMap.get(dec), endLabelMap.get(dec), dec.slotNum);
            }
        }
        mv.visitMaxs(1, 1);
        mv.visitEnd(); // end of run method

        cw.visitEnd();//end of class

        //generate classfile and return it
        return cw.toByteArray();
    }


    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
        assignStatement.getE().visit(this, arg);
        if (assignStatement.getE().getTypeName().isType(IMAGE)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
        }
        CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
        CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
        assignStatement.getVar().visit(this, arg);

        return null;
    }

    @Override
    public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
        binaryChain.getE0().visit(this, true);
        TypeName e0TypeName = binaryChain.getE0().getTypeName();
        if (e0TypeName.isType(URL)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
        } else if (e0TypeName.isType(FILE)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
        }
        binaryChain.getE1().visit(this, false);
        return null;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
        binaryExpression.getE0().visit(this, arg);
        binaryExpression.getE1().visit(this, arg);
        TypeName e0TypeName = binaryExpression.getE0().getTypeName();
        TypeName e1TypeName = binaryExpression.getE1().getTypeName();
        Scanner.Kind opKind = binaryExpression.getOp().kind;
        Label falseLabel = new Label();
        Label after = new Label();
        if (e0TypeName.isType(TypeName.INTEGER, BOOLEAN) && e1TypeName.isType(TypeName.INTEGER, BOOLEAN)) {
            switch (opKind) {
                case PLUS: {
                    mv.visitInsn(IADD);
                }
                break;
                case MINUS: {
                    mv.visitInsn(ISUB);
                }
                break;
                case OR: {
                    mv.visitInsn(IOR);
                }
                break;
                case TIMES: {
                    mv.visitInsn(IMUL);
                }
                break;
                case DIV: {
                    mv.visitInsn(IDIV);
                }
                break;
                case AND: {
                    mv.visitInsn(IAND);
                }
                break;
                case MOD: {
                    mv.visitInsn(IREM);
                }
                break;
                case LT: {
                    mv.visitJumpInsn(IF_ICMPGE, falseLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitJumpInsn(GOTO, after);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(after);
                }
                break;
                case LE: {
                    mv.visitJumpInsn(IF_ICMPGT, falseLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitJumpInsn(GOTO, after);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(after);
                }
                break;
                case GT: {
                    mv.visitJumpInsn(IF_ICMPLE, falseLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitJumpInsn(GOTO, after);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(after);
                }
                break;
                case GE: {
                    mv.visitJumpInsn(IF_ICMPLT, falseLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitJumpInsn(GOTO, after);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(after);
                }
                break;
                case EQUAL: {
                    mv.visitJumpInsn(IF_ICMPNE, falseLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitJumpInsn(GOTO, after);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(after);
                }
                break;
                case NOTEQUAL: {
                    mv.visitJumpInsn(IF_ICMPEQ, falseLabel);
                    mv.visitInsn(ICONST_1);
                    mv.visitJumpInsn(GOTO, after);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(ICONST_0);
                    mv.visitLabel(after);
                }
                break;

            }
        } else if (e0TypeName.isType(IMAGE) && e1TypeName.isType(IMAGE)) {
            switch (opKind) {
                case PLUS: {
                    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
                }
                break;
                case MINUS: {
                    mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
                }
                break;
            }
        } else if (e0TypeName.isType(IMAGE) && e1TypeName.isType(TypeName.INTEGER) && binaryExpression.getOp().isKind(TIMES)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
        } else if (e0TypeName.isType(TypeName.INTEGER) && e1TypeName.isType(IMAGE) && binaryExpression.getOp().isKind(TIMES)) {
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
        } else if (e0TypeName.isType(IMAGE) && e1TypeName.isType(TypeName.INTEGER)) {
            if (binaryExpression.getOp().isKind(DIV)) {
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
            } else if (binaryExpression.getOp().isKind(MOD)) {
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
            }
        }
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        Label endLabel = new Label();
        for (Dec dec : block.getDecs()) {
            dec.visit(this, arg);
            endLabelMap.put(dec, endLabel);
        }

        for (Statement statement : block.getStatements()) {
            statement.visit(this, arg);
            if (statement instanceof BinaryChain) {
                mv.visitInsn(POP);
            }
        }
        mv.visitLabel(endLabel);
        return null;
    }

    @Override
    public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
        if (booleanLitExpression.getValue()) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
        return null;
    }

    @Override
    public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
        if (constantExpression.getFirstToken().isKind(Scanner.Kind.KW_SCREENHEIGHT)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
        } else if (constantExpression.getFirstToken().isKind(Scanner.Kind.KW_SCREENWIDTH)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
        }
        return null;
    }

    @Override
    public Object visitDec(Dec declaration, Object arg) throws Exception {
        declaration.slotNum = slot_Number++;
        if (declaration.getTypeName().isType(FRAME, IMAGE)) {
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, declaration.slotNum);
        }
        return null;
    }

    @Override
    public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
        filterOpChain.getArg().visit(this, arg);
        Scanner.Token filterOp = filterOpChain.getFirstToken();
        if (filterOp.isKind(OP_BLUR)) {
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
        } else if (filterOp.isKind(OP_CONVOLVE)) {
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
        } else if (filterOp.isKind(OP_GRAY)) {
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
        }
        return null;
    }

    @Override
    public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
        frameOpChain.getArg().visit(this, arg);
        Scanner.Token frameOp = frameOpChain.getFirstToken();
        if (frameOp.isKind(KW_SHOW)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
        } else if (frameOp.isKind(KW_HIDE)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
        } else if (frameOp.isKind(KW_MOVE)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
        } else if (frameOp.isKind(KW_XLOC)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
        } else if (frameOp.isKind(KW_YLOC)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
        }
        return null;
    }

    @Override
    public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
//        if ident is on the left side of binary chain, the arg == true. Otherwise, arg == false
        if ((boolean) arg == true) {
            if (identChain.getDec() instanceof ParamDec) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getTypeName().getJVMTypeDesc());
            } else if (identChain.getDec().getTypeName().isType(BOOLEAN, TypeName.INTEGER)) {
                mv.visitVarInsn(ILOAD, identChain.getDec().slotNum);
            } else if (identChain.getDec().getTypeName().isType(IMAGE, FRAME)) {
                mv.visitVarInsn(ALOAD, identChain.getDec().slotNum);
            }
        }
        //identChain is on the right side
        else if ((boolean) arg == false) {
            TypeName decTypeName = identChain.getDec().getTypeName();
            if (decTypeName.isType(TypeName.INTEGER)) {
                mv.visitInsn(DUP);
                mv.visitVarInsn(ISTORE, identChain.getDec().slotNum);
            } else if (decTypeName.isType(IMAGE)) {
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, identChain.getDec().slotNum);
            } else if (decTypeName.isType(FILE)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getTypeName().getJVMTypeDesc());
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
            } else if (decTypeName.isType(FRAME)) {
                mv.visitVarInsn(ALOAD, identChain.getDec().slotNum);
                mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, identChain.getDec().slotNum);
            }
        }
        return null;
    }

    @Override
    public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
        if (identExpression.getDec() instanceof ParamDec) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(), identExpression.getDec().getTypeName().getJVMTypeDesc());
        } else if (identExpression.getDec().getTypeName().isType(BOOLEAN, Type.TypeName.INTEGER)) {
            mv.visitVarInsn(ILOAD, identExpression.getDec().slotNum);
        } else if (identExpression.getDec().getTypeName().isType(IMAGE, FRAME)) {
            mv.visitVarInsn(ALOAD, identExpression.getDec().slotNum);
        }
        return null;
    }

    @Override
    public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
        if (identX.getDec() instanceof ParamDec) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(SWAP);
            mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(), identX.getDec().getTypeName().getJVMTypeDesc());
        } else if (identX.getDec().getTypeName().isType(BOOLEAN, Type.TypeName.INTEGER)) {
            mv.visitVarInsn(ISTORE, identX.getDec().slotNum);
        } else if (identX.getDec().getTypeName().isType(IMAGE, FRAME)) {
            mv.visitVarInsn(ASTORE, identX.getDec().slotNum);
        }
        Label startL = new Label();
        mv.visitLabel(startL);
        startLabelMap.put(identX.getDec(), startL);
        return null;

    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
        ifStatement.getE().visit(this, arg);
        Label AFTER = new Label();
        mv.visitJumpInsn(IFEQ, AFTER);
        ifStatement.getB().visit(this, arg);
        mv.visitLabel(AFTER);
        return null;
    }

    @Override
    public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
        imageOpChain.getArg().visit(this, arg);
        Scanner.Token imageOp = imageOpChain.getFirstToken();
        if (imageOp.isKind(OP_WIDTH)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
        } else if (imageOp.isKind(OP_HEIGHT)) {
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getHeightSig, false);
        } else if (imageOp.isKind(KW_SCALE)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
        }
        return null;
    }

    @Override
    public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
        mv.visitLdcInsn(intLitExpression.value);
        return null;
    }


    @Override
    public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
        FieldVisitor fv;
        fv = cw.visitField(0, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
        fv.visitEnd();
        mv.visitVarInsn(ALOAD, 0);
        if (paramDec.getTypeName().isType(FILE)) {
            mv.visitTypeInsn(NEW, "java/io/File");
            mv.visitInsn(DUP);
        }
        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn(count++);
        if (!paramDec.getTypeName().isType(URL)) {
            mv.visitInsn(AALOAD);
        }
        if (paramDec.getTypeName().isType(TypeName.INTEGER)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
        } else if (paramDec.getTypeName().isType(BOOLEAN)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
        } else if (paramDec.getTypeName().isType(FILE)) {
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
        } else if (paramDec.getTypeName().isType(URL)) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
        }
        mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
        return null;
    }

    @Override
    public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
        sleepStatement.getE().visit(this, arg);
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
        return null;
    }

    @Override
    public Object visitTuple(Tuple tuple, Object arg) throws Exception {
        List<Expression> exprlist = tuple.getExprList();
        for (Expression expr : exprlist) {
            expr.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
        Label GUARD = new Label();
        mv.visitJumpInsn(GOTO, GUARD);
        Label BODY = new Label();
        mv.visitLabel(BODY);
        whileStatement.getB().visit(this, arg);
        mv.visitLabel(GUARD);
        whileStatement.getE().visit(this, arg);
        mv.visitJumpInsn(IFNE, BODY);
        return null;
    }

}
