// Whole BTree class

import java.io.IOException;
import java.util.List;

public class BTree {
    BTreeNode root;
    int t;
    WriteAheadLog wal;

    /**
     * @param t max depth
     * @param walPath path of log
     */
    public BTree(int t, String walPath) throws IOException {
        this.root = null;
        this.t = t;
        this.wal = new WriteAheadLog(walPath);
        recover(); //Recover from log
    }

    /**
     * Helper method to traverse the tree
     */
    public void traverse() {
        if(this.root != null){
            root.traverse();
        }
    }

    public BTreeNode search(int key) {
        if(root == null){
            return null;
        }
        else {
            return root.search(key);
        }
    }

    /**
     * Inserts a new key into the B-Tree
     *
     * @param key the key to insert
     * @throws IOException If an I/O error occurs during logging.
     */
    public void insert(int key) throws IOException {
        wal.logInsert(key);

        if(root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = key;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1){
                BTreeNode s = new BTreeNode(t, false);
                s.children[0] = root;
                s.splitChild(0, root);

                int i = 0;
                // Find key position
                if(s.keys[0] < key){
                    i++;
                }

                s.children[i].insertNonFull(key);
                root = s;
            } else {
                root.insertNonFull(key);
            }
        }
    }

    /**
     * Deletes a key from the B-Tree
     * @param key the key to be deleted
     * @throws IOException If an I/O error occurs during logging.
     */
    public void delete(int key) throws IOException {
        wal.logDelete(key);

        if (root == null) {
            System.out.println("The tree is empty.");
            return;
        }

        root.delete(key);

        // If the root node has 0 keys, make its first child the new root if it has children
        if (root.n == 0) {
            if (root.isLeaf) {
                root = null;
            }
            else {
                root = root.children[0];
            }
        }
    }

    /**
     * Recovers the B-Tree state by replaying the WAL.
     *
     * @throws IOException If an I/O error occurs during recovery.
     */
    public void recover() throws IOException {
        List<String> logs = wal.readLogs();
        for(String log : logs) {
            if (log.startsWith("INSERT")) {
                String[] parts = log.split(" ");
                if (parts.length != 2) continue; // Invalid log entry
                
                int key = Integer.parseInt(parts[1]);
                applyInsert(key);
            }
            else if( log.startsWith("DELETE")) {
                String[] parts = log.split(" ");
                if (parts.length != 2) continue; // Invalid log entry

                int key = Integer.parseInt(parts[1]);
                applyDelete(key);
            }
        }
    }

    /**
     * Applies a delete operation without logging. Used during recovery.
     *
     * @param key the key to delete
     */
    private void applyDelete(int key) {
        if(root == null) {
            System.out.println("The tree is empty");
            return;
        }

        root.delete(key);

        if (root.n == 0) {
            if(root.isLeaf) {
                root = null;
            }
            else {
                root = root.children[0];
            }
        }
    }

    /**
     * Applies an insert operation without logging. Used during recovery.
     *
     * @param key the key to insert
     */
    private void applyInsert(int key) {
        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = key;
            root.n = 1;
        }
        else {
            if(root.n == 2 * t - 1){
                BTreeNode s = new BTreeNode(t, false);
                s.children[0] = root;
                s.splitChild(0, root);

                int i = 0;
                if(s.keys[0] < key) {
                    i++;
                }
                s.children[i].insertNonFull(key);

                //Change root
                root = s;
            }
            else {
                root.insertNonFull(key);
            }
        }

    }


    /**
     * Closes the WAL when done.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void close() throws IOException {
        if(wal != null) {
            wal.close();
        }
    }
}