package random.gba.randomizer;

import java.util.*;
import java.util.stream.Collectors;

import fedata.gba.GBAFEChapterData;
import fedata.gba.GBAFEChapterItemData;
import fedata.gba.GBAFEChapterUnitData;
import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEItemData;
import fedata.gba.general.WeaponRanks;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase.GameType;
import random.gba.loader.*;
import random.gba.randomizer.service.GBASlotAdjustmentService;
import random.gba.randomizer.service.ItemAssignmentService;
import random.general.PoolDistributor;
import random.general.RelativeValueMapper;
import ui.model.ClassOptions;
import ui.model.ItemAssignmentOptions;
import ui.model.ClassOptions.GenderRestrictionOption;
import ui.model.ItemAssignmentOptions.WeaponReplacementPolicy;
import util.DebugPrinter;
import util.OptionRecorder;
import util.OptionRecorder.GBAOptionBundle;

public class ClassRandomizer extends AbstractGBARandomizerComponent{

	static final int rngSalt = 874;

    public ClassRandomizer(GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, GameType type) {
        super(allOptions, dataLoaders, rng, type);
    }

    public void randomizeClassMovement() {
		GBAFEClassData[] allClasses = classData.allClasses();
		List<GBAFEClassData> unpromotedClasses = Arrays.asList(allClasses).stream()
				.filter(currentClass -> classData.isPromotedClass(currentClass.getID()) == false)
				.sorted(GBAFEClassData.defaultComparator)
				.collect(Collectors.toList());
        int maxMOV = otherCharacterOptions.movementOptions.maxValue;
        int minMOV = otherCharacterOptions.movementOptions.minValue;
		for (GBAFEClassData currentClass : unpromotedClasses) {
			if (currentClass.getMOV() > 0) {
				// #259: Allow for maximum provided in UI
				// Fringe benefit of allowing (min == max), i.e. every class has the same MOV
				int randomMOV = rng.nextInt(maxMOV - minMOV + 1) + minMOV;
				currentClass.setMOV(randomMOV);
			}
		}

		// Make sure all promoted classes have at least their base class's MOV so you can never lose MOV from promotion.
		List<GBAFEClassData> promotedClasses = Arrays.asList(allClasses).stream()
				.filter(currentClass -> classData.isPromotedClass(currentClass.getID()))
				.sorted(GBAFEClassData.defaultComparator)
				.collect(Collectors.toList());
		for (GBAFEClassData currentClass : promotedClasses) {
			List<GBAFEClassData> unpromoted = classData.demotionOptions(currentClass.getID());
			int highestUnpromotedMOV = 0;
			for (GBAFEClassData charClass : unpromoted) { highestUnpromotedMOV = Math.max(highestUnpromotedMOV, charClass.getMOV()); }
			if (highestUnpromotedMOV > 0) {
				int randomMOV = rng.nextInt(maxMOV - highestUnpromotedMOV + 1) + highestUnpromotedMOV;
				currentClass.setMOV(randomMOV);
			}
		}
	}

