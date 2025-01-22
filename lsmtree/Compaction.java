// Used to merge multiple SSL tables into a single one

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;


/**
 * Handles the compaction process for LSM Trees.
 */
public class Compaction {


    /**
     * Merges multiple SSTables into a single SSTable.
     *
     * @param sstables   The list of SSTables to merge.
     * @param directory  The directory to store the new SSTable.
     * @return The newly created merged SSTable.
     * @throws IOException If an I/O error occurs.
     */
    public static SSTable compact(List<SSTable> sstables, String directory) throws IOException {
        PriorityQueue<IteratorWrapper> pq = new PriorityQueue<IteratorWrapper>(Comparator.comparing(wrapper -> wrapper.currentKey));

        List<BufferedReader> readers = new ArrayList<>(); // Iterators for each SSLTable
        try {
            for (SSTable table : sstables) {
                BufferedReader reader = Files.newBufferedReader(table.getFilePath(), StandardCharsets.UTF_8);
                readers.add(reader);
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(" ", 2);
                    if (parts.length >= 1) {
                        String key = parts[0];
                        String value = parts.length == 2 ? parts[1] : "NULL";
                        pq.add(new IteratorWrapper(key, value, reader));
                    }
                }
            }

            TreeMap<String, String> merged = new TreeMap<>();
            while (!pq.isEmpty()) {
                IteratorWrapper it = pq.poll();
                String key = it.currentKey;
                String value = it.currentValue;

                String line = it.reader.readLine();

                if (line != null) {
                    String[] parts = line.split(" ", 2);
                    if (parts.length >= 1) {
                        String nextKey = parts[0];
                        String nextValue = parts.length == 2 ? parts[1] : "NULL";
                        pq.add(new IteratorWrapper(nextKey, nextValue, it.reader));
                    }
                }

                merged.put(key, value);
            }

            // Remove keys with null values
            merged.entrySet().removeIf(entry -> entry.getValue().equals("NULL"));

            SSTable newSSTable = SSTable.createFromMemTable(merged, directory);

            return newSSTable;
        } finally {
            for (BufferedReader reader : readers) {
                if (reader != null) {
                    reader.close();
                }
            }
        }


    }



    /**
     * Helper class to wrap iterator state.
     */
    private static class IteratorWrapper {
        String currentKey;
        String currentValue;
        BufferedReader reader;

        public IteratorWrapper(String key, String value, BufferedReader reader) {
            this.currentKey = key;
            this.currentValue = value;
            this.reader = reader;
        }
    }
}
