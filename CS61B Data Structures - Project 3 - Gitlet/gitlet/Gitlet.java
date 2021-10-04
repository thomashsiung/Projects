package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.Collections;

/** Gitlet object to preserve Gitlet's metadata for private HashMaps.
 *  Provides methods for most of Gitlet's functionality.
 *
 *  @author Thomas Hsiung
 */
public class Gitlet implements Serializable {

    /** Current Working Directory. */
    private final File _cwd = new File(".");
    /** Git repo location. */
    private final File _git = new File(".gitlet/");
    /** Git Branch folder. */
    private final File _branches = Utils.join(_git, "branches/");
    /** Git Blobs folder. */
    private final File _blobs = Utils.join(_git, "blobs/");
    /** Git Commits folder. */
    private final File _commits = Utils.join(_git, "commits/");
    /** Git Stage folder. */
    private final File _stage = Utils.join(_git, "stage/");
    /** Git Remotes folder. */
    private final File _remotes = Utils.join(_git, "remotes/");
    /** Git LOG folder. */
    private final File _logs = Utils.join(_git, "logs/");
    /** Git Active reference file. */
    private final File _active = Utils.join(_git, "active");
    /** Git Global Log file. */
    private final File _globlog = Utils.join(_git, "global-log");
    /** Git Master Head reference file. */
    private final File _masthead = Utils.join(_branches, "master");
    /** Git Master Log file. */
    private final File _mastlog = Utils.join(_logs, "master-log");


    /** New Gitlet object. */
    public Gitlet() {
        _toremove = new ArrayList<String>();
        _allcommits = new HashMap<String, String>();
        _abbrID = new HashMap<String, String>();
    }

    /** Loads Gitlet from META if META exists. Otherwise
     *  initializes metadata variables. */
    public void getMeta(File meta) {
        Gitlet oldgit = Utils.readObject(meta, Gitlet.class);
        _toremove = oldgit.getRemoveList();
        _allcommits = oldgit.getCommitHash();
        _abbrID = oldgit.getshortIDs();
    }

    /** Initializes new Git repo with all subfolders and other
     *  necessary files if not already present. */
    public void init() {
        boolean chkdir = _git.exists();
        if (!chkdir) {
            _git.mkdir();
            _branches.mkdir();
            _blobs.mkdir();
            _commits.mkdir();
            _stage.mkdir();
            _remotes.mkdir();
            _logs.mkdir();

            try {
                _active.createNewFile();
                _globlog.createNewFile();
                _masthead.createNewFile();
                _mastlog.createNewFile();
            } catch (IOException err) {
                throw new GitletException("I/O File Creation error");
            }

            String timestamp = "Wed Dec 31 16:00:00 1969 -0800";
            String initmsg = "initial commit";
            Commit initial = new Commit(initmsg, "master", timestamp);

            writeLog(_globlog, initial.getSHA(), timestamp, initmsg);
            writeLog(_mastlog, initial.getSHA(), timestamp, initmsg);
            setActive("master");
            setHead("master", initial.getSHA());
            initial.saveCommit();

            _allcommits.put(initial.getSHA(), initmsg);
            _abbrID.put(initial.getSHA().substring(0, 7), initial.getSHA());
        } else {
            throw new GitletException("A Gitlet version-control system"
                    + " already exists in the current directory.");
        }
    }

    /** Reads existing Log file at LOC for Global or Branch and
     *  updates with newest log on top. Format: commit SHA /
     *  date TIMESTAMP (formatted) / log MSG. */
    public void writeLog(File loc, String sha, String timestamp, String msg) {
        String newlog = "===" + "\n";
        newlog = newlog + "commit " + sha + "\n";
        newlog = newlog + "Date: " + timestamp + "\n";
        newlog = newlog + msg + "\n";
        if (loc.exists()) {
            newlog = newlog + Utils.readContentsAsString(loc);
            Utils.writeContents(loc, newlog);
        } else {
            try {
                loc.createNewFile();
            } catch (IOException err) {
                throw new GitletException("I/O File Creation error");
            }
            Utils.writeContents(loc, newlog);
        }
    }

