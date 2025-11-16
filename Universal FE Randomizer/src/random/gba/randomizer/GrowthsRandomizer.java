package random.gba.randomizer;

import java.util.Random;

import fedata.gba.GBAFECharacterData;
import fedata.general.FEBase;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.GBADataLoaders;
import util.OptionRecorder;
import util.WhyDoesJavaNotHaveThese;

public class GrowthsRandomizer extends AbstractGBARandomizerComponent{
	
	static final int rngSalt = 124;

    public GrowthsRandomizer(OptionRecorder.GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, FEBase.GameType type) {
        super(allOptions, dataLoaders, rng, type);
    }

    public void randomizeGrowthsByRedistribution() {
		GBAFECharacterData[] allPlayableCharacters = charData.playableCharacters();
        int variance = growths.redistributionOption.variance;
        int min = growths.redistributionOption.minValue;
        int max = growths.redistributionOption.maxValue;
        boolean adjustHP = growths.adjustHP;
		// Commit anything outstanding first.
		// In case any other randomization step modified characters, because we
		// need to start from a clean slate.
        charData.commit();
		
		for (GBAFECharacterData character : allPlayableCharacters) {
			
			// Do not modify anything that was already modified.
			// This is here because some characters are linked (for example, FE7 Lyn has two variants: Tutorial and Not Tutorial).
			// If we generate growths for one, we apply it to all linked characters at the end of this loop.
			if (character.wasModified()) {
				continue;
			}
			
			int growthTotal = character.getHPGrowth() + character.getSTRGrowth() + character.getSKLGrowth() + character.getSPDGrowth() + 
					character.getLCKGrowth() + character.getDEFGrowth() + character.getRESGrowth();

			int randomNum = rng.nextInt(2);
			if (randomNum == 0) {
				growthTotal += rng.nextInt(variance + 1);
			} else {
				growthTotal -= rng.nextInt(variance + 1);
			}
			
			int newHPGrowth = min;
			int newSTRGrowth = min;
			int newSKLGrowth = min;
			int newSPDGrowth = min;
			int newLCKGrowth = min;
			int newDEFGrowth = min;
			int newRESGrowth = min;
			
			growthTotal -= (min * 7);
		
			int availableGrowthRemaining = (max - newHPGrowth) + (max - newSTRGrowth) + (max - newSKLGrowth) +
					(max - newSPDGrowth) + (max - newLCKGrowth) + (max - newDEFGrowth) + (max - newRESGrowth);
			
			if (availableGrowthRemaining > growthTotal) {
				while (growthTotal > 0) {
					randomNum = rng.nextInt(adjustHP ? 10 : 8);
					int amount = Math.min(5,  growthTotal);
					int increaseAmount = 0;
					switch (randomNum) {
					case 0:
					case 1:
						increaseAmount = Math.min(amount, max - newHPGrowth);
						growthTotal -= increaseAmount;
						newHPGrowth += increaseAmount;
						break;
					case 2:
						increaseAmount = Math.min(amount, max - newSTRGrowth);
						growthTotal -= increaseAmount;
						newSTRGrowth += increaseAmount;
						break;
					case 3:
						increaseAmount = Math.min(amount, max - newSKLGrowth);
						growthTotal -= increaseAmount;
						newSKLGrowth += increaseAmount;
						break;
					case 4:
						increaseAmount = Math.min(amount, max - newSPDGrowth);
						growthTotal -= increaseAmount;
						newSPDGrowth += increaseAmount;
						break;
					case 5:
						increaseAmount = Math.min(amount, max - newLCKGrowth);
						growthTotal -= increaseAmount;
						newLCKGrowth += increaseAmount;
						break;
					case 6: 
						increaseAmount = Math.min(amount, max - newDEFGrowth);
						growthTotal -= increaseAmount;
						newDEFGrowth += increaseAmount;
						break;
					case 7:
						increaseAmount = Math.min(amount, max - newDEFGrowth);
						growthTotal -= increaseAmount;
						newRESGrowth += increaseAmount;
						break;
					default:
						increaseAmount = Math.min(amount, max - newHPGrowth);
						growthTotal -= increaseAmount;
						newHPGrowth += increaseAmount;
						break;
					}
				}
			} else {
				// We can't satisfy the max constraints.
				// Just max out everything.
				newHPGrowth = max;
				newSTRGrowth = max;
				newSKLGrowth = max;
				newSPDGrowth = max;
				newLCKGrowth = max;
				newDEFGrowth = max;
				newRESGrowth = max;
			}
			
			for (GBAFECharacterData thisCharacter : charData.linkedCharactersForCharacter(character)) {
				thisCharacter.setHPGrowth(newHPGrowth);
				thisCharacter.setSTRGrowth(newSTRGrowth);
				thisCharacter.setSKLGrowth(newSKLGrowth);
				thisCharacter.setSPDGrowth(newSPDGrowth);
				thisCharacter.setLCKGrowth(newLCKGrowth);
				thisCharacter.setDEFGrowth(newDEFGrowth);
				thisCharacter.setRESGrowth(newRESGrowth);
			}
		}

        charData.commit();
	}
	
