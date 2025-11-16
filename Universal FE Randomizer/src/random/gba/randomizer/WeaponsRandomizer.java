package random.gba.randomizer;

import java.util.Random;

import fedata.gba.GBAFEItemData;
import fedata.gba.general.WeaponEffects;
import fedata.general.FEBase;
import random.gba.loader.GBADataLoaders;
import random.gba.loader.ItemDataLoader;
import random.gba.loader.TextLoader;
import random.general.WeightedDistributor;
import ui.model.WeaponEffectOptions;
import util.OptionRecorder;
import util.WhyDoesJavaNotHaveThese;

public class WeaponsRandomizer extends AbstractGBARandomizerComponent{
	
	static final int rngSalt = 64;

    public WeaponsRandomizer(OptionRecorder.GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, FEBase.GameType type) {
        super(allOptions, dataLoaders, rng, type);
    }

    public void randomizeMights() {
		GBAFEItemData[] allWeapons = itemData.getAllWeapons();
		int variance = weapons.mightOptions.variance;
		int minMT = weapons.mightOptions.minValue;
		int maxMT = weapons.mightOptions.maxValue;
		for (GBAFEItemData weapon : allWeapons) {
			int originalMight = weapon.getMight();
			int newMight = originalMight;
			int randomNum = rng.nextInt(2);
			if (randomNum == 0) {
				newMight += rng.nextInt(variance + 1);
			} else {
				newMight -= rng.nextInt(variance + 1);
			}
			
			weapon.setMight(WhyDoesJavaNotHaveThese.clamp(newMight, minMT, maxMT));
		}
		
		itemData.commit();
	}
	
	public void randomizeHit() {
		GBAFEItemData[] allWeapons = itemData.getAllWeapons();
        int variance = weapons.hitOptions.variance;
        int minHit = weapons.hitOptions.minValue;
        int maxHit = weapons.hitOptions.maxValue;
		for (GBAFEItemData weapon : allWeapons) {
			int originalHit = weapon.getHit();
			int newHit = originalHit;
			int randomNum = rng.nextInt(2);
			if (randomNum == 0) {
				newHit += rng.nextInt(variance + 1);
			} else {
				newHit -= rng.nextInt(variance + 1);
			}
			
			weapon.setHit(WhyDoesJavaNotHaveThese.clamp(newHit, minHit, maxHit));
		}
		
		itemData.commit();
	}
	
	public void randomizeDurability() {
		GBAFEItemData[] allWeapons = itemData.getAllWeapons();
        int variance = weapons.durabilityOptions.variance;
        int minDurability = weapons.durabilityOptions.minValue;
        int maxDurability = weapons.durabilityOptions.maxValue;
		for (GBAFEItemData weapon : allWeapons) {
			int originalDurability = weapon.getDurability();
			int newDurability = originalDurability;
			int randomNum = rng.nextInt(2);
			if (randomNum == 0) {
				newDurability += rng.nextInt(variance + 1);
			} else {
				newDurability -= rng.nextInt(variance + 1);
			}
			
			if (weapon.getMaxRange() == 10) {
				// Siege Tomes get a minimum of 1 since they're normally low use.
				weapon.setDurability(WhyDoesJavaNotHaveThese.clamp(newDurability, 1, maxDurability));
			} else {
				weapon.setDurability(WhyDoesJavaNotHaveThese.clamp(newDurability, minDurability, maxDurability));
			}
		}
		
		itemData.commit();
	}
	
	public void randomizeWeight() {
		GBAFEItemData[] allWeapons = itemData.getAllWeapons();
        int variance = weapons.weightOptions.variance;
        int minWT = weapons.weightOptions.minValue;
        int maxWT = weapons.weightOptions.maxValue;
		for (GBAFEItemData weapon : allWeapons) {
			int originalWeight = weapon.getWeight();
			int newWeight = originalWeight;
			int randomNum = rng.nextInt(2);
			if (randomNum == 0) {
				newWeight += rng.nextInt(variance + 1);
			} else {
				newWeight -= rng.nextInt(variance + 1);
			}
			
			weapon.setWeight(WhyDoesJavaNotHaveThese.clamp(newWeight, minWT, maxWT));
		}
		
		itemData.commit();
	}
	
	public void randomizeEffects() {
		GBAFEItemData[] allWeapons = itemData.getAllWeapons();
        WeaponEffectOptions effectOptions = weapons.effectsList;
		WeightedDistributor<WeaponEffects> enabledEffects = new WeightedDistributor<WeaponEffects>();
		
		if (effectOptions.statBoosts > 0) { enabledEffects.addItem(WeaponEffects.STAT_BOOSTS, effectOptions.statBoosts); }
		if (effectOptions.effectiveness > 0) { enabledEffects.addItem(WeaponEffects.EFFECTIVENESS, effectOptions.effectiveness); }
		if (effectOptions.unbreakable > 0) { enabledEffects.addItem(WeaponEffects.UNBREAKABLE, effectOptions.unbreakable); }
		if (effectOptions.brave > 0) { enabledEffects.addItem(WeaponEffects.BRAVE, effectOptions.brave); }
		if (effectOptions.reverseTriangle > 0) { enabledEffects.addItem(WeaponEffects.REVERSE_TRIANGLE, effectOptions.reverseTriangle); }
		if (effectOptions.extendedRange > 0) { enabledEffects.addItem(WeaponEffects.EXTEND_RANGE, effectOptions.extendedRange); }
		if (effectOptions.highCritical > 0) {
			WeaponEffects effect = WeaponEffects.HIGH_CRITICAL;
			effect.additionalInfo.put(WeaponEffects.InfoKeys.CRITICAL_RANGE, effectOptions.criticalRange);
			enabledEffects.addItem(effect, effectOptions.highCritical);
		}
		if (effectOptions.magicDamage > 0) { enabledEffects.addItem(WeaponEffects.MAGIC_DAMAGE, effectOptions.magicDamage); }
		if (effectOptions.poison > 0) { enabledEffects.addItem(WeaponEffects.POISON, effectOptions.poison); }
		if (effectOptions.eclipse > 0) { enabledEffects.addItem(WeaponEffects.HALF_HP, effectOptions.eclipse); }
		if (effectOptions.devil > 0) { enabledEffects.addItem(WeaponEffects.DEVIL, effectOptions.devil); }
		
		for (GBAFEItemData weapon : allWeapons) {
			if (weapons.noEffectIronWeapons && itemData.isBasicWeapon(weapon.getID())) { continue; }
			if (weapons.noEffectSteelWeapons && itemData.isSteelWeapon(weapon.getID())) { continue; }
			if (weapons.noEffectThrownWeapons && itemData.isBasicThrowingWeapon(weapon.getID())) { continue; }
			
			if (rng.nextInt(100) < weapons.effectChance) {
				weapon.applyRandomEffect(new WeightedDistributor<WeaponEffects>(enabledEffects), itemData, textData, itemData.spellAnimations, rng);
			}
		}
		
		itemData.commit();
	}
}
