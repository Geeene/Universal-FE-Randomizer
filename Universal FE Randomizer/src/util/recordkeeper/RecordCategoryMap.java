package util.recordkeeper;

import java.util.*;

public class RecordCategoryMap {
    private boolean sorted = false;
    private List<String> keyList = new ArrayList<>();
    private Map<String, RecordCategory> categories = new HashMap<>();

    public void addEntry(String key, RecordCategory entry){
        if(!keyList.contains(key))
            keyList.add(key);
        categories.put(key, entry);
    }

    public RecordCategory getCategory(String key){
        return categories.get(key);
    }

    public Map<String, RecordCategory> getCategories(){
        return categories;
    }

    public List<String> getKeyList(){
        if(sorted){
            Collections.sort(keyList);
        }
        return keyList;
    }

    public void setSorted(){
        this.sorted = true;
    }

}