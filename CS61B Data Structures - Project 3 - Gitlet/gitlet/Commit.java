package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.util.HashMap;

/** Commits for Gitlet.
 *  Metadata of Commits contain a log message, a timestamp,
 *  a HashMap of the file contents, a reference to its
 *  parent, and a second parent for Merges, if used.
 *
 *  @author Thomas Hsiung
 */
public class Commit implements Serializable {

    /** Git repo location. */
    private final File _git = new File(".gitlet/");
    /** Git Commits folder. */
    private final File _commits = Utils.join(_git, "commits/");
    /** Git Stage folder. */

    /** Special case initial commit for new repo. Only needs to
     *  take MSG, BRANCH, and TIMESTAMP as parameters. */
    public Commit(String msg, String branch, String timestamp) {
        _log = msg;
        _branch = branch;
        _timestamp = timestamp;
        _parent = "";
        _sha = getCommitSHA();
    }

    /** Normal single-parent commit. Requires MSG, BRANCH, PARENT,
     *  BLOBS, and TIMESTAMP. */
    public Commit(String msg, String branch, String parent,
                  HashMap<String, String> blobs, String timestamp) {
        _log = msg;
        _branch = branch;
        _timestamp = timestamp;
        _parent = parent;
        _blobs = blobs;
        _sha = getCommitSHA();
    }

    /** Takes existing Commit SHA ID and reads back Commit data for use. */
    public Commit(String sha) {
        File comm = Utils.join(_commits, sha);
        Commit oldcomm = Utils.readObject(comm, Commit.class);
        _log = oldcomm.getLog();
        _branch = oldcomm.getBranch();
        _timestamp = oldcomm.getTimestamp();
        _parent = oldcomm.getParent();
        if (oldcomm.getBlobs() == null) {
            _blobs = new HashMap<String, String>();
        } else {
            _blobs = oldcomm.getBlobs();
        }
        _sha = oldcomm.getSHA();
        if (!_sha.equals(sha)) {
            throw new GitletException("Err @ Commit from SHA");
        }
    }

    /** Returns new SHA ID for current Commit. */
    public String getCommitSHA() {
        if (_blobs != null) {
            return Utils.sha1(_log, _branch, _parent,
                    _blobs, _timestamp);
        }
        return Utils.sha1(_log, _branch, _parent, _timestamp);
    }

    /** Saves Commit to Commits folder as serialized data named SHA. */
    public void saveCommit() {
        File newcommit = Utils.join(_commits, _sha);
        try {
            newcommit.createNewFile();
        } catch (IOException err) {
            throw new GitletException("I/O File Creation error");
        }
        Utils.writeObject(newcommit, this);
    }

    /** Returns Commit's Parent. */
    public String getParent() {
        return _parent;
    }

    /** Returns Commit's Blobs for comparison. */
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }

    /** Returns Commit's Branch. */
    public String getBranch() {
        return _branch;
    }

    /** Returns Commit's SHA ID. */
    public String getSHA() {
        return _sha;
    }

    /** Returns Commit's Log Message. */
    public String getLog() {
        return _log;
    }

    /** Returns Commit's Timestamp. */
    public String getTimestamp() {
        return _timestamp;
    }

    /** File contents of this Commit. */
    private HashMap<String, String> _blobs;

    /** SHA ID of Parent. */
    private String _parent;

    /** Current Branch of this Commit. */
    private String _branch;

    /** This Commit's SHA ID. */
    private String _sha;

    /** Log Message of this Commit. */
    private String _log;

    /** Timestamp for this Commit. */
    private String _timestamp;
}
