package random.gba.randomizer.service;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEStatDto;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponRanks;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase;
import random.gba.loader.GBADataLoaders;
import random.gba.randomizer.AbstractGBARandomizerComponent;
import random.general.RelativeValueMapper;
import ui.model.RecruitmentOptions.ClassMode;
import util.DebugPrinter;
import util.OptionRecorder;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GBASlotAdjustmentService extends AbstractGBARandomizerComponent {

    public static GBASlotAdjustmentService instance;

	/**
	 * This Constant is for the level at which a character is assumed to be promoted with regards to Slot Adjustment.
	 * F.e. if Eirika -> Seth she would only get 9 levels then promote. 
	 * Rather than going to level 20 for promotion and getting 19 levels.
	 */
	private static final int ASSUMED_PROMOTION_LEVEL = 10;

	/**
	 * This constant describes the threshold after which the number of Autolevels will be reduced a bit.
	 * 
	 * This help with Making sure htat characters which receive a lot of positive levels aren't too strong, 
	 * and characters that get negative levels don't become too weak. 
	 */
	private static final int AUTOLEVEL_REDUCTION_THRESHOLD = 10;

    public GBASlotAdjustmentService(OptionRecorder.GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, FEBase.GameType type) {
        super(allOptions, dataLoaders, rng, type);
        this.instance = this;
    }

    /**
	 * Used by Recruitment Randomization and Character Shuffling to Calculate the following information:
	 * 
	 * A) Should the character, be demoted, promoted or are they already in the right tier of class.
	 * B) How many auto levels should they receive
	 * C) Which promotion bonuses will they receive
     *
     * @param alreadyChosenTargetClass used for characters which appear in multiple places, so that each instance has the same promotion
	 */
	public ClassAdjustmentDto handleClassAdjustment(int toLevel, int fromLevel,
			GBAFEClassData fillSourceClass, GBAFECharacterData fill,
			GBAFEClassData slotSourceClass, GBAFEClassData alreadyChosenTargetClass, DebugPrinter.Key key) {
		ClassAdjustmentDto dto = new ClassAdjustmentDto();
        boolean shouldBePromoted = classData.isPromotedClass(slotSourceClass.getID());
        boolean isPromoted = classData.isPromotedClass(fillSourceClass.getID());
        // if we have a level 15 unpromoted unit (e.g. Hugh FE6, and they replace a lvl 1 promoted unit, don't give them -4 levels, just promote)
		if (shouldBePromoted) { toLevel += Math.max(ASSUMED_PROMOTION_LEVEL, fromLevel); }
		if (isPromoted && !classData.isSpecialLevelingClass(fillSourceClass)) { fromLevel += ASSUMED_PROMOTION_LEVEL; }
		dto.levelAdjustment = toLevel - fromLevel;
		
		// To make newly created pre-promotes not completely busted (since they probably had higher growths than real pre-promotes)
		// we'll subtract a few levels from their autoleveling amount, assuming they get a lot (like 10).
		if (!isPromoted && shouldBePromoted && dto.levelAdjustment > AUTOLEVEL_REDUCTION_THRESHOLD) {
			DebugPrinter.log(key, "Dropping 3 additional levels for new prepromotes.");
			dto.levelAdjustment  -= 3;
		} else if(isPromoted && !shouldBePromoted && dto.levelAdjustment < -AUTOLEVEL_REDUCTION_THRESHOLD) {
			// Likewise, don't ruin former prepromotes as much
			DebugPrinter.log(key, "Dropping 3 less levels for newly demoted units.");
			dto.levelAdjustment  += 3;
		}

		if (shouldBePromoted && !isPromoted) {
			DebugPrinter.log(key, "Promoting [" + textData.getStringAtIndex(fill.getNameIndex(), true) + "]");
			// Promote Fill.
			if (alreadyChosenTargetClass == null) {
                List<GBAFEClassData> promotionOptions = classData.promotionOptions(fillSourceClass.getID());
                DebugPrinter.log(key, "Promotion Options: [" + String.join(", ", promotionOptions.stream().map(charClass -> (textData.getStringAtIndex(charClass.getNameIndex(), true))).collect(Collectors.toList())) + "]");
                if (!promotionOptions.isEmpty()) {
                    alreadyChosenTargetClass = promotionOptions.get(rng.nextInt(promotionOptions.size()));
                    if (!classData.isPromotedClass(alreadyChosenTargetClass.getID())) {
                        // This is really only for FE8. If a trainee switches into a promoted unit, there's two promotions that need to be done.
                        dto.promoBonuses.add(alreadyChosenTargetClass.getPromoBonuses());
                        promotionOptions = classData.promotionOptions(alreadyChosenTargetClass.getID());
                        DebugPrinter.log(key, "Promotion Options: [" + String.join(", ", promotionOptions.stream().map(charClass -> (textData.getStringAtIndex(charClass.getNameIndex(), true))).collect(Collectors.toList())) + "]");
                        if (!promotionOptions.isEmpty()) {
                            alreadyChosenTargetClass = promotionOptions.get(rng.nextInt(promotionOptions.size()));
                            dto.levelAdjustment += 10;
                        }
                    }
                } else {
                    alreadyChosenTargetClass = fillSourceClass;
                }

                if (recruitOptions != null && recruitOptions.classMode == ClassMode.USE_SLOT) {
                    alreadyChosenTargetClass = slotSourceClass;
                }

                DebugPrinter.log(key, "Selected Class: " + (alreadyChosenTargetClass != null ? textData.getStringAtIndex(alreadyChosenTargetClass.getNameIndex(), true) : "None"));
			}
			dto.promoBonuses.add(alreadyChosenTargetClass.getPromoBonuses());
			// For some reason, some promoted class seem to have lower bases than their unpromoted variants (FE8 lords are an example). If they are lower, adjust upwards.
			dto.promoBonuses.add(GBAFEStatDto.upAdjust(alreadyChosenTargetClass.getBases(), fillSourceClass.getBases()));

		} else if (!shouldBePromoted && isPromoted) {
			DebugPrinter.log(key, "Demoting [" + textData.getStringAtIndex(fill.getNameIndex(), true) + "]");
			// Demote Fill.
			if (alreadyChosenTargetClass == null) {
                List<GBAFEClassData> demotionOptions = classData.demotionOptions(fill.getClassID());
                DebugPrinter.log(key, "Demotion Options: [" + String.join(", ", demotionOptions.stream().map(charClass -> (textData.getStringAtIndex(charClass.getNameIndex(), true))).collect(Collectors.toList())) + "]");
                if (!demotionOptions.isEmpty()) {
                    alreadyChosenTargetClass = demotionOptions.get(rng.nextInt(demotionOptions.size()));
                } else {
                    alreadyChosenTargetClass = fillSourceClass;
                }

                if (recruitOptions != null && recruitOptions.classMode == ClassMode.USE_SLOT) {
                    alreadyChosenTargetClass = slotSourceClass;
                }

                DebugPrinter.log(key, "Selected Class: " + (alreadyChosenTargetClass != null ? textData.getStringAtIndex(alreadyChosenTargetClass.getNameIndex(), true) : "None"));
			}

			dto.promoBonuses.add(fillSourceClass.getPromoBonuses()
					.multiply(-1)); // Multiply the promotion bonuses with -1 to make them demotion bonuses 
			
			// For some reason, some promoted class seem to have lower bases than their unpromoted variants (FE8 lords are an example). If our demoted class has higher bases, adjust downwards
			dto.promoBonuses.add(GBAFEStatDto.downAdjust(alreadyChosenTargetClass.getBases(), fillSourceClass.getBases()));
		} else {
			// Transfer as is.
            if (alreadyChosenTargetClass == null) {
                if (recruitOptions != null && recruitOptions.classMode == ClassMode.USE_FILL) { alreadyChosenTargetClass = fillSourceClass; }
                else if (recruitOptions != null && recruitOptions.classMode == ClassMode.USE_SLOT) { alreadyChosenTargetClass = slotSourceClass; }
                else {
                    // This shouldn't happen, but default to fill.
                    alreadyChosenTargetClass = fillSourceClass;
                }
            }
			DebugPrinter.log(key, "No Promotion/Demotion Needed. Class: " + (alreadyChosenTargetClass != null ? textData.getStringAtIndex(alreadyChosenTargetClass.getNameIndex(), true) : "None"));
		}
		
		dto.targetClass = alreadyChosenTargetClass;
		
		DebugPrinter.log(key, String.format("Finished Adjusting class for character %s, fill sourceClass %s, slot source class %s, new class %s, should receive %d auto levels, and Promotion bonuses: %s",
				fill.displayString(), fillSourceClass.displayString(), slotSourceClass.displayString(), dto.targetClass.displayString(), dto.levelAdjustment, dto.promoBonuses));
		return dto;
	}


    public GBAFEStatDto performStatAdjustment(GBAFEStatDto fillStats, GBAFECharacterData linkedSlot, GBAFEStatDto growthsForAutoLevels, ClassAdjustmentDto classAdjustmentDto,
                                              GBAFEClassData slotSourceClass, GBAFEClassData fillSourceClass) {
        GBAFEStatDto newStats = new GBAFEStatDto();
        List<GBAFEStatDto> promoBonuses = classAdjustmentDto.promoBonuses;
        GBAFEClassData targetClass = classAdjustmentDto.targetClass;
        int levelsToAdd = classAdjustmentDto.levelAdjustment;
        switch(autolevelingParameters.baseMode) {
            case AUTOLEVEL:

                // Calculate the auto leveled personal bases
                newStats = autolevel(fillStats, growthsForAutoLevels,
                        promoBonuses, levelsToAdd, targetClass, DebugPrinter.Key.GBA_RANDOM_RECRUITMENT);

                DebugPrinter.log(DebugPrinter.Key.GBA_RANDOM_RECRUITMENT, String.format("== New Bases ==%n%s", newStats.toString()));
                return newStats;

            case MATCH_SLOT:
                newStats.add(linkedSlot.getBases()) // Add the original Bases of the slot
                        .add(targetClass.getBases()) // Add the stats from the new class
                        .subtract(slotSourceClass.getBases()); // remove the stats from the original class
                return newStats;

            case RELATIVE_TO_SLOT:
                newStats = new GBAFEStatDto();
                newStats.hp = linkedSlot.getBaseHP() + slotSourceClass.getBaseHP() - targetClass.getBaseHP(); // Keep HP the same logic as above.
                GBAFEStatDto slotStats = linkedSlot.getBases().add(slotSourceClass.getBases());

                // Set HP to an absurdly high value so that the HP values will be mapped to one another and we can ignore them easily
                slotStats.hp = Integer.MAX_VALUE;
                fillStats.hp = Integer.MAX_VALUE;


                List<Integer> mappedStats = RelativeValueMapper.mappedValues(slotStats.asList(), fillStats.asList());

                // ignore the index 0 in the list, as that is HP, and will be handled separately
                newStats.str = Math.max(mappedStats.get(1) - targetClass.getBaseSTR(), -1 * targetClass.getBaseSTR());
                newStats.skl = Math.max(mappedStats.get(2) - targetClass.getBaseSKL(), -1 * targetClass.getBaseSKL());
                newStats.spd = Math.max(mappedStats.get(3) - targetClass.getBaseSPD(), -1 * targetClass.getBaseSPD());
                newStats.def = Math.max(mappedStats.get(4) - targetClass.getBaseDEF(), -1 * targetClass.getBaseDEF());
                newStats.res = Math.max(mappedStats.get(5) - targetClass.getBaseRES(), -1 * targetClass.getBaseRES());
                newStats.lck = Math.max(mappedStats.get(6) - targetClass.getBaseLCK(), -1 * targetClass.getBaseLCK());
                return newStats;

        }

        return newStats;
    }
	

	/**
	 * Calculates the personal bases for the unit after applying the promotions /
	 * demotions and required autolevels.
	 * 
	 * Ensures that the Personal bases don't lead to over / underflows by clamping.
	 * 
	 * @param bases          the personal bases prior to the current change.
	 * @param growths        the growths that will be used for the auto levels
	 * @param promoBonuses   a list with all the promotion (or demotion) bonuses
	 *                       that will be given to the unit
	 * @param levelsRequired the number of autolevels to apply
	 * @param targetClass    the class that the unit will be in, used for clamping
	 * @param key            the Key for which to log the changes
	 * @return a GBAFEStatDto with the new personal bases.
	 */
	public GBAFEStatDto autolevel(GBAFEStatDto bases, GBAFEStatDto growths, List<GBAFEStatDto> promoBonuses
			, int levelsRequired, GBAFEClassData targetClass, DebugPrinter.Key key) {
		// initialize a new DAO with the original Bases
		GBAFEStatDto newBases = new GBAFEStatDto(bases);
		GBAFEStatDto classBases = targetClass.getBases();
		DebugPrinter.log(key, String.format("Original Bases: %s%n", newBases.toString()));
		// Add all necessary promotion or demotions
		if (!promoBonuses.isEmpty()) {
			GBAFEStatDto totalPromoChanges = new GBAFEStatDto(promoBonuses);
			DebugPrinter.log(key, String.format("Total Promotion Changes: %s%n", totalPromoChanges.toString()));
			newBases.add(totalPromoChanges); // Demotion bonuses are already negative
			DebugPrinter.log(key, String.format("Stats after Promotion / Demotion: %s%n", newBases.toString()));
		}

		// add the required number of levels
		newBases.add(calculateLevels(growths, levelsRequired, key));
		DebugPrinter.log(key, String.format("Stats after Autolevels: %s%n", newBases.toString()));
		
		// Now we have the calculated auto leveled stats.
		// Here we must ensure that the character doesn't over or underflow, so we add the Stats to the class bases, and clamp it to the max and min stats.
		GBAFEStatDto totalBases = new GBAFEStatDto(Arrays.asList(newBases));
		totalBases = totalBases.clamp(GBAFEStatDto.MINIMUM_STATS, targetClass.getCaps()); // Clamp to prevent over or underflow
		DebugPrinter.log(key, String.format("Theoretical final Stats after clamp: %s%n", totalBases.toString()));

		// Now we remove the Class bases again and are left with proper Personal Bases
		newBases = totalBases.subtract(classBases);
		DebugPrinter.log(key, String.format("Proper personal bases: %s", newBases.toString()));
		return newBases;
	}

	public GBAFEStatDto calculateLevels(GBAFEStatDto growths, int levelsRequired, DebugPrinter.Key key) {
		GBAFEStatDto levelGains = new GBAFEStatDto();
		levelGains.hp += (int) Math.floor((growths.hp / 100.0) * levelsRequired);
		levelGains.str += (int) Math.floor((growths.str / 100.0) * levelsRequired);
		levelGains.skl += (int) Math.floor((growths.skl / 100.0) * levelsRequired);
		levelGains.spd += (int) Math.floor((growths.spd / 100.0) * levelsRequired);
		levelGains.def += (int) Math.floor((growths.def / 100.0) * levelsRequired);
		levelGains.res += (int) Math.floor((growths.res / 100.0) * levelsRequired);
		levelGains.lck += (int) Math.floor((growths.lck / 100.0) * levelsRequired);
		DebugPrinter.log(key, String.format("Stats from %d Autolevels: %s with growths %s %n", levelsRequired, levelGains.toString(), growths.toString()));
		return levelGains;
	}

	/**
	 * Method to unify weapon rank transfer from one class to another
	 *
	 * @param slot {@link GBAFECharacterData} which contains the original weapon ranks of the current character slot
	 * @param sourceClass the sourceClass that the character originated from
	 * @param targetClass the targetClass that the character is now
	 */
	public void transferWeaponRanks(GBAFECharacterData slot, GBAFEClassData sourceClass, GBAFEClassData targetClass) {
		WeaponRanks weaponRanks = new WeaponRanks(slot, sourceClass);
		WeaponRanks targetClassRanks = targetClass.getWeaponRanks();

		List<WeaponRank> rankValues = weaponRanks.asList().stream()
				.filter(rank -> !WeaponRank.NONE.equals(rank))
				.sorted(WeaponRank::compare)
				.collect(Collectors.toList());

		if (rankValues.isEmpty()) {
			slot.setWeaponRanks(targetClassRanks);
			return;
		}

		int targetWeaponUsage = targetClass.getWeaponRanks().getTypes().size();

		while (rankValues.size() > targetWeaponUsage) {
			rankValues.remove(0); // Remove the lowest rank if we're filling less weapons than we have to work with.
		}

		for (WeaponType weaponType : WeaponType.getWeaponTypes()) {
			if (WeaponRank.NONE.equals(targetClassRanks.rankForType(weaponType))) {
				slot.setWeaponRank(weaponType, WeaponRank.NONE);
				continue;
			}
			WeaponRank randomRank = rankValues.get(rng.nextInt(rankValues.size()));
			if (rankValues.size() > 1) {
				rankValues.remove(randomRank);
			}

			// The lowest rank dark tome is D, so make sure to round up
			if (WeaponType.DARK.equals(weaponType) && WeaponRank.D.isHigherThan(randomRank)) {
				randomRank = WeaponRank.D;
			}
			slot.setWeaponRank(weaponType, randomRank);
		}
	}

}
