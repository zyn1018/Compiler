package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;

public abstract class Expression extends ASTNode {

    protected Expression(Token firstToken) {
        super(firstToken);
    }

    private TypeName typeName = TypeName.NONE;

    public TypeName getTypeName() {
        return typeName;
    }

    public void setTypeName(TypeName typeName) {
        this.typeName = typeName;
    }

    @Override

    abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