	public void randomizeGrowthsByRandomDelta() {
		GBAFECharacterData[] allPlayableCharacters = charData.playableCharacters();
        int maxDelta = growths.deltaOption.variance;
        int min = growths.deltaOption.minValue;
        int max = growths.deltaOption.maxValue;
        boolean adjustHP = growths.adjustHP;
		charData.commit();
		
		for (GBAFECharacterData character : allPlayableCharacters) {
			
			if (character.wasModified()) {
				continue;
			}
			
			int newHPGrowth = character.getHPGrowth();
			int newSTRGrowth = character.getSTRGrowth();
			int newSKLGrowth = character.getSKLGrowth();
			int newSPDGrowth = character.getSPDGrowth();
			int newLCKGrowth = character.getLCKGrowth();
			int newDEFGrowth = character.getDEFGrowth();
			int newRESGrowth = character.getRESGrowth();
			
			int randomNum = rng.nextInt(2);
			if ((randomNum == 0 && newHPGrowth < max) || adjustHP) {
				newHPGrowth += rng.nextInt(Math.min(maxDelta + 1, max - newHPGrowth + 1));
			} else if (newHPGrowth > min) {
				newHPGrowth -= rng.nextInt(Math.min(maxDelta + 1, newHPGrowth - min + 1));
			}
			randomNum = rng.nextInt(2);
			if (randomNum == 0 && newSTRGrowth < max) {
				newSTRGrowth += rng.nextInt(Math.min(maxDelta + 1, max - newSTRGrowth + 1));
			} else if (newSTRGrowth > min) {
				newSTRGrowth -= rng.nextInt(Math.min(maxDelta + 1, newSTRGrowth - min + 1));
			}
			randomNum = rng.nextInt(2);
			if (randomNum == 0 && newSKLGrowth < max) {
				newSKLGrowth += rng.nextInt(Math.min(maxDelta + 1, max - newSKLGrowth + 1));
			} else if (newSKLGrowth > min) {
				newSKLGrowth -= rng.nextInt(Math.min(maxDelta + 1, newSKLGrowth - min + 1));
			}
			randomNum = rng.nextInt(2);
			if (randomNum == 0 && newSPDGrowth < max) {
				newSPDGrowth += rng.nextInt(Math.min(maxDelta + 1, max - newSPDGrowth + 1));
			} else if (newSPDGrowth > min) {
				newSPDGrowth -= rng.nextInt(Math.min(maxDelta + 1, newSPDGrowth - min + 1));
			}
			randomNum = rng.nextInt(2);
			if (randomNum == 0 && newLCKGrowth < max) {
				newLCKGrowth += rng.nextInt(Math.min(maxDelta + 1, max - newLCKGrowth + 1));
			} else if (newLCKGrowth > min) {
				newLCKGrowth -= rng.nextInt(Math.min(maxDelta + 1, newLCKGrowth - min + 1));
			}
			randomNum = rng.nextInt(2);
			if (randomNum == 0 && newDEFGrowth < max) {
				newDEFGrowth += rng.nextInt(Math.min(maxDelta + 1, max - newDEFGrowth + 1));
			} else if (newDEFGrowth > min) {
				newDEFGrowth -= rng.nextInt(Math.min(maxDelta + 1, newDEFGrowth - min + 1));
			}
			randomNum = rng.nextInt(2);
			if (randomNum == 0 && newRESGrowth < max) {
				newRESGrowth += rng.nextInt(Math.min(maxDelta + 1, max - newRESGrowth + 1));
			} else if (newRESGrowth > min) {
				newRESGrowth -= rng.nextInt(Math.min(maxDelta + 1, newRESGrowth - min + 1));
			}
			
			for (GBAFECharacterData thisCharacter : charData.linkedCharactersForCharacter(character)) {
				thisCharacter.setHPGrowth(WhyDoesJavaNotHaveThese.clamp(newHPGrowth, min, max));
				thisCharacter.setSTRGrowth(WhyDoesJavaNotHaveThese.clamp(newSTRGrowth, min, max));
				thisCharacter.setSKLGrowth(WhyDoesJavaNotHaveThese.clamp(newSKLGrowth, min, max));
				thisCharacter.setSPDGrowth(WhyDoesJavaNotHaveThese.clamp(newSPDGrowth, min, max));
				thisCharacter.setLCKGrowth(WhyDoesJavaNotHaveThese.clamp(newLCKGrowth, min, max));
				thisCharacter.setDEFGrowth(WhyDoesJavaNotHaveThese.clamp(newDEFGrowth, min, max));
				thisCharacter.setRESGrowth(WhyDoesJavaNotHaveThese.clamp(newRESGrowth, min, max));
			}
		}
		
		charData.commit();
	}
	