	public void randomizePlayableCharacterClasses() {
        ClassOptions options = allOptions.classes;
        ItemAssignmentOptions itemAssignmentOptions = allOptions.itemAssignmentOptions;
		GBAFECharacterData[] allPlayableCharacters = charData.playableCharacters();
		Map<Integer, GBAFEClassData> determinedClasses = new HashMap<Integer, GBAFEClassData>();

		Boolean includeLords = options.includeLords;
		Boolean includeThieves = options.includeThieves;
		Boolean includeSpecial = options.includeSpecial;
		Boolean hasMonsters = false;
		Boolean separateMonsters = false;

		Boolean forceChange = options.forceChange;

		if (type == GameType.FE8) {
			hasMonsters = true;
			separateMonsters = options.separateMonsters;
		}

		PoolDistributor<GBAFEClassData> classDistributor = new PoolDistributor<GBAFEClassData>();
		Arrays.asList(classData.allClasses()).stream().forEach(charClass -> {
			classDistributor.addItem(charClass);
		});

		for (GBAFECharacterData character : allPlayableCharacters) {

			Boolean isLordCharacter = charData.isLordCharacterID(character.getID());
			Boolean isThiefCharacter = charData.isThiefCharacterID(character.getID());
			Boolean isSpecialCharacter = charData.isSpecialCharacterID(character.getID());
			Boolean canChange = allOptions.raceMode || charData.canChangeCharacterID(character.getID());

			if (isLordCharacter && !includeLords) { continue; }
			if (isThiefCharacter && !includeThieves) { continue; }
			if (isSpecialCharacter && !includeSpecial) { continue; }
			if (!canChange) { continue; }

			Boolean characterRequiresRange = charData.characterIDRequiresRange(character.getID());
			Boolean characterRequiresMelee = charData.characterIDRequiresMelee(character.getID());

			int originalClassID = character.getClassID();
			GBAFEClassData originalClass = classData.classForID(originalClassID);

			GBAFEClassData targetClass = null;

			boolean isFemale = charData.isFemale(character.getID());

			if (determinedClasses.containsKey(character.getID())) {
				continue;
			} else {
				GBAFEClassData[] possibleClasses = hasMonsters ? classData.potentialClasses(originalClass, charData.isEnemyAtAnyPoint(character.getID()), !includeLords, !includeThieves, !includeSpecial, separateMonsters, forceChange, isLordCharacter, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), options.genderOption, null) :
						classData.potentialClasses(originalClass, charData.isEnemyAtAnyPoint(character.getID()), !includeLords, !includeThieves, !includeSpecial, forceChange, isLordCharacter, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), options.genderOption, null);
				if (possibleClasses.length == 0) {
					continue;
				}

				if (options.assignEvenly) {
					Set<GBAFEClassData> classSet = new HashSet<GBAFEClassData>(Arrays.asList(possibleClasses));
					if (Collections.disjoint(classDistributor.possibleResults(), classSet)) {
						Arrays.asList(classData.allClasses()).stream().forEach(charClass -> {
							classDistributor.addItem(charClass);
						});
					}
					classSet.retainAll(classDistributor.possibleResults());
					List<GBAFEClassData> classList = classSet.stream().sorted(GBAFEClassData.defaultComparator).collect(Collectors.toList());
					PoolDistributor<GBAFEClassData> pool = new PoolDistributor<GBAFEClassData>();
					for (GBAFEClassData charClass : classList) {
						pool.addItem(charClass, classDistributor.itemCount(charClass));
					}
					targetClass = pool.getRandomItem(rng, true);
					classDistributor.removeItem(targetClass, false);
				} else {
					int randomIndex = rng.nextInt(possibleClasses.length);
					targetClass = possibleClasses[randomIndex];
				}

				if (options.genderOption == GenderRestrictionOption.LOOSE) {
					if (isFemale) {
						targetClass = classData.correspondingFemaleClass(targetClass);
					} else {
						targetClass = classData.correspondingMaleClass(targetClass);
					}
				}
			}

			if (targetClass == null) {
				continue;
			}

			DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Assigning character 0x" + Integer.toHexString(character.getID()).toUpperCase() + " (" + textData.getStringAtIndex(character.getNameIndex(), true) + ") to class 0x" + Integer.toHexString(targetClass.getID()) + " (" + textData.getStringAtIndex(targetClass.getNameIndex(), true) + ")");

