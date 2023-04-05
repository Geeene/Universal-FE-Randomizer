package fedata.gba.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEItemData;
import fedata.gba.GBAFESpellAnimationCollection;
import fedata.gba.GBAFEWeaponRankDto;
import random.gba.loader.ItemDataLoader.AdditionalData;

public interface GBAFEItemProvider {
	
	public static class WeaponRanks {
		public final WeaponRank swordRank;
		public final WeaponRank lanceRank;
		public final WeaponRank axeRank;
		public final WeaponRank bowRank;
		public final WeaponRank animaRank;
		public final WeaponRank lightRank;
		public final WeaponRank darkRank;
		public final WeaponRank staffRank;
		
		public WeaponRanks(GBAFECharacterData character, GBAFEClassData characterClass) {
			if (characterClass == null) {
				swordRank = WeaponRank.valueOf(character.getSwordRank());
				lanceRank = WeaponRank.valueOf(character.getLanceRank());
				axeRank = WeaponRank.valueOf(character.getAxeRank());
				bowRank = WeaponRank.valueOf(character.getBowRank());
				animaRank = WeaponRank.valueOf(character.getAnimaRank());
				lightRank = WeaponRank.valueOf(character.getLightRank());
				darkRank = WeaponRank.valueOf(character.getDarkRank());
				staffRank = WeaponRank.valueOf(character.getStaffRank());
			} else {
				swordRank = WeaponRank.valueOf(character.getSwordRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getSwordRank()) : WeaponRank.valueOf(character.getSwordRank());
				lanceRank = WeaponRank.valueOf(character.getLanceRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getLanceRank()) : WeaponRank.valueOf(character.getLanceRank());
				axeRank = WeaponRank.valueOf(character.getAxeRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getAxeRank()) : WeaponRank.valueOf(character.getAxeRank());
				bowRank = WeaponRank.valueOf(character.getBowRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getBowRank()) : WeaponRank.valueOf(character.getBowRank());
				animaRank = WeaponRank.valueOf(character.getAnimaRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getAnimaRank()) : WeaponRank.valueOf(character.getAnimaRank());
				lightRank = WeaponRank.valueOf(character.getLightRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getLightRank()) : WeaponRank.valueOf(character.getLightRank());
				darkRank = WeaponRank.valueOf(character.getDarkRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getDarkRank()) : WeaponRank.valueOf(character.getDarkRank());
				staffRank = WeaponRank.valueOf(character.getStaffRank()) == WeaponRank.NONE ? WeaponRank.valueOf(characterClass.getStaffRank()) : WeaponRank.valueOf(character.getStaffRank());
			}
		}
		
		public WeaponRanks(GBAFEClassData characterClass, GBAFEItemProvider provider) {
			swordRank = WeaponRank.valueOf(characterClass.getSwordRank());
			lanceRank = WeaponRank.valueOf(characterClass.getLanceRank());
			axeRank = WeaponRank.valueOf(characterClass.getAxeRank());
			bowRank = WeaponRank.valueOf(characterClass.getBowRank());
			animaRank = WeaponRank.valueOf(characterClass.getAnimaRank());
			lightRank = WeaponRank.valueOf(characterClass.getLightRank());
			darkRank = WeaponRank.valueOf(characterClass.getDarkRank());
			staffRank = WeaponRank.valueOf(characterClass.getStaffRank());
		}
		
	}
	
	public long itemTablePointer();
	public int numberOfItems();
	public int bytesPerItem();
	
	public GBAFEItem[] allItems();
	
	public GBAFEItem itemWithID(int itemID);
	public GBAFEItem basicWeaponOfType(WeaponType type);
	
	public int rankValueForRank(WeaponRank rank); 
	public int getHighestWeaponRankValue();
	
	public Set<GBAFEItem> allWeapons();
	public Set<GBAFEItem> weaponsWithStatBoosts();
	public Set<GBAFEItem> weaponsWithEffectiveness();
	public Set<GBAFEItem> weaponsOfTypeUpToRank(WeaponType type, WeaponRank rank, Boolean rangedOnly, Boolean requiresMelee);
	public Set<GBAFEItem> weaponsOfTypeAndEqualRank(WeaponType type, WeaponRank rank, Boolean rangedOnly, Boolean requiresMelee, Boolean allowLower);
	public Set<GBAFEItem> healingStaves(WeaponRank maxRank);
	public Set<GBAFEItem> prfWeaponsForClassID(int classID);
	public Set<GBAFEItem> allPotentialChestRewards();
	public Set<GBAFEItem> relatedItemsToItem(GBAFEItemData item);
	public Set<GBAFEItem> weaponsLockedToClass(int classID);
	public Set<GBAFEItem> weaponsForClass(int classID);
	public Set<GBAFEItem> basicWeaponsForClass(int classID);
	public Set<GBAFEItem> comparableWeaponsForClass(int classID, GBAFEWeaponRankDto ranks, GBAFEItemData originalItem, boolean strict);
	public Set<GBAFEItem> formerThiefInventory();
	public Set<GBAFEItem> thiefItemsToRemove();
	public Set<GBAFEItem> specialItemsToRetain();
	public Set<GBAFEItem> itemKitForSpecialClass(int classID, Random rng);
	public Set<GBAFEItem> playerOnlyWeapons();
	
	public Set<GBAFEItem> promoWeapons();
	public Set<GBAFEItem> poisonWeapons();
	
	public Set<GBAFEItem> commonDrops();
	public Set<GBAFEItem> uncommonDrops();
	public Set<GBAFEItem> rareDrops();
	
	public String statBoostStringForWeapon(GBAFEItem weapon);
	public String effectivenessStringForWeapon(GBAFEItem weapon, Boolean shortString);
	
	public AdditionalData effectivenessPointerType(long effectivenessPtr);
	
	public GBAFEItemData itemDataWithData(byte[] data, long offset, int itemID); // itemID is required for FE8
	
	public List<GBAFEClass> knightCavEffectivenessClasses();
	public List<GBAFEClass> knightEffectivenessClasses();
	public List<GBAFEClass> cavalryEffectivenessClasses();
	public List<GBAFEClass> dragonEffectivenessClasses();
	public List<GBAFEClass> flierEffectivenessClasses();
	public List<GBAFEClass> myrmidonEffectivenessClasses(); // FE7 and FE8
	public List<GBAFEClass> monsterEffectivenessClasses(); // FE8 only
	
	public GBAFEPromotionItem[] allPromotionItems();
	
	public List<GBAFEClass> additionalClassesForPromotionItem(GBAFEPromotionItem promotionItem, List<Byte> existingClassIDs);
	
	public long spellAnimationTablePointer();
	public int numberOfAnimations();
	public int bytesPerAnimation();
	public GBAFESpellAnimationCollection spellAnimationCollectionAtAddress(byte[] data, long offset);

}
