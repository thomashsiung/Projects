package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Thomas Hsiung
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numrotors = numRotors;
        _numpawls = pawls;
        _allrotorsarr = new ArrayList<Rotor>(allRotors);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numrotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numpawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotors = new Rotor[_numrotors];
        boolean foundrotor = false;
        for (int i = 0; i < _numrotors; i++) {
            for (int j = 0; j < _allrotorsarr.size(); j++) {
                if (rotors[i].equals(_allrotorsarr.get(j).name())) {
                    _rotors[i] = _allrotorsarr.get(j);
                    foundrotor = true;
                }
            }
            if (!foundrotor) {
                throw new EnigmaException("Mach.insertRotors: No such rotor.");
            }
            foundrotor = false;
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _numrotors - 1) {
            throw new EnigmaException("Mach.setRotors: Setting mismatch.");
        } else {
            for (int i = 1; i < _numrotors; i++) {
                _rotors[i].set(setting.charAt(i - 1));
            }
        }
    }

    /** EC: Shift rotors according to OFFSET, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setOffset(String offset) {
        if (offset.length() != _numrotors - 1) {
            throw new EnigmaException("Mach.setRotors: Offset mismatch.");
        } else {
            for (int i = 1; i < _numrotors; i++) {
                _rotors[i].setOffset(offset.charAt(i - 1));

                if (_rotors[i].offset() != 0) {
                    int off = _rotors[i].offset();
                    String cycles = _rotors[i].permutation().cycles();
                    String alpha = _rotors[i].permutation().alphabet().chars();
                    String tmp = alpha.substring(off) + alpha.substring(0, off);
                    Alphabet newalpha = new Alphabet(tmp);
                    int diff = _rotors[i].setting() - off;
                    int newset = _rotors[i].permutation().wrap(diff);

                    _rotors[i].updatePerm(new Permutation(cycles, newalpha));
                    _rotors[i].set(newset);
                }
            }
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean[] rotated = new boolean[_numrotors];
        boolean atnotch = false;
        int index = _numrotors - 1;
        for (int i = index; i > 0; i--) {
            if (i == index) {
                atnotch = _rotors[i].atNotch();
                rotated[index] = true;
            } else {
                if (atnotch) {
                    atnotch = _rotors[i].atNotch();
                    rotated[i] = true;
                } else if (index - i < _numpawls - 1) {
                    atnotch = _rotors[i].atNotch();
                    rotated[i] = atnotch;
                }
            }
        }
        for (int i = index; i > 0; i--) {
            if (rotated[i]) {
                _rotors[i].advance();
            }
        }

        int chr = c;
        chr = _plugboard.permute(chr);
        for (int i = index; i >= 0; i--) {
            chr = _rotors[i].convertForward(chr);
        }
        for (int i = 1; i <= index; i++) {
            chr = _rotors[i].convertBackward(chr);
        }
        return _plugboard.invert(chr);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        String message = msg.replaceAll("\\t*", "");
        int charind = 0;
        for (int i = 0; i < message.length(); i++) {
            if (i > 0 && message.charAt(i) == '*') {
                return result;
            } else if (message.charAt(i) == ' ') {
                continue;
            } else {
                charind = convert(_alphabet.toInt(message.charAt(i)));
                result += _alphabet.toChar(charind);
            }
        }
        return result;
    }


    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Local instance of # of Rotors. */
    private int _numrotors;

    /** Local instance of # of Pawls. */
    private int _numpawls;

    /** Local instance of Plugboard permutation. */
    private Permutation _plugboard;

    /** Local instance of all Rotors available. */
    private ArrayList<Rotor> _allrotorsarr;

    /** Local instance of Rotors being used. */
    private Rotor[] _rotors;
}
