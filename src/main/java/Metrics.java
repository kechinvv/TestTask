import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Metrics {
    double averageOverride = 0.0;
    double averageFields = 0.0;
    int countFields = 0;
    int countOverride = 0;
    double ABC = 0.0;
    int A = 0;
    int B = 0;
    int C = 0;
    double averageDepth = 0.0;
    int maxDepth = 0;
    HashMap<String, String> classes = new HashMap<>();

    public void metricHandler(int a, int b, int c, int fields, int override, HashMap<String, String> extend) {
        A += a;
        B += b;
        C += c;
        averageOverride += override;
        countOverride += 1;
        averageFields += fields;
        countFields += 1;
        classes.putAll(extend);
    }

    public void summary() throws IOException {
        ABC = Math.sqrt(A * A + B * B + C * C);
        for (String key : classes.keySet()) {
            String k = key;
            int count = 0;
            while (classes.get(k) != null) {
                count++;
                k = classes.get(k);
            }
            if (count > maxDepth) maxDepth = count;
            averageDepth += count;
        }
        averageDepth = averageDepth / classes.size();
        averageFields = averageFields / countFields;
        averageOverride = averageOverride / countOverride;
        System.out.println("ABC=" + ABC + " averageDepth=" + averageDepth + " maxDepth="
                + maxDepth + " averageFields=" + averageFields + " averageOverride=" + averageOverride);
      //  System.out.println(classes);
        createJson();
    }

    private void createJson() throws IOException {
        JsonObject m = new JsonObject();
        m.addProperty("maxDepth", maxDepth);
        m.addProperty("averageDepth", averageDepth);
        m.addProperty("ABC", ABC);
        m.addProperty("averageFields", averageFields);
        m.addProperty("averageOverride", averageOverride);
        try (FileWriter file = new FileWriter("m.json")) {

            file.write(String.valueOf(m));
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
