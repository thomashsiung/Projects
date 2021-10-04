package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *
 *  As part of the simplification of Git, trees are implied, but not
 *  explicit data structures. The organization of Commits and
 *  Branches with Merging is tracked through permanent local logs,
 *  with parents as a stored value within each Commit. Branches have
 *  a Head file to track its active Commit by SHA ID, and a main
 *  Active file tracks the currently active Branch.
 *  Finally, all files and Commits are stored as Blobs.
 *
 *  @author Thomas Hsiung
 */
public class Main {

    /** The git repo. */
    static final File GIT = new File(".gitlet/");
    /** Gitlet metadata, if Gitlet already exists. */
    static final File META = Utils.join(GIT, "metadata");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....  */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }
            Gitlet gitlet = new Gitlet();
            if (META.exists()) {
                gitlet.getMeta(META);
            }
            switch (args[0]) {
            case "init":
                gitlet.init();
                break;
            case "add":
                gitlet.add(args[1]);
                break;
            case "commit":
                gitlet.commit(args[1]);
                break;
            case "rm":
                gitlet.remove(args[1]);
                break;
            case "log":
                gitlet.log();
                break;
            case "global-log":
                gitlet.globallog();
                break;
            case "find":
                gitlet.find(args[1]);
                break;
            case "status":
                gitlet.status();
                break;
            case "checkout":
                gitlet.checkout(args);
                break;
            case "branch":
                gitlet.branch(args[1]);
                break;
            case "rm-branch":
                gitlet.rmvbranch(args[1]);
                break;
            case "reset":
                gitlet.reset(args[1]);
                break;
            case "merge":
                gitlet.merge(args[1]);
                break;
            default:
                throw new GitletException("No command with that name exists.");
            }
            gitlet.saveGitlet();
            System.exit(0);
        } catch (IndexOutOfBoundsException err) {
            throw new GitletException("Incorrect operands.");
        }
    }
}
