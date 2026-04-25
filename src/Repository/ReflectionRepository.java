package Repository;

import java.io.*;
import java.lang.reflect.Field;

public class ReflectionRepository {

    public String getCSVDataRecursive(Object obj) {
        if (obj == null) return "";
        StringBuilder result = new StringBuilder();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null) continue;

                Class<?> type = field.getType();
                if (type.isPrimitive() || value instanceof String ||
                        value instanceof Number || value instanceof Boolean) {
                    result.append(value.toString()).append(",");
                } else {
                    result.append(getCSVDataRecursive(value));
                }
            } catch (IllegalAccessException e) {
                System.err.println("Reflection error: " + e.getMessage());
            }
        }
        return result.toString();
    }
    public boolean saveCSV(Object obj, String filePath) {
        if (obj == null || filePath == null || filePath.isEmpty()) {
            return false;
        }

        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs(); // יצירת תיקיות אם אינן קיימות
        }

        try (FileWriter writer = new FileWriter(file)) {
            // שמירת שם המחלקה בתחילת הקובץ כדי שנדע מה לשחזר בקריאה
            writer.write(obj.getClass().getName() + ",");
            writer.write(getCSVDataRecursive(obj));
            return true;
        } catch (IOException e) {
            System.err.println("IO Error in saveCSV: " + e.getMessage());
            return false;
        }
    }
    public Object readCsv(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line == null) return null;
            String[] values = line.split(",");
            int[] index = {0};
            String className = values[index[0]++];
            Class<?> clazz = Class.forName(className);
            return buildObjectRecursive(clazz, values, index);
        } catch (Exception e) {
            System.err.println("Error reading CSV with Reflection: " + e.getMessage());
        }
        return null;
    }

    private Object buildObjectRecursive(Class<?> clazz, String[] values, int[] index) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> type = field.getType();
            if (type == int.class || type == Integer.class) {
                field.set(obj, Integer.parseInt(values[index[0]++]));
            } else if (type == double.class || type == Double.class) {
                field.set(obj, Double.parseDouble(values[index[0]++]));
            } else if (type == boolean.class || type == Boolean.class) {
                field.set(obj, Boolean.parseBoolean(values[index[0]++]));
            } else if (type == String.class) {
                field.set(obj, values[index[0]++]);
            } else {
                Object inner = buildObjectRecursive(type, values, index);
                field.set(obj, inner);
            }
        }
        return obj;
    }
}