# OTLP Project README

> **Reference**: This project draws heavily on the concepts described in Martin Kleppmann’s _Designing Data-Intensive Applications_. In particular, I implement two core indexing data structures—**B-Trees** and **Log-Structured Merge (LSM) Trees**—along with associated components like a **Write-Ahead Log (WAL)** and **Compaction**. The goal is to show how each structure leverages its properties and how they can be implemented from scratch.

---
## Table of Contents

1. **Overview**
2. **`btree` Package**
    - **BTree.java**
    - **BTreeNode.java**
3. **`lsmtree` Package**
    - **Compaction.java**
    - **LSMTree.java**
    - **MemTable.java**
    - **SSTable.java**
4. **`wal` Package**
    - **WriteAheadLog.java**
5. **Tests** *(Quick Note)*

---

## 1. Overview

### Two Primary Index Structures

1. **B-Tree**  
   A disk-oriented data structure common in traditional relational databases. Balances node usage, minimizing disk I/O. Each node can store multiple keys, and the B-Tree grows or shrinks by splitting or merging nodes. Ideal for **read-dominant** or balanced workloads.

2. **LSM Tree with SSTables**  
   Optimized for **high write throughput**, deferring writes by batching them in memory (MemTable) and periodically flushing sorted chunks (SSTables) to disk. A compaction process merges these chunks to maintain efficient reads over time.

### Write-Ahead Log (WAL)

Both data structures rely on a WAL to ensure durability: all modifications are recorded on disk before being applied in-memory. This allows **crash recovery** by replaying WAL entries.

### Project Structure

- **`btree/`**: Contains the B-Tree implementation (and node definitions).
- **`lsmtree/`**: Contains the LSM Tree logic, including MemTable, SSTable, and Compaction.
- **`wal/`**: Contains the WriteAheadLog class for consistent logging of operations.
- **`test/`**: Houses test classes (omitted here, but mentioned for completeness).

---

## 2. `btree` Package

### 2.1 BTree.java

- **Purpose**:  
  Manages the high-level operations of a B-Tree (insertion, searching, traversal, optional deletion). Maintains a reference to the root node.

- **Key Methods**:
    - `insert(int key)`: Inserts a key into the B-Tree, splitting the root if it’s full.
    - `search(int key)`: Recursively searches for a key, returning the node if found.
    - `traverse()`: In-order traversal, printing out keys in sorted order.
    - `delete(int key)`: (Optional, more complex) Removes a key and rebalances the tree.

- **Technical Notes**:
    - Relies on **BTreeNode** for node-level operations.
    - **Minimum Degree `t`** determines node capacity (between `t-1` and `2t-1` keys).
    - Splitting logic in `insert()` may increase tree height when the root is full.

### 2.2 BTreeNode.java

- **Purpose**:  
  Represents the fundamental unit of a B-Tree. Stores keys, child pointers, node capacity info, and whether it is a leaf or not.

- **Key Fields**:
    - `keys[]`: Array to store keys.
    - `children[]`: Array of child node pointers.
    - `isLeaf`: Flag for leaf status.
    - `n`: Number of keys in this node.

- **Key Methods**:
    - `insertNonFull(int key)`: Inserts into a node guaranteed not to be full.
    - `splitChild(int i, BTreeNode y)`: Splits a full child node `y` at index `i`.
    - `search(int key)`: Searches recursively if not a leaf.
    - `traverse()`: Prints out (or collects) keys in ascending order.

- **Technical Notes**:
    - Balances the tree by splitting nodes upon insertion if `n` reaches `2t - 1`.
    - If **deletion** is implemented, merges or redistributes keys from siblings.

---

## 3. `lsmtree` Package

### 3.1 Compaction.java

- **Purpose**:  
  Manages the compaction (merge) of multiple SSTables into a single consolidated SSTable.

- **Process**:
    1. **Select** multiple SSTables to merge.
    2. **K-way Merge**: Read all SSTables in sorted order, taking the newest value for each key.
    3. **Write** the merged data into a new SSTable.
    4. **Discard** (delete) old SSTables to reduce storage overhead.