    /** Special case writeLog for Merge logs.
     *  Reads existing Log file at LOC for Global or Branch and updates
     *  with newest log on top. Format: commit SHA / merge PAR1 PAR2
     *  (truncated) / date TIMESTAMP (formatted) / log MSG. */
    public void writeLog(File loc, String sha, String par1, String par2,
                         String timestamp, String msg) {
        String newlog = "===" + "\n";
        newlog = newlog + "commit " + sha + "\n";
        newlog = newlog + "Merge: " + par1.substring(0, 7) + " "
                + par2.substring(0, 7) + "\n";
        newlog = newlog + "Date: " + timestamp + "\n";
        newlog = newlog + msg + "\n";
        if (loc.exists()) {
            newlog = newlog + Utils.readContentsAsString(loc);
            Utils.writeContents(loc, newlog);
        } else {
            try {
                loc.createNewFile();
            } catch (IOException err) {
                throw new GitletException("I/O File Creation error");
            }
            Utils.writeContents(loc, newlog);
        }
    }

    /** Reads existing Head file for BRANCH and replaces with SHA ID. */
    public void setHead(String branch, String sha) {
        File currhead = Utils.join(_branches, branch);
        Utils.writeContents(currhead, sha);
    }

    /** Updates existing Active BRANCH reference. */
    public void setActive(String branch) {
        Utils.writeContents(_active, branch);
    }

    /** Reads and returns Active Branch's Commit SHA. */
    public String readActive() {
        String branch = Utils.readContentsAsString(_active);
        File active = Utils.join(_branches, branch);
        return Utils.readContentsAsString(active);
    }

    /** Reads and returns Active file's Branch. */
    public String getActBranch() {
        return Utils.readContentsAsString(_active);
    }

    /** Takes FILENAME and adds to Git staging area. If the file
     *  exists, overwrites existing file in stage. If file is
     *  identical to current commit, then remove from stage. */
    public void add(String filename) {
        boolean chkdir = _git.exists();
        if (chkdir) {
            File newfile = Utils.join(_cwd, filename);
            if (newfile.exists()) {
                Blob newblob = new Blob(filename);
                File stage = Utils.join(_stage, newblob.getSHA());
                Commit current = new Commit(readActive());
                if (current.getBlobs() != null && current.
                        getBlobs().containsKey(filename)) {
                    if (current.getBlobs().get(filename).
                            equals(newblob.getSHA())) {
                        if (stage.exists()) {
                            Utils.restrictedDelete(stage);
                        }
                    } else {
                        Utils.writeObject(stage, newblob);
                    }
                } else {
                    try {
                        stage.createNewFile();
                    } catch (IOException err) {
                        throw new GitletException("I/O File Creation error");
                    }
                    Utils.writeObject(stage, newblob);
                }
            } else {
                throw new GitletException("File does not exist.");
            }
        } else {
            throw new GitletException("Not in an initialized"
                    + " Gitlet directory.");
        }
    }

    /** Commit snapshots current Git files and organization. By
     *  default identical to parent(s), except for files in stage.
     *  Takes MSG as a message for the LOG file. Stage is cleared
     *  after a commit. Errors if stage is empty or if no MSG. */
    public void commit(String msg) {
        Commit parent = new Commit(readActive());
        String branch = parent.getBranch();
        String par = parent.getSHA();

        HashMap<String, String> blobs = new HashMap<String, String>();
        if (parent.getBlobs() != null) {
            blobs = parent.getBlobs();
        }
        if (_stage.listFiles() != null) {
            File[] staged = _stage.listFiles();
            if (staged != null) {
                for (File i : staged) {
                    Blob contents = Utils.readObject(i, Blob.class);
                    blobs.put(contents.getName(), contents.getSHA());
                    File newblob = Utils.join(_blobs, contents.getSHA());
                    try {
                        newblob.createNewFile();
                    } catch (IOException err) {
                        throw new GitletException("I/O File Creation error");
                    }
                    Utils.writeObject(newblob, contents);
                }
            }
        }

        if (_toremove != null) {
            for (String j : _toremove) {
                blobs.remove(j);
            }
        }
        _toremove = new ArrayList<String>();

        String timestamp = getTimestamp();
        Commit newcommit = new Commit(msg, branch, par, blobs, timestamp);
        String newSHA = newcommit.getSHA();
        newcommit.saveCommit();
        _allcommits.put(newSHA, msg);
        _abbrID.put(newSHA.substring(0, 7), newSHA);

        setHead(branch, newSHA);
        setActive(branch);
        File branchlog = Utils.join(_logs, branch + "-log");
        writeLog(_globlog, newSHA, timestamp, msg);
        writeLog(branchlog, newSHA, timestamp, msg);
    }

    /** Unstages FILENAME if in Stage. Or if tracked in current
     *  Commit, stages FILENAME for removal and removes file from
     *  work directory. Errors if not in stage nor tracked by
     *  current Commit. */
    public void remove(String filename) {
        boolean removed = false;
        Commit active = new Commit(readActive());
        if (active.getBlobs() != null) {
            if (active.getBlobs().containsKey(filename)) {
                _toremove.add(filename);
                File remwork = Utils.join(_cwd, filename);
                Utils.restrictedDelete(remwork);
                removed = true;
            }
        }

        File stagechk = Utils.join(_stage, filename);
        if (stagechk.exists()) {
            Utils.restrictedDelete(stagechk);
            removed = true;
        }

        if (!removed) {
            throw new GitletException("No reason to remove the file.");
        }
    }

    /** Displays information about each Commit: Commit ID, Timestamp,
     *  and Commit MSG. Begins at Head, then runs backward along
     *  commit tree (ignores second parents). Log should already be
     *  correctly formatted on input. */
    public void log() {
        File currlog = Utils.join(_logs, getActBranch() + "-log");
        String log = Utils.readContentsAsString(currlog);
        System.out.println(log);
    }

    /** Displays information about EVERY Commit: Commit ID, Timestamp,
     *  and Commit MSG. Ordered by Timestamp and runs backward. Log
     *  should already be correctly formatted on input. */
    public void globallog() {
        String log = Utils.readContentsAsString(_globlog);
        System.out.println(log);
    }

    /** Displays IDs of all Commits with matching MSG log. Checks
     *  against the private Commit HashMap. Errors if no such MSG. */
    public void find(String msg) {
        if (_allcommits.containsValue(msg)) {
            Set<String> keys = _allcommits.keySet();
            for (String i : keys) {
                if (_allcommits.get(i).equals(msg)) {
                    System.out.println(i);
                }
            }
        } else {
            throw new GitletException("Found no commit with that message.");
        }
    }

    /** Displays current Branches, marks current Branch with
     *  an asterisk (*), and displays Stage status.
     *  (Potential EC with additional Status information.) */
    public void status() {
        System.out.print("=== Branches ===");
        List<String> branches = Utils.plainFilenamesIn(_branches);
        String branch = getActBranch();
        for (String i : branches) {
            if (i.equals(branch)) {
                System.out.println("*" + i);
            } else {
                System.out.println(i);
            }
        }

        System.out.println("\n" + "=== Staged Files ===");
        if (_stage.listFiles() != null) {
            List<String> stages = Utils.plainFilenamesIn(_stage);
            for (String j : stages) {
                System.out.println(j);
            }
        }

        System.out.println("\n" + "=== Removed Files ===");
        if (_toremove.size() > 0) {
            Collections.sort(_toremove);
            for (String k : _toremove) {
                System.out.println(k);
            }
        }
        System.out.println("\n" + "=== Modifications Not Staged"
                + " For Commit ===");
        System.out.println("\n" + "=== Untracked Files ===");
    }

