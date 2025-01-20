import btree.BTree;
import btree.BTreeNode;
public class BTreeTest {
    public static void main(String[] args) {
        // Create a B-Tree with minimum degree t = 3
        BTree btree = new BTree(3);

        // Insert keys into the B-Tree
        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17};

        for (int key : keys) {
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
    }
}