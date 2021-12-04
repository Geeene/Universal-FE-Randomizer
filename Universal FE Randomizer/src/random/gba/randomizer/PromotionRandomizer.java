package random.gba.randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import ui.model.PromotionOptions.Mode;

public class PromotionRandomizer {
	static final int rngSalt = 879845164;

	public static void randomizePromotions(PromotionOptions options, PromotionDataLoader promotionData,
			ClassDataLoader classData, GameType type, Random rng) {
		if (options.promotionMode.equals(Mode.STRICT)) {
		}

		switch (type) {
		case FE6:
		case FE7:
			if (options.promotionMode.equals(Mode.STRICT)) {
				// Keep normal promotions, except letting Soldiers promote.
				Map<GBAFEClass, GBAFEClassData> classMap = getClassMapForGame(classData, type);
				GBAFEClassData soldierClassData = classMap.get(
						type.equals(GameType.FE6) ? FE6Data.CharacterClass.SOLDIER : FE7Data.CharacterClass.SOLDIER);
				soldierClassData.setTargetPromotionID(type.equals(GameType.FE6) ? FE6Data.CharacterClass.GENERAL.ID
						: FE7Data.CharacterClass.GENERAL.ID);
				break;
			}
			randomizePromotions(options, classData, type, rng);
			break;
		case FE8:
			if (options.promotionMode.equals(Mode.STRICT)) {
				Map<CharacterClass, PromotionBranch> promotionBranches = promotionData.getAllPromotionBranches();
				PromotionBranch soldierPromotions = promotionBranches.get(FE8Data.CharacterClass.SOLDIER);
				soldierPromotions.setFirstPromotion(FE8Data.CharacterClass.GENERAL.ID);
				soldierPromotions.setSecondPromotion(FE8Data.CharacterClass.PALADIN.ID);
				promotionBranches.put(FE8Data.CharacterClass.SOLDIER, soldierPromotions);
				break;
			}
			randomizePromotionsForFE8(options, promotionData, classData, rng);
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
			ClassDataLoader classData, Random rng) {
		Map<FE8Data.CharacterClass, PromotionBranch> promotionBranches = promotionData.getAllPromotionBranches();
		for (FE8Data.CharacterClass classToRandomize : FE8Data.CharacterClass.values()) {
			PoolDistributor<GBAFEClass> promotionDistributor = new PoolDistributor<>();
			List<GBAFEClass> promotions = getValidPromotionsForClass(options, classData, classToRandomize,
					GameType.FE8);
			promotionDistributor.addAll(promotions);
			if (promotions == null || promotions.isEmpty() || promotions.size() == 1) {
				continue;
			}
			if (!classToRandomize.isPromoted() && !classToRandomize.equals(FE8Data.CharacterClass.DANCER)) {
				PromotionBranch promotionBranch = promotionBranches.get(classToRandomize);
				GBAFEClass firstPromotion = promotionDistributor.getRandomItem(rng, true);
				GBAFEClass secondPromotion = promotionDistributor.getRandomItem(rng, true);

				// These classes could be equivalent (i.e. Swordmaster and Swordmaster_F), try
				// again.
				if (mightNeedReroll(firstPromotion, secondPromotion)) {
					secondPromotion = promotionDistributor.getRandomItem(rng, true);
				}

				promotionBranch.setFirstPromotion(firstPromotion.getID());
				promotionBranch.setSecondPromotion(secondPromotion.getID());
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
	 * Checks if the two given classes are either the exact same (e.g. SWORDMASTER
	 * and SWORDMASTER) or Equivalent (SWORDMASTER and SWORDMASTER_F)
	 */
	public static boolean mightNeedReroll(GBAFEClass firstPromotion, GBAFEClass secondPromotion) {
		String nameFirstPromo = firstPromotion.getName().endsWith("_F")
				? firstPromotion.getName().substring(0, firstPromotion.getName().length() - 2)
				: firstPromotion.getName();

		String nameSecondPromo = firstPromotion.getName().endsWith("_F")
				? firstPromotion.getName().substring(0, firstPromotion.getName().length() - 2)
				: firstPromotion.getName();

		return nameFirstPromo.equals(nameSecondPromo)
				&& Arrays.asList(-1, 0, 1).contains(firstPromotion.getID() - secondPromotion.getID());

	}

	/**
	 * For FE6 & FE7
	 * 
	 * Figures out valid promotions for each class based on the given options and
	 * sets their new promotions.
	 */
	public static void randomizePromotions(PromotionOptions options, ClassDataLoader classData, GameType type,
			Random rng) {
		final String REGEX_SHOULD_NOT_PROMOTE = "^[DANCER|BARD]";
		Set<GBAFEClass> unpromotedClasses = new HashSet<>(
				type.equals(GameType.FE6) ? FE6Data.CharacterClass.allUnpromotedClasses
						: FE7Data.CharacterClass.allUnpromotedClasses);

		// Get the classdata for all unpromoted classes that should promote (i.e. not
		// dancers)
		Map<GBAFEClass, GBAFEClassData> unpromotedClassDataMapping = getClassMapForGame(classData, type).entrySet()
				.stream()
				.filter(e -> unpromotedClasses.contains(e.getKey())
						|| e.getKey().getName().matches(REGEX_SHOULD_NOT_PROMOTE))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
		PoolDistributor<GBAFEClass> promotionDistributor = new PoolDistributor<>();
		promotionDistributor.addAll(getValidPromotionsForClass(options, classData, null, type));

		for (GBAFEClass unpromotedClass : unpromotedClassDataMapping.keySet()) {
			GBAFEClassData classToEdit = unpromotedClassDataMapping.get(unpromotedClass);
			classToEdit.setTargetPromotionID(promotionDistributor.getRandomItem(rng, false).getID());
		}
	}

	/**
	 * Given a character class returns all Classes that are valid promotions for the
	 * given promotion requirements.
	 */
	public static List<GBAFEClass> getValidPromotionsForClass(PromotionOptions options, ClassDataLoader classData,
			GBAFEClass baseClass, GameType type) {
		Map<GBAFEClass, GBAFEClassData> allClasses = getClassMapForGame(classData, type);
		List<GBAFEClass> validPromotions = allClasses.keySet().stream().filter(new Predicate<GBAFEClass>() {

			@Override
			public boolean test(GBAFEClass candidate) {
				Boolean isValid = true;

				isValid &= isCorrectTier(baseClass, candidate, type);

				if (options.promotionMode.equals(Mode.LOOSE) && !options.allowMountChanges) {
					// If unit is a flier they should stay a flier.
					isValid &= classData.isFlying(baseClass.getID()) == classData.isFlying(candidate.getID());
					// If unit is a non-flying mounted unit keep them as such.
					isValid &= (isMounted(baseClass, type) == isMounted(candidate, type));
				}

				if (options.promotionMode.equals(Mode.RANDOM) && options.requireCommonWeapon) {
					GBAFEClassData baseClassData = allClasses.get(baseClass);
					GBAFEClassData candidateData = allClasses.get(candidate);
					isValid &= hasAnyMatchingWeaponType(baseClassData, candidateData);
				} else if (options.promotionMode.equals(Mode.LOOSE)) {
					GBAFEClassData baseClassData = allClasses.get(baseClass);
					GBAFEClassData candidateData = allClasses.get(candidate);
					isValid &= canUseAllUnpromotedWeapons(baseClassData, candidateData);
				}

				if (options.promotionMode.equals(Mode.RANDOM) && options.keepSameDamageType) {
					GBAFEClassData baseClassData = allClasses.get(baseClass);
					GBAFEClassData candidateData = allClasses.get(candidate);
					isValid &= useSameDamageType(baseClassData, candidateData);
				}

				if (GameType.FE8.equals(type) && !options.allowMonsterClasses) {
					isValid &= !FE8Data.CharacterClass.allMonsterClasses.contains(candidate);
				}

				return isValid;
			}

		}).collect(Collectors.toList());

		return validPromotions;
	}

	/**
	 * Checks that both classes have a magical rank, since in GBAFE there are no
	 * mixed physical and special classes, we can just assume that both classes have
	 * the same magic attributes
	 */
	public static boolean useSameDamageType(GBAFEClassData baseClassData, GBAFEClassData candidateData) {
		boolean baseClassHasMagicRank = (baseClassData.getAnimaRank() + baseClassData.getDarkRank()
				+ baseClassData.getLightRank() + baseClassData.getStaffRank()) > 0;

		boolean candidateClassHasMagicRank = (candidateData.getAnimaRank() + candidateData.getDarkRank()
				+ candidateData.getLightRank() + candidateData.getStaffRank()) > 0;

		return baseClassHasMagicRank == candidateClassHasMagicRank;
	}

	public static boolean isMounted(GBAFEClass classToCheck, GameType type) {
		List<GBAFEClass> classesOfAttribute = new ArrayList<>();
		if (type.equals(GameType.FE6)) {
			classesOfAttribute.addAll(FE6Data.CharacterClass.allMountedClasses);
		} else if (type.equals(GameType.FE7)) {
			classesOfAttribute.addAll(FE7Data.CharacterClass.allMountedClasses);
		} else if (type.equals(GameType.FE8)) {
			classesOfAttribute.addAll(FE8Data.CharacterClass.allMountedClasses);
		}
		return classesOfAttribute.contains(classToCheck);
	}

	public static boolean hasAnyMatchingWeaponType(GBAFEClassData baseClassData, GBAFEClassData candidateData) {
		boolean ret = false;
		ret |= baseClassData.getSwordRank() <= candidateData.getSwordRank();
		ret |= baseClassData.getLanceRank() <= candidateData.getLanceRank();
		ret |= baseClassData.getAxeRank() <= candidateData.getAxeRank();
		ret |= baseClassData.getBowRank() <= candidateData.getBowRank();
		ret |= baseClassData.getDarkRank() <= candidateData.getDarkRank();
		ret |= baseClassData.getLightRank() <= candidateData.getLightRank();
		ret |= baseClassData.getAnimaRank() <= candidateData.getAnimaRank();
		ret |= baseClassData.getStaffRank() <= candidateData.getStaffRank();
		return ret;
	}

	/**
	 * Checks that a given promotion candidate class can use all the weapons of the
	 * unpromoted class (and more)
	 * 
	 * e.g. Mercenary -> Paladin = True Cavalier ->
	 * 
	 */
	public static boolean canUseAllUnpromotedWeapons(GBAFEClassData baseClassData, GBAFEClassData candidateData) {
		boolean ret = true;
		ret &= baseClassData.getSwordRank() <= candidateData.getSwordRank();
		ret &= baseClassData.getLanceRank() <= candidateData.getLanceRank();
		ret &= baseClassData.getAxeRank() <= candidateData.getAxeRank();
		ret &= baseClassData.getBowRank() <= candidateData.getBowRank();
		ret &= baseClassData.getDarkRank() <= candidateData.getDarkRank();
		ret &= baseClassData.getLightRank() <= candidateData.getLightRank();
		ret &= baseClassData.getAnimaRank() <= candidateData.getAnimaRank();
		ret &= baseClassData.getStaffRank() <= candidateData.getStaffRank();

		return ret;
	}

	public static boolean isCorrectTier(GBAFEClass baseClass, GBAFEClass candidate, GameType type) {
		if (GameType.FE8.equals(type)) {
			FE8Data.CharacterClass castedBaseClass = (FE8Data.CharacterClass) baseClass;
			// In the case of FE8 we could have a trainee class, which will promote to an
			// unpromoted class.
			return (castedBaseClass.isTrainee() && !candidate.isPromoted())
					|| (!castedBaseClass.isPromoted() && !castedBaseClass.isTrainee() && candidate.isPromoted());
		}
		return !baseClass.isPromoted() && candidate.isPromoted();
	}

	/**
	 * Builds a map between the FE6/7/8Data.CharacterClass Enum and the actual game
	 * data, to figure out what are valid promotions for the Data.CharacterClass
	 */
	public static Map<GBAFEClass, GBAFEClassData> getClassMapForGame(ClassDataLoader classData, GameType type) {
		HashMap<Integer, GBAFEClassData> classes = classData.getClassMap();
		HashMap<GBAFEClass, GBAFEClassData> ret = new HashMap<>();
		switch (type) {
		case FE6:
			Arrays.asList(FE6Data.CharacterClass.values()).stream().forEach(c -> ret.put(c, classes.get(c.ID)));
			break;
		case FE7:
			Arrays.asList(FE7Data.CharacterClass.values()).stream().forEach(c -> ret.put(c, classes.get(c.ID)));
			break;
		case FE8:
			Arrays.asList(FE8Data.CharacterClass.values()).stream().forEach(c -> ret.put(c, classes.get(c.ID)));
			break;
		default:
			// Do Nothing.
		}
		return ret;
	}
}
