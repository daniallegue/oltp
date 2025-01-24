import java.io.IOException;

public class LSMTreeTest {
    public static void main(String[] args) {
        String walPath = "../wal/lsm_wal.log";
        String sstableDir = "../sstables/";
        int memTableSize = 5;

        try {
            // 1. Create an LSM Tree
            LSMTree lsmTree = new LSMTree(memTableSize, walPath, sstableDir);

            // 2. Insert key-value pairs
            System.out.println("Inserting key-value pairs into the LSM Tree:");
            lsmTree.put("apple", "red");
            lsmTree.put("banana", "yellow");
            lsmTree.put("grape", "purple");
            lsmTree.put("orange", "orange");
            lsmTree.put("lemon", "yellow");
            lsmTree.put("cherry", "red");

            System.out.println("\nSearch results:");
            System.out.println("apple -> " + lsmTree.search("apple"));
            System.out.println("banana -> " + lsmTree.search("banana"));
            System.out.println("kiwi -> " + lsmTree.search("kiwi"));   // not inserted, should be null

            System.out.println("\nDeleting 'banana' from LSM Tree.");
            lsmTree.delete("banana");

            System.out.println("banana -> " + lsmTree.search("banana")); // should be null after deletion

            // 5. Compact the SSTables
            System.out.println("\nPerforming compaction...");
            lsmTree.compact();

            // 6. Search again after compaction
            System.out.println("apple -> " + lsmTree.search("apple"));
            System.out.println("banana -> " + lsmTree.search("banana"));

            lsmTree.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
