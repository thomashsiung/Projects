package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Thomas Hsiung
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _cycles = cycles;
        _alphabet = alphabet;
        _cleancycles = _cycles;
        _cleancycles = _cleancycles.replaceAll("\\s+", "");
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles = _cycles + '(' + cycle + ')';
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char input = _alphabet.toChar(wrap(p));
        char output = permute(input);
        return _alphabet.toInt(output);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char input = _alphabet.toChar(wrap(c));
        char output = invert(input);
        return _alphabet.toInt(output);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!_alphabet.contains(p)) {
            throw new EnigmaException("Perm.permute: Char not in alpha.");
        }
        char[] check = _cleancycles.toCharArray();
        int index = 0;
        char output = '(';
        for (int i = 0; i < _cleancycles.length(); i++) {
            if (check[i] == '(') {
                index = i + 1;
            } else if (check[i] == p) {
                if (check[i + 1] == ')') {
                    output = check[index];
                } else {
                    output = check[i + 1];
                }
                break;
            }
        }
        if (output == '(') {
            output = p;
        }
        return output;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!_alphabet.contains(c)) {
            throw new EnigmaException("Perm.invert: Char not in alpha.");
        }
        char[] check = _cleancycles.toCharArray();
        int index = 0;
        char output = '(';
        for (int i = _cleancycles.length() - 1; i >= 0; i--) {
            if (check[i] == ')') {
                index = i - 1;
            } else if (check[i] == c) {
                if (check[i - 1] == '(') {
                    output = check[index];
                } else {
                    output = check[i - 1];
                }
                break;
            }
        }
        if (output == '(') {
            output = c;
        }
        return output;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** EC: Return the cycles used in this Permutation. */
    String cycles() {
        return _cycles;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself).
     *  First checks for single cycles (X), then checks for omissions */
    boolean derangement() {
        String checkcycles = _cycles;
        int count = 0;
        char[] check = checkcycles.toCharArray();
        for (int i = 0; i < checkcycles.length(); i++) {
            if (check[i] == '(' || check[i] == ')') {
                count = 0;
            } else {
                for (int j = i + 1; j < checkcycles.length(); j++) {
                    count += 1;
                    if (check[j] == ')' && count == 1) {
                        return false;
                    }
                }
            }
        }

        String ccycles = _cleancycles;
        ccycles = ccycles.replace("(", "");
        ccycles = ccycles.replace(")", "");
        if (ccycles.length() < _alphabet.size()) {
            return false;
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Local instance of cycles. */
    private String _cycles;

    /** Local variable only; sanitized cycles. */
    private String _cleancycles;
}
