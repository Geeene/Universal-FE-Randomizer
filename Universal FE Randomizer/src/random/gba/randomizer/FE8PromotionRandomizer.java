package random.gba.randomizer;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import fedata.gba.fe8.FE8Data;
import fedata.gba.fe8.FE8Data.CharacterClass;
import fedata.gba.fe8.PromotionBranch;
import random.gba.loader.PromotionDataLoader;
import random.general.PoolDistributor;
import ui.model.PromotionOptions;

public class FE8PromotionRandomizer {
	static final int rngSalt = 879845164;

	public static void randomizePromotions(PromotionOptions options, PromotionDataLoader promotionData, Random rng) {
		Map<FE8Data.CharacterClass, PromotionBranch> promotionBranches = promotionData.getAllPromotionBranches();
		for (FE8Data.CharacterClass classToRandomize : FE8Data.CharacterClass.values()) {
			PoolDistributor<CharacterClass> promotionDistributor = new PoolDistributor<>();
			if (classToRandomize.isTrainee()) {
				Set<CharacterClass> firstStageClasses = FE8Data.CharacterClass.allUnpromotedClasses;
				promotionDistributor.addAll(firstStageClasses);
				PromotionBranch promotionBranch = promotionBranches.get(classToRandomize);
				promotionBranch.setFirstPromotion(promotionDistributor.getRandomItem(rng, true).getID());
				promotionBranch.setSecondPromotion(promotionDistributor.getRandomItem(rng, true).getID());
				promotionBranches.put(classToRandomize, promotionBranch);
			} else if (FE8Data.CharacterClass.allUnpromotedClasses.contains(classToRandomize)) {
				Set<CharacterClass> secondStageClasses = FE8Data.CharacterClass.allPromotedClasses;
				promotionDistributor.addAll(secondStageClasses);
				PromotionBranch promotionBranch = promotionBranches.get(classToRandomize);
				promotionBranch.setFirstPromotion(promotionDistributor.getRandomItem(rng, true).getID());
				promotionBranch.setSecondPromotion(promotionDistributor.getRandomItem(rng, true).getID());
				promotionBranches.put(classToRandomize, promotionBranch);
			} else {
				PromotionBranch promotionBranch = promotionBranches.get(classToRandomize);
				promotionBranch.setFirstPromotion(0);
				promotionBranch.setSecondPromotion(0);
				promotionBranches.put(classToRandomize, promotionBranch);
			}
		}
	}

	/**
	 * Given a character class returns all Classes that are valid promotions for the
	 * given promotion requirements.
	 */
	public static Set<CharacterClass> getValidPromotionsForClass(PromotionOptions options, CharacterClass baseClass,
			Set<CharacterClass> allPromotions) {
		allPromotions.stream().filter(new Predicate<CharacterClass>() {

			@Override
			public boolean test(CharacterClass characterClass) {
				Boolean isValid = true;
				if (!options.allowMountChanges) {
					isValid &= (FE8Data.CharacterClass.flyingClasses
							.contains(characterClass) == FE8Data.CharacterClass.flyingClasses.contains(baseClass));
					isValid &= (FE8Data.CharacterClass.mountedClasses
							.contains(characterClass) == FE8Data.CharacterClass.mountedClasses.contains(baseClass));
				}

				if (options.requireCommonWeapon) {

				}

				return true;
			}

		});

		return null;
	}
}
