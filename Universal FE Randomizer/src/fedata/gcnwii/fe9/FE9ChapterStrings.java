package fedata.gcnwii.fe9;

import io.gcn.GCNMessageFileHandler;
import util.DebugPrinter;

public class FE9ChapterStrings {
	private static final DebugPrinter LOGGER = DebugPrinter.forKey(DebugPrinter.Key.FE9_CHAPTER_STRINGS);
	private GCNMessageFileHandler messageHandler;
	
	public FE9ChapterStrings(GCNMessageFileHandler handler) {
		messageHandler = handler;
	}

	public String textStringForIdentifier(String identifier) {
		return messageHandler.getStringWithIdentifier(identifier);
	}
	
	public void setStringForIdentifier(String identifier, String string) {
		messageHandler.addStringWithIdentifier(identifier, string);
	}
	
	public void debugPrintStrings() {
		for (String identifier : messageHandler.allIdentifiers()) {
			LOGGER.log( identifier);
			LOGGER.log( "-----------------------");
			LOGGER.log( textStringForIdentifier(identifier));
			LOGGER.log( "");
		}
	}
}