	public void fullyRandomizeGrowthsWithRange() {
		GBAFECharacterData[] allPlayableCharacters = charData.playableCharacters();
		int minGrowth = growths.fullOption.minValue;
		int maxGrowth = growths.fullOption.maxValue;
        boolean adjustHP = growths.adjustHP;
		charData.commit();
		
		for (GBAFECharacterData character : allPlayableCharacters) {
			
			if (character.wasModified()) {
				continue;
			}
			
			int range = maxGrowth - minGrowth + 1;
			
			int newHPGrowth = rng.nextInt(range) + minGrowth;
			int newSTRGrowth = rng.nextInt(range) + minGrowth;
			int newSKLGrowth = rng.nextInt(range) + minGrowth;
			int newSPDGrowth = rng.nextInt(range) + minGrowth;
			int newLCKGrowth = rng.nextInt(range) + minGrowth;
			int newDEFGrowth = rng.nextInt(range) + minGrowth;
			int newRESGrowth = rng.nextInt(range) + minGrowth;
			
			if (adjustHP) {
				int threshold = range / 2 + minGrowth;
				if (newHPGrowth < threshold) {
					if (newHPGrowth + range / 2 <= maxGrowth) {
						newHPGrowth += range / 2;
					} else {
						newHPGrowth = maxGrowth;
					}
				}
			}
			
			for (GBAFECharacterData thisCharacter : charData.linkedCharactersForCharacter(character)) {
				thisCharacter.setHPGrowth(newHPGrowth);
				thisCharacter.setSTRGrowth(newSTRGrowth);
				thisCharacter.setSKLGrowth(newSKLGrowth);
				thisCharacter.setSPDGrowth(newSPDGrowth);
				thisCharacter.setLCKGrowth(newLCKGrowth);
				thisCharacter.setDEFGrowth(newDEFGrowth);
				thisCharacter.setRESGrowth(newRESGrowth);
			}
		}
		
		charData.commit();
	}

}
