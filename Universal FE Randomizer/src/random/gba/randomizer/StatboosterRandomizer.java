package random.gba.randomizer;

import java.util.Random;

import fedata.gba.GBAFEItemData;
import fedata.gba.general.GBAFEStatboost;
import fedata.gba.general.GBAFEStatboost.BoostedStat;
import fedata.gba.general.GBAFEStatboost.GBAFEStatboostDao;
import fedata.general.FEBase;
import random.gba.loader.GBADataLoaders;
import random.gba.loader.ItemDataLoader;
import random.gba.loader.StatboostLoader;
import random.gba.loader.TextLoader;
import random.general.PoolDistributor;
import ui.model.StatboosterOptions;
import util.OptionRecorder;

/**
 * Class containing the actual Randomizer logic for the Statboosters. 
 */
public class StatboosterRandomizer extends AbstractGBARandomizerComponent {
	
	public static int rngSalt = 4831789;

    public StatboosterRandomizer(OptionRecorder.GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, FEBase.GameType type) {
        super(allOptions, dataLoaders, rng, type);
    }

    public void randomize() {
		if (!statboosters.enabled) {
			return;
		}

		switch (statboosters.mode) {
		case SAME_STAT:
			randomizeSameStat();
			break;
		case SHUFFLE:
			randomizeShuffle();
			break;
		case MULTIPLE_STATS:
			randomizeMultipleStats();
			break;

		default:
			throw new UnsupportedOperationException("No Statbooster Randomization Mode was selected.");

		}
		
		// Update the descriptions
		for (GBAFEStatboost boost : statboostData.getStatboosters(statboosters.includeMov, statboosters.includeCon)) {
			for (GBAFEItemData item : itemData.itemsByStatboostAddress(boost.getAddressOffset())) {
				System.out.println(item.displayString());
				int descriptionIndex = item.getDescriptionIndex();
				int useDescriptionIndex = item.getUseDescriptionIndex();
				
				textData.setStringAtIndex(descriptionIndex, boost.dao.buildDescription());
				
				// As the Use Description doesn't include the value, if it's still the same stat, then there is no need to change it.
				if (statboosters.mode != StatboosterOptions.StatboosterRandomizationModes.SAME_STAT) {
					textData.setStringAtIndex(useDescriptionIndex, boost.dao.buildUseDescription());
				}
			}
		}
	}

	/**
	 * Randomizes the Statboosters keeping the Statbooster for the same stat.
	 * 
	 * F.e. an energy ring in this case will always boost Power.
	 */
	public void randomizeSameStat() {
		for (GBAFEStatboost boost : statboostData.getStatboosters(statboosters.includeMov, statboosters.includeCon)) {
			// Get the DAO which contains the stat values
			GBAFEStatboostDao dao = boost.dao;

			// Since in this case we should have single stat boosters, get the index of the only stat
			BoostedStat indexOfOnlyStat = BoostedStat.valueOf(dao.getIndexOfOnlyStat());
			randomizeStatImpl(indexOfOnlyStat, dao);
			boost.write();
		}
	}

	/**
	 * Randomizes the Statboosters while potentially changing the stat that a
	 * stabooster boosts. Makes sure that there is still a statbooster for each
	 */
	private void randomizeShuffle() {
		PoolDistributor<BoostedStat> distributor = BoostedStat.getPool(statboosters.excludeMovConRandomization);

		for (GBAFEStatboost boost : statboostData.getStatboosters(statboosters.includeMov, statboosters.includeCon)) {
			// Get the DAO which contains the stat values
			GBAFEStatboostDao dao = boost.dao;
			// subtract the boosts from itself to remove the vanilla boost.
			dao.subtract(dao);

			// Randomize the new Stat that this boosts.
			BoostedStat selectedStat = distributor.getRandomItem(rng, true);
			randomizeStatImpl(selectedStat, dao);
			dao.parent.write();
		}
	}

	/**
	 * Randomizes Statboosters while having the options for a Statbooster to boost 0-N Stats. 
	 * The bounds as to how many stats can be boosted can be selected by the user.
	 */
	private void randomizeMultipleStats() {
		for (GBAFEStatboost boost : statboostData.getStatboosters(statboosters.includeMov, statboosters.includeCon)) {
			PoolDistributor<BoostedStat> distributor = BoostedStat.getPool(statboosters.excludeMovConRandomization);
			// Get the DAO which contains the stat values
			GBAFEStatboostDao dao = boost.dao;
			// subtract the boosts from itself to remove the vanilla boost.
			dao.subtract(dao);

			int numberStats = getNumberInMinMaxSafe(statboosters.multipleStatsMin, statboosters.multipleStatsMax);
			for (int i = 0; i < numberStats && !distributor.possibleResults().isEmpty(); i++) {
				// Randomize the new Stat boost to add.
				BoostedStat selectedStat = distributor.getRandomItem(rng, true);
				randomizeStatImpl(selectedStat, dao);
			}
			dao.parent.write();
		}
	}

	/**
	 * Randomizes the boost for the stat with the given index (HP,POW,SKL,SPD,DEF,RES,LCK,
	 */
	private void randomizeStatImpl(BoostedStat index, GBAFEStatboostDao dao) {
		// randomize the new boost within the bounds
		int newBoost = getNumberInMinMaxSafe(statboosters.boostStrengthMin, statboosters.boostStrengthMax);

		// If this stat is HP, and the user selected to apply an HP Modifier to keep it
		// higher than other stats (like in vanilla) then apply it.
		if (BoostedStat.HP.equals(index) && statboosters.applyHpModifier) {
			newBoost += statboosters.hpModifier;
		}

		// write the changes.
		dao.setStatAtIndex(index, newBoost);
	}

	private int getNumberInMinMaxSafe(int min, int max) {
		if (min == max)  {
			return min;
		}

		return rng.nextInt(max - min) + min;
	}
}
