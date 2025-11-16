package random.gba.randomizer;

import java.util.Random;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.general.FEBase;
import random.gba.loader.GBADataLoaders;
import util.OptionRecorder;

public class CharacterRandomizer extends AbstractGBARandomizerComponent {
	
	public static int rngSalt = 9002;

    public CharacterRandomizer(OptionRecorder.GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, FEBase.GameType type) {
        super(allOptions, dataLoaders, rng, type);
    }

    public void randomizeAffinity() {
		GBAFECharacterData[] playableCharacters = charData.playableCharacters();
		int[] values = charData.validAffinityValues();
		for (GBAFECharacterData character : playableCharacters) {
			int affinity = values[rng.nextInt(values.length)];
			character.setAffinityValue(affinity);
		}
	}
	
	public void randomizeConstitution() {
		GBAFECharacterData[] allPlayableCharacters = charData.playableCharacters();
		for (GBAFECharacterData character : allPlayableCharacters) {
			GBAFEClassData currentClass = classData.classForID(character.getClassID());
			int classCON = currentClass.getCON();
			int personalCON = character.getConstitution();
			int totalCON = classCON + personalCON;
			
			int newCON = totalCON;
			
			int direction = rng.nextInt(2);
			if (direction == 0) {
				newCON += rng.nextInt(otherCharacterOptions.constitutionOptions.variance);
			} else {
				newCON -= rng.nextInt(otherCharacterOptions.constitutionOptions.variance);
			}
			
			newCON = Math.max(otherCharacterOptions.constitutionOptions.minValue, newCON);
			
			int newPersonalCON = newCON - classCON;
			
			character.setConstitution(newPersonalCON);
		}
	}
}