- **Technical Notes**:
    - Removes or skips keys marked with “NULL” (tombstones) so they do not clutter new SSTables.
    - Ensures read amplification remains low by minimizing the number of SSTables.

### 3.2 LSMTree.java

- **Purpose**:  
  Coordinates all LSM Tree components: MemTable, SSTables, Compaction, and the WAL.

- **Key Methods**:
    - `put(String key, String value)`: Appends to WAL, updates MemTable; may trigger flush if MemTable is full.
    - `delete(String key)`: Logs a deletion, sets a tombstone in the MemTable.
    - `search(String key)`: Checks MemTable first, then searches SSTables in reverse order (most recent first).
    - `compact()`: Invokes **Compaction** to merge existing SSTables into a smaller set.
    - `recover()`: On startup, replays the WAL to restore the MemTable’s most recent state.

- **Technical Notes**:
    - A **size threshold** triggers MemTable flush, writing an SSTable to disk.
    - Each flush can optionally clear the WAL to limit its growth.
    - Insertions and deletions become **append-only** in the WAL and MemTable for high write throughput.

### 3.3 MemTable.java

- **Purpose**:  
  Functions as the in-memory buffer. All writes (inserts/deletes) go here before being persisted to an SSTable.

- **Key Methods**:
    - `put(String key, String value)`: Adds/updates a key in a sorted structure (like a `TreeMap`).
    - `delete(String key)`: Marks the key as deleted (tombstone).
    - `isFull(int maxSize)`: Checks if the threshold has been reached.
    - `flush(String directory)`: Writes its current contents to an SSTable on disk.

- **Technical Notes**:
    - Being **sorted** in memory simplifies creation of sorted on-disk tables (SSTables).
    - Tombstones carry over to the SSTable, eventually removed by compaction.

### 3.4 SSTable.java

- **Purpose**:  
  Represents an **immutable** on-disk structure that stores key-value pairs in ascending key order. Indexes enable quicker lookups.

- **Key Methods**:
    - `createFromMemTable(TreeMap<String, String> memData, String directory)`: Serializes MemTable content to disk, generating a sorted file.
    - `search(String key)`: Uses a **sparse index** plus a partial sequential scan to locate the key if it exists.
    - `getFilePath()`: Accessor for the on-disk file.
    - `getSize()`: Returns the number of entries in this SSTable.

- **Technical Notes**:
    - Stores “NULL” to indicate deletions.
    - The **index** is typically a map of certain “pivot” keys to file offsets, reducing memory usage while still facilitating searches.

---

## 4. `wal` Package

### WriteAheadLog.java

- **Purpose**:  
  Provides durability by logging all writes (inserts and deletes) before they are applied to the in-memory structures. On a crash, the log can be replayed to restore recent updates.

- **Key Methods**:
    - `logPut(String key, String value)`: Appends a “PUT” entry (key-value) to the log.
    - `logDelete(String key)`: Appends a “DELETE” entry.
    - `readLogs()`: Reads all log entries for recovery.
    - `close()`: Closes file resources.
    - `clear()`: Optionally truncates or removes the log after a successful flush.

- **Technical Notes**:
    - Typically opened in **append** mode so writes don’t overwrite old entries.
    - Must `flush()` changes to ensure they’re durable on disk.

## 5. Tests 

Below is a brief summary of the **stress test** results obtained when running 10,000 operations (split into 5,000 inserts, 3,000 searches, and 2,000 deletes) on both the **B-Tree** and the **LSM Tree**:

**B-Tree Performance**
- **Insert Time (5,000 ops)**: ~39.74 ms
- **Search Time (3,000 ops)**: ~0.72 ms
- **Delete Time (2,000 ops)**: ~24.77 ms

**LSM Tree Performance**
- **Insert Time (5,000 ops)**: ~64.33 ms
- **Search Time (3,000 ops)**: ~2,538.56 ms
- **Delete Time (2,000 ops)**: ~44.15 ms

