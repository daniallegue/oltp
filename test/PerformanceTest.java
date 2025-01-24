import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class PerformanceTest {
    public static void main(String[] args) {
        final int NUM_OPERATIONS = 10000;
        final int SEARCH_FRACTION = 30;
        final int DELETE_FRACTION = 20;

        String btreeWalPath = "C:/Users/danie/Desktop/oltp/wal/btree_stress_wal.log";
        String lsmWalPath   = "C:/Users/danie/Desktop/oltp/wal/lsm_stress_wal.log";
        String sstableDir   = "C:/Users/danie/Desktop/oltp/sstables_stress/";

        // For LSM Tree
        int memTableSize = 1000;

        // 1. Create data structures
        BTree btree = null;
        LSMTree lsmTree = null;

        try {
            btree = new BTree(3, btreeWalPath);
            lsmTree = new LSMTree(memTableSize, lsmWalPath, sstableDir);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize data structures.");
            System.exit(1);
        }

        System.out.println("\n=== Starting Performance Test ===");
        System.out.println("Number of Operations: " + NUM_OPERATIONS + "\n");

        // 2. Prepare random keys and operation distribution
        Random random = new Random();
        int numSearchOps = (NUM_OPERATIONS * SEARCH_FRACTION) / 100;
        int numDeleteOps = (NUM_OPERATIONS * DELETE_FRACTION) / 100;
        int numInsertOps = NUM_OPERATIONS - numSearchOps - numDeleteOps;

        int[] insertKeys = new int[numInsertOps];
        for (int i = 0; i < numInsertOps; i++) {
            // e.g., random keys in some range
            insertKeys[i] = random.nextInt(NUM_OPERATIONS * 10);
        }

        // Similarly, create arrays of keys for searching and deleting
        int[] searchKeys = new int[numSearchOps];
        for (int i = 0; i < numSearchOps; i++) {
            searchKeys[i] = random.nextInt(NUM_OPERATIONS * 10);
        }

        int[] deleteKeys = new int[numDeleteOps];
        for (int i = 0; i < numDeleteOps; i++) {
            deleteKeys[i] = random.nextInt(NUM_OPERATIONS * 10);
        }

        try {
            // ----------------------------------------------------------------
            // B-Tree Performance
            // ----------------------------------------------------------------
            System.out.println("*** B-Tree Performance ***");

            // a) Insert
            long btreeInsertStart = System.nanoTime();
            for (int key : insertKeys) {
                btree.insert(key);
            }
            long btreeInsertEnd = System.nanoTime();
            double btreeInsertTimeMs = (btreeInsertEnd - btreeInsertStart) / 1_000_000.0;
            System.out.printf("B-Tree Insert Time (%d ops): %.2f ms%n", numInsertOps, btreeInsertTimeMs);

            // b) Search
            long btreeSearchStart = System.nanoTime();
            for (int key : searchKeys) {
                btree.search(key);
            }
            long btreeSearchEnd = System.nanoTime();
            double btreeSearchTimeMs = (btreeSearchEnd - btreeSearchStart) / 1_000_000.0;
            System.out.printf("B-Tree Search Time (%d ops): %.2f ms%n", numSearchOps, btreeSearchTimeMs);

            // c) Delete
            long btreeDeleteStart = System.nanoTime();
            for (int key : deleteKeys) {
                btree.delete(key);
            }
            long btreeDeleteEnd = System.nanoTime();
            double btreeDeleteTimeMs = (btreeDeleteEnd - btreeDeleteStart) / 1_000_000.0;
            System.out.printf("B-Tree Delete Time (%d ops): %.2f ms%n", numDeleteOps, btreeDeleteTimeMs);

            // ----------------------------------------------------------------
            // LSM Tree Performance
            // ----------------------------------------------------------------
            System.out.println("\n*** LSM Tree Performance ***");

            // Reset or re-initialize data by creating a new LSM instance
            lsmTree.close();
            Files.deleteIfExists(java.nio.file.Paths.get(lsmWalPath)); // Clear WAL
            lsmTree = new LSMTree(memTableSize, lsmWalPath, sstableDir);

            // a) Insert
            long lsmInsertStart = System.nanoTime();
            for (int key : insertKeys) {
                // convert integer key to string
                lsmTree.put(String.valueOf(key), "v" + key);
            }
            long lsmInsertEnd = System.nanoTime();
            double lsmInsertTimeMs = (lsmInsertEnd - lsmInsertStart) / 1_000_000.0;
            System.out.printf("LSM Insert Time (%d ops): %.2f ms%n", numInsertOps, lsmInsertTimeMs);

            // b) Search
            long lsmSearchStart = System.nanoTime();
            for (int key : searchKeys) {
                lsmTree.search(String.valueOf(key));
            }
            long lsmSearchEnd = System.nanoTime();
            double lsmSearchTimeMs = (lsmSearchEnd - lsmSearchStart) / 1_000_000.0;
            System.out.printf("LSM Search Time (%d ops): %.2f ms%n", numSearchOps, lsmSearchTimeMs);

            // c) Delete
            long lsmDeleteStart = System.nanoTime();
            for (int key : deleteKeys) {
                lsmTree.delete(String.valueOf(key));
            }
            long lsmDeleteEnd = System.nanoTime();
            double lsmDeleteTimeMs = (lsmDeleteEnd - lsmDeleteStart) / 1_000_000.0;
            System.out.printf("LSM Delete Time (%d ops): %.2f ms%n", numDeleteOps, lsmDeleteTimeMs);

            btree.close();
            lsmTree.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n=== Performance Test Completed ===");
    }
}
