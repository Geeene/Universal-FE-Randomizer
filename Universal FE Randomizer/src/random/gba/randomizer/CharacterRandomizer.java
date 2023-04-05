package random.gba.randomizer;

import java.util.Random;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEHolisticCharacter;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.ClassDataLoader;

public class CharacterRandomizer {
	
	public static int rngSalt = 9002;
	
	public static void randomizeAffinity(CharacterDataLoader charactersData, Random rng) {
		GBAFECharacterData[] playableCharacters = charactersData.playableCharacters();
		int[] values = charactersData.validAffinityValues();
		for (GBAFECharacterData character : playableCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			int affinity = values[rng.nextInt(values.length)];
			holisticCharacter.setAffinity(affinity);
		}
	}
	
	public static void randomizeConstitution(int minCON, int variance, CharacterDataLoader characterData, ClassDataLoader classData, Random rng) {
		GBAFECharacterData[] allPlayableCharacters = characterData.playableCharacters();
		for (GBAFECharacterData character : allPlayableCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			int totalCON = holisticCharacter.getCon();
			
			int direction = rng.nextInt(2);
			if (direction == 0) {
				totalCON += rng.nextInt(variance);
			} else {
				totalCON -= rng.nextInt(variance);
			}
			
			holisticCharacter.setCon(totalCON);
		}
	}
}
