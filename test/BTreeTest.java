import java.io.IOException;

public class BTreeTest {
    public static void main(String[] args) {
        String walPath = "../wal/btree_wal.log";
        int t = 3; // Minimum degree

        try {
            BTree btree = new BTree(t, walPath);

            int[] keysToInsert = {10, 20, 5, 6, 12, 30, 7, 17};
            System.out.println("Inserting keys into the B-Tree:");
            for (int key : keysToInsert) {
                System.out.print(key + " ");
                btree.insert(key);
            }
            System.out.println();

            // 3. Traverse the B-Tree
            System.out.println("B-Tree traversal after insertion:");
            btree.traverse();
            System.out.println("\n");

            // 4. Search for some keys
            int[] searchKeys = {6, 15};
            for (int key : searchKeys) {
                BTreeNode result = btree.search(key);
                if (result != null) {
                    System.out.println("Key " + key + " found in B-Tree.");
                } else {
                    System.out.println("Key " + key + " not found in B-Tree.");
                }
            }
            System.out.println();

            // 5. Delete a few keys
            System.out.println("Deleting keys 6 and 20 from the B-Tree.\n");
            btree.delete(6);
            btree.delete(20);

            // 6. Traverse after deletion
            System.out.println("B-Tree traversal after deletions:");
            btree.traverse();
            System.out.println("\n");

            // 7. Close the B-Tree (and WAL)
            btree.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
