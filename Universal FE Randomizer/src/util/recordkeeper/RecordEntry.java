package util.recordkeeper;

public class RecordEntry {
    String originalValue;
    String updatedValue;
    String additionalInfo;

    public RecordEntry(String originalValue){
        this.originalValue = originalValue;
    }
    public RecordEntry(String originalValue, String updatedValue){
        this.originalValue = originalValue;
        this.updatedValue = updatedValue;
    }
}