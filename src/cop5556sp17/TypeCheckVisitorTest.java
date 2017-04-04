/**
 * Important to test the error cases in case the
 * AST is not being completely traversed.
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TypeCheckVisitorTest {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAssignmentBoolLit0() throws Exception {
        String input = "p {\nboolean y \ny <- false;}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        program.visit(v, null);
    }

    @Test
    public void testAssignmentBoolLitError0() throws Exception {
        String input = "p {\nboolean y \ny <- 3;}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        thrown.expect(TypeCheckVisitor.TypeCheckException.class);
        program.visit(v, null);
    }

    @Test
    public void testWhileIfStatement() throws Exception {
        String input = "p integer zyn {integer x while(zyn!=1){zyn <- zyn +1;} if(zyn==2){x<-1;}}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        program.visit(v, null);
    }

    @Test
    public void testBinaryExpression() throws Exception {
        String input = "p {integer x integer y boolean flag1 boolean flag2 image img  x <- x+y; y <-x-y; x<-x*y; y<-x/y; img <- x*img; " +
                "    img<-img*y; flag1 <-x<y; flag2<-x<=y; flag1<-x>y; flag1<-x>=y; " +
                "flag1<-flag1 > flag2; flag2<-flag1 >= flag2; flag1<-flag1<flag2; flag1<-flag1<=flag2; flag1<- x==y; flag2<- x!=y;}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        program.visit(v, null);
    }

    @Test
    public void testBinaryChain() throws Exception {
        String input = "p url com,file doc { \n" +
                "image img frame f integer x com->img;\n" +
                "doc->img; f->show;f->hide; \n" +
                "f->move(x+1,x+2);f->xloc;f->yloc;\n" +
                "img->width ; img->height; img->f;\n" +
                " img->doc;img|->gray;img|->blur; \n" +
                "img->convolve;img->scale(x+1);}\n";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        program.visit(v, null);
    }

    @Test
    public void testDeclaredVar() throws Exception {
        String input = "p {integer x integer x}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        thrown.expect(TypeCheckVisitor.TypeCheckException.class);
        program.visit(v, null);

    }

    @Test
    public void test1() throws Exception {
        String input = "p integer a, integer b {image img1 image img2 if(img1 != img2) {image a a <- img1; } if(a != b) {boolean a a <- img1 != img2; }}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        //thrown.expect(TypeCheckVisitor.TypeCheckException.class);
        program.visit(v, null);
    }

    @Test
    public void test2() throws Exception {
        String input = "prog3 integer i{ integer x integer y boolean z  if(true){x<-1;y<-1000;y<-y+1;z<-x==y;} while(i<4){i<-i+1;integer xx xx<-1;xx<-xx-1;}}";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        ASTNode program = parser.parse();
        TypeCheckVisitor v = new TypeCheckVisitor();
        //thrown.expect(TypeCheckVisitor.TypeCheckException.class);
        program.visit(v, null);
    }
}
