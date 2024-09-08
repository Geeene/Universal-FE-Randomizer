package random.gba.randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import fedata.gba.GBAFEChapterData;
import fedata.gba.GBAFEChapterItemData;
import fedata.gba.GBAFEChapterUnitData;
import fedata.gba.GBAFEItemData;
import fedata.gcnwii.fe9.FE9Data.Chapter;
import random.gba.loader.ChapterLoader;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.ItemDataLoader;
import random.general.PoolDistributor;
import random.general.WeightedDistributor;
import util.WhyDoesJavaNotHaveThese;

public class RandomRandomizer {
	static final int rngSalt = 27682;
	
	public static void randomizeRewards(ItemDataLoader itemData, ChapterLoader chapterData, boolean includePromoWeapons, Random rng) {
		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			GBAFEChapterItemData[] allRewards = chapter.allRewards();
			for (GBAFEChapterItemData chapterItem : allRewards) {
				int itemID = chapterItem.getItemID();
				GBAFEItemData[] relatedItems = itemData.relatedItems(itemID);
				GBAFEItemData[] allPossibleItems = itemData.getChestRewards(includePromoWeapons);
				
				if (relatedItems.length == 0 && allPossibleItems.length == 0) {
					continue;
				}
				
				int chanceOfRelatedItem = 50;
				if (relatedItems.length == 0) {
					chanceOfRelatedItem = 0;
				}
				
				int random = rng.nextInt(100);
				if (random < chanceOfRelatedItem) {
					int randomIndex = rng.nextInt(relatedItems.length);
					chapterItem.setItemID(relatedItems[randomIndex].getID());
				} else {
					int randomIndex = rng.nextInt(allPossibleItems.length);
					chapterItem.setItemID(allPossibleItems[randomIndex].getID());
				}
			}
		}
	}

	public static void addRandomEnemyDrops(int chance, CharacterDataLoader charData, ItemDataLoader itemData, ChapterLoader chapterData, Random rng) {
		GBAFEChapterData[] chapters = chapterData.allChapters();
		List<WeightedDistributor<GBAFEItemData>> distributors = getDistributorsForDrops(itemData);

		for (int i = 0; i < chapters.length; i++) {
			GBAFEChapterData chapter = chapters[i];
			for (GBAFEChapterUnitData unit : chapter.allUnits()) {
				if (!unit.isEnemy() || charData.isBossCharacterID(unit.getCharacterNumber())) {
					continue;
				}

				if (rng.nextInt(100) >= chance) {
					continue;
				}

				unit.setUnitToDropLastItem(true);
				if (rng.nextInt(4) != 0) {
					unit.giveItem(WhyDoesJavaNotHaveThese.clamp(Math.round(i / (chapters.length / 4)), 1,4) -1);
				}
			}
		}
	}

	private static List<WeightedDistributor<GBAFEItemData>> getDistributorsForDrops(ItemDataLoader itemData) {
		WeightedDistributor<GBAFEItemData> firstQuarter = new WeightedDistributor<>();
		WeightedDistributor<GBAFEItemData> secondQuarter = new WeightedDistributor<>();
		WeightedDistributor<GBAFEItemData> thirdQuarter = new WeightedDistributor<>();
		WeightedDistributor<GBAFEItemData> fourthQuarter = new WeightedDistributor<>();
		for (GBAFEItemData item : itemData.commonDrops()) {
			firstQuarter.addItem(item, 90);
			secondQuarter.addItem(item, 70);
			thirdQuarter.addItem(item, 60);
			fourthQuarter.addItem(item, 50);
		}
		for (GBAFEItemData item : itemData.uncommonDrops()) {
			firstQuarter.addItem(item, 9);
			secondQuarter.addItem(item, 20);
			thirdQuarter.addItem(item, 30);
			fourthQuarter.addItem(item, 30);
		}
		for (GBAFEItemData item : itemData.rareDrops()) {
			firstQuarter.addItem(item, 1);
			secondQuarter.addItem(item, 10);
			thirdQuarter.addItem(item, 10);
			fourthQuarter.addItem(item, 20);
		}
		return Arrays.asList(firstQuarter, secondQuarter, thirdQuarter, fourthQuarter);
	}
}
