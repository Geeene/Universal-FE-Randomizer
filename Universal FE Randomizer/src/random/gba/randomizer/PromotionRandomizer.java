package random.gba.randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fedata.gba.GBAFEClassData;
import fedata.gba.fe6.FE6Data;
import fedata.gba.fe7.FE7Data;
import fedata.gba.fe8.FE8Data;
import fedata.gba.fe8.FE8Data.CharacterClass;
import fedata.gba.fe8.PromotionBranch;
import fedata.gba.general.GBAFEClass;
import fedata.general.FEBase.GameType;
import random.gba.loader.ClassDataLoader;
import random.gba.loader.PromotionDataLoader;
import random.general.PoolDistributor;
import ui.model.PromotionOptions;

public class PromotionRandomizer {
	static final int rngSalt = 879845164;

	public static void randomizePromotions(PromotionOptions options, PromotionDataLoader promotionData,
			ClassDataLoader classData, GameType type, Random rng) {
		switch (type) {
		case FE6:
			randomizePromotions(options, classData, type, rng);
			break;
		case FE7:
			randomizePromotions(options, classData, type, rng);
			break;
		case FE8:
			randomizePromotionsForFE8(options, promotionData, rng);
			break;
		default:
			return;
		}

	}

	/**
	 * Due to branched Promotions FE8 needs to be handled much different from FE6
	 * and FE7.
	 */
	public static void randomizePromotionsForFE8(PromotionOptions options, PromotionDataLoader promotionData,
			Random rng) {
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
				Set<CharacterClass> secondStageClasses = FE8Data.CharacterClass.allPromotedClasses.stream()
						.filter(c -> FE8Data.CharacterClass.allValidClasses.contains(c)).collect(Collectors.toSet());
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

	public static void randomizePromotions(PromotionOptions options, ClassDataLoader classData, GameType type,
			Random rng) {

		Map<GBAFEClass, GBAFEClassData> unpromotedClasses = getClassMapForGame(classData, type).entrySet().stream()
				.filter(e -> !e.getKey().isPromoted())
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

		List<GBAFEClass> availablePromotedClasses = new ArrayList<>(
				type.equals(GameType.FE6) ? FE6Data.CharacterClass.allPromotedClasses
						: FE7Data.CharacterClass.allPromotedClasses);
		List<GBAFEClassData> allClasses = Arrays.asList(classData.allClasses());
		PoolDistributor<GBAFEClass> promotionDistributor = new PoolDistributor<>();
		promotionDistributor.addAll(availablePromotedClasses);

		for (GBAFEClass unpromotedClass : unpromotedClasses.keySet()) {
			GBAFEClassData classToEdit = unpromotedClasses.get(unpromotedClass);
			classToEdit.setTargetPromotionID(promotionDistributor.getRandomItem(rng, false).getID());
		}
	}

	/**
	 * Given a character class returns all Classes that are valid promotions for the
	 * given promotion requirements.
	 */
	public static Set<CharacterClass> getValidPromotionsForClass(PromotionOptions options, ClassDataLoader classData,
			CharacterClass baseClass, GameType type) {
		Map<GBAFEClass, GBAFEClassData> allPromotions = getClassMapForGame(classData, type);
		allPromotions.keySet().stream().filter(new Predicate<GBAFEClass>() {

			@Override
			public boolean test(GBAFEClass characterClass) {
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

	public static Map<GBAFEClass, GBAFEClassData> getClassMapForGame(ClassDataLoader classData, GameType type) {
		HashMap<Integer, GBAFEClassData> classes = new HashMap<>();
		HashMap<GBAFEClass, GBAFEClassData> ret = new HashMap<>();
		switch (type) {
		case FE6:
			Arrays.asList(classData.allClasses()).stream().forEach(c -> classes.put(c.getID(), c));
			Arrays.asList(FE6Data.CharacterClass.values()).stream().forEach(c -> ret.put(c, classes.get(c.ID)));
			break;
		case FE7:
			Arrays.asList(classData.allClasses()).stream().forEach(c -> classes.put(c.getID(), c));
			Arrays.asList(FE6Data.CharacterClass.values()).stream().forEach(c -> ret.put(c, classes.get(c.ID)));
			break;
		default:
			// Do Nothing.
		}
		return new HashMap<>();
	}
}
