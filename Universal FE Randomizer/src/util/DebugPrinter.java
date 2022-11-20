package util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

public class DebugPrinter {
	private static Map<String, DebugListener> listeners = new HashMap<String, DebugListener>();
	private final Key label;
	public enum Key {
		MAIN("Main"),
		
		PALETTE("Palette"), CHAPTER_LOADER("ChapterLoader"), DIFF("Diff"), HUFFMAN("Huffman"), TEXT_LOADING("Text"), RANDOM("Random"), FREESPACE("Free Space"), WEAPONS("Weapon Effect"), UPS("UPS"), CLASS_RANDOMIZER("Class Random"),
		PALETTE_RECYCLER("Palette Recycling"), FE8_SUMMONER_MODULE("Summoner"), FE4_CHARACTER_LOADER("FE4 Character Loader"), FE4_ITEM_MAPPER("FE4 Item Mapper"), FE4_SKILL_RANDOM("FE4 Skill Randomizer"), 
		GBA_TEXT_CODE_CHANGE("GBAFE Text Change"), GBA_RANDOM_RECRUITMENT("GBA Random Recruitment"), LZ77("LZ77"), 
		
		GCN_HANDLER("GCN Handler"), FE9_CHARACTER_LOADER("FE9 Character Loader"), FE9_TEXT_LOADER("FE9 Text Loader"),
		FE9_CLASS_LOADER("FE9 Class Loader"), FE9_ITEM_LOADER("FE9 Item Loader"), FE9_SKILL_LOADER("FE9 Skill Loader"),
		FE9_CHAPTER_LOADER("FE9 Chapter Loader"), FE9_ARMY_LOADER("FE9 Army Loader"), FE9_RANDOM_CLASSES("FE9 Class Randomization"),
		FE9_CHAPTER_SCRIPT("FE9 Chapter Script"), FE9_CHAPTER_STRINGS("FE9 Chapter Strings"), DBX_HANDLER("DBX Handler"),
		FE9_DATA_FILE_HANDLER_V2("FE9 Data File Handler V2"),
		
		MISC("MISC");
		
		String label;
		
		private Key(String key) {
			this.label = key;
		}
	}
	
	private DebugPrinter(Key label) {
		this.label = label;
	}
	
	public static DebugPrinter forKey(Key label) {
		return new DebugPrinter(label);
	}
	
	public void log(String output) {
		DebugPrinterSingleton.get().log(this.label, output);
	}
	
	public void error(String output) {
		DebugPrinterSingleton.get().log(this.label, output);
	}
	
	public static void registerListener(DebugListener listener, String key) {
		DebugPrinterSingleton.get().registerListener(listener, key);
	}
	
	public static void unregisterListener(String key) {
		DebugPrinterSingleton.get().unregisterListener(key);
	}
	
}
