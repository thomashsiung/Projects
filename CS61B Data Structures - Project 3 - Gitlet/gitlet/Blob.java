package gitlet;

import java.io.Serializable;
import java.io.File;

/** Generic serialized file format with metadata for Gitlet.
 *
 *  @author Thomas Hsiung
 */
public class Blob implements Serializable {

    /** Current Working Directory. */
    private final File _cwd = new File(".");

    /** New Blobs from FILENAME. */
    public Blob(String filename) {
        File newfile = Utils.join(_cwd, filename);
        _name = filename;
        _contents = Utils.readContents(newfile);
        _sha = Utils.sha1(_name, _contents);
    }

    /** Returns file contents as byte[]. */
    public byte[] getContents() {
        return _contents;
    }

    /** Returns SHA ID as String. */
    public String getSHA() {
        return _sha;
    }

    /** Returns file name as String. */
    public String getName() {
        return _name;
    }

    /** Contents of File as byte[]. */
    private byte[] _contents;

    /** SHA1 code of file as String. */
    private String _sha;

    /** File name in String format. */
    private String _name;

}
