package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.PLUS;
import static org.junit.Assert.assertEquals;

public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}



	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}


	@Test
	public void testBinaryC() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "zyn -> zyn;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		BinaryChain bc = (BinaryChain) ast;
		assertEquals(IdentChain.class, bc.getE0().getClass());
		assertEquals(IdentChain.class, bc.getE1().getClass());
		assertEquals(ARROW, bc.getArrow().kind);
	}


	@Test
	public void testBinaryChain() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "blur (zyn ==1) -> zyn;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		BinaryChain bc = (BinaryChain) ast;
		System.out.println(bc.toString());

	}

	@Test
	public void testWhileStatement() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "while(zyn ==1 ){ zyn <- 1+2;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(WhileStatement.class, ast.getClass());
		WhileStatement ws = (WhileStatement) ast;
		assertEquals(BinaryExpression.class, ws.getE().getClass());
		assertEquals(Block.class, ws.getB().getClass());
		System.out.println(ws.toString());

	}
}
