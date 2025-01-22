
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.TreeMap;


/**
 * Immutable, sorted SSTable stored on disk.
 */
public class SSTable {

    private final Path filePath;

    // Sparse idx: key -> offset in file
    private final TreeMap<String, Long> index;
    private final long size;


    /**
     * Private constructor.
     *
     * @param filePath The path to the SSTable file.
     * @param index    The index.
     * @param size     The number of entries.
     */
    private SSTable(Path filePath, TreeMap<String, Long> index, long size){
        this.filePath = filePath;
        this.index = index;
        this.size = size;
    }

    /**
     * Returns the number of entries in the SSTable.
     *
     * @return The size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the path of the SSTable.
     *
     * @return The file path.
     */
    public Path getFilePath() {
        return filePath;
    }


    /**
     * Creates an SSTable from the given MemTable.
     *
     * @param memTable  The MemTable data.
     * @param directory The directory to store the SSTable.
     * @return The created SSTable instance.
     * @throws IOException If an I/O error occurs.
     */
    public static SSTable createFromMemTable(TreeMap<String, String> memTable, String directory) throws IOException {
        //Generates unique filename with timestamp
        String filename = "sstable_" + System.currentTimeMillis() + ".sst";
        Path sstablePath = Paths.get(directory, filename);
        Files.createDirectories(sstablePath.getParent());

        TreeMap<String, Long> index = new TreeMap<>();
        long size = 0;
        // Adjustable for sparsity
        final int INDEX_INTERVAL = 10;

        try( BufferedWriter writer = Files.newBufferedWriter(sstablePath, StandardCharsets.UTF_8)) {
            long offset = 0;
            int count = 0;
            for(Map.Entry<String, String> entry : memTable.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();

                if(value == null) {
                    value = "NULL";
                }

                String line = key + " " + value;
                writer.write(line);
                writer.newLine();
                size++;

                if(count % INDEX_INTERVAL == 0) {
                    index.put(key, offset);
                }

                offset += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes().length;
                count++;
            }
        }

        return new SSTable(sstablePath, index, size);
    }


    /**
     * Searches for a key in the SSTable.
     *
     * @param key The key to search for.
     * @return The associated value, or null if not found or marked as deleted.
     * @throws IOException If an I/O error occurs.
     */
    public String search(String key) throws IOException {
        String seekKey = index.floorKey(key); //Closest key smaller than the key in the index

        long offset = 0;
        if(seekKey != null) {
            offset = index.get(seekKey);
        }

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            raf.seek(offset);
            String line;

            while((line = raf.readLine()) != null) {
                // Charset name: ISO-8859-1
                String decodedLine = new String(line.getBytes("ISO-8859-1"), StandardCharsets.UTF_8);
                String[] parts = decodedLine.split(" ", 2);
                if (parts.length < 1) {
                    continue;
                }

                String currentKey = parts[0];

                if (currentKey.equals(key)) {
                    if (parts.length == 1 || parts[1].equals("NULL")){
                        return null;
                    }

                    return parts[1];
                } else if (currentKey.compareTo(key) > 0){
                    break; // Key not found
                }
            }
        }

        return null;
    }
}
