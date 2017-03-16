package cop5556sp17;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "abc";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        parser.factor();
    }

    @Test
    public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "  (3,5) ";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        System.out.println(scanner);
        Parser parser = new Parser(scanner);
        parser.arg();
    }

    @Test
    public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "  (3,) ";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Parser parser = new Parser(scanner);
        thrown.expect(Parser.SyntaxException.class);
        parser.arg();
    }

    @Test
    public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog0 {}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog1 {sleep zyn10;}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram2() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog2 {while(zyn!=1){} if(zyn==2){}}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram3() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog3 integer zyn,file zyn1,url zyn3 {zyn -> gray; zyn<-8/7+1;\n" + "}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram4() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog4 integer zyn,file zyn1,url zyn3 {zyn -> gray; zyn<-8/7+1;\n"
                + "zyn |-> convolve; zyn->show ;\n" + "zyn |-> blur;}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram5() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog5 integer zyn,file zyn1,url zyn3 {zyn -> hide; zyn<-8/7+1;\n"
                + "zyn |-> move; zyn->xloc ;\n" + "zyn |->yloc;}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram6() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog6 {while(true){zyn <- 5*6+(3/2+3%2);}}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram7() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog7 {show |-> zyn;}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram8() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "prog8 { xyza123|->; }";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.parse();
    }

    @Test
    public void testProgram9() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "x -> show -> hide ;";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.chain();

    }

}