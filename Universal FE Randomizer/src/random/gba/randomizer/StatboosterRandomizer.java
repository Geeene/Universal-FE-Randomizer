package random.gba.randomizer;

import java.util.Arrays;
import java.util.Random;

import fedata.gba.general.GBAFEStatboost;
import fedata.gba.general.GBAFEStatboost.GBAFEStatboostDao;
import random.gba.loader.StatboostLoader;
import random.general.PoolDistributor;
import ui.model.StatboosterOptions;

/**
 * Class containing the actual Randomizer logic for the Statboosters. 
 */
public class StatboosterRandomizer {
	
	public static int SALT = 4831789;
	
	public static void randomize(StatboosterOptions options, StatboostLoader loader, Random rng) {
		if (!options.enabled) {
			return;
		}

		switch (options.mode) {
		case SAME_STAT:
			randomizeSameStat(options, loader, rng);
			break;
		case SHUFFLE:
			randomizeShuffle(options, loader, rng);
			break;
		case MULTIPLE_STATS:
			randomizeMultipleStats(options, loader, rng);
			break;

		default:
			throw new UnsupportedOperationException("No Statbooster Randomization Mode was selected.");

		}
	}

	/**
	 * Randomizes the Statboosters keeping the Statbooster for the same stat.
	 * 
	 * F.e. an energy ring in this case will always boost Power.
	 */
	public static void randomizeSameStat(StatboosterOptions options, StatboostLoader loader, Random rng) {
		for (GBAFEStatboost boost : loader.getStatboosters(options.includeMov, options.includeCon)) {
			// Get the DAO which contains the stat values
			GBAFEStatboostDao dao = boost.dao;

			// Since in this case we should have single stat boosters, get the index of the only stat
			int indexOfOnlyStat = dao.getIndexOfOnlyStat();
			randomizeStatImpl(options, indexOfOnlyStat, dao, rng);
			boost.write();
		}
	}

	/**
	 * Randomizes the Statboosters while potentially changing the stat that a
	 * stabooster boosts. Makes sure that there is still a statbooster for each
	 */
	private static void randomizeShuffle(StatboosterOptions options, StatboostLoader loader, Random rng) {
		PoolDistributor<Integer> distributor = getStatIndexPool();

		for (GBAFEStatboost boost : loader.getStatboosters(options.includeMov, options.includeCon)) {
			// Get the DAO which contains the stat values
			GBAFEStatboostDao dao = boost.dao;
			// subtract the boosts from itself to remove the vanilla boost.
			dao.subtract(dao);

			// Randomize the new Stat that this boosts.
			int selectedStat = distributor.getRandomItem(rng, true);
			randomizeStatImpl(options, selectedStat, dao, rng);
			dao.parent.write();
		}
	}
	
	private static PoolDistributor<Integer> getStatIndexPool(){
		PoolDistributor<Integer> distributor = new PoolDistributor<>();
		distributor.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
		return distributor;
	}

	/**
	 * Randomizes Statboosters while having the options for a Statbooster to boost 0-N Stats. 
	 * The bounds as to how many stats can be boosted can be selected by the user.
	 */
	private static void randomizeMultipleStats(StatboosterOptions options, StatboostLoader loader, Random rng) {
		for (GBAFEStatboost boost : loader.getStatboosters(options.includeMov, options.includeCon)) {
			PoolDistributor<Integer> distributor = getStatIndexPool();
			// Get the DAO which contains the stat values
			GBAFEStatboostDao dao = boost.dao;
			// subtract the boosts from itself to remove the vanilla boost.
			dao.subtract(dao);

			int numberStats = rng.nextInt(options.multipleStatsMin, options.multipleStatsMax);
			for (int i = 0; i < numberStats; i++) {
				// Randomize the new Stat boost to add.
				Integer selectedStat = distributor.getRandomItem(rng, true);
				randomizeStatImpl(options, selectedStat, dao, rng);
			}
			dao.parent.write();
		}
	}

	/**
	 * Randomizes the boost for the stat with the given index (HP,POW,SKL,SPD,DEF,RES,LCK,
	 */
	private static void randomizeStatImpl(StatboosterOptions options, int index, GBAFEStatboostDao dao, Random rng) {
		// randomize the new boost within the bounds
		int newBoost = rng.nextInt(options.boostStrengthMin, options.boostStrengthMax);

		// If this stat is HP, and the user selected to apply an HP Modifier to keep it
		// higher than other stats (like in vanilla) then apply it.
		if (dao.statIndexIsHp(index) && options.applyHpModifier) {
			newBoost += options.hpModifier;
		}

		// write the changes.
		dao.setStatAtIndex(index, newBoost);
	}
}
