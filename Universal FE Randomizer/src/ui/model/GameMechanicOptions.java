package ui.model;

import fedata.general.FEBase;
import fedata.general.FEBase.GameType;
import random.general.Randomizer;
import util.recordkeeper.RecordKeeper;

public class GameMechanicOptions implements RecordableOption {


	public enum ExperienceRate {
		NORMAL, PARAGON, RENEGADE
	}

	public final Boolean applyEnglishPatch;

	public final Boolean tripleEffectiveness;
	public final Boolean singleRNMode;

	public final Boolean randomizeFogOfWar;
	public final Integer fogOfWarChance;
	public final MinMaxOption fogOfWarVisionRange;
	public final ExperienceRate experienceRate;
	public final Boolean casualMode;
	
	public static class FollowupRequirement {
		public final Boolean requiresPursuit;
		public final int thresholdWithPursuit;
		public final int thresholdWithoutPursuit;

		public FollowupRequirement(boolean requiresPursuit, int withPursuit, int withoutPursuit) {
			this.requiresPursuit = requiresPursuit;
			thresholdWithPursuit = withPursuit;
			thresholdWithoutPursuit = withoutPursuit;
		}
	}

	public final FollowupRequirement followupRequirement;

	// FE7, FE8
	public GameMechanicOptions(Boolean tripleEffectiveness, Boolean singleRN, Boolean fogOfWar, int fogOfWarChance, MinMaxOption visionRange, Boolean casualMode, ExperienceRate experienceRate) {
		this.applyEnglishPatch = false;
		this.tripleEffectiveness = tripleEffectiveness;
		followupRequirement = null;
		singleRNMode = singleRN;
		randomizeFogOfWar = fogOfWar;
		this.fogOfWarChance = fogOfWarChance;
		fogOfWarVisionRange = visionRange;
		this.casualMode = casualMode;
		this.experienceRate = experienceRate;
	}

	// FE4
	public GameMechanicOptions(Boolean applyEnglishPatch, FollowupRequirement followupRequirement) {
		this.applyEnglishPatch = applyEnglishPatch;
		this.followupRequirement = followupRequirement;
		this.tripleEffectiveness = false;
		singleRNMode = false;
		randomizeFogOfWar = false;
		fogOfWarVisionRange = null;
		fogOfWarChance = 0;
		casualMode = false;
		experienceRate = ExperienceRate.NORMAL;
	}

	// FE6
	public GameMechanicOptions(Boolean applyEnglishPatch, Boolean tripleEffectiveness, Boolean singleRN, Boolean fogOfWar, int fogOfWarChance, MinMaxOption visionRange, Boolean casualMode, ExperienceRate experienceRate) {
		this.applyEnglishPatch = applyEnglishPatch;
		this.tripleEffectiveness = tripleEffectiveness;
		followupRequirement = null;
		singleRNMode = singleRN;
		randomizeFogOfWar = fogOfWar;
		fogOfWarVisionRange = visionRange;
		this.fogOfWarChance = fogOfWarChance;
		this.casualMode = casualMode;
		this.experienceRate = experienceRate;
	}

	@Override
	public void record(RecordKeeper rk, GameType type) {
		if (singleRNMode) {
			rk.addHeaderItem("Enable Single RN", "YES");
		} else {
			rk.addHeaderItem("Enable Single RN", "NO");
		}

		if (randomizeFogOfWar) {
			rk.addHeaderItem("Randomize Fog of War", "YES - " + Integer.toString(fogOfWarChance) + "%");
			rk.addHeaderItem("Fog of War Vision Range", Integer.toString(fogOfWarVisionRange.minValue) + " ~ "
					+ Integer.toString(fogOfWarVisionRange.maxValue));
		} else {
			rk.addHeaderItem("Randomize Fog of War", "NO");
		}
		
		String expRateDesc = "";
		switch(experienceRate) {
		case NORMAL: expRateDesc = "Regular exp gain."; break;
		case PARAGON: expRateDesc = "Paragon Mode. Exp gain doubled."; break;
		case RENEGADE: expRateDesc = "Renegade Mode. Exp gain halved."; break;
		}
		
		rk.addHeaderItem("Experience Rate: ", expRateDesc);
		
		if (casualMode) {
			rk.addHeaderItem("Casual Mode", "YES");
		} else {
			rk.addHeaderItem("Casual Mode", "NO");
		}
	}
}