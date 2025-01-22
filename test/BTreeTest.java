import btree.BTree;
import btree.BTreeNode;

import java.io.IOException;

public class BTreeTest {
    public static void main(String[] args) {
        String walPath = "../wal/btree_wal.log";

        try (BTree btree = new BTree(3, walPath)) {
            // Insert keys into the B-Tree
            int[] keysToInsert = {10, 20, 5, 6, 12, 30, 7, 17};
            for (int key : keysToInsert) {
                btree.insert(key);
            }

            // Traverse the B-Tree
            System.out.println("Traversal of the constructed B-Tree:");
            btree.traverse();
            System.out.println();

            // Search for existing and non-existing keys
            int[] searchKeys = {6, 15};
            for (int key : searchKeys) {
                BTreeNode result = btree.search(key);
                if (result != null) {
                    System.out.println("Key " + key + " found in the B-Tree.");
                } else {
                    System.out.println("Key " + key + " not found in the B-Tree.");
                }
            }

            // Delete keys from the B-Tree
            int[] keysToDelete = {6, 13, 7, 4, 2, 16};
            for (int key : keysToDelete) {
                System.out.println("\nDeleting key " + key + " from the B-Tree.");
                btree.delete(key);

                System.out.println("Traversal after deletion:");
                btree.traverse();
                System.out.println();
            }

            // Final traversal
            System.out.println("\nFinal traversal of the B-Tree:");
            btree.traverse();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}