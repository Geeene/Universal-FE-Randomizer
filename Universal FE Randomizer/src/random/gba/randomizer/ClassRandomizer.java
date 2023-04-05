package random.gba.randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import fedata.gba.GBAFEChapterData;
import fedata.gba.GBAFEChapterItemData;
import fedata.gba.GBAFEChapterUnitData;
import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEHolisticCharacter;
import fedata.gba.GBAFEItemData;
import fedata.gba.GBAFEStatDto;
import fedata.gba.GBAFEWeaponRankDto;
import fedata.gba.general.GBAFEItemProvider.WeaponRanks;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase.GameType;
import random.gba.loader.ChapterLoader;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.ClassDataLoader;
import random.gba.loader.ItemDataLoader;
import random.gba.loader.TextLoader;
import random.general.PoolDistributor;
import random.general.RelativeValueMapper;
import ui.model.ClassOptions;
import ui.model.ItemAssignmentOptions;
import ui.model.ClassOptions.GenderRestrictionOption;
import ui.model.ItemAssignmentOptions.WeaponReplacementPolicy;
import util.DebugPrinter;

public class ClassRandomizer {
	
	static final int rngSalt = 874;
	
	public static void randomizeClassMovement(int minMOV, int maxMOV, ClassDataLoader classData, Random rng) {
		GBAFEClassData[] allClasses = classData.allClasses();
		List<GBAFEClassData> unpromotedClasses = Arrays.asList(allClasses).stream()
				.filter(currentClass -> classData.isPromotedClass(currentClass.getID()) == false)
				.sorted(GBAFEClassData.defaultComparator)
				.collect(Collectors.toList());
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
	
	public static void randomizePlayableCharacterClasses(ClassOptions options, ItemAssignmentOptions inventoryOptions, 
			GameType type, CharacterDataLoader charactersData, ClassDataLoader classData, 
			ChapterLoader chapterData, ItemDataLoader itemData, TextLoader textData, Random rng) {
		GBAFECharacterData[] allPlayableCharacters = charactersData.playableCharacters();
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
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			
			Boolean isLordCharacter = charactersData.isLordCharacterID(character.getID());
			Boolean isThiefCharacter = charactersData.isThiefCharacterID(character.getID());
			Boolean isSpecialCharacter = charactersData.isSpecialCharacterID(character.getID());
			Boolean canChange = charactersData.canChangeCharacterID(character.getID());
			
			holisticCharacter.setLord(isLordCharacter);
			
			if (isLordCharacter && !includeLords) { continue; }
			if (isThiefCharacter && !includeThieves) { continue; }
			if (isSpecialCharacter && !includeSpecial) { continue; }
			if (!canChange) { continue; }
			
			Boolean characterRequiresRange = charactersData.characterIDRequiresRange(character.getID());
			Boolean characterRequiresMelee = charactersData.characterIDRequiresMelee(character.getID());
			
			int originalClassID = character.getClassID();
			GBAFEClassData originalClass = classData.classForID(originalClassID);
			
			GBAFEClassData targetClass = null;
			
			boolean isFemale = charactersData.isFemale(character.getID());
			
			if (determinedClasses.containsKey(character.getID())) {
				continue;
			} else {
				GBAFEClassData[] possibleClasses = hasMonsters ? classData.potentialClasses(originalClass, charactersData.isEnemyAtAnyPoint(character.getID()), !includeLords, !includeThieves, !includeSpecial, separateMonsters, forceChange, isLordCharacter, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), options.genderOption, null) :
					classData.potentialClasses(originalClass, charactersData.isEnemyAtAnyPoint(character.getID()), !includeLords, !includeThieves, !includeSpecial, forceChange, isLordCharacter, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), options.genderOption, null);
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
			
			for (GBAFECharacterData linked : charactersData.linkedCharactersForCharacter(character)) {
				determinedClasses.put(linked.getID(), targetClass);
				updateCharacterToClass(options, inventoryOptions, linked, originalClass, targetClass, characterRequiresRange, characterRequiresMelee, charactersData, classData, chapterData, itemData, textData, false, rng);
			}
		}
	}
	
	public static void randomizeBossCharacterClasses(ClassOptions options, ItemAssignmentOptions inventoryOptions, GameType type, CharacterDataLoader charactersData, ClassDataLoader classData, ChapterLoader chapterData, ItemDataLoader itemData, TextLoader textData, Random rng) {
		GBAFECharacterData[] allBossCharacters = charactersData.bossCharacters();
		
		Boolean includeLords = false;
		Boolean includeThieves = false;
		Boolean includeSpecial = false;
		Boolean hasMonsters = false;
		Boolean separateMonsters = false;
		Boolean forceChange = options.forceChange;
		if (type == GameType.FE8) {
			hasMonsters = true;
			separateMonsters = options.separateMonsters;
		}
		
		Map<Integer, GBAFEClassData> determinedClasses = new HashMap<Integer, GBAFEClassData>();
		
		for (GBAFECharacterData character : allBossCharacters) {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
			Boolean canChange = charactersData.canChangeCharacterID(character.getID());
			if (!canChange) { continue; }
			
			Boolean characterRequiresRange = charactersData.characterIDRequiresRange(character.getID());
			Boolean characterRequiresMelee = charactersData.characterIDRequiresMelee(character.getID());
			
			int originalClassID = character.getClassID();
			GBAFEClassData originalClass = classData.classForID(originalClassID);
			if (originalClass == null) {
				System.err.println("Invalid Class found: Class ID = " + Integer.toHexString(originalClassID));
				continue;
			}
			
			GBAFEClassData targetClass = null;
			
			Boolean forceBasicWeaponry = false;
			Boolean shouldNerf = false;
			
			boolean isFemale = charactersData.isFemale(character.getID());
			
			if (determinedClasses.containsKey(character.getID())) {
				continue;
			} else {			
				GBAFECharacterData mustLoseToCharacter = charactersData.characterRequiresCounterToCharacter(character);
				GBAFEClassData mustLoseToClass = null;
				if (mustLoseToCharacter != null) {
					mustLoseToClass = classData.classForID(mustLoseToCharacter.getClassID());
					forceBasicWeaponry = true;
					shouldNerf = true;
				}
				
				GBAFEClassData[] possibleClasses = hasMonsters ? 
						classData.potentialClasses(originalClass, true, !includeLords, !includeThieves, !includeSpecial, separateMonsters, forceChange, true, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), options.genderOption, mustLoseToClass) :
					classData.potentialClasses(originalClass, true, !includeLords, !includeThieves, !includeSpecial, forceChange, true, characterRequiresRange, characterRequiresMelee, character.isClassRestricted(), options.genderOption, mustLoseToClass);
				if (possibleClasses.length == 0) {
					continue;
				}
			
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
			
			if (targetClass == null) {
				continue;
			}
			
			
			for (GBAFECharacterData linked : charactersData.linkedCharactersForCharacter(character)) {
				determinedClasses.put(linked.getID(), targetClass);
				updateCharacterToClass(options, inventoryOptions, linked, originalClass, targetClass, characterRequiresRange, characterRequiresMelee, charactersData, classData, chapterData, itemData, textData, forceBasicWeaponry && linked.getID() == character.getID(), rng);
			}
			if (shouldNerf) {
				// Halve skill, speed, defense, and resistance if we need to make sure he loses to us.
				holisticCharacter.nerf();
			}
		}
	}
	
	public static void randomizeMinionClasses(ClassOptions options, ItemAssignmentOptions inventoryOptions, GameType type, CharacterDataLoader charactersData, ClassDataLoader classData, ChapterLoader chapterData, ItemDataLoader itemData, Random rng) {
		Boolean includeLords = false;
		Boolean includeThieves = false;
		Boolean includeSpecial = false;
		Boolean hasMonsters = type == GameType.FE8;
		Boolean separateMonsters = options.separateMonsters; // Defaulted to false for non FE8
		Boolean forceChange = options.forceChange;
		
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
			GBAFECharacterData lordCharacter = charactersData.characterWithID(chapter.lordLeaderID());
			GBAFEClassData lordClass = classData.classForID(lordCharacter.getClassID());
			for (GBAFEChapterUnitData chapterUnit : chapter.allUnits()) {
				// int leaderID = chapterUnit.getLeaderID();
				int characterID = chapterUnit.getCharacterNumber();
				int classID = chapterUnit.getStartingClass();
				// It's safe to check for boss leader ID in the case of FE7, but FE6 tends to put other IDs there (kind of like squad captains).
				// We're going to remove this safety check in the meantime, but we should be wary of any accidental changes.
				// Also check to make sure it's not any character we definitely don't want to change.
				// Finally, also make sure the starting class is valid. Classes we don't recognize, we shouldn't touch.
				if (!charactersData.isBossCharacterID(characterID) && /*charactersData.isBossCharacterID(leaderID) &&*/ !charactersData.isPlayableCharacterID(characterID) && 
						charactersData.canChangeCharacterID(characterID) && classData.isValidClass(classID)) {
					
					GBAFEClassData originalClass = classData.classForID(classID);
					if (originalClass == null) {
						continue;
					}
					
					if (classData.isThief(originalClass.getID())) {
						continue;
					}
					
					GBAFEHolisticCharacter minionCharacterData = AbstractGBARandomizer.holisticCharacterMap.get(characterID);
					if (minionCharacterData == null) {
						continue;
					}
					
					GBAFEClassData targetClass = null;
					boolean characterHasWeaponRanks = !minionCharacterData.getUsableWeaponTypes().isEmpty();
					
					// If he's been modified already, we use the same class.
					// Otherwise, we randomize the class.
					if (characterHasWeaponRanks) {
						if (minionCharacterData.getPersonalData().wasModified()) {
							targetClass = classData.classForID(minionCharacterData.getPersonalData().getClassID());
						}
					}
					
					if (targetClass != null) {
						updateMinionToClass(inventoryOptions, chapterUnit, minionCharacterData, targetClass, classData, itemData, rng);
					} else {
						Boolean shouldRestrictToSafeClasses = !chapter.isClassSafe();
						Boolean shouldMakeEasy = chapter.shouldBeSimplified();
						GBAFEClassData loseToClass = shouldMakeEasy ? lordClass : null;
						GBAFEClassData[] possibleClasses = hasMonsters ? 
								classData.potentialClasses(originalClass, true, !includeLords, !includeThieves, !includeSpecial, separateMonsters, forceChange, true, false, false, shouldRestrictToSafeClasses, options.genderOption, loseToClass) :
							classData.potentialClasses(originalClass, true, false, false, false, forceChange, true, false, false, shouldRestrictToSafeClasses, options.genderOption, loseToClass);
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
							updateMinionCharacterToClass(inventoryOptions, chapterUnit, minionCharacterData, originalClass, targetClass, classData, itemData, rng);
						} else {
							updateMinionToClass(inventoryOptions, chapterUnit, minionCharacterData, targetClass, classData, itemData, rng);
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

	private static void updateCharacterToClass(ClassOptions classOptions, ItemAssignmentOptions inventoryOptions, GBAFECharacterData character, GBAFEClassData sourceClass, GBAFEClassData targetClass, Boolean ranged, Boolean melee, CharacterDataLoader charData, ClassDataLoader classData, ChapterLoader chapterData, ItemDataLoader itemData, TextLoader textData, Boolean forceBasicWeapons, Random rng) {
		GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(character.getID());
		GBAFEStatDto oldFinalStats = holisticCharacter.getStats();
		holisticCharacter.changeClass(targetClass);

		if (charData.isBossCharacterID(character.getID())) {
			transferBossWeaponLevels(holisticCharacter, sourceClass, targetClass);
		} else {
			holisticCharacter.updateWeaponRanksToMatchClass(rng);
		}
		switch (classOptions.basesTransfer) {
		case ADJUST_TO_MATCH:
			// Setting "Retain Final Bases in the UI"
			applyCorrectionRetainFinalBases(holisticCharacter, oldFinalStats);
			break;
		case NO_CHANGE:
			// No longer needed
			break;
		case ADJUST_TO_CLASS:
			// Shuffle the STR, SKL, SPD, DEF, and RES Stats so that they stay the same as the class strengths
			adjustBasesToNewClassStrengths(holisticCharacter, oldFinalStats);
			break;
		}
		
		switch (classOptions.growthOptions) {
		case TRANSFER_PERSONAL_GROWTHS:
			GBAFEStatDto growthDelta = holisticCharacter.getGrowths().subtract(sourceClass.getGrowths());
			holisticCharacter.setGrowths(holisticCharacter.getGrowths().add(growthDelta));
			break;

		case CLASS_RELATIVE_GROWTHS:
			adjustGrowthsToMatchClass(holisticCharacter, sourceClass, targetClass);
			break;
		default:
			break;
		}
		
		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			GBAFEChapterItemData reward = chapter.chapterItemGivenToCharacter(character.getID());
			if (reward != null) {
				GBAFEItemData item = itemData.getRandomWeaponForCharacter(holisticCharacter, ranged, melee, false, inventoryOptions.assignPromoWeapons, inventoryOptions.assignPoisonWeapons, rng); 
				
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
					validateCharacterInventory(inventoryOptions, holisticCharacter, targetClass, chapterUnit, ranged, melee, charData, classData, itemData, textData, forceBasicWeapons, rng);
					if (classData.isThief(sourceClass.getID())) {
						validateFormerThiefInventory(chapterUnit, itemData);
					}
					validateSpecialClassInventory(chapterUnit, itemData, rng);
				}
			}
		}
	}
	
	private static void applyCorrectionRetainFinalBases(GBAFEHolisticCharacter character, GBAFEStatDto oldFinalStats) {
		// Right now the stats are automatically adjusted to the new classes bases, 
		// instead here we want to buff / nerf the stats so that the final bases are the same as before (except cap changes)
		// The easiest way of doing this is to just straight up override the adjusted stats with the old stats, and then let the default logic apply Clamping.
		DebugPrinter.log(DebugPrinter.Key.GBA_CLASS_RANDO_STATS, String.format("Retain Final: old %s, new %s", character.getStats().toString(), oldFinalStats));
		character.setStats(oldFinalStats);
	}
	
	private static void adjustBasesToNewClassStrengths(GBAFEHolisticCharacter character, GBAFEStatDto oldStats) {
		// LCK / HP are automatically handled with the class adjustment
		GBAFEStatDto newStats = character.getStats();
		
		// STR, SKL, SPD, DEF, and RES are transfered based on which one is highest on the target class.
		List<Integer> mappedStats = RelativeValueMapper.mappedValues(
				Arrays.asList(oldStats.str, oldStats.skl, oldStats.spd, oldStats.def, oldStats.res),
				Arrays.asList(newStats.str, newStats.skl, newStats.spd, newStats.def, newStats.res));
		
		newStats.str = mappedStats.get(0);
		newStats.skl = mappedStats.get(1);
		newStats.spd = mappedStats.get(2);
		newStats.def = mappedStats.get(3);
		newStats.res = mappedStats.get(4);
		
		character.setStats(newStats);
		DebugPrinter.log(DebugPrinter.Key.GBA_CLASS_RANDO_STATS, String.format("Adjust to new class: old %s, new %s", oldStats, newStats.toString()));
	}
	
	private static void adjustGrowthsToMatchClass(GBAFEHolisticCharacter character, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		List<Integer> mappedGrowths = RelativeValueMapper.mappedValues(
				character.getGrowths().asList(),
				character.getCurrentClass().getGrowths().asList());
		
		character.setGrowths(new GBAFEStatDto(mappedGrowths.get(0), mappedGrowths.get(1), mappedGrowths.get(2), 
				mappedGrowths.get(3), mappedGrowths.get(4), mappedGrowths.get(5), mappedGrowths.get(6)));
	}
	
	// TODO: Offer an option for sidegrade strictness?
	private static void updateMinionToClass(ItemAssignmentOptions inventoryOptions, GBAFEChapterUnitData chapterUnit, GBAFEHolisticCharacter minionCharacter, GBAFEClassData targetClass, ClassDataLoader classData, ItemDataLoader itemData, Random rng) {
		DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Updating minion from class 0x" + Integer.toHexString(chapterUnit.getStartingClass()) + " to class 0x" + Integer.toHexString(targetClass.getID()));
		DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Starting Inventory: [0x" + Integer.toHexString(chapterUnit.getItem1()) + ", 0x" + Integer.toHexString(chapterUnit.getItem2()) + ", 0x" + Integer.toHexString(chapterUnit.getItem3()) + ", 0x" + Integer.toHexString(chapterUnit.getItem4()) + "]");
		chapterUnit.setStartingClass(targetClass.getID());
		validateMinionInventory(inventoryOptions, chapterUnit, targetClass, classData, itemData, rng);
		DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Minion update complete. Inventory: [0x" + Integer.toHexString(chapterUnit.getItem1()) + ", 0x" + Integer.toHexString(chapterUnit.getItem2()) + ", 0x" + Integer.toHexString(chapterUnit.getItem3()) + ", 0x" + Integer.toHexString(chapterUnit.getItem4()) + "]");
	}
	
	private static void updateMinionCharacterToClass(ItemAssignmentOptions inventoryOptions, GBAFEChapterUnitData chapterUnit, GBAFEHolisticCharacter minionCharacter, GBAFEClassData sourceClass, GBAFEClassData targetClass, ClassDataLoader classData, ItemDataLoader itemData, Random rng) {
		minionCharacter.changeClass(targetClass);
		minionCharacter.updateWeaponRanksToMatchClass(rng);
		
		// Write this into the character data.
		chapterUnit.setStartingClass(targetClass.getID());
		validateMinionInventory(inventoryOptions, chapterUnit, AbstractGBARandomizer.holisticCharacterMap.get(minionCharacter.getID()), classData, itemData, rng);
	}
	
	public static void validateFormerThiefInventory(GBAFEChapterUnitData chapterUnit, ItemDataLoader itemData) {
		Set<GBAFEItemData> itemsToRetain = itemsToRetain(chapterUnit, itemData);
		
		GBAFEItemData[] requiredItems = itemData.formerThiefInventory();
		if (requiredItems != null) {
			giveItemsToChapterUnit(chapterUnit, requiredItems);
		}
		
		GBAFEItemData[] thiefItemsToRemove = itemData.thiefItemsToRemove();
		for (GBAFEItemData item : thiefItemsToRemove) {
			chapterUnit.removeItem(item.getID());
		}
		
		itemsToGiveBack(chapterUnit, itemsToRetain, itemData);
		if (!itemsToRetain.isEmpty()) {
			int[] idsToGiveBack = itemsToRetain.stream().mapToInt(item -> (item.getID())).toArray();
			chapterUnit.giveItems(idsToGiveBack);
		}
	}
	
	private static Set<GBAFEItemData> itemsToRetain(GBAFEChapterUnitData chapterUnit, ItemDataLoader itemData) {
		Set<GBAFEItemData> existingItemSet = new HashSet<GBAFEItemData>();
		for (int i = 1; i<=4; i++) {
			int itemId = chapterUnit.getItem(i);
			GBAFEItemData item = itemData.itemWithID(itemId);
			if(item != null) {existingItemSet.add(item);}
		}
		
		Set<GBAFEItemData> itemsToRetain = new HashSet<GBAFEItemData>(Arrays.asList(itemData.specialItemsToRetain()));
		itemsToRetain.retainAll(existingItemSet);
		return itemsToRetain;
	}
	
	private static void itemsToGiveBack(GBAFEChapterUnitData chapterUnit, Set<GBAFEItemData> itemsToRetain, ItemDataLoader itemData) {
		// If there is nothing to retain there is no need to do something
		if (itemsToRetain.isEmpty()) {
			return;
		}
		
		// Remove all items that are currently in the inventory from the items to retain, so that if the item to retain is still in the list we don't add it a second time.
		for (int i = 1; i<=4; i++) {
			itemsToRetain.remove(itemData.itemWithID(chapterUnit.getItem(i)));
		}
	}
	
	public static void validateSpecialClassInventory(GBAFEChapterUnitData chapterUnit, ItemDataLoader itemData, Random rng) {
		Set<GBAFEItemData> itemsToRetain = itemsToRetain(chapterUnit, itemData);
		
		GBAFEItemData[] requiredItems = itemData.specialInventoryForClass(chapterUnit.getStartingClass(), rng);
		if (requiredItems != null && requiredItems.length > 0) {
			giveItemsToChapterUnit(chapterUnit, requiredItems);
		}
		
		itemsToGiveBack(chapterUnit, itemsToRetain, itemData);
		if (!itemsToRetain.isEmpty()) {
			int[] idsToGiveBack = itemsToRetain.stream().mapToInt(item -> (item.getID())).toArray();
			chapterUnit.giveItems(idsToGiveBack);
		}
	}
	
	private static void giveItemsToChapterUnit(GBAFEChapterUnitData chapterUnit, GBAFEItemData[] items) {
		int[] requiredItemIDs = new int[items.length];
		for (int i = 0; i < items.length; i++) {
			requiredItemIDs[i] = items[i].getID();
		}
		chapterUnit.giveItems(requiredItemIDs);
	}
	
	private static void validateMinionInventory(ItemAssignmentOptions inventoryOptions, GBAFEChapterUnitData chapterUnit, GBAFEClassData targetClass, ClassDataLoader classData, ItemDataLoader itemData, Random rng) {
		int classID = chapterUnit.getStartingClass();
		GBAFEClassData unitClass = classData.classForID(classID);
		
		boolean canAttack = classData.canClassAttack(classID);
		boolean isHealer = unitClass.getStaffRank() > 0;
		
		boolean limitStaves = isHealer && canAttack;
		boolean hasStaff = false;
		boolean hasWeapon = false;
		boolean hasItems = false;
		
		GBAFEItemData replacementItem = null;
		
		int item1ID = chapterUnit.getItem1();
		GBAFEItemData item1 = itemData.itemWithID(item1ID);
		if (!hasItems) { hasItems = item1 != null; }
		if (item1 != null && (itemData.isWeapon(item1) || item1.getType() == WeaponType.STAFF)) {
			if (!unitClass.canUseWeapon(item1)) {
				replacementItem = itemData.getSidegradeWeapon(unitClass, item1, inventoryOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, inventoryOptions.assignPromoWeapons, true, rng);
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
		
		for(int i = 2; i <= 4;i++) {
			int itemID = chapterUnit.getItem(i);
			GBAFEItemData item = itemData.itemWithID(itemID);
			if (!hasItems) { hasItems = item != null; }
			if (item != null && (itemData.isWeapon(item) || item.getType() == WeaponType.STAFF)) {
				if (!unitClass.canUseWeapon(item)) {
					replacementItem = itemData.getSidegradeWeapon(unitClass, item, inventoryOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, inventoryOptions.assignPromoWeapons, true, rng);
					if ((isHealer && limitStaves && hasStaff) && replacementItem.getType() == WeaponType.STAFF) {
						replacementItem = null; // We'll handle this later.
					}
					if (replacementItem != null) {
						chapterUnit.setItem(i, replacementItem.getID());
					} else {
						chapterUnit.setItem(i, 0);
					}
					item = replacementItem;
				}
			}
			
			if (item != null) {
				hasStaff |= item.getType() == WeaponType.STAFF;
				hasWeapon |= itemData.isWeapon(item);
			}
		}
		
		// Sanity check.
		if (hasItems) {
			if (canAttack && !hasWeapon) {
				// Make sure enemies that can attack have weapons.
				GBAFEWeaponRankDto ranks = unitClass.getWeaponRanks();
				List<WeaponType> types = ranks.getTypes();
				types.remove(WeaponType.STAFF);
				if (!types.isEmpty()) {
					while(true) {
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
			if (isHealer && !canAttack) {
				assert hasStaff : "No staff for healer.";
			}
		}
	}
	
	private static void validateMinionInventory(ItemAssignmentOptions inventoryOptions, GBAFEChapterUnitData chapterUnit, GBAFEHolisticCharacter minionCharacter, ClassDataLoader classData, ItemDataLoader itemData, Random rng) {
		int classID = chapterUnit.getStartingClass();
		GBAFEClassData unitClass = classData.classForID(classID);
		
		boolean canAttack = classData.canClassAttack(classID);
		boolean isHealer = unitClass.getStaffRank() > 0;
		
		boolean limitStaves = isHealer && canAttack;
		boolean hasStaff = false;
		boolean hasWeapon = false;
		boolean hasItems = false;
		
		GBAFEItemData replacementItem = null;
		
		for (int i = 1; i <= 4; i++) {
			int itemID = chapterUnit.getItem(i);
			GBAFEItemData item = itemData.itemWithID(itemID);
			if (!hasItems) { hasItems = item != null; }
			if (item != null && (itemData.isWeapon(item) || item.getType() == WeaponType.STAFF)) {
				if (!canCharacterUseItem(minionCharacter, item, itemData)) {
					replacementItem = itemData.getSidegradeWeapon(minionCharacter, unitClass, item, true, inventoryOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, inventoryOptions.assignPromoWeapons, true, rng);
					if ((isHealer && limitStaves && hasStaff) && replacementItem.getType() == WeaponType.STAFF) {
						replacementItem = null; // We'll handle this later.
					}
					if (replacementItem != null) {
						chapterUnit.setItem(i, replacementItem.getID());
					} else {
						chapterUnit.setItem(i, 0);
					}
					item = replacementItem;
				}
			}
			
			if (item != null) {
				if (!hasStaff) { hasStaff = item.getType() == WeaponType.STAFF; }
				if (!hasWeapon) { hasWeapon = itemData.isWeapon(item); }
			}
		}
		
		
		// Sanity check.
		if (hasItems) {
			if (canAttack && !hasWeapon) {
				// Make sure enemies that can attack have weapons.
				GBAFEWeaponRankDto ranks = minionCharacter.getWeaponRanks();
				List<WeaponType> types = ranks.getTypes();
				types.remove(WeaponType.STAFF);
				if (!types.isEmpty()) {
					while(true) {
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
			if (isHealer && !canAttack) {
				assert hasStaff : "No staff for healer.";
			}
		}
	}
	
	public static void validateCharacterInventory(ItemAssignmentOptions inventoryOptions, GBAFEHolisticCharacter character, GBAFEClassData charClass, GBAFEChapterUnitData chapterUnit, Boolean ranged, Boolean melee, CharacterDataLoader charData, ClassDataLoader classData, ItemDataLoader itemData, TextLoader textData, Boolean forceBasic, Random rng) {
		GBAFEItemData[] prfWeapons = itemData.prfWeaponsForClass(charClass.getID());
		Set<Integer> prfIDs = new HashSet<Integer>();
		for (GBAFEItemData prfWeapon : prfWeapons) {
			prfIDs.add(prfWeapon.getID());
		}
		
		Boolean isHealerClass = charClass.getStaffRank() > 0;
		Boolean hasAtLeastOneHealingStaff = false;
		
		Boolean classCanAttack = classData.canClassAttack(charClass.getID());
		Boolean hasAtLeastOneWeapon = false;
		
		Set<GBAFEItemData> itemsToRetain = itemsToRetain(chapterUnit, itemData);
		
		DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Validating inventory for character 0x" + Integer.toHexString(character.getPersonalData().getID()) + " (" + textData.getStringAtIndex(character.getPersonalData().getNameIndex(), true) +") in class 0x" + Integer.toHexString(charClass.getID()) + " (" + textData.getStringAtIndex(charClass.getNameIndex(), true) + ")");
		
		
		for (int i = 0; i<=4; i++) {
			int itemId = chapterUnit.getItem(i);
			GBAFEItemData item = itemData.itemWithID(itemId);
			if (item == null) {
				continue;
			}
			
			if (itemData.isWeapon(item) || item.getType() == WeaponType.STAFF) {
				if (!canCharacterUseItem(character, item, itemData) || (item.getWeaponRank() == WeaponRank.PRF && !prfIDs.contains(itemId))) {
					GBAFEItemData replacementItem = itemData.getBasicWeaponForCharacter(character.getPersonalData(), ranged, false, rng);
					if (!forceBasic) {
						if (inventoryOptions.weaponPolicy == WeaponReplacementPolicy.ANY_USABLE || ranged || melee) {
							replacementItem = itemData.getRandomWeaponForCharacter(character, ranged, melee, charData.isEnemyAtAnyPoint(character.getID()), inventoryOptions.assignPromoWeapons, inventoryOptions.assignPoisonWeapons, rng);
						} else {
							replacementItem = itemData.getSidegradeWeapon(character, charClass, item, charData.isEnemyAtAnyPoint(character.getID()), inventoryOptions.weaponPolicy == WeaponReplacementPolicy.STRICT, inventoryOptions.assignPromoWeapons, inventoryOptions.assignPoisonWeapons, rng);
						}
					}
					
					if (item.getWeaponRank() == WeaponRank.S) {
						GBAFEItemData[] topWeapons = topRankWeaponsForClass(charClass, itemData);
						if (topWeapons.length > 0) {
							replacementItem = topWeapons[rng.nextInt(topWeapons.length)];
						}
					}
					if (replacementItem != null) {
						if (replacementItem.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(replacementItem.getID()); }
						else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(replacementItem); }
						chapterUnit.setItem(i, replacementItem.getID());
					} else {
						chapterUnit.setItem(i, 0);
					}
				} else {
					if (item.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(item.getID()); }
					else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(item); }
				}
			}
			
			int newItemId = chapterUnit.getItem(i);
			GBAFEItemData newItem = itemData.itemWithID(itemId);

			String s = String.format("Validated Item Slot %d was originally %s(%s) after validation is %s(%s)",i,Integer.toHexString(itemId), textData.getStringAtIndex(item.getNameIndex(), true),Integer.toHexString(newItemId), textData.getStringAtIndex(newItem.getNameIndex(), true));
			DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, s);
			
		}
		
		// Sanity check, if a unit that can heal has no healing staff, make sure they get one.
		if (isHealerClass && !hasAtLeastOneHealingStaff) {
			chapterUnit.giveItems(new int[] {itemData.getRandomHealingStaff(character.getWeaponRanks().staff, rng).getID()});
		}
		
		// Sanity check, if a unit that can attack wasn't given a weapon, make sure they get one.
		if (classCanAttack && !hasAtLeastOneWeapon) {
			GBAFEItemData basicWeapon = itemData.getBasicWeaponForCharacter(character.getPersonalData(), ranged, true, rng);
			if (basicWeapon != null) {
				chapterUnit.giveItems(new int[] {basicWeapon.getID()});
			}
		}
		
		// check if the items that should be kept are already still in the inventory
		itemsToGiveBack(chapterUnit, itemsToRetain, itemData);
		if (!itemsToRetain.isEmpty()) {
			int[] idsToGiveBack = itemsToRetain.stream().mapToInt(item -> (item.getID())).toArray();
			chapterUnit.giveItems(idsToGiveBack);
		}
		
		// Give out the classes prf if there is one 
		GBAFEItemData prf = itemData.getPrfWeaponForClass(charClass.getID());
		if (prf != null) { chapterUnit.giveItem(prf.getID()); }
		
		// Make sure that the weapon is at the top of the inventory
		if (charData.characterIDRequiresAttack(character.getID())) {
			if (!itemData.isWeapon(itemData.itemWithID(chapterUnit.getItem1()))) {
				int swap = chapterUnit.getItem1();
				if (swap != 0) {
					if (itemData.isWeapon(itemData.itemWithID(chapterUnit.getItem2()))) {
						chapterUnit.setItem1(chapterUnit.getItem2());
						chapterUnit.setItem2(swap);
					} else if (itemData.isWeapon(itemData.itemWithID(chapterUnit.getItem3()))) {
						chapterUnit.setItem1(chapterUnit.getItem3());
						chapterUnit.setItem3(swap);
					} else if (itemData.isWeapon(itemData.itemWithID(chapterUnit.getItem4()))) {
						chapterUnit.setItem1(chapterUnit.getItem4());
						chapterUnit.setItem4(swap);
					}
				}
			}
		}
	}
	
	private static GBAFEItemData[] topRankWeaponsForClass(GBAFEClassData characterClass, ItemDataLoader itemData) {
		ArrayList<GBAFEItemData> items = new ArrayList<GBAFEItemData>();
		if (characterClass.getSwordRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.SWORD, WeaponRank.S, false, false, true))); }
		if (characterClass.getLanceRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.LANCE, WeaponRank.S, false, false, true))); }
		if (characterClass.getAxeRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.AXE, WeaponRank.S, false, false, true))); }
		if (characterClass.getBowRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.BOW, WeaponRank.S, false, false, true))); }
		if (characterClass.getAnimaRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.ANIMA, WeaponRank.S, false, false, true))); }
		if (characterClass.getLightRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.LIGHT, WeaponRank.S, false, false, true))); }
		if (characterClass.getDarkRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.DARK, WeaponRank.S, false, false, true))); }
		if (characterClass.getStaffRank() > 0) { items.addAll(Arrays.asList(itemData.itemsOfTypeAndEqualRank(WeaponType.STAFF, WeaponRank.S, false, false, true))); }
		
		return items.toArray(new GBAFEItemData[items.size()]);
	}
	
	private static Boolean canCharacterUseItem(GBAFEHolisticCharacter character, GBAFEItemData weapon, ItemDataLoader itemData) {
		WeaponRank weaponRankValue = weapon.getWeaponRank();
		if ((weapon.getType() == WeaponType.SWORD && character.getWeaponRanks().sword.isHigherThan(weaponRankValue, null)) ||
				(weapon.getType() == WeaponType.LANCE && character.getWeaponRanks().lance.isHigherThan(weaponRankValue, null)) ||
				(weapon.getType() == WeaponType.AXE && character.getWeaponRanks().axe.isHigherThan(weaponRankValue, null)) ||
				(weapon.getType() == WeaponType.BOW && character.getWeaponRanks().bow.isHigherThan(weaponRankValue, null)) ||
				(weapon.getType() == WeaponType.ANIMA && character.getWeaponRanks().anima.isHigherThan(weaponRankValue, null)) ||
				(weapon.getType() == WeaponType.LIGHT && character.getWeaponRanks().light.isHigherThan(weaponRankValue, null)) ||
				(weapon.getType() == WeaponType.DARK && character.getWeaponRanks().dark.isHigherThan(weaponRankValue, null)) ||
				(weapon.getType() == WeaponType.STAFF && character.getWeaponRanks().staff.isHigherThan(weaponRankValue, null))) {
			return true;
		}
		
		return false;
	}
	
	private static void transferBossWeaponLevels(GBAFEHolisticCharacter character, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		GBAFEWeaponRankDto weaponRanks = character.getWeaponRanks();
		List<WeaponRank> wrList = weaponRanks.asList().stream().sorted().collect(Collectors.toList());
		WeaponRank highestRank = wrList.get(wrList.size()-1);

		GBAFEWeaponRankDto newRanks = new GBAFEWeaponRankDto();
		newRanks.sword = highestRank;
		newRanks.lance = highestRank;
		newRanks.axe = highestRank;
		newRanks.bow = highestRank;
		newRanks.staff = highestRank;
		newRanks.anima = highestRank;
		newRanks.light = highestRank;
		newRanks.dark = highestRank;
		
		character.setWeaponRanks(newRanks);
	}
}
