# Gitlet Design Document

**Name**: Thomas Hsiung

**Version**: 1.4

## Classes and Data Structures

#### Main
Main program. Takes arguments from command line to run Gitlet.

Main tracks Branches through BRANCH file in Branches subfolder. A file is created for each Branch.
By default, a MASTER file is instantiated for Master branch on running Gitlet.init() with new repos.
New BRANCH files created for each new Branch. BRANCH file contents refer to SHA ID of that Branch's
current Head Commit.

Main tracks Commits through ACTIVE and Branch LOG files.
ACTIVE gives current active Branch.
BRANCH files give current Head of Branch as a Commit SHA ID.
LOG files for each Branch record Branch history. 


#### Gitlet

Initializes directories and initial CommitLoads tree from memory, if one exists, or creates a tree, if none exists.
Takes arguments from command line for execution. Basic command methods contained here for all arguments.
Generates and preserves own metadata.

#### Blobs
Generic file object with metadata. Serializes all contents and stores as single SHA ID for comparison.


#### Commit
Creates Commit objects with metadata for file contents and identifying information.

SHA ID comprised of: _log, _branch, _parent, _parent2, _timestamp, blobs

blobs: HashMap _blobs of files converted to String blobs.



## Algorithms
#### Merging1 - Initial Check
Checks if a merge is necessary.
Checks all blobs by bytestream for discrepancies, then uses those to merge files. Differences are merged based on algorithm, but current inclination is to go with local > newest > branch.

#### Merging2 - Looks for Ancestors
Loops and finds Ancestors of each Parent Commit.

#### Merging3 - Matches Common Ancestors
Compares results from (Merging2) to discover the latest/newest Common Ancestor.

#### Merging4 - Compares Blobs
Checks Blob SHAs of Common Ancestor, Parent1, and Parent2 for conflicts and tracking.
Marks files for Merge, checks for conflicts.

#### Merging5
Manages Merge Commit based on (Merge4).


## Persistence
#### METADATA (FILE)
Saves Gitlet metadata.

#### HEAD (FILE)
File at Root. Tracks current active Commit. 

#### GLOBAL LOG (FILE)
File at Root (.gitlet). Logs all Commits, ordered by Timestamp in DESC order. Used for Global Log and Find methods.
Contains SHA1 IDs, Timestamp, Message, and abbreviated IDs.

#### LOG (FILE)
File at Branches (.gitlet/branches). New File created for each Branch. Like GLOBAL LOG, but only logs data for Current Branch.
A new file is copied and created with each new Commit for faster log queries.