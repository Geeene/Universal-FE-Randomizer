package random.gba.randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import fedata.gba.GBAFEChapterData;
import fedata.gba.GBAFEChapterUnitData;
import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEHolisticCharacter;
import fedata.gba.GBAFEItemData;
import fedata.gba.GBAFEStatDto;
import fedata.gba.GBAFEWeaponRankDto;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponType;
import random.gba.loader.ChapterLoader;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.ClassDataLoader;
import random.gba.loader.ItemDataLoader;
import ui.model.EnemyOptions;

public class EnemyBuffer {
	
	static final int rngSalt = 252521;
	
	// Enemy growths top out at 127. Going above that will underflow back to 0.
	private static int MaximumGrowthRate = 127;

	public static void buffMinionGrowthRates(int buffAmount, ClassDataLoader classData, EnemyOptions.BuffStats buffStats) {
		GBAFEClassData[] allClasses = classData.allClasses();
		for (GBAFEClassData currentClass : allClasses) {
			if (buffStats.hp) { currentClass.setHPGrowth(Math.min(MaximumGrowthRate, currentClass.getHPGrowth() + buffAmount)); }
			if (buffStats.str) { currentClass.setSTRGrowth(Math.min(MaximumGrowthRate, currentClass.getSTRGrowth() + buffAmount)); }
			if (buffStats.skl) { currentClass.setSKLGrowth(Math.min(MaximumGrowthRate, currentClass.getSKLGrowth() + buffAmount)); }
			if (buffStats.spd) { currentClass.setSPDGrowth(Math.min(MaximumGrowthRate, currentClass.getSPDGrowth() + buffAmount)); }
			if (buffStats.def) { currentClass.setDEFGrowth(Math.min(MaximumGrowthRate, currentClass.getDEFGrowth() + buffAmount)); }
			if (buffStats.res) { currentClass.setRESGrowth(Math.min(MaximumGrowthRate, currentClass.getRESGrowth() + buffAmount)); }
			if (buffStats.lck) { currentClass.setLCKGrowth(Math.min(MaximumGrowthRate, currentClass.getLCKGrowth() + buffAmount)); }
		}
	}
	
	public static void buffBossStatsLinearly(int maxBuff, CharacterDataLoader charData, ClassDataLoader classData, EnemyOptions.BuffStats buffStats) {
		for (GBAFECharacterData boss : charData.bossCharacters()) {
			double appearanceFactor = (double)charData.appearanceChapter(boss) / (double)charData.chapterCount();
			int buffAmount = Math.min(maxBuff, (int)Math.ceil(maxBuff * appearanceFactor));
			applyBuff(boss.getID(), buffStats, buffAmount);
		}
	}
	
	public static void scaleEnemyGrowthRates(int scaleAmount, ClassDataLoader classData, EnemyOptions.BuffStats buffStats) {
		GBAFEClassData[] allClasses = classData.allClasses();
		double multiplier = 1 + (double)scaleAmount / 100.0;
		for (GBAFEClassData currentClass : allClasses) {
			GBAFEStatDto newGrowths = currentClass.getGrowths().multiply(multiplier);
			if (buffStats.hp) { currentClass.setHPGrowth(newGrowths.hp); }
			if (buffStats.str) { currentClass.setSTRGrowth(newGrowths.str); }
			if (buffStats.skl) { currentClass.setSKLGrowth(newGrowths.skl); }
			if (buffStats.spd) { currentClass.setSPDGrowth(newGrowths.spd); }
			if (buffStats.def) { currentClass.setDEFGrowth(newGrowths.def); }
			if (buffStats.res) { currentClass.setRESGrowth(newGrowths.res); }
			if (buffStats.lck) { currentClass.setLCKGrowth(newGrowths.lck); }
		}
	}
	
	public static void buffBossStatsWithEaseInOutCurve(int maxBuff, CharacterDataLoader charData, ClassDataLoader classData, EnemyOptions.BuffStats buffStats) {
		for (GBAFECharacterData boss : charData.bossCharacters()) {
			double appearanceFactor = (double)charData.appearanceChapter(boss) / (double)charData.chapterCount();
			appearanceFactor = Math.pow(appearanceFactor, 2) / (Math.pow(appearanceFactor, 2) + Math.pow(1 - appearanceFactor, 2));
			int buffAmount = Math.min(maxBuff, (int)Math.ceil(maxBuff * appearanceFactor));
			
			applyBuff(boss.getID(), buffStats, buffAmount);
		}
	}
	
	private static void applyBuff(int charId, EnemyOptions.BuffStats buffStats, int buffAmount) {
		GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(charId);
		GBAFEStatDto stats = holisticCharacter.getStats();
		if (buffStats.hp)  { stats.hp += buffAmount; }
		if (buffStats.str) { stats.str += buffAmount; }
		if (buffStats.skl) { stats.skl += buffAmount; }
		if (buffStats.spd) { stats.spd += buffAmount; }
		if (buffStats.def) { stats.def += buffAmount; }
		if (buffStats.res) { stats.res += buffAmount; }
		if (buffStats.lck) { stats.lck += buffAmount; }
		holisticCharacter.setStats(stats);
	}
	
	public static void improveMinionWeapons(int probability, CharacterDataLoader charactersData, 
			ClassDataLoader classData, ChapterLoader chapterData, ItemDataLoader itemData, Random rng) {
		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			for (GBAFEChapterUnitData chapterUnit : chapter.allUnits()) {
				int leaderID = chapterUnit.getLeaderID();
				if (charactersData.isBossCharacterID(leaderID) || (chapterUnit.isEnemy() && chapterUnit.isAutolevel())) {
					GBAFEClassData originalClass = classData.classForID(chapterUnit.getStartingClass());
					if (originalClass == null) {
						continue;
					}
					
					if (classData.isThief(originalClass.getID())) {
						continue;
					}
					
					if (rng.nextInt(100) < probability) {
						upgradeWeapons(chapterUnit, classData, itemData, rng);
					}
				}
			}
		}
	}
	
	public static void improveBossWeapons(int probability, CharacterDataLoader charactersData, 
			ClassDataLoader classData, ChapterLoader chapterData, ItemDataLoader itemData, Random rng) {
		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			for (GBAFEChapterUnitData chapterUnit : chapter.allUnits()) {
				if (!charactersData.isBossCharacterID(chapterUnit.getCharacterNumber())) { continue; }
				if (rng.nextInt(100) < probability) {
					upgradeWeapons(chapterUnit, classData, itemData, rng);
				}
				GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(chapterUnit.getCharacterNumber());
				GBAFEWeaponRankDto weaponRanks = holisticCharacter.getWeaponRanks();

				WeaponRank highestRank = WeaponRank.valueOf(itemData.getHighestWeaponRank());
				
				if (weaponRanks.sword != WeaponRank.NONE) { weaponRanks.sword = highestRank; }
				if (weaponRanks.lance != WeaponRank.NONE) { weaponRanks.lance = highestRank; }
				if (weaponRanks.axe   != WeaponRank.NONE) { weaponRanks.axe   = highestRank; }
				if (weaponRanks.bow   != WeaponRank.NONE) { weaponRanks.bow   = highestRank; }
				if (weaponRanks.anima != WeaponRank.NONE) { weaponRanks.anima = highestRank; }
				if (weaponRanks.light != WeaponRank.NONE) { weaponRanks.light = highestRank; }
				if (weaponRanks.dark  != WeaponRank.NONE) { weaponRanks.dark  = highestRank; }
				if (weaponRanks.staff != WeaponRank.NONE) { weaponRanks.staff = highestRank; }
				
				holisticCharacter.setWeaponRanks(weaponRanks);
			}
		}
	}
	
	private static void upgradeWeapons(GBAFEChapterUnitData unit, ClassDataLoader classData, ItemDataLoader itemData, Random rng) {
		GBAFEClassData unitClass = classData.classForID(unit.getStartingClass());
		
		for(int i = 1; i <= 4; i++) {
			// Get the current Item of the slot
			GBAFEItemData item = itemData.itemWithID(unit.getItem(i));
			// If the slot isn't empty or a non-weapon Item. And the Weapon Item isn't already A Rank
			if (item != null && item.getType() != WeaponType.NOT_A_WEAPON && item.getWeaponRank() != WeaponRank.A) {
				// Get all items that could be considered better
				GBAFEItemData[] improvedItems = availableItems(unitClass, item, itemData);
				// If there is at least one then select one of them
				if (improvedItems.length > 0) {
					GBAFEItemData replacementItem = improvedItems[rng.nextInt(improvedItems.length)];
					unit.setItem(i, replacementItem.getID());
				}

			}

		}
	}
	
	private static GBAFEItemData[] availableItems(GBAFEClassData characterClass, GBAFEItemData original, ItemDataLoader itemData) {
		WeaponRank rank = WeaponRank.nextRankHigherThanRank(original.getWeaponRank());
		WeaponType type = original.getType();
		
		if (rank == WeaponRank.NONE) {
			return new GBAFEItemData[] {};
		}
		
		List<GBAFEItemData> items = new ArrayList<GBAFEItemData>();
		
		GBAFEItemData[] improvedItems = itemData.itemsOfTypeAndEqualRank(type, rank, false, false, true);
		items.addAll(Arrays.asList(improvedItems));
		
		GBAFEItemData[] prfWeapons = itemData.prfWeaponsForClass(characterClass.getID());
		if (prfWeapons != null) {
			items.addAll(Arrays.asList(prfWeapons));
		}
		GBAFEItemData[] classWeapons = itemData.lockedWeaponsToClass(characterClass.getID());
		if (classWeapons != null) {
			items.addAll(Arrays.asList(classWeapons));
		}
		
		GBAFEItemData[] sameRank = itemData.itemsOfTypeAndEqualRank(type, original.getWeaponRank(), false, false, false);
		for (GBAFEItemData weapon : sameRank) {
			if (weapon.getMight() > original.getMight()) {
				items.add(weapon);
			}
		}
		
		List<GBAFEItemData> filteredList = items.stream().filter(item -> (!itemData.isPlayerOnly(item.getID()))).collect(Collectors.toList());
		
		return filteredList.toArray(new GBAFEItemData[filteredList.size()]);
	}
}
