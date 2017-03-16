package cop5556sp17;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.LinePos;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

public class ScannerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testEmpty() throws IllegalCharException, IllegalNumberException {
        String input = "";
        Scanner scanner = new Scanner(input);
        scanner.scan();
    }

    @Test
    public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
        // input string
        String input = ";;;";
        // create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        // get the first token and check its kind, position, and contents
        Scanner.Token token = scanner.nextToken();
        assertEquals(SEMI, token.kind);
        assertEquals(0, token.pos);
        String text = SEMI.getText();
        assertEquals(text.length(), token.length);
        assertEquals(text, token.getText());
        // get the next token and check its kind, position, and contents
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(SEMI, token1.kind);
        assertEquals(1, token1.pos);
        assertEquals(text.length(), token1.length);
        assertEquals(text, token1.getText());
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(SEMI, token2.kind);
        assertEquals(2, token2.pos);
        assertEquals(text.length(), token2.length);
        assertEquals(text, token2.getText());
        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token3 = scanner.nextToken();
        assertEquals(Scanner.Kind.EOF, token3.kind);
    }

    /**
     * This test illustrates how to check that the Scanner detects errors
     * properly. In this test, the input contains an int literal with a value
     * that exceeds the range of an int. The scanner should detect this and
     * throw and IllegalNumberException.
     *
     * @throws IllegalCharException
     * @throws IllegalNumberException
     */
    @Test
    public void testIntOverflowError() throws IllegalCharException, IllegalNumberException {
        String input = "99999999999999999";
        Scanner scanner = new Scanner(input);
        thrown.expect(IllegalNumberException.class);
        scanner.scan();

    }

    @Test
    public void testIllegalCharException() throws IllegalCharException, IllegalNumberException {
        String input = " a=b  ";
        Scanner scanner = new Scanner(input);
        thrown.expect(IllegalCharException.class);
        scanner.scan();
    }

    // TODO more tests
    @Test
    public void testComment() throws IllegalCharException, IllegalNumberException {
        String input = "/*Yinan*/*/";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(TIMES, token1.kind);
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(DIV, token2.kind);

    }

    @Test
    public void testReservedWords() throws IllegalCharException, IllegalNumberException {
        String input = "if+";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(KW_IF, token1.kind);
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(PLUS, token2.kind);

    }

    @Test
    public void test1() throws IllegalCharException, IllegalNumberException {
        String input = "        a==b;  <<=>>=";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(IDENT, token1.kind);
        assertEquals(8, token1.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(EQUAL, token2.kind);
        assertEquals(9, token2.pos);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(IDENT, token3.kind);
        assertEquals(11, token3.pos);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(SEMI, token4.kind);
        assertEquals(12, token4.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token5 = scanner.nextToken();
        assertEquals(LT, token5.kind);
        assertEquals(15, token5.pos);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(LE, token6.kind);
        assertEquals(16, token6.pos);

        Scanner.Token token7 = scanner.nextToken();
        assertEquals(GT, token7.kind);
        assertEquals(18, token7.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token8 = scanner.nextToken();
        assertEquals(GE, token8.kind);
        assertEquals(19, token8.pos);

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token9 = scanner.nextToken();
        assertEquals(Scanner.Kind.EOF, token9.kind);
    }

    @Test
    public void test2() throws IllegalCharException, IllegalNumberException {
        String input = "/*yinan*/ |&!!=+-*/%";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(OR, token1.kind);
        assertEquals(10, token1.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(AND, token2.kind);
        assertEquals(11, token2.pos);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(NOT, token3.kind);
        assertEquals(12, token3.pos);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(NOTEQUAL, token4.kind);
        assertEquals(13, token4.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token5 = scanner.nextToken();
        assertEquals(PLUS, token5.kind);
        assertEquals(15, token5.pos);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(MINUS, token6.kind);
        assertEquals(16, token6.pos);

        Scanner.Token token7 = scanner.nextToken();
        assertEquals(TIMES, token7.kind);
        assertEquals(17, token7.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token8 = scanner.nextToken();
        assertEquals(DIV, token8.kind);
        assertEquals(18, token8.pos);

        Scanner.Token token9 = scanner.nextToken();
        assertEquals(MOD, token9.kind);
        assertEquals(19, token9.pos);

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token10 = scanner.nextToken();
        assertEquals(Scanner.Kind.EOF, token10.kind);
    }

    @Test
    public void test3() throws IllegalCharException, IllegalNumberException {
        String input = "yinan/*asdasdaad";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(Scanner.Kind.IDENT, token1.kind);
        assertEquals(token1.getText(), "yinan");
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(Scanner.Kind.EOF, token2.kind);

    }

    @Test
    public void test4() throws IllegalCharException, IllegalNumberException {
        String input = "||->--><<-";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(OR, token1.kind);
        assertEquals(0, token1.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(BARARROW, token2.kind);
        assertEquals(1, token2.pos);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(MINUS, token3.kind);
        assertEquals(4, token3.pos);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(ARROW, token4.kind);
        assertEquals(5, token4.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token5 = scanner.nextToken();
        assertEquals(LT, token5.kind);
        assertEquals(7, token5.pos);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(ASSIGN, token6.kind);
        assertEquals(8, token6.pos);

    }

    @Test
    public void test5() throws IllegalCharException, IllegalNumberException {
        String input = "integer boolean image url file frame while if sleep screenheight "
                + "screenwidth gray convolve blur scale width height true false";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(KW_INTEGER, token1.kind);

        // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(KW_BOOLEAN, token2.kind);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(KW_IMAGE, token3.kind);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(KW_URL, token4.kind);
        // get the next token and check its kind, position, and contents
        Scanner.Token token5 = scanner.nextToken();
        assertEquals(KW_FILE, token5.kind);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(KW_FRAME, token6.kind);

        Scanner.Token token7 = scanner.nextToken();
        assertEquals(KW_WHILE, token7.kind);

        // get the next token and check its kind, position, and contents
        Scanner.Token token8 = scanner.nextToken();
        assertEquals(KW_IF, token8.kind);

        Scanner.Token token9 = scanner.nextToken();
        assertEquals(OP_SLEEP, token9.kind);

        Scanner.Token token10 = scanner.nextToken();
        assertEquals(KW_SCREENHEIGHT, token10.kind);

        // get the next token and check its kind, position, and contents
        Scanner.Token token11 = scanner.nextToken();
        assertEquals(KW_SCREENWIDTH, token11.kind);

        Scanner.Token token12 = scanner.nextToken();
        assertEquals(OP_GRAY, token12.kind);

        Scanner.Token token13 = scanner.nextToken();
        assertEquals(OP_CONVOLVE, token13.kind);

        Scanner.Token token14 = scanner.nextToken();
        assertEquals(OP_BLUR, token14.kind);

        // get the next token and check its kind, position, and contents
        Scanner.Token token15 = scanner.nextToken();
        assertEquals(KW_SCALE, token15.kind);

        Scanner.Token token16 = scanner.nextToken();
        assertEquals(OP_WIDTH, token16.kind);

        Scanner.Token token17 = scanner.nextToken();
        assertEquals(OP_HEIGHT, token17.kind);

        // get the next token and check its kind, position, and contents
        Scanner.Token token18 = scanner.nextToken();
        assertEquals(KW_TRUE, token18.kind);

        Scanner.Token token19 = scanner.nextToken();
        assertEquals(KW_FALSE, token19.kind);

    }

    @Test
    public void test6() throws IllegalCharException, IllegalNumberException {
        String input = "Yinan\n1018zyn";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(IDENT, token1.kind);
        LinePos token1pos = new LinePos(0, 0);
        assertEquals(token1pos.line, token1.getLinePos().line);
        assertEquals(token1pos.posInLine, token1.getLinePos().posInLine);
        // //System.out.println("token 1 at line:" + token1.getLinePos().line +
        // " column:" + token1.getLinePos().posInLine);
        // // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(INT_LIT, token2.kind);
        LinePos token2pos = new LinePos(1, 0);
        assertEquals(token2pos.line, token2.getLinePos().line);
        assertEquals(token2pos.posInLine, token2.getLinePos().posInLine);
        // //System.out.println("token 2 at line:" + token2.getLinePos().line +
        // " column:" + token2.getLinePos().posInLine);
        Scanner.Token token3 = scanner.nextToken();
        assertEquals(IDENT, token3.kind);
        LinePos token3pos = new LinePos(1, 4);
        assertEquals(token3pos.line, token3.getLinePos().line);
        assertEquals(token3pos.posInLine, token3.getLinePos().posInLine);
        // //System.out.println("token 3 at line:" + token3.getLinePos().line +
        // " column:" + token3.getLinePos().posInLine);

    }

    @Test
    public void test7() throws IllegalCharException, IllegalNumberException {
        String input = "/*yinan\n1018*/yinan";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(IDENT, token1.kind);
        LinePos token1pos = new LinePos(1, 6);
        assertEquals(token1pos.line, token1.getLinePos().line);
        assertEquals(token1pos.posInLine, token1.getLinePos().posInLine);

    }

    @Test

    public void test8() throws IllegalCharException, IllegalNumberException {
        String input = "/*...*/a/***/\nbc!/ /*/ /**/ !\nd/*.**/";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(IDENT, token1.kind);
        assertEquals(7, token1.pos);
        System.out.println(token1.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(IDENT, token2.kind);
        assertEquals(14, token2.pos);
        System.out.println(token2.pos);
        Scanner.Token token3 = scanner.nextToken();
        assertEquals(NOT, token3.kind);
        assertEquals(16, token3.pos);
        System.out.println(token3.pos);
        Scanner.Token token4 = scanner.nextToken();
        assertEquals(DIV, token4.kind);
        assertEquals(17, token4.pos);
        Scanner.Token token5 = scanner.nextToken();
        assertEquals(NOT, token5.kind);
        assertEquals(28, token5.pos);
        System.out.println(token4.pos);
        Scanner.Token token6 = scanner.nextToken();
        assertEquals(IDENT, token6.kind);
        assertEquals(30, token6.pos);


    }

    @Test
    public void test9() throws IllegalCharException, IllegalNumberException {
        String input = "show\r\n hide \n move \n file";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(KW_SHOW, token1.kind);
        LinePos token1pos = new LinePos(0, 0);
        assertEquals(token1pos.line, token1.getLinePos().line);
        assertEquals(token1pos.posInLine, token1.getLinePos().posInLine);
        // //System.out.println("token 1 at line:" + token1.getLinePos().line +
        // " column:" + token1.getLinePos().posInLine);
        // // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(KW_HIDE, token2.kind);
        LinePos token2pos = new LinePos(1, 1);
        assertEquals(token2pos.line, token2.getLinePos().line);
        assertEquals(token2pos.posInLine, token2.getLinePos().posInLine);
        // //System.out.println("token 2 at line:" + token2.getLinePos().line +
        // " column:" + token2.getLinePos().posInLine);
        Scanner.Token token3 = scanner.nextToken();
        assertEquals(KW_MOVE, token3.kind);
        LinePos token3pos = new LinePos(2, 1);
        assertEquals(token3pos.line, token3.getLinePos().line);
        assertEquals(token3pos.posInLine, token3.getLinePos().posInLine);
        // //System.out.println("token 3 at line:" + token3.getLinePos().line +
        // " column:" + token3.getLinePos().posInLine);
        Scanner.Token token4 = scanner.nextToken();
        assertEquals(KW_FILE, token4.kind);
        LinePos token4pos = new LinePos(3, 1);
        assertEquals(token4pos.line, token4.getLinePos().line);
        assertEquals(token4pos.posInLine, token4.getLinePos().posInLine);

    }

    @Test
    public void test10() throws IllegalCharException, IllegalNumberException {
        String input = "|;|--->";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        // check that the scanner has inserted an EOF token at the end
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(OR, token1.kind);
        assertEquals(0, token1.pos);

        // get the next token and check its kind, position, and contents
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(SEMI, token2.kind);
        assertEquals(1, token2.pos);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(OR, token3.kind);
        assertEquals(2, token3.pos);
        Scanner.Token token4 = scanner.nextToken();
        assertEquals(MINUS, token4.kind);
        assertEquals(3, token4.pos);
        Scanner.Token token5 = scanner.nextToken();
        assertEquals(MINUS, token5.kind);
        assertEquals(4, token5.pos);
        Scanner.Token token6 = scanner.nextToken();
        assertEquals(ARROW, token6.kind);
        assertEquals(5, token6.pos);
    }


}