    /** Checkout 0 of 3. Takes ARGS from Main to determine which
     *  Checkout function to use. */
    public void checkout(String... args) {
        if (args.length == 3 && args[1].equals("--")) {
            checkout1(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkout2(args[1], args[3]);
        } else if (args.length == 2) {
            checkout3(args[1]);
        } else {
            throw new GitletException("Incorrect operands.");
        }
    }

    /** Checkout 1 of 3. Takes a single file FILENAME from
     *  Head commit / front of current branch and puts in
     *  work directory. Overwrites file if already present.
     *  File is NOT staged. */
    public void checkout1(String filename) {
        Commit active = new Commit(readActive());
        HashMap<String, String> blobs = active.getBlobs();
        if (blobs.containsKey(filename)) {
            String newSHA = blobs.get(filename);
            File oldSHA = Utils.join(_cwd, filename);
            if (oldSHA.exists()) {
                Utils.writeContents(oldSHA, newSHA);
            } else {
                try {
                    oldSHA.createNewFile();
                } catch (IOException err) {
                    throw new GitletException("I/O File Creation error");
                }
                Utils.writeContents(oldSHA, newSHA);
            }
        } else {
            throw new GitletException("File does not exist in that commit.");
        }
    }

    /** Checkout 2 of 3. Takes FILENAME and SHAID from ARGS
     *  and puts in work directory. Overwrites file if
     *  already present. File is NOT staged. */
    public void checkout2(String shaID, String filename) {
        if (shaID.length() < SHALEN) {
            shaID = _abbrID.get(shaID);
        }
        Commit source = new Commit(shaID);
        File sourcechk = Utils.join(_commits, source.getSHA());
        if (sourcechk.exists()) {
            HashMap<String, String> blobs = source.getBlobs();
            if (blobs.containsKey(filename)) {
                String newSHA = blobs.get(filename);
                File oldSHA = Utils.join(_cwd, filename);
                if (oldSHA.exists()) {
                    Utils.writeContents(oldSHA, newSHA);
                } else {
                    try {
                        oldSHA.createNewFile();
                    } catch (IOException err) {
                        throw new GitletException("I/O File Creation error");
                    }
                    Utils.writeContents(oldSHA, newSHA);
                }
            } else {
                throw new GitletException("File does not"
                        + " exist in that commit.");
            }
        } else {
            throw new GitletException("No commit with that id exists.");
        }
    }

    /** Checkout 3 of 3. Takes all files from Head of BRANCH
     *  and puts in work directory. Overwrites file if
     *  already present. BRANCH also set as Head. Clears Stage. */
    public void checkout3(String branch) {
        File branchchk = Utils.join(_branches, branch);
        if (branchchk.exists()) {
            Commit active = new Commit(readActive());
            String current = active.getBranch();
            if (current.equals(branch)) {
                throw new GitletException("No need to checkout"
                        + " the current branch.");
            } else {

                HashMap<String, String> blobs = active.getBlobs();
                File[] worklist = _cwd.listFiles();
                for (File i : worklist) {
                    if (!blobs.containsKey(i.toString())) {
                        throw new GitletException("There is an untracked"
                                + " file in the way; delete it, or add"
                                + " and commit it first.");
                    }
                }

                Set<String> keys = blobs.keySet();
                for (String j : keys) {
                    File newfile = Utils.join(_cwd, j);
                    String newSHA = blobs.get(j);
                    if (newfile.exists()) {
                        Utils.writeContents(newfile, newSHA);
                    } else {
                        try {
                            newfile.createNewFile();
                        } catch (IOException err) {
                            throw new GitletException("I/O File error");
                        }
                        Utils.writeContents(newfile, newSHA);
                    }
                }

                setActive(branch);
                File[] stagelist = _stage.listFiles();
                if (stagelist != null) {
                    for (File k : stagelist) {
                        Utils.restrictedDelete(k);
                    }
                }
            }
        } else {
            throw new GitletException("No such branch exists.");
        }
    }

    /** Creates a new Branch with NAME. Points at current
     *  Head. Does NOT automatically switch to new Branch. */
    public void branch(String name) {
        File branchhead = Utils.join(_branches, name);
        File branchchk = Utils.join(_logs, name + "-log");
        if (branchhead.exists()) {
            throw new GitletException("A branch with that name"
                    + " already exists.");
        } else {
            try {
                branchchk.createNewFile();
                branchhead.createNewFile();
            } catch (IOException err) {
                throw new GitletException("I/O File Creation error");
            }
            Utils.writeContents(branchchk, "");
            Utils.writeContents(branchhead, "");
            Commit active = new Commit(readActive());
            writeLog(branchchk, active.getSHA(),
                    active.getTimestamp(), active.getLog());
            setHead(name, active.getSHA());
        }
    }

    /** Deletes Branch with NAME. Removes pointers / references
     *  to Branch, not actual Blobs or Commits. */
    public void rmvbranch(String name) {
        File remove = Utils.join(_branches, name);
        if (remove.exists()) {
            Commit active = new Commit(readActive());
            String branch = active.getBranch();
            if (name.equals(branch)) {
                throw new GitletException("Cannot remove the current branch.");
            } else {
                Utils.restrictedDelete(remove);
            }
        } else {
            throw new GitletException("A branch with that name"
                    + " does not exist.");
        }
    }

    /** Checks out files from Commit COMM. Removes files
     *  not part of COMM, and moves Head to COMM. Clears Stage. */
    public void reset(String comm) {
        if (comm.length() < SHALEN) {
            comm = _abbrID.get(comm);
        }
        Commit source = new Commit(comm);
        File sourcechk = Utils.join(_commits, source.getSHA());
        if (sourcechk.exists()) {
            HashMap<String, String> blobs = source.getBlobs();
            File[] worklist = _cwd.listFiles();
            if (worklist != null) {
                for (File i : worklist) {
                    if (!blobs.containsKey(i.toString())) {
                        throw new GitletException("There is an untracked file"
                                + " in the way; delete it, or add and commit"
                                + " it first.");
                    }
                    Utils.restrictedDelete(i);
                }
            }

            Set<String> keys = blobs.keySet();
            for (String j : keys) {
                File newfile = Utils.join(_cwd, j);
                String newSHA = blobs.get(j);
                try {
                    newfile.createNewFile();
                } catch (IOException err) {
                    throw new GitletException("I/O File Creation error");
                }
                Utils.writeContents(newfile, newSHA);
            }

            setActive(source.getBranch());
            setHead(source.getBranch(), source.getSHA());

            File[] stagelist = _stage.listFiles();
            if (stagelist != null) {
                for (File k : stagelist) {
                    Utils.restrictedDelete(k);
                }
            }
        } else {
            throw new GitletException("No commit with that id exists.");
        }

    }

    /** Merges Branch BRANCH with current Branch.
     *  (Spec for details.) */
    public void merge(String branch) {

    }

    /** Formats Date to Git Log specs and returns formatted timestamp. */
    public String getTimestamp() {
        return ZonedDateTime.now().format(_gitTime);
    }

    /** Converts this Gitlet object to SHA to preserve metadata. */
    public void saveGitlet() {
        String gitSHA = Utils.sha1(Utils.serialize(this));
        File newGit = Utils.join(_git, "metadata");
        if (newGit.exists()) {
            Utils.writeContents(newGit, gitSHA);
        } else {
            try {
                newGit.createNewFile();
            } catch (IOException err) {
                throw new GitletException("I/O File Creation error");
            }
            Utils.writeContents(newGit, gitSHA);
        }
    }

    /** Returns files staged for removal. */
    public ArrayList<String> getRemoveList() {
        return _toremove;
    }

    /** Returns map of all current Commits : Log Message. */
    public HashMap<String, String> getCommitHash() {
        return _allcommits;
    }

    /** Returns map of short SHA ID : full SHA ID. */
    public HashMap<String, String> getshortIDs() {
        return _abbrID;
    }

    /** Formats timestamps for Log data and reading Logs. */
    private final DateTimeFormatter _gitTime =
            DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z");

    /** List of files to be removed during Commit. */
    private ArrayList<String> _toremove;

    /** Additional HashMap of every Commit's SHA ID : MSG.
     *  For quick referencing of Commit SHAs and for Find(MSG). */
    private HashMap<String, String> _allcommits;

    /** Additional HashMap of every Commit's 7-letter SHA : full SHA. */
    private HashMap<String, String> _abbrID;

    /** Length of a full SHA ID (to check against for short IDs). */
    private static final int SHALEN = 40;

}
