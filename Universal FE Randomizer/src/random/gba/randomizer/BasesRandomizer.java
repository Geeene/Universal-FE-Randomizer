package random.gba.randomizer;

import java.util.Random;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEHolisticCharacter;
import fedata.gba.GBAFEStatDto;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.ClassDataLoader;

public class BasesRandomizer {
	
	public static int rngSalt = 9001;
	
	public static void randomizeBasesByRedistribution(int variance, CharacterDataLoader charactersData, ClassDataLoader classData, Random rng) {
		GBAFECharacterData[] allPlayableCharacters = charactersData.playableCharacters();
		for (GBAFECharacterData character : allPlayableCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			GBAFEClassData charClass = holisticCharacter.getCurrentClass();

			// calculate the total number of stats that can be redistributed. This is the complete bases (personal and class) + the selected variance
			int baseTotal = holisticCharacter.getStats().getStatTotal();
			// 50/50 chance for the variance to be negative, so multiply it by -1 or 1
			baseTotal += rng.nextInt(variance + 1) * nextMultiplier(rng);
			

			GBAFEStatDto newStats = new GBAFEStatDto();
			GBAFEStatDto caps = charClass.getCaps();
			
			while (baseTotal > 0) {
				int randomNum = rng.nextInt(10);
				int amount = rng.nextInt(3) + 1;
				
				switch (randomNum) {
				case 0:
				case 5:
				case 1:
					amount = reduceAmountIfNecessary(caps.hp, amount, newStats.hp);
					newStats.hp += amount;
					break;
				case 2:
					amount = reduceAmountIfNecessary(caps.str, amount, newStats.str);
					newStats.str += amount;
					break;
				case 3:
					amount = reduceAmountIfNecessary(caps.skl, amount, newStats.skl);
					newStats.skl += amount;
					break;
				case 4:
					amount = reduceAmountIfNecessary(caps.spd, amount, newStats.spd);
					newStats.spd += amount;
					break;
				case 8:
				case 9:
					amount = reduceAmountIfNecessary(caps.lck, amount, newStats.lck);
					newStats.lck += amount;
					break;
				case 6: 
					amount = reduceAmountIfNecessary(caps.def, amount, newStats.def);
					newStats.def += amount;
					break;
				case 7:
					amount = reduceAmountIfNecessary(caps.res, amount, newStats.res);
					newStats.res += amount;
					break;
				default:
					break;
				}
				
				baseTotal -= amount;
			} 
			
			// Set the Stats for the Holistic Character. This automatically clamps to caps / minimum values again if needed.
			holisticCharacter.setStats(newStats);
			
		}
	}
	
	public static int reduceAmountIfNecessary(int cap, int amount, int currentStat) {
		int statAfterIncrease = currentStat + amount;
		if (statAfterIncrease > cap) {
			return amount - (statAfterIncrease - cap);
		}
		return amount;
	}

	
	public static void randomizeBasesByRandomDelta(int maxDelta, CharacterDataLoader charactersData, ClassDataLoader classData, Random rng) {
		GBAFECharacterData[] allPlayableCharacters = charactersData.playableCharacters();
		for (GBAFECharacterData character : allPlayableCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			GBAFEStatDto oldStats = holisticCharacter.getStats();
			GBAFEStatDto deltas = new GBAFEStatDto();
			deltas.hp = rng.nextInt(maxDelta + 1) * nextMultiplier(rng);
			deltas.str = rng.nextInt(maxDelta + 1) * nextMultiplier(rng);
			deltas.skl = rng.nextInt(maxDelta + 1) * nextMultiplier(rng);
			deltas.spd = rng.nextInt(maxDelta + 1) * nextMultiplier(rng);
			deltas.lck = rng.nextInt(maxDelta + 1) * nextMultiplier(rng);
			deltas.def = rng.nextInt(maxDelta + 1) * nextMultiplier(rng);
			deltas.res = rng.nextInt(maxDelta + 1) * nextMultiplier(rng);
			
			// Set the Stats for the Holistic Character. This automatically clamps to caps / minimum values if needed.
			holisticCharacter.setStats(oldStats.add(deltas));
		}
	}
	
	private static int nextMultiplier(Random rng) {
		int randomNum = rng.nextInt(2);
		if (randomNum == 0) {
			return 1;
		}
		
		return -1;
	}
}
