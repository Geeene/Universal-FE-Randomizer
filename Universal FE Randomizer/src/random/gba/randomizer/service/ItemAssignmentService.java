package random.gba.randomizer.service;

import java.util.*;

import fedata.gba.GBAFEChapterData;
import fedata.gba.GBAFEChapterItemData;
import fedata.gba.GBAFEChapterUnitData;
import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEItemData;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponRanks;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase;
import random.gba.loader.*;
import random.gba.randomizer.AbstractGBARandomizerComponent;
import random.gba.randomizer.ClassRandomizer;
import ui.model.ItemAssignmentOptions;
import util.DebugPrinter;
import util.OptionRecorder;

/**
 * Service responsible for giving new usable items to a character who had their class changed
 */
public class ItemAssignmentService extends AbstractGBARandomizerComponent {

    public static ItemAssignmentService instance;

    public static final int rngSalt = 458489168;

    public ItemAssignmentService(OptionRecorder.GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, FEBase.GameType type) {
        super(allOptions, dataLoaders, rng, type);
        instance = this;
    }

    public void assignNewItems(GBAFECharacterData slot, GBAFEClassData targetClass) {
		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			GBAFEChapterItemData reward = chapter.chapterItemGivenToCharacter(slot.getID());
			if (reward != null) {
				GBAFEItemData item = null;
				GBAFEItemData[] prfWeapons = itemData.prfWeaponsForClass(targetClass.getID());
				if (prfWeapons.length > 0) {
					item = prfWeapons[rng.nextInt(prfWeapons.length)];
				} else {
					item = itemData.getRandomWeaponForCharacter(slot, false, false,
							charData.isEnemyAtAnyPoint(slot.getID()), itemAssignmentOptions.assignPromoWeapons,
                            itemAssignmentOptions.assignPoisonWeapons, rng);
				}

				if (item != null) {
					reward.setItemID(item.getID());
				}
			}

			for (GBAFEChapterUnitData unit : chapter.allUnits()) {
				if (unit.getCharacterNumber() == slot.getID()) {
					unit.setStartingClass(targetClass.getID());

					// Set Inventory.
					validateCharacterInventory(slot, targetClass, unit,
							charData.characterIDRequiresRange(slot.getID()),
                            charData.characterIDRequiresMelee(slot.getID()), false);
					if (charData.isThiefCharacterID(slot.getID())) {
						validateFormerThiefInventory(unit);
					}
					validateSpecialClassInventory(unit);
				}
			}
		}
	}

    public void validateCharacterInventory(GBAFECharacterData character, GBAFEClassData charClass, GBAFEChapterUnitData chapterUnit, Boolean ranged, Boolean melee, Boolean forceBasic) {
        int item1ID = chapterUnit.getItem1();
        GBAFEItemData item1 = itemData.itemWithID(item1ID);
        int item2ID = chapterUnit.getItem2();
        GBAFEItemData item2 = itemData.itemWithID(item2ID);
        int item3ID = chapterUnit.getItem3();
        GBAFEItemData item3 = itemData.itemWithID(item3ID);
        int item4ID = chapterUnit.getItem4();
        GBAFEItemData item4 = itemData.itemWithID(item4ID);

        GBAFEItemData[] prfWeapons = itemData.prfWeaponsForClass(charClass.getID());
        Set<Integer> prfIDs = new HashSet<Integer>();
        for (GBAFEItemData prfWeapon : prfWeapons) {
            prfIDs.add(prfWeapon.getID());
        }

        Boolean isHealerClass = charClass.getStaffRank() > 0;
        Boolean hasAtLeastOneHealingStaff = false;

        Boolean classCanAttack = classData.canClassAttack(charClass.getID());
        Boolean hasAtLeastOneWeapon = false;

        Set<GBAFEItemData> itemsToRetain = itemsToRetain(chapterUnit);

        DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Validating inventory for character 0x" + Integer.toHexString(character.getID()) + " (" + textData.getStringAtIndex(character.getNameIndex(), true) +") in class 0x" + Integer.toHexString(charClass.getID()) + " (" + textData.getStringAtIndex(charClass.getNameIndex(), true) + ")");
        DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Original Inventory: [0x" + Integer.toHexString(item1ID) + (item1 == null ? "" : " (" + textData.getStringAtIndex(item1.getNameIndex(), true) + ")") + ", 0x" + Integer.toHexString(item2ID) + (item2 == null ? "" : " (" + textData.getStringAtIndex(item2.getNameIndex(), true) + ")") + ", 0x" + Integer.toHexString(item3ID) + (item3 == null ? "" : " (" + textData.getStringAtIndex(item3.getNameIndex(), true) + ")") + ", 0x" + Integer.toHexString(item4ID) + (item4 == null ? "" : " (" + textData.getStringAtIndex(item4.getNameIndex(), true) + ")") + "]");

        if (itemData.isWeapon(item1) || (item1 != null && item1.getType() == WeaponType.STAFF)) {
            if (!canCharacterUseItem(character, item1) || (item1.getWeaponRank() == WeaponRank.PRF && !prfIDs.contains(item1ID))) {
                GBAFEItemData replacementItem = itemData.getBasicWeaponForCharacter(character, ranged, false, rng);
                if (!forceBasic) {
                    if (itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.ANY_USABLE || ranged || melee) {
                        replacementItem = itemData.getRandomWeaponForCharacter(character, ranged, melee, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    } else {
                        replacementItem = itemData.getSidegradeWeapon(character, charClass, item1, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    }
                }

                if (item1.getWeaponRank() == WeaponRank.S) {
                    GBAFEItemData[] topWeapons = topRankWeaponsForClass(charClass);
                    if (topWeapons.length > 0) {
                        replacementItem = topWeapons[rng.nextInt(topWeapons.length)];
                    }
                }
                if (replacementItem != null) {
                    if (replacementItem.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(replacementItem.getID()); }
                    else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(replacementItem); }
                    chapterUnit.setItem1(replacementItem.getID());
                } else {
                    chapterUnit.setItem1(0);
                }
            } else {
                if (item1.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(item1.getID()); }
                else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(item1); }
            }
        }

        if (itemData.isWeapon(item2) || (item2 != null && item2.getType() == WeaponType.STAFF)) {
            if (!canCharacterUseItem(character, item2) || (item2.getWeaponRank() == WeaponRank.PRF && !prfIDs.contains(item2ID))) {
                GBAFEItemData replacementItem = itemData.getBasicWeaponForCharacter(character, ranged, false, rng);
                if (!forceBasic) {
                    if (itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.ANY_USABLE || ranged || melee) {
                        replacementItem = itemData.getRandomWeaponForCharacter(character, ranged, melee, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    } else {
                        replacementItem = itemData.getSidegradeWeapon(character, charClass, item2, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    }
                }

                if (item2.getWeaponRank() == WeaponRank.S) {
                    GBAFEItemData[] topWeapons = topRankWeaponsForClass(charClass);
                    if (topWeapons.length > 0) {
                        replacementItem = topWeapons[rng.nextInt(topWeapons.length)];
                    }
                }
                if (replacementItem != null) {
                    if (replacementItem.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(replacementItem.getID()); }
                    else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(replacementItem); }
                    chapterUnit.setItem2(replacementItem.getID());
                } else {
                    chapterUnit.setItem2(0);
                }
            } else {
                if (item2.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(item2.getID()); }
                else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(item2); }
            }
        }

        if (itemData.isWeapon(item3) || (item3 != null && item3.getType() == WeaponType.STAFF)) {
            if (!canCharacterUseItem(character, item3) || (item3.getWeaponRank() == WeaponRank.PRF && !prfIDs.contains(item3ID))) {
                GBAFEItemData replacementItem = itemData.getBasicWeaponForCharacter(character, ranged, false, rng);
                if (!forceBasic) {
                    if (itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.ANY_USABLE || ranged || melee) {
                        replacementItem = itemData.getRandomWeaponForCharacter(character, ranged, melee, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    } else {
                        replacementItem = itemData.getSidegradeWeapon(character, charClass, item3, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    }
                }

                if (item3.getWeaponRank() == WeaponRank.S) {
                    GBAFEItemData[] topWeapons = topRankWeaponsForClass(charClass);
                    if (topWeapons.length > 0) {
                        replacementItem = topWeapons[rng.nextInt(topWeapons.length)];
                    }
                }
                if (replacementItem != null) {
                    if (replacementItem.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(replacementItem.getID()); }
                    else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(replacementItem); }
                    chapterUnit.setItem3(replacementItem.getID());
                } else {
                    chapterUnit.setItem3(0);
                }
            } else {
                if (item3.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(item3.getID()); }
                else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(item3); }
            }
        }

        if (itemData.isWeapon(item4) || (item4 != null && item4.getType() == WeaponType.STAFF)) {
            if (!canCharacterUseItem(character, item4) || (item4.getWeaponRank() == WeaponRank.PRF && !prfIDs.contains(item4ID))) {
                GBAFEItemData replacementItem = itemData.getBasicWeaponForCharacter(character, ranged, false, rng);
                if (!forceBasic) {
                    if (itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.ANY_USABLE || ranged || melee) {
                        replacementItem = itemData.getRandomWeaponForCharacter(character, ranged, melee, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    } else {
                        replacementItem = itemData.getSidegradeWeapon(character, charClass, item4, charData.isEnemyAtAnyPoint(character.getID()), itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, itemAssignmentOptions.assignPoisonWeapons, rng);
                    }
                }

                if (item4.getWeaponRank() == WeaponRank.S) {
                    GBAFEItemData[] topWeapons = topRankWeaponsForClass(charClass);
                    if (topWeapons.length > 0) {
                        replacementItem = topWeapons[rng.nextInt(topWeapons.length)];
                    }
                }
                if (replacementItem != null) {
                    if (replacementItem.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(replacementItem.getID()); }
                    else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(replacementItem); }
                    chapterUnit.setItem4(replacementItem.getID());
                } else {
                    chapterUnit.setItem4(0);
                }
            } else {
                if (item4.getType() == WeaponType.STAFF) { hasAtLeastOneHealingStaff = hasAtLeastOneHealingStaff || itemData.isHealingStaff(item4.getID()); }
                else { hasAtLeastOneWeapon = hasAtLeastOneWeapon || itemData.isWeapon(item4); }
            }
        }

        if (isHealerClass && !hasAtLeastOneHealingStaff) {
            chapterUnit.giveItems(new int[] {itemData.getRandomHealingStaff(itemData.weaponRankFromValue(character.getStaffRank()), rng).getID()});
        }
        if (classCanAttack && !hasAtLeastOneWeapon) {
            GBAFEItemData basicWeapon = itemData.getBasicWeaponForCharacter(character, ranged, true, rng);
            if (basicWeapon != null) {
                chapterUnit.giveItems(new int[] {basicWeapon.getID()});
            }
        }

        itemsToGiveBack(chapterUnit, itemsToRetain);
        if (!itemsToRetain.isEmpty()) {
            int[] idsToGiveBack = itemsToRetain.stream().mapToInt(item -> (item.getID())).toArray();
            chapterUnit.giveItems(idsToGiveBack);
        }

        GBAFEItemData prf = itemData.getPrfWeaponForClass(charClass.getID());
        if (prf != null) { chapterUnit.giveItem(prf.getID()); }

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

        DebugPrinter.log(DebugPrinter.Key.CLASS_RANDOMIZER, "Final Inventory: [0x" + Integer.toHexString(item1ID) + (item1 == null ? "" : " (" + textData.getStringAtIndex(item1.getNameIndex(), true) + ")") + ", 0x" + Integer.toHexString(item2ID) + (item2 == null ? "" : " (" + textData.getStringAtIndex(item2.getNameIndex(), true) + ")") + ", 0x" + Integer.toHexString(item3ID) + (item3 == null ? "" : " (" + textData.getStringAtIndex(item3.getNameIndex(), true) + ")") + ", 0x" + Integer.toHexString(item4ID) + (item4 == null ? "" : " (" + textData.getStringAtIndex(item4.getNameIndex(), true) + ")") + "]");
    }

    public void validateMinionInventory(GBAFEChapterUnitData chapterUnit, GBAFECharacterData minionCharacter) {
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
            if (!canCharacterUseItem(minionCharacter, item1)) {
                replacementItem = itemData.getSidegradeWeapon(minionCharacter, unitClass, item1, true, itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
                if ((isHealer && limitStaves && hasStaff) && replacementItem.getType() == WeaponType.STAFF) {
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
            if (!canCharacterUseItem(minionCharacter, item2)) {
                replacementItem = itemData.getSidegradeWeapon(minionCharacter, unitClass, item2, true, itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
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
            if (!canCharacterUseItem(minionCharacter, item3)) {
                replacementItem = itemData.getSidegradeWeapon(minionCharacter, unitClass, item3, true, itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
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
            if (!canCharacterUseItem(minionCharacter, item4)) {
                replacementItem = itemData.getSidegradeWeapon(minionCharacter, unitClass, item4, true, itemAssignmentOptions.weaponPolicy == ItemAssignmentOptions.WeaponReplacementPolicy.STRICT, itemAssignmentOptions.assignPromoWeapons, true, rng);
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
                    WeaponRanks ranks = itemData.ranksForCharacter(minionCharacter, unitClass);
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

    private Boolean canCharacterUseItem(GBAFECharacterData character, GBAFEItemData weapon) {
        int weaponRankValue = itemData.weaponRankValueForRank(weapon.getWeaponRank());
        if ((weapon.getType() == WeaponType.SWORD && character.getSwordRank() >= weaponRankValue) ||
                (weapon.getType() == WeaponType.LANCE && character.getLanceRank() >= weaponRankValue) ||
                (weapon.getType() == WeaponType.AXE && character.getAxeRank() >= weaponRankValue) ||
                (weapon.getType() == WeaponType.BOW && character.getBowRank() >= weaponRankValue) ||
                (weapon.getType() == WeaponType.ANIMA && character.getAnimaRank() >= weaponRankValue) ||
                (weapon.getType() == WeaponType.LIGHT && character.getLightRank() >= weaponRankValue) ||
                (weapon.getType() == WeaponType.DARK && character.getDarkRank() >= weaponRankValue) ||
                (weapon.getType() == WeaponType.STAFF && character.getStaffRank() >= weaponRankValue)) {
            return true;
        }

        return false;
    }

    public void validateFormerThiefInventory(GBAFEChapterUnitData chapterUnit) {
        Set<GBAFEItemData> itemsToRetain = itemsToRetain(chapterUnit);

        GBAFEItemData[] requiredItems = itemData.formerThiefInventory();
        if (requiredItems != null) {
            giveItemsToChapterUnit(chapterUnit, requiredItems);
        }

        GBAFEItemData[] thiefItemsToRemove = itemData.thiefItemsToRemove();
        for (GBAFEItemData item : thiefItemsToRemove) {
            chapterUnit.removeItem(item.getID());
        }

        itemsToGiveBack(chapterUnit, itemsToRetain);
        if (!itemsToRetain.isEmpty()) {
            int[] idsToGiveBack = itemsToRetain.stream().mapToInt(item -> (item.getID())).toArray();
            chapterUnit.giveItems(idsToGiveBack);
        }
    }

    private void giveItemsToChapterUnit(GBAFEChapterUnitData chapterUnit, GBAFEItemData[] items) {
        int[] requiredItemIDs = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            requiredItemIDs[i] = items[i].getID();
        }
        chapterUnit.giveItems(requiredItemIDs);
    }

    private Set<GBAFEItemData> itemsToRetain(GBAFEChapterUnitData chapterUnit) {
        int item1ID = chapterUnit.getItem1();
        GBAFEItemData item1 = itemData.itemWithID(item1ID);
        int item2ID = chapterUnit.getItem2();
        GBAFEItemData item2 = itemData.itemWithID(item2ID);
        int item3ID = chapterUnit.getItem3();
        GBAFEItemData item3 = itemData.itemWithID(item3ID);
        int item4ID = chapterUnit.getItem4();
        GBAFEItemData item4 = itemData.itemWithID(item4ID);

        Set<GBAFEItemData> existingItemSet = new HashSet<GBAFEItemData>();
        if (item1 != null) { existingItemSet.add(item1); }
        if (item2 != null) { existingItemSet.add(item2); }
        if (item3 != null) { existingItemSet.add(item3); }
        if (item4 != null) { existingItemSet.add(item4); }

        Set<GBAFEItemData> itemsToRetain = new HashSet<GBAFEItemData>(Arrays.asList(itemData.specialItemsToRetain()));
        itemsToRetain.retainAll(existingItemSet);
        return itemsToRetain;
    }

    private void itemsToGiveBack(GBAFEChapterUnitData chapterUnit, Set<GBAFEItemData> itemsToRetain) {
        int item1ID = chapterUnit.getItem1();
        GBAFEItemData item1 = itemData.itemWithID(item1ID);
        int item2ID = chapterUnit.getItem2();
        GBAFEItemData item2 = itemData.itemWithID(item2ID);
        int item3ID = chapterUnit.getItem3();
        GBAFEItemData item3 = itemData.itemWithID(item3ID);
        int item4ID = chapterUnit.getItem4();
        GBAFEItemData item4 = itemData.itemWithID(item4ID);

        if (!itemsToRetain.isEmpty()) {
            if (item1 != null) { itemsToRetain.remove(item1); }
            if (item2 != null) { itemsToRetain.remove(item2); }
            if (item3 != null) { itemsToRetain.remove(item3); }
            if (item4 != null) { itemsToRetain.remove(item4); }
        }
    }

    public void validateSpecialClassInventory(GBAFEChapterUnitData chapterUnit) {
        Set<GBAFEItemData> itemsToRetain = itemsToRetain(chapterUnit);

        GBAFEItemData[] requiredItems = itemData.specialInventoryForClass(chapterUnit.getStartingClass(), rng);
        if (requiredItems != null && requiredItems.length > 0) {
            giveItemsToChapterUnit(chapterUnit, requiredItems);
        }

        itemsToGiveBack(chapterUnit, itemsToRetain);
        if (!itemsToRetain.isEmpty()) {
            int[] idsToGiveBack = itemsToRetain.stream().mapToInt(item -> (item.getID())).toArray();
            chapterUnit.giveItems(idsToGiveBack);
        }
    }

    private GBAFEItemData[] topRankWeaponsForClass(GBAFEClassData characterClass) {
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
}
