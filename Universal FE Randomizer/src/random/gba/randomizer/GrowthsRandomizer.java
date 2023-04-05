package random.gba.randomizer;

import java.util.Random;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEHolisticCharacter;
import fedata.gba.GBAFEStatDto;
import random.gba.loader.CharacterDataLoader;

public class GrowthsRandomizer {
	
	static final int rngSalt = 124;
	
	private static int nextMultiplier(Random rng) {
		int randomNum = rng.nextInt(2);
		if (randomNum == 0) {
			return 1;
		}
		
		return -1;
	}
	
	public static void randomizeGrowthsByRedistribution(int variance, int min, int max, boolean adjustHP, CharacterDataLoader charactersData, Random rng) {
		GBAFECharacterData[] allPlayableCharacters = charactersData.playableCharacters();
		
		// Commit anything outstanding first.
		// In case any other randomization step modified characters, because we
		// need to start from a clean slate.
		charactersData.commit();
		
		for (GBAFECharacterData character : allPlayableCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			int growthTotal = holisticCharacter.getGrowths().getStatTotal();
			growthTotal += rng.nextInt(variance + 1) * nextMultiplier(rng);
			
			GBAFEStatDto newGrowths = new GBAFEStatDto(min, min, min, min, min, min, min);
			
			growthTotal -= (min * 7);
			
			int maximumAvailableGrowthsRemaining = (max - min) * 7;
			
			if (maximumAvailableGrowthsRemaining > growthTotal) {
				while (growthTotal > 0) {
					int randomNum = rng.nextInt(adjustHP ? 10 : 8);
					int amount = Math.min(5,  growthTotal);
					int increaseAmount = 0;
					switch (randomNum) {
					case 0:
					case 1:
						increaseAmount = Math.min(amount, max - newGrowths.hp);
						growthTotal -= increaseAmount;
						newGrowths.hp += increaseAmount;
						break;
					case 2:
						increaseAmount = Math.min(amount, max - newGrowths.str);
						growthTotal -= increaseAmount;
						newGrowths.str += increaseAmount;
						break;
					case 3:
						increaseAmount = Math.min(amount, max - newGrowths.skl);
						growthTotal -= increaseAmount;
						newGrowths.skl += increaseAmount;
						break;
					case 4:
						increaseAmount = Math.min(amount, max - newGrowths.spd);
						growthTotal -= increaseAmount;
						newGrowths.spd += increaseAmount;
						break;
					case 5:
						increaseAmount = Math.min(amount, max - newGrowths.lck);
						growthTotal -= increaseAmount;
						newGrowths.lck += increaseAmount;
						break;
					case 6: 
						increaseAmount = Math.min(amount, max - newGrowths.def);
						growthTotal -= increaseAmount;
						newGrowths.def += increaseAmount;
						break;
					case 7:
						increaseAmount = Math.min(amount, max - newGrowths.res);
						growthTotal -= increaseAmount;
						newGrowths.res += increaseAmount;
						break;
					default:
						increaseAmount = Math.min(amount, max - newGrowths.hp);
						growthTotal -= increaseAmount;
						newGrowths.hp += increaseAmount;
						break;
					}
				}
			} else {
				// We can't satisfy the max constraints.
				// Just max out everything.
				newGrowths = new GBAFEStatDto(max, max, max, max, max, max, max);
			}
			
			holisticCharacter.setGrowths(newGrowths);
		}
		
	}
	
	public static void randomizeGrowthsByRandomDelta(int maxDelta, int min, int max, boolean adjustHP, CharacterDataLoader charactersData, Random rng) {
		GBAFECharacterData[] allPlayableCharacters = charactersData.playableCharacters();
		
		charactersData.commit();
		
		for (GBAFECharacterData character : allPlayableCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			
			GBAFEStatDto growths = holisticCharacter.getGrowths();
			GBAFEStatDto minGrowths = new GBAFEStatDto(min, min, min, min, min, min, min);
			GBAFEStatDto maxGrowths = new GBAFEStatDto(max, max, max, max, max, max, max);
			
			growths.hp += nextMultiplier(rng) * rng.nextInt(maxDelta+1);
			growths.str += nextMultiplier(rng) * rng.nextInt(maxDelta+1);
			growths.skl += nextMultiplier(rng) * rng.nextInt(maxDelta+1);
			growths.spd += nextMultiplier(rng) * rng.nextInt(maxDelta+1);
			growths.def += nextMultiplier(rng) * rng.nextInt(maxDelta+1);
			growths.res += nextMultiplier(rng) * rng.nextInt(maxDelta+1);
			growths.lck += nextMultiplier(rng) * rng.nextInt(maxDelta+1);
			
			growths.clamp(minGrowths, maxGrowths);
			holisticCharacter.setGrowths(maxGrowths);
		}
	}
	
	public static void fullyRandomizeGrowthsWithRange(int minGrowth, int maxGrowth, boolean adjustHP, CharacterDataLoader charactersData, Random rng) {
		GBAFECharacterData[] allPlayableCharacters = charactersData.playableCharacters();
		
		charactersData.commit();
		
		for (GBAFECharacterData character : allPlayableCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			int range = maxGrowth - minGrowth + 1;
			
			GBAFEStatDto newGrowths = new GBAFEStatDto();
			newGrowths.hp = rng.nextInt(range) + minGrowth;
			newGrowths.str = rng.nextInt(range) + minGrowth;
			newGrowths.skl = rng.nextInt(range) + minGrowth;
			newGrowths.spd = rng.nextInt(range) + minGrowth;
			newGrowths.def = rng.nextInt(range) + minGrowth;
			newGrowths.res = rng.nextInt(range) + minGrowth;
			newGrowths.lck = rng.nextInt(range) + minGrowth;
			
			if (adjustHP) {
				int threshold = range / 2 + minGrowth;
				if (newGrowths.hp < threshold) {
					if (newGrowths.hp + range / 2 <= maxGrowth) {
						newGrowths.hp += range / 2;
					} else {
						newGrowths.hp = maxGrowth;
					}
				}
			}
			
			holisticCharacter.setGrowths(newGrowths);
		}
	}

}