			for (GBAFECharacterData linked : charData.linkedCharactersForCharacter(character)) {
				determinedClasses.put(linked.getID(), targetClass);
				updateCharacterToClass(linked, originalClass, targetClass, characterRequiresRange, characterRequiresMelee, false);
				linked.setIsLord(isLordCharacter);
			}
		}
	}

	public void randomizeBossCharacterClasses() {
		GBAFECharacterData[] allBossCharacters = charData.bossCharacters();

		Boolean includeLords = false;
		Boolean includeThieves = false;
		Boolean includeSpecial = false;
		Boolean hasMonsters = false;
		Boolean separateMonsters = false;
		Boolean forceChange = classes.forceChange;
		if (type == GameType.FE8) {
			hasMonsters = true;
			separateMonsters = classes.separateMonsters;
		}

		Map<Integer, GBAFEClassData> determinedClasses = new HashMap<Integer, GBAFEClassData>();

		for (GBAFECharacterData character : allBossCharacters) {

			Boolean canChange = charData.canChangeCharacterID(character.getID());
			if (!canChange) { continue; }

			Boolean characterRequiresRange = charData.characterIDRequiresRange(character.getID());
			Boolean characterRequiresMelee = charData.characterIDRequiresMelee(character.getID());

			int originalClassID = character.getClassID();
			GBAFEClassData originalClass = classData.classForID(originalClassID);
			if (originalClass == null) {
				System.err.println("Invalid Class found: Class ID = " + Integer.toHexString(originalClassID));
				continue;
			}

			GBAFEClassData targetClass = null;

			Boolean forceBasicWeaponry = false;
			Boolean shouldNerf = false;

			boolean isFemale = charData.isFemale(character.getID());

			if (determinedClasses.containsKey(character.getID())) {
				continue;
			} else {
				GBAFECharacterData mustLoseToCharacter = charData.characterRequiresCounterToCharacter(character);
				GBAFEClassData mustLoseToClass = null;
				if (mustLoseToCharacter != null) {
					mustLoseToClass = classData.classForID(mustLoseToCharacter.getClassID());
					forceBasicWeaponry = true;
					shouldNerf = true;
				}

				GBAFEClassData[] possibleClasses = hasMonsters ?
						classData.potentialClasses(originalClass, true, !includeLords, !includeThieves, !includeSpecial, separateMonsters, forceChange, true, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), classes.genderOption, mustLoseToClass) :
						classData.potentialClasses(originalClass, true, !includeLords, !includeThieves, !includeSpecial, forceChange, true, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), classes.genderOption, mustLoseToClass);
				if (possibleClasses.length == 0) {
					continue;
				}

				int randomIndex = rng.nextInt(possibleClasses.length);
				targetClass = possibleClasses[randomIndex];
			}

			if (classes.genderOption == GenderRestrictionOption.LOOSE) {
				if (isFemale) {
					targetClass = classData.correspondingFemaleClass(targetClass);
				} else {
					targetClass = classData.correspondingMaleClass(targetClass);
				}
			}

			if (targetClass == null) {
				continue;
			}

			for (GBAFECharacterData linked : charData.linkedCharactersForCharacter(character)) {
				determinedClasses.put(linked.getID(), targetClass);
				updateCharacterToClass(linked, originalClass, targetClass, characterRequiresRange, characterRequiresMelee, forceBasicWeaponry && linked.getID() == character.getID());
				if (shouldNerf) { // Halve skill, speed, defense, and resistance if we need to make sure he loses to us.
					linked.setBaseSKL(linked.getBaseSKL() >> 1);
					linked.setBaseSPD(linked.getBaseSPD() >> 1);
					linked.setBaseDEF(linked.getBaseDEF() >> 1);
					linked.setBaseRES(linked.getBaseRES() >> 1);
				}
			}
		}
	}

	public void randomizeMinionClasses() {
		Boolean includeLords = false;
		Boolean includeThieves = false;
		Boolean includeSpecial = false;
		Boolean hasMonsters = false;
		Boolean separateMonsters = false;
		Boolean forceChange = classes.forceChange;
		if (type == GameType.FE8) {
			hasMonsters = true;
			separateMonsters = classes.separateMonsters;
		}

		// Before we start, make all classes naturally have A rank so that weapons can transfer more easily.
		// Somehow, some enemies, despite all signs of them only being able to use up to C rank weapons,
		// are able to use A rank somehow in some cases. Since I don't know why this is,
		// we're going to modify all classes to have A rank in all areas. Characters with lower ranks will override it
		// which includes all playable characters.
		for (GBAFEClassData charClass : classData.allClasses()) {
			if (charClass.getSwordRank() > 0) { charClass.setSwordRank(WeaponRank.A); }
			if (charClass.getLanceRank() > 0) { charClass.setLanceRank(WeaponRank.A); }
			if (charClass.getAxeRank() > 0) { charClass.setAxeRank(WeaponRank.A); }
			if (charClass.getBowRank() > 0) { charClass.setBowRank(WeaponRank.A); }
			if (charClass.getAnimaRank() > 0) { charClass.setAnimaRank(WeaponRank.A); }
			if (charClass.getLightRank() > 0) { charClass.setLightRank(WeaponRank.A); }
			if (charClass.getDarkRank() > 0) { charClass.setDarkRank(WeaponRank.A); }
			if (charClass.getStaffRank() > 0) { charClass.setStaffRank(WeaponRank.A); }
		}

		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			int maxEnemyClassLimit = chapter.getMaxEnemyClassLimit();
			// There's really four slots we need to reserve.
			// Unpromoted land unit
			// Promoted land unit
			// Unpromoted flying unit
			// Promoted flying unit
			// If we have all of these, we can guarantee a replacement if we run into the limit.

			List<GBAFEClassData> unpromotedLandUnit = new ArrayList<GBAFEClassData>();
			List<GBAFEClassData> promotedLandUnit = new ArrayList<GBAFEClassData>();
			List<GBAFEClassData> unpromotedFlyingUnit = new ArrayList<GBAFEClassData>();
			List<GBAFEClassData> promotedFlyingUnit = new ArrayList<GBAFEClassData>();

			Map<GBAFEClassData, List<GBAFEChapterUnitData>> selectedClasses = new HashMap<GBAFEClassData, List<GBAFEChapterUnitData>>();
			GBAFECharacterData lordCharacter = charData.characterWithID(chapter.lordLeaderID());
			GBAFEClassData lordClass = classData.classForID(lordCharacter.getClassID());
			for (GBAFEChapterUnitData chapterUnit : chapter.allUnits()) {
				// int leaderID = chapterUnit.getLeaderID();
				int characterID = chapterUnit.getCharacterNumber();
				int classID = chapterUnit.getStartingClass();
				// It's safe to check for boss leader ID in the case of FE7, but FE6 tends to put other IDs there (kind of like squad captains).
				// We're going to remove this safety check in the meantime, but we should be wary of any accidental changes.
				// Also check to make sure it's not any character we definitely don't want to change.
				// Finally, also make sure the starting class is valid. Classes we don't recognize, we shouldn't touch.
				if (!charData.isBossCharacterID(characterID) && /*charData.isBossCharacterID(leaderID) &&*/ !charData.isPlayableCharacterID(characterID) &&
						charData.canChangeCharacterID(characterID) && classData.isValidClass(classID)) {

					GBAFEClassData originalClass = classData.classForID(classID);
					if (originalClass == null) {
						continue;
					}

					if (classData.isThief(originalClass.getID())) {
						continue;
					}

					GBAFECharacterData minionCharacterData = charData.minionCharacterWithID(characterID);
					if (minionCharacterData == null) {
						continue;
					}

					GBAFEClassData targetClass = null;
					boolean characterHasWeaponRanks = !itemData.ranksForCharacter(minionCharacterData, null).getTypes().isEmpty();

					// If he's been modified already, we use the same class.
					// Otherwise, we randomize the class.
					if (characterHasWeaponRanks) {
						if (minionCharacterData.wasModified()) {
							targetClass = classData.classForID(minionCharacterData.getClassID());
						}
					}

					if (targetClass != null) {
						updateMinionToClass(chapterUnit, minionCharacterData, targetClass);
					} else {
						Boolean shouldRestrictToSafeClasses = !chapter.isClassSafe();
						Boolean shouldMakeEasy = chapter.shouldBeSimplified();
						GBAFEClassData loseToClass = shouldMakeEasy ? lordClass : null;
						GBAFEClassData[] possibleClasses = hasMonsters ?
								classData.potentialClasses(originalClass, true, !includeLords, !includeThieves, !includeSpecial, separateMonsters, forceChange, true, false, false, shouldRestrictToSafeClasses, classes.genderOption, loseToClass) :
								classData.potentialClasses(originalClass, true, false, false, false, forceChange, true, false, false, shouldRestrictToSafeClasses, classes.genderOption, loseToClass);
						if (possibleClasses.length == 0) {
							continue;
						}

						if (maxEnemyClassLimit > 0) {
							int numberOfSlotsNeededToFill = 4;
							if (!promotedFlyingUnit.isEmpty()) { numberOfSlotsNeededToFill--; }
							if (!unpromotedFlyingUnit.isEmpty()) { numberOfSlotsNeededToFill--; }
							if (!promotedLandUnit.isEmpty()) { numberOfSlotsNeededToFill--; }
							if (!unpromotedLandUnit.isEmpty()) { numberOfSlotsNeededToFill--; }

							if (selectedClasses.size() >= maxEnemyClassLimit - numberOfSlotsNeededToFill) {
								// We've reached the maximum limit. Reuse one of the classes we've already assigned.
								boolean isPromoted = classData.isPromotedClass(originalClass.getID());
								boolean isFlying = classData.isFlying(originalClass.getID());

								if (isPromoted && isFlying && !promotedFlyingUnit.isEmpty()) {
									targetClass = promotedFlyingUnit.get(rng.nextInt(promotedFlyingUnit.size()));
								} else if (isPromoted && !isFlying && !promotedLandUnit.isEmpty()) {
									// A land unit can be subbed with a flying unit.
									targetClass = promotedLandUnit.get(rng.nextInt(promotedLandUnit.size()));
								} else if (!isPromoted && isFlying && !unpromotedFlyingUnit.isEmpty()) {
									targetClass = unpromotedFlyingUnit.get(rng.nextInt(unpromotedFlyingUnit.size()));
								} else if (!isPromoted && !isFlying && !unpromotedLandUnit.isEmpty()) {
									// A land unit can be subbed with a flying unit too.
									targetClass = unpromotedLandUnit.get(rng.nextInt(unpromotedLandUnit.size()));
								}
							}
						}

						if (targetClass == null) {
							int randomIndex = rng.nextInt(possibleClasses.length);
							targetClass = possibleClasses[randomIndex];


							if (classData.isFlying(originalClass.getID()) == false && classData.isFlying(targetClass.getID())) {
								// If this is a new flier, roll one more time.
								// Reduce the number of non-flying minions that become fliers.
								randomIndex = rng.nextInt(possibleClasses.length);
								targetClass = possibleClasses[randomIndex];
							}

							// If we have a class limit, don't allow any non-flying unit to be flying.
							if (maxEnemyClassLimit > 0) {
								while (classData.isFlying(targetClass.getID()) && !classData.isFlying(originalClass.getID())) {
									randomIndex = rng.nextInt(possibleClasses.length);
									targetClass = possibleClasses[randomIndex];
								}
							}
						}

						if (characterHasWeaponRanks) {
							updateMinionCharacterToClass(chapterUnit, minionCharacterData, originalClass, targetClass);
						} else {
							updateMinionToClass(chapterUnit, minionCharacterData, targetClass);
						}

						if (classData.isPromotedClass(targetClass.getID())) {
							if (classData.isFlying(targetClass.getID())) {
								promotedFlyingUnit.add(targetClass);
							} else {
								promotedLandUnit.add(targetClass);
							}
						} else {
							if (classData.isFlying(targetClass.getID())) {
								unpromotedFlyingUnit.add(targetClass);
							} else {
								unpromotedLandUnit.add(targetClass);
							}
						}

						List<GBAFEChapterUnitData> unitsInClass = selectedClasses.get(targetClass);
						if (unitsInClass == null) {
							unitsInClass = new ArrayList<GBAFEChapterUnitData>();
							selectedClasses.put(targetClass, unitsInClass);
						}

						unitsInClass.add(chapterUnit);
					}
				}
			}
		}
	}

	private void updateCharacterToClass(GBAFECharacterData character, GBAFEClassData sourceClass, GBAFEClassData targetClass, Boolean ranged, Boolean melee, Boolean forceBasicWeapons) {

		character.prepareForClassRandomization();
		character.setClassID(targetClass.getID());
		if (charData.isBossCharacterID(character.getID())) {
			transferBossWeaponLevels(character, sourceClass, targetClass);
		} else {
			GBASlotAdjustmentService.transferWeaponRanks(character, sourceClass, targetClass, rng);
		}
		switch (classes.basesTransfer) {
			case ADJUST_TO_MATCH:
				applyBaseCorrectionForCharacter(character, sourceClass, targetClass);
				break;
			case NO_CHANGE:
				// We need to make sure nobody underflows, so keep an eye out for negative personal bases.
				if (character.getBaseHP() + targetClass.getBaseHP() < 0) { character.setBaseHP(-1 * targetClass.getBaseHP() + 1); } // Should always have at least 1 HP.
				if (character.getBaseSTR() + targetClass.getBaseSTR() < 0) { character.setBaseSTR(-1 * targetClass.getBaseSTR()); }
				if (character.getBaseSKL() + targetClass.getBaseSKL() < 0) { character.setBaseSKL(-1 * targetClass.getBaseSKL()); }
				if (character.getBaseSPD() + targetClass.getBaseSPD() < 0) { character.setBaseSPD(-1 * targetClass.getBaseSPD()); }
				if (character.getBaseDEF() + targetClass.getBaseDEF() < 0) { character.setBaseDEF(-1 * targetClass.getBaseDEF()); }
				if (character.getBaseRES() + targetClass.getBaseRES() < 0) { character.setBaseRES(-1 * targetClass.getBaseRES()); }
				if (character.getBaseLCK() + targetClass.getBaseLCK() < 0) { character.setBaseLCK(-1 * targetClass.getBaseLCK()); }
				break;
			case ADJUST_TO_CLASS:
				adjustBasesToMatchClass(character, sourceClass, targetClass);
				break;
		}

		switch (classes.growthOptions) {
			case TRANSFER_PERSONAL_GROWTHS:
				int hpOffset = character.getHPGrowth() - sourceClass.getHPGrowth();
				int strOffset = character.getSTRGrowth() - sourceClass.getSTRGrowth();
				int sklOffset = character.getSKLGrowth() - sourceClass.getSKLGrowth();
				int spdOffset = character.getSPDGrowth() - sourceClass.getSPDGrowth();
				int lckOffset = character.getLCKGrowth() - sourceClass.getLCKGrowth();
				int defOffset = character.getDEFGrowth() - sourceClass.getDEFGrowth();
				int resOffset = character.getRESGrowth() - sourceClass.getRESGrowth();

				character.setHPGrowth(Math.max(0, targetClass.getHPGrowth() + hpOffset));
				character.setSTRGrowth(Math.max(0, targetClass.getSTRGrowth() + strOffset));
				character.setSKLGrowth(Math.max(0, targetClass.getSKLGrowth() + sklOffset));
				character.setSPDGrowth(Math.max(0, targetClass.getSPDGrowth() + spdOffset));
				character.setLCKGrowth(Math.max(0, targetClass.getLCKGrowth() + lckOffset));
				character.setDEFGrowth(Math.max(0, targetClass.getDEFGrowth() + defOffset));
				character.setRESGrowth(Math.max(0, targetClass.getRESGrowth() + resOffset));
				break;
			case CLASS_RELATIVE_GROWTHS:
				adjustGrowthsToMatchClass(character, sourceClass, targetClass);
				break;
			default:
				break;
		}

		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			GBAFEChapterItemData reward = chapter.chapterItemGivenToCharacter(character.getID());
			if (reward != null) {
				GBAFEItemData item = itemData.getRandomWeaponForCharacter(character, ranged, melee, false, itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);

				// If this character has a prf weapon, use that instead.
				GBAFEItemData[] prfWeapons = itemData.prfWeaponsForClass(targetClass.getID());
				if (prfWeapons.length > 0) {
					item = prfWeapons[rng.nextInt(prfWeapons.length)];
				}
				reward.setItemID(item.getID());
			}

			for (GBAFEChapterUnitData chapterUnit : chapter.allUnits()) {
				if (chapterUnit.getCharacterNumber() == character.getID()) {
					if (chapterUnit.getStartingClass() != sourceClass.getID()) {
						System.err.println("Class mismatch for character with ID " + character.getID() + ". Expected Class " + sourceClass.getID() + " but found " + chapterUnit.getStartingClass());
						if (!classData.isValidClass(chapterUnit.getStartingClass()) && chapterUnit.getStartingClass() != 0) {
							System.err.println("Invalid class detected. Skipping class change for " + charData.debugStringForCharacter(character.getID()) + ". Invalid class: " + classData.debugStringForClass(chapterUnit.getStartingClass()));
							continue;
						}
					}
					chapterUnit.setStartingClass(targetClass.getID());
                    ItemAssignmentService.instance.validateCharacterInventory(character, targetClass, chapterUnit, ranged, melee, forceBasicWeapons);
					if (classData.isThief(sourceClass.getID())) {
                        ItemAssignmentService.instance.validateFormerThiefInventory(chapterUnit);
					}
					ItemAssignmentService.instance.validateSpecialClassInventory(chapterUnit);
				}
			}
		}
	}

	private void applyBaseCorrectionForCharacter(GBAFECharacterData character, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		int hpDelta = sourceClass.getBaseHP() - targetClass.getBaseHP();
		character.setBaseHP(character.getBaseHP() + hpDelta);
		int strDelta = sourceClass.getBaseSTR() - targetClass.getBaseSTR();
		character.setBaseSTR(character.getBaseSTR() + strDelta);
		int sklDelta = sourceClass.getBaseSKL() - targetClass.getBaseSKL();
		character.setBaseSKL(character.getBaseSKL() + sklDelta);
		int spdDelta = sourceClass.getBaseSPD() - targetClass.getBaseSPD();
		character.setBaseSPD(character.getBaseSPD() + spdDelta);
		int defDelta = sourceClass.getBaseDEF() - targetClass.getBaseDEF();
		character.setBaseDEF(character.getBaseDEF() + defDelta);
		int resDelta = sourceClass.getBaseRES() - targetClass.getBaseRES();
		character.setBaseRES(character.getBaseRES() + resDelta);
		int lckDelta = sourceClass.getBaseLCK() - targetClass.getBaseLCK();
		character.setBaseLCK(character.getBaseLCK() + lckDelta);

		// Only correct CON if it ends up being an invalid (i.e. negative) CON.
		// This is only really possible if the character had a negative CON adjustment to begin with.
		if (character.getConstitution() < 0 && Math.abs(character.getConstitution()) > targetClass.getCON()) {
			character.setConstitution(-1 * targetClass.getCON());
		}
	}

	private void adjustBasesToMatchClass(GBAFECharacterData character, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		// HP transfers directly, as does LCK.
		int hpDelta = sourceClass.getBaseHP() - targetClass.getBaseHP();
		character.setBaseHP(character.getBaseHP() + hpDelta);
		int lckDelta = sourceClass.getBaseLCK() - targetClass.getBaseLCK();
		character.setBaseLCK(character.getBaseLCK() + lckDelta);

		// STR, SKL, SPD, DEF, and RES are transfered based on which one is highest on the target class.
		int effectiveSTR = character.getBaseSTR() + sourceClass.getBaseSTR();
		int effectiveSKL = character.getBaseSKL() + sourceClass.getBaseSKL();
		int effectiveSPD = character.getBaseSPD() + sourceClass.getBaseSPD();
		int effectiveDEF = character.getBaseDEF() + sourceClass.getBaseDEF();
		int effectiveRES = character.getBaseRES() + sourceClass.getBaseRES();

		List<Integer> mappedStats = RelativeValueMapper.mappedValues(Arrays.asList(effectiveSTR, effectiveSKL, effectiveSPD, effectiveDEF, effectiveRES),
				Arrays.asList(targetClass.getBaseSTR(), targetClass.getBaseSKL(), targetClass.getBaseSPD(), targetClass.getBaseDEF(), targetClass.getBaseRES()));

		character.setBaseSTR(mappedStats.get(0) - targetClass.getBaseSTR());
		character.setBaseSKL(mappedStats.get(1) - targetClass.getBaseSKL());
		character.setBaseSPD(mappedStats.get(2) - targetClass.getBaseSPD());
		character.setBaseDEF(mappedStats.get(3) - targetClass.getBaseDEF());
		character.setBaseRES(mappedStats.get(4) - targetClass.getBaseRES());
	}

	private void adjustGrowthsToMatchClass(GBAFECharacterData character, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		List<Integer> mappedGrowths = RelativeValueMapper.mappedValues(Arrays.asList(character.getHPGrowth(), character.getSTRGrowth(), character.getSKLGrowth(), character.getSPDGrowth(), character.getDEFGrowth(), character.getRESGrowth(), character.getLCKGrowth()),
				Arrays.asList(targetClass.getHPGrowth(), targetClass.getSTRGrowth(), targetClass.getSKLGrowth(), targetClass.getSPDGrowth(), targetClass.getDEFGrowth(), targetClass.getRESGrowth(), targetClass.getLCKGrowth()));

		character.setHPGrowth(mappedGrowths.get(0));
		character.setSTRGrowth(mappedGrowths.get(1));
		character.setSKLGrowth(mappedGrowths.get(2));
		character.setSPDGrowth(mappedGrowths.get(3));
		character.setDEFGrowth(mappedGrowths.get(4));
		character.setRESGrowth(mappedGrowths.get(5));
		character.setLCKGrowth(mappedGrowths.get(6));
	}

	// TODO: Offer an option for sidegrade strictness?
	private void updateMinionToClass(GBAFEChapterUnitData chapterUnit, GBAFECharacterData minionCharacter, GBAFEClassData targetClass) {
		DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Updating minion from class 0x" + Integer.toHexString(chapterUnit.getStartingClass()) + " to class 0x" + Integer.toHexString(targetClass.getID()));
		DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Starting Inventory: [0x" + Integer.toHexString(chapterUnit.getItem1()) + ", 0x" + Integer.toHexString(chapterUnit.getItem2()) + ", 0x" + Integer.toHexString(chapterUnit.getItem3()) + ", 0x" + Integer.toHexString(chapterUnit.getItem4()) + "]");
		chapterUnit.setStartingClass(targetClass.getID());
		validateMinionInventory(chapterUnit, targetClass);
		DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Minion update complete. Inventory: [0x" + Integer.toHexString(chapterUnit.getItem1()) + ", 0x" + Integer.toHexString(chapterUnit.getItem2()) + ", 0x" + Integer.toHexString(chapterUnit.getItem3()) + ", 0x" + Integer.toHexString(chapterUnit.getItem4()) + "]");
	}

	private void updateMinionCharacterToClass(GBAFEChapterUnitData chapterUnit, GBAFECharacterData minionCharacter, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		// Write this into the character data.
		minionCharacter.setClassID(targetClass.getID());
		GBASlotAdjustmentService.transferWeaponRanks(minionCharacter, sourceClass, targetClass, rng);
		chapterUnit.setStartingClass(targetClass.getID());
        ItemAssignmentService.instance.validateMinionInventory(chapterUnit, minionCharacter);
	}

	private void validateMinionInventory(GBAFEChapterUnitData chapterUnit, GBAFEClassData targetClass) {
		int classID = chapterUnit.getStartingClass();
		GBAFEClassData unitClass = classData.classForID(classID);

		boolean canAttack = classData.canClassAttack(classID);
		boolean isHealer = unitClass.getStaffRank() > 0;

		boolean limitStaves = isHealer && canAttack;
		boolean hasStaff = false;
		boolean hasWeapon = false;
		boolean hasItems = false;

		GBAFEItemData replacementItem = null;

		if (unitClass != null) {
			int item1ID = chapterUnit.getItem1();
			GBAFEItemData item1 = itemData.itemWithID(item1ID);
			if (!hasItems) { hasItems = item1 != null; }
			if (item1 != null && (itemData.isWeapon(item1) || item1.getType() == WeaponType.STAFF)) {
				if (!unitClass.canUseWeapon(item1)) {
					replacementItem = itemData.getSidegradeWeapon(unitClass, item1, itemAssignmentOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
					if (replacementItem != null && (isHealer && limitStaves && hasStaff) && replacementItem.getType() == WeaponType.STAFF) {
						replacementItem = null; // We'll handle this later.
					}
					if (replacementItem != null) {
						chapterUnit.setItem1(replacementItem.getID());
					} else {
						chapterUnit.setItem1(0);
					}
					item1 = replacementItem;
				}
			}

			if (item1 != null) {
				if (!hasStaff) { hasStaff = item1.getType() == WeaponType.STAFF; }
				if (!hasWeapon) { hasWeapon = itemData.isWeapon(item1); }
			}

			int item2ID = chapterUnit.getItem2();
			GBAFEItemData item2 = itemData.itemWithID(item2ID);
			if (!hasItems) { hasItems = item2 != null; }
			if (item2 != null && (itemData.isWeapon(item2) || item2.getType() == WeaponType.STAFF)) {
				if (!unitClass.canUseWeapon(item2)) {
					replacementItem = itemData.getSidegradeWeapon(unitClass, item2, itemAssignmentOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
					if ((isHealer && limitStaves && hasStaff) && replacementItem.getType() == WeaponType.STAFF) {
						replacementItem = null; // We'll handle this later.
					}
					if (replacementItem != null) {
						chapterUnit.setItem2(replacementItem.getID());
					} else {
						chapterUnit.setItem2(0);
					}
					item2 = replacementItem;
				}
			}

			if (item2 != null) {
				if (!hasStaff) { hasStaff = item2.getType() == WeaponType.STAFF; }
				if (!hasWeapon) { hasWeapon = itemData.isWeapon(item2); }
			}

			int item3ID = chapterUnit.getItem3();
			GBAFEItemData item3 = itemData.itemWithID(item3ID);
			if (!hasItems) { hasItems = item3 != null; }
			if (item3 != null && (itemData.isWeapon(item3) || item3.getType() == WeaponType.STAFF)) {
				if (!unitClass.canUseWeapon(item3)) {
					replacementItem = itemData.getSidegradeWeapon(unitClass, item3, itemAssignmentOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
					if ((isHealer && limitStaves && hasStaff) && replacementItem.getType() == WeaponType.STAFF) {
						replacementItem = null; // We'll handle this later.
					}
					if (replacementItem != null) {
						chapterUnit.setItem3(replacementItem.getID());
					} else {
						chapterUnit.setItem3(0);
					}
					item3 = replacementItem;
				}
			}

			if (item3 != null) {
				if (!hasStaff) { hasStaff = item3.getType() == WeaponType.STAFF; }
				if (!hasWeapon) { hasWeapon = itemData.isWeapon(item3); }
			}

			int item4ID = chapterUnit.getItem4();
			GBAFEItemData item4 = itemData.itemWithID(item4ID);
			if (!hasItems) { hasItems = item4 != null; }
			if (item4 != null && (itemData.isWeapon(item4) || item4.getType() == WeaponType.STAFF)) {
				if (!unitClass.canUseWeapon(item4)) {
					replacementItem = itemData.getSidegradeWeapon(unitClass, item4, itemAssignmentOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
					if ((isHealer && limitStaves && hasStaff) && replacementItem.getType() == WeaponType.STAFF) {
						replacementItem = null; // We'll handle this later.
					}
					if (replacementItem != null) {
						chapterUnit.setItem4(replacementItem.getID());
					} else {
						chapterUnit.setItem4(0);
					}
					item4 = replacementItem;
				}
			}

			if (item4 != null) {
				if (!hasStaff) { hasStaff = item4.getType() == WeaponType.STAFF; }
				if (!hasWeapon) { hasWeapon = itemData.isWeapon(item4); }
			}

			// Sanity check.
			if (hasItems) {
				if (canAttack) {
					if (!hasWeapon) {
						// Make sure enemies that can attack have weapons.
						WeaponRanks ranks = itemData.ranksForClass(unitClass);
						List<WeaponType> types = ranks.getTypes();
						types.remove(WeaponType.STAFF);
						if (!types.isEmpty()) {
							for(;;) {
								WeaponType randomType = types.get(rng.nextInt(types.size()));
								GBAFEItemData[] candidates = itemData.itemsOfTypeAndBelowRank(randomType, ranks.rankForType(randomType), false, false);
								if (candidates.length > 0) {
									GBAFEItemData randomWeapon = candidates[rng.nextInt(candidates.length)];
									chapterUnit.giveItems(new int[] {randomWeapon.getID()});
									break;
								}
							}
						}
					}
				}
				if (isHealer && !canAttack) {
					assert hasStaff : "No staff for healer.";
				}
			}
		}
	}

	private void transferBossWeaponLevels(GBAFECharacterData character, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		WeaponRanks ranks = new WeaponRanks(character, sourceClass);
		Optional<WeaponRank> highestRank = ranks.asList().stream().max(WeaponRank::compare);

		WeaponRanks targetRanks = targetClass.getWeaponRanks();
		for (WeaponType weaponType : WeaponType.getWeaponTypes()) {
			WeaponRank newRank = WeaponRank.NONE;
			if (targetRanks.rankForType(weaponType).isHigherThan(WeaponRank.NONE)) {
				newRank = highestRank.get();
			}
			character.setWeaponRank(weaponType, newRank);
		}
	}

}
