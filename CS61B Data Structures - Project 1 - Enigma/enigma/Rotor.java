package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Thomas Hsiung
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        _setting = alphabet().toInt(cposn);
    }

    /** EC: Return my current offset. */
    int offset() {
        return _offset;
    }

    /** EC: Set offset() to POSN.  */
    void setOffset(int posn) {
        _offset = posn;
    }

    /** EC: Set offset() to character CPOSN. */
    void setOffset(char cposn) {
        _offset = alphabet().toInt(cposn);
    }

    /** EC: Update Rotor's permutation with PERM
     *  to account for Alpha shift by offset. */
    void updatePerm(Permutation perm) {
        _permutation = perm;
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int result = _permutation.wrap(p + _setting);
        result = _permutation.permute(result);
        result = _permutation.wrap(result - _setting);
        return result;
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int result = _permutation.wrap(e + _setting);
        result = _permutation.invert(result);
        result = _permutation.wrap(result - _setting);
        return result;
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** Local instance of Rotors settings (as int). */
    private int _setting;

    /** EC: Local instance of Rotors offset (as int). */
    private int _offset;
}
