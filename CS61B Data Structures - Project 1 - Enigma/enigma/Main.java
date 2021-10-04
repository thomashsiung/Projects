package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Thomas Hsiung
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        try {
            _machine = readConfig();
            String message = ""; String codedtext = ""; String settings = "";
            if (!_input.hasNextLine()) {
                throw new EnigmaException("Main.process: No input.");
            } else {
                message = _input.nextLine();
                if (message.startsWith("*")) {
                    settings = message;
                    setUp(_machine, settings);
                } else {
                    throw new EnigmaException("Main.process: No settings.");
                }
            }
            while (_input.hasNextLine()) {
                message = _input.nextLine();
                if (message.startsWith("*")) {
                    printMessageLine(codedtext);

                    settings = message;
                    setUp(_machine, settings);
                    codedtext = "";
                } else {
                    codedtext += _machine.convert(message) + '\n';
                }
            }
            if (!codedtext.equals("")) {
                printMessageLine(codedtext);
            }
        } catch (NoSuchElementException excp) {
            throw new EnigmaException("Main.process: Errors with _input.");
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String check = _config.next();
            if (check.equals("")) {
                throw new EnigmaException("Main.readConfig: No settings.");
            } else {
                _alphabet = new Alphabet(check);
                if (_alphabet.contains('(') || _alphabet.contains(')')
                        || _alphabet.contains('*')) {
                    throw new EnigmaException("Main.readConfig: Bad alpha.");
                }
            }
            ArrayList<Rotor> rotors = new ArrayList<Rotor>();
            int numrotors = _config.nextInt();
            int numpawls = _config.nextInt();
            if (numpawls >= numrotors) {
                throw new EnigmaException("Main.readConfig: Bad # pawls.");
            }
            rotors = readRotor();
            return new Machine(_alphabet, numrotors, numpawls, rotors);
        } catch (NoSuchElementException excp) {
            throw new EnigmaException("Main.readConfig: Config incomplete.");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private ArrayList<Rotor> readRotor() {
        try {
            String name = ""; String type = ""; String cycle = "";
            ArrayList<Rotor> rotors = new ArrayList<Rotor>();

            name = _config.next();
            type = _config.next();
            while (_config.hasNext()) {
                String temp = _config.next();
                if (temp.matches("[(].*?[)]$")) {
                    cycle += temp;
                } else {
                    rotors.add(makeRotor(name, cycle, type));
                    name = temp;
                    type = _config.next();
                    cycle = "";
                }
            }
            rotors.add(makeRotor(name, cycle, type));
            return rotors;
        } catch (NoSuchElementException excp) {
            throw new EnigmaException("Main.readRotor: Bad rotor file.");
        }
    }

    /** Creates and returns the appropriate Rotor (M, N, R)
     * to readRotors based on NAME, CYCLE, and TYPECYCLE. */
    private Rotor makeRotor(String name, String cycle, String typecycle) {
        Rotor newrotor;
        if (typecycle.equals("")) {
            throw new EnigmaException("Main.makeRotor: No rotor type.");
        }
        char type = typecycle.charAt(0);
        String notches = typecycle.substring(1);
        Permutation perm = new Permutation(cycle, _alphabet);

        if (type == 'M') {
            newrotor = new MovingRotor(name, perm, notches);
        } else if (type == 'N') {
            newrotor = new FixedRotor(name, perm);
        } else if (type == 'R') {
            newrotor = new Reflector(name, perm);
        } else {
            throw new EnigmaException("Main.makeRotor: Bad rotor type.");
        }
        return newrotor;
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        try {
            int numrotors = _machine.numRotors();
            String[] setting; String[] rotors = new String[numrotors];
            if (settings.startsWith("*")) {
                settings = settings.substring(1); settings = settings.trim();
                setting = settings.split("\\s+");

                System.arraycopy(setting, 0, rotors, 0, numrotors);
                _machine.insertRotors(rotors);

                if (setting[numrotors].length() == numrotors - 1) {
                    _machine.setRotors(setting[numrotors]);
                } else {
                    throw new EnigmaException("Main.setUp: Bad wheels.");
                }

                int index = numrotors + 1;
                int plug = setting.length - index;
                String plugs = "";
                Permutation pb = new Permutation("", _alphabet);
                if (plug > 0) {
                    if (setting[index].startsWith("(")) {
                        for (int i = index; i < setting.length; i++) {
                            plugs += setting[i];
                        }
                    } else {
                        _machine.setOffset(setting[index]);
                        for (int i = index + 1; i < setting.length; i++) {
                            plugs += setting[i];
                        }
                    }
                }
                pb = new Permutation(plugs, _alphabet);
                _machine.setPlugboard(pb);
            } else {
                throw new EnigmaException("Main.setUp: Error with settings.");
            }
        } catch (NoSuchElementException excp) {
            throw new EnigmaException("Main.setUp: Error with settings.");
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int count5 = 0;

        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == '\n' || msg.charAt(i) == '\r') {
                _output.print("\r\n");
                count5 = 0;
            } else {
                if (count5 == 5) {
                    _output.print(" " + msg.charAt(i));
                    count5 = 1;
                } else {
                    _output.print(msg.charAt(i));
                    count5 += 1;
                }
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Local instance of Machine class. */
    private Machine _machine;
}
