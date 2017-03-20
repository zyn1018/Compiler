package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

import static cop5556sp17.AST.Type.TypeName.NONE;

public abstract class Chain extends Statement {

    private TypeName typeName = NONE;

    public TypeName getTypeName() {
        return typeName;
    }

    public void setTypeName(TypeName typeName) {
        this.typeName = typeName;
    }

    public Chain(Token firstToken) {
        super(firstToken);
    }

}
