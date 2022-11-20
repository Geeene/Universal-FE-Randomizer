package util.recordkeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordCategory {
    List<String> allKeys = new ArrayList<>();
    private Map<String, RecordEntry> entries = new HashMap<>();

    public void addEntry(String key, RecordEntry entry) {
        allKeys.add(key);
        entries.putIfAbsent(key, entry);
    }

    public Map<String, RecordEntry> getEntries() {
        return this.entries;
    }

    public RecordEntry getEntry(String key) {
        return this.entries.get(key);
    }

    public void removeEntry(String key) {
            this.entries.remove(key);
    }
}