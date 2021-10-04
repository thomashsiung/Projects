package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;
import static enigma.TestUtils.*;

/**
 * The suite of all JUnit tests for the Permutation class. For the purposes of
 * this lab (in order to test) this is an abstract class, but in proj1, it will
 * be a concrete class. If you want to copy your tests for proj1, you can make
 * this class concrete by removing the 4 abstract keywords and implementing the
 * 3 abstract methods.
 *
 * Copied from Lab 6
 *
 *  @author Thomas Hsiung
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that PERM has an ALPHABET whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                    e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                    c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                    ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                    ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }


    @Test(expected = EnigmaException.class)
    public void testNotInAlphabet() {
        Permutation p1 = new Permutation("(BACD)", new Alphabet("ABCD"));
        p1.invert('F');

        Permutation p2 = new Permutation("", new Alphabet("%6^."));
        p2.invert(0);
    }




    @Test
    public void testInvertChar() {
        Alphabet a1 = new Alphabet("ABCD");
        Permutation p1 = new Permutation("(BACD)", a1);
        assertEquals(a1, p1.alphabet());
        assertEquals('B', p1.invert('A'));
        assertEquals('D', p1.invert('B'));

        Alphabet a2 = new Alphabet("ABCDE");
        Permutation p2 = new Permutation("", a2);
        assertEquals(a2, p2.alphabet());
        assertEquals('A', p2.invert('A'));
        assertEquals('C', p2.invert('C'));

        Alphabet a3 = new Alphabet("ABCDEF");
        Permutation p3 = new Permutation("(AB) (C) (D) (EF)", a3);
        assertEquals(a3, p3.alphabet());
        assertEquals('B', p3.invert('A'));
        assertEquals('A', p3.invert('B'));
        assertEquals('C', p3.invert('C'));
    }

    @Test
    public void testInvertInd() {
        Permutation p1 = new Permutation("(BACD)", new Alphabet("ABCD"));
        assertEquals(4, p1.size());
        assertEquals(3, p1.invert(1));
        assertEquals(1, p1.invert(0));

        Permutation p2 = new Permutation("", new Alphabet("ABCDE"));
        assertEquals(5, p2.size());
        assertEquals(0, p2.invert(0));
        assertEquals(2, p2.invert(2));

        Permutation p3 = new Permutation("(AB) (C) (D) (EF)",
                new Alphabet("ABCDEF"));
        assertEquals(6, p3.size());
        assertEquals(1, p3.invert(0));
        assertEquals(0, p3.invert(1));
        assertEquals(2, p3.invert(2));
    }

    @Test
    public void testPermuteChar() {
        Permutation p1 = new Permutation("(BACD)", new Alphabet("ABCD"));
        assertEquals('C', p1.permute('A'));
        assertEquals('B', p1.permute('D'));

        Permutation p2 = new Permutation("", new Alphabet("ABCDE"));
        assertEquals('A', p2.permute('A'));
        assertEquals('C', p2.permute('C'));

        Permutation p3 = new Permutation("(AB) (C) (D) (EF)",
                new Alphabet("ABCDEF"));
        assertEquals('B', p3.permute('A'));
        assertEquals('A', p3.permute('B'));
        assertEquals('C', p3.permute('C'));
    }

    @Test
    public void testPermuteInd() {
        Permutation p1 = new Permutation("(BACD)", new Alphabet("ABCD"));
        assertTrue(p1.derangement());
        assertEquals(1, p1.permute(3));
        assertEquals(2, p1.permute(0));

        Permutation p2 = new Permutation("", new Alphabet("ABCDE"));
        assertFalse(p2.derangement());
        assertEquals(0, p2.permute(0));
        assertEquals(2, p2.permute(2));

        Permutation p3 = new Permutation("(AB) (C) (D) (EF)",
                new Alphabet("ABCDEF"));
        assertFalse(p3.derangement());
        assertEquals(1, p3.permute(0));
        assertEquals(0, p3.permute(1));
        assertEquals(2, p3.permute(2));
    }

}
