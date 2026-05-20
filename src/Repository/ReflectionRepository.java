package Repository;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionRepository {

    public String getCSVDataRecursive(Object obj) {
        if (obj == null) return "";
        StringBuilder result = new StringBuilder();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null) continue;
                Class<?> type = field.getType();
                boolean isSimpleType = type.isPrimitive() ||
                        type == String.class ||
                        Number.class.isAssignableFrom(type) ||
                        type == Boolean.class ||
                        type == java.time.LocalDateTime.class;

                if (isSimpleType) {
                    result.append(value.toString()).append(",");
                } else {
                    result.append(getCSVDataRecursive(value));
                }
            }catch (IllegalAccessException e){
                System.err.println("Reflection error: "+e.getMessage());
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
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(obj.getClass().getName() + ",");
            writer.write(getCSVDataRecursive(obj));
            writer.close();
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
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Class<?> type = field.getType();
            boolean isSimpleType = type.isPrimitive() ||
                    type == String.class ||
                    Number.class.isAssignableFrom(type) ||
                    type == Boolean.class ||
                    type == java.time.LocalDateTime.class;

            if (!isSimpleType) {
                continue;
            }
            if (index[0] >= values.length) {
                break;
            }

            String currentVal = values[index[0]];
            if (currentVal == null || currentVal.trim().isEmpty()) {
                if (type == String.class) {
                    field.set(obj, "");
                }
                index[0]++;
                continue;
            }
            if (type == int.class || type == Integer.class) {
                field.set(obj, Integer.parseInt(currentVal.trim()));
                index[0]++;
            } else if (type == double.class || type == Double.class) {
                field.set(obj, Double.parseDouble(currentVal.trim()));
                index[0]++;
            } else if (type == boolean.class || type == Boolean.class) {
                field.set(obj, Boolean.parseBoolean(currentVal.trim()));
                index[0]++;
            } else if (type == long.class || type == Long.class) {
                field.set(obj, Long.parseLong(currentVal.trim()));
                index[0]++;
            } else if (type == float.class || type == Float.class) {
                field.set(obj, Float.parseFloat(currentVal.trim()));
                index[0]++;
            } else if (type == String.class) {
                field.set(obj, currentVal.trim());
                index[0]++;
            } else if (type == java.time.LocalDateTime.class) {
                field.set(obj, java.time.LocalDateTime.parse(currentVal.trim()));
                index[0]++;
            }
        }
        return obj;
    }}