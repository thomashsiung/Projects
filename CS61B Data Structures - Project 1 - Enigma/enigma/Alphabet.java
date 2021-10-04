package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Thomas Hsiung
 */
class Alphabet {

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = sanitizeChars(chars);
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        String check = Character.toString(ch);
        return _chars.contains(check);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size().
     *  Also catches indices > _chars or negative values. */
    char toChar(int index) {
        if (index > _chars.length() || index < 0) {
            throw new EnigmaException("Alpha.toChar: Index out of range.");
        }
        return _chars.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar().
     *  Also catches chars not in alphabet. */
    int toInt(char ch) {
        if (!contains(ch)) {
            throw new EnigmaException("Alpha.toInt: Char not in Alpha." + ch);
        }
        return _chars.indexOf(ch);
    }

    /** Sanitizes input string CHARS for whitespaces and throws exception for
     *  duplicate chars; returns a string without whitespace. */
    public String sanitizeChars(String chars) {
        String cleanchars = chars.replaceAll("\\s+", "");
        if (checkDuplicates(chars)) {
            throw new EnigmaException("Alpha.sanitizeChars: Duplicate chars.");
        }
        return cleanchars;
    }

    /** Returns boolean for duplicate chars in CHARS string. */
    public boolean checkDuplicates(String chars) {
        char[] check = chars.toCharArray();
        for (int i = 0; i < chars.length(); i++) {
            for (int j = i + 1; j < chars.length(); j++) {
                if (check[i] == check[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    /** EC returns chars. */
    String chars() {
        return _chars;
    }

    /** Local private instance of chars. */
    private String _chars;
}
