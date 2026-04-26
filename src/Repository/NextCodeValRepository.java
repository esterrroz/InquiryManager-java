package Repository;

import java.io.*;

public class NextCodeValRepository {
    private static final String FILE_PATH = "data/nextVal";

    public int load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) return Integer.parseInt(line.trim());
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading nextVal: " + e.getMessage());
        }
        return 0;
    }

    public void save(int val) {
        new File("data").mkdirs();
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(String.valueOf(val));
        } catch (IOException e) {
            System.err.println("Error saving nextVal: " + e.getMessage());
        }
    }
}
