package ui.model;

import java.util.List;

import fedata.general.FEBase.GameType;
import util.recordkeeper.RecordKeeper;

/**
 * Model containing the different Options for shuffling character between games
 */
public class CharacterShufflingOptions implements RecordableOption {
	public enum ShuffleLevelingMode {
		UNCHANGED, AUTOLEVEL;
	}
	
	protected final ShuffleLevelingMode levelingMode;
	protected final boolean shuffleEnabled;
	protected final int chance;
	protected final List<String> includedShuffles;
	protected final boolean changeDescription;
	
	public CharacterShufflingOptions(ShuffleLevelingMode mode, boolean shuffleEnabled, int chance, List<String> selectedFiles, boolean changeDescription) {
		this.levelingMode = mode;
		this.shuffleEnabled = shuffleEnabled;
		this.chance = chance;
		this.includedShuffles = selectedFiles;
		this.changeDescription = changeDescription;
	}
	
	public ShuffleLevelingMode getLevelingMode() {
		return levelingMode;
	}

	public boolean isShuffleEnabled() {
		return shuffleEnabled;
	}
	
	public int getChance() {
		return chance;
	}

	public List<String> getIncludedShuffles() {
		return includedShuffles;
	}

	public boolean shouldChangeDescription() {
		return changeDescription;
	}

	@Override
	public void record(RecordKeeper rk, GameType type) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Leveling Mode: %s", this.levelingMode == ShuffleLevelingMode.AUTOLEVEL ? "autolevel characters": "leave characters unchanged")).append("<br>");
		sb.append(String.format("Shuffle chance: %d%%", this.chance)).append("<br>");
		sb.append(changeDescription ? "Description will be changed" : "Description will be left unchanged").append("<br>");
		sb.append("Included configurations:<br>");
		for (String s : includedShuffles) {
			sb.append(s).append("<br>");
		}
		rk.addHeaderItem("Character Shuffling", sb.toString());
	}
	
}
