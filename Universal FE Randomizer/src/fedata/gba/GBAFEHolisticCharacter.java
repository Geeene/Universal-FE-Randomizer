package fedata.gba;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import fedata.gba.GBAFECharacterData.Affinity;
import fedata.gba.general.WeaponRank;
import fedata.gba.general.WeaponType;
import fedata.general.FEBase.GameType;
import random.gba.loader.ClassDataLoader;
import random.gba.randomizer.AbstractGBARandomizer;
import random.gba.randomizer.service.ItemAssignmentService;
import util.WhyDoesJavaNotHaveThese;

/**
 * This class is a Wrapper around the Personal and Class data that makeup a GBAFE Character.
 * 
 * It can be used to swap the class of a character, update weapon ranks, stats, weapon ranks etc.
 * 
 * This class should be the only point of contact for a randomizer for setting this information.
 */
public class GBAFEHolisticCharacter {
	private GBAFECharacterData personalData;
	private GBAFEClassData classData;
	private GBAFEStatDto stats;
	private GBAFEStatDto growths;
	private GBAFEWeaponRankDto weaponRanks;
	private int con;

	public String charDebugString;
	public String classDebugString;
	public List<GBAFEHolisticCharacter> linkedCharacters = new ArrayList<>();
	
	private GameType gameType;

	/** Package Private Constructor, for Tests maybe? */
	GBAFEHolisticCharacter() {
	}

	/**
	 * Main Constructor 
	 */
	public GBAFEHolisticCharacter(GBAFECharacterData personalData, GBAFEClassData classData, GameType gameType) {
		this.personalData = personalData;
		this.classData = classData;
		this.gameType = gameType;
		this.stats = personalData.getBases().add(classData.getBases());
		this.growths = personalData.getGrowths();
		this.weaponRanks = classData.getWeaponRanks().override(personalData.getWeaponRanks());
		this.con = personalData.getConstitution() + classData.getCON();
		this.charDebugString = personalData.debugString;
		this.classDebugString = classData.debugString;
	}
 
	/** Getter for the current class */
	public GBAFEClassData getCurrentClass() {
		return this.classData;
	}
	
	/** Getter for the Weapon Ranks of the current character */
	public GBAFEWeaponRankDto getWeaponRanks() {
		return new GBAFEWeaponRankDto(this.weaponRanks);
	}
	
	/** Getter for the Character Id */
	public int getID() {
		return this.personalData.getID();
	}

	/** Getter for the Character Id */
	public int getClassID() {
		return this.classData.getID();
	}
	
	
	/** Associate the charcater with other related CharacterData so they can be updated together */
	public void addLinkedChar(GBAFEHolisticCharacter link) {
		if (linkedCharacters == null) {
			linkedCharacters = new ArrayList<>();
		}
		
		linkedCharacters.add(link);
	}
	
	/** Associate the character with other related CharacterData so they can be updated together */
	public void setLinkedCharacters(List<GBAFEHolisticCharacter> chars) {
		this.linkedCharacters = chars;
	}
	
	/** Gets a list of Weapon Types that this Character can use in their current class */
	public List<WeaponType> getUsableWeaponTypes() {
		return weaponRanks.getTypes();
	}

	/**
	 * Change the class of the Character and ensure that the stats are updated
	 * accordingly.
	 */
	public void changeClass(GBAFEClassData newClass) {
		// Save old class data for now
		GBAFEClassData oldClass = this.classData;

		// If needed prepare the char for changing class (currently only needed for fe7)
		this.personalData.prepareForClassRandomization();

		// update the reference to the new class data
		this.classData = newClass;
		this.personalData.setClassID(newClass.getID());

		// update the stats on this object to have the new classes bases
		GBAFEStatDto newStats = getStats().subtract(oldClass.getBases()).add(newClass.getBases());
		setStats(newStats);

		int newCon = this.con - oldClass.getCON() + newClass.getCON();
		setCon(newCon);
		
		this.classDebugString = newClass.debugString;
	}

	/**
	 * Returns a copy of the characters current Stats (personal + class bases),
	 * makes a copy to ensure this can't be changed directly and has to go through
	 * setStats() to recalculate personal bases.
	 */
	public GBAFEStatDto getStats() {
		return new GBAFEStatDto(stats);
	}

	/** Getter for the Characters current growths. This returns a copy, so they can not be edited. */
	public GBAFEStatDto getGrowths() {
		return new GBAFEStatDto(growths);
	}

	/** Setter for the characters growths, makes sure to update all related characters aswell. */
	public void setGrowths(GBAFEStatDto newGrowths) {
		this.growths = newGrowths.clamp(GBAFEStatDto.ALL_ZEROS, GBAFEStatDto.MAXIMUM_GROWTHS);
		this.linkedCharacters.forEach(chara -> chara.setGrowths(newGrowths));
	}

	/** getter for the characters current con in their current class */
	public int getCon() {
		return this.con;
	}

	/**
	 * Update the characters stats
	 */
	public void setStats(GBAFEStatDto stats) {
		this.stats = stats;
		updatePersonalBases();
	}

	/**
	 * Updates the characters personal con while ensuring that it doesn't go into
	 * the negative or overflow
	 */
	public void setCon(int newCon) {
		this.con = WhyDoesJavaNotHaveThese.clamp(newCon, 0, classData.getMaxCON()); // clamp to prevent over or
																					// underflow
		this.personalData.setConstitution(con - classData.getCON()); // convert to personal con
	}

	/**
	 * Updates personal bases while ensuring that the
	 */
	private void updatePersonalBases() {
		stats.clamp(GBAFEStatDto.MINIMUM_STATS, classData.getCaps()); // Make sure that the stats are currently valid
		this.personalData.setBases(new GBAFEStatDto(stats).subtract(classData.getBases())); // Update the personal bases
	}

	/** Getter for the characters current personal Data, this should probably not be usable */
	public GBAFECharacterData getPersonalData() {
		return this.personalData;
	}


	public void updateWeaponRanksToMatchClass(Random rng) {
		GBAFEWeaponRankDto characterRanks = this.personalData.getWeaponRanks();
		GBAFEWeaponRankDto classRanks = this.classData.getWeaponRanks();

		List<WeaponRank> classRanksList = classRanks.asList();
		WeaponRank lowestRank = null;
		for (WeaponRank weaponRank : classRanksList) {
			if (weaponRank == null || lowestRank == null || 
					(weaponRank.isLowerThan(lowestRank, gameType) && !weaponRank.equals(WeaponRank.NONE) )) {
				lowestRank = weaponRank;
			}
		}
		
		// Create a new Dto containing all the weapon ranks of the character and the
		// class
		GBAFEWeaponRankDto combinedRanks = new GBAFEWeaponRankDto(classRanks);
		classRanks.override(characterRanks);

		// find the maximum number of classRanks necessary (i.e. number of weapons the
		// class can use)
		long numberRanksClass = classRanks.asList().stream().filter(wr -> wr != WeaponRank.NONE).count();

		// get all of the weapon ranks from our currently combined ones that aren't 0
		List<WeaponRank> rankValues = combinedRanks.asList().stream().filter(wr -> wr != WeaponRank.NONE).sorted()
				.collect(Collectors.toList());

		// While we have more rank values to choose from than slots to put them, 
		// throw the lowest one out
		while (rankValues.size() > numberRanksClass) {
			rankValues.remove(0);
		}

		GBAFEWeaponRankDto newRanks = new GBAFEWeaponRankDto();
		for (int i = 0; i < classRanksList.size(); i++) {
			if (classRanksList.get(i) != WeaponRank.NONE) {
				int rankIndex = rng.nextInt(rankValues.size());
				WeaponRank currentRank = rankValues.get(rankIndex);
				if (currentRank != null) {
					newRanks.setRankAtIndex(i, currentRank);
					rankValues.remove(rankIndex);
				} else {
					newRanks.setRankAtIndex(i, lowestRank);
				}
			}
		}
		
		setWeaponRanks(newRanks);
	}
	
	public void setWeaponRanks(GBAFEWeaponRankDto newRanks) {
		// Update the full weapon rank including the class part for this character
		this.weaponRanks = new GBAFEWeaponRankDto(newRanks);
		this.personalData.setWeaponRanks(weaponRanks);
		this.linkedCharacters.forEach(c -> c.setWeaponRanks(newRanks));
		
	}

	/**
	 * To be used for minion characters so that they automatically choose a weapon rank based on their current inventory rather than this mechanism being overriden by personal data
	 */
	public void resetPersonalWeaponRanks() {
		// set all the personal ranks to 0
		this.personalData.getWeaponRanks().reset();
		// set the combined weapon ranks to the Class Ranks to reflect there not being personal ranks
		this.weaponRanks = this.classData.getWeaponRanks();
	}
	
	/**
	 * Used for characters where the Level in the personal data doesn't match the level as a chapter unit 
	 */
	public void setLevel(Integer newLevel) {
		if (newLevel != null) {
			this.personalData.setLevel(newLevel);
		}
	}
	
	public void autoLevel(GBAFEClassData newClass, boolean shouldBePromoted, boolean isPromoted, ClassDataLoader classData) {
		this.changeClass(newClass);
	}
	
	public void setAffinity(int affinity) {
		this.personalData.setAffinityValue(affinity);
		this.linkedCharacters.forEach(c -> c.setAffinity(affinity));
	}
	
	public int getNameIndex() {
		return this.personalData.getNameIndex();
	}
	
	public void setNameIndex(int nameIndex) {
		this.linkedCharacters.forEach(c -> c.setNameIndex(nameIndex));
	}

	public void setDescriptionIndex(int descIndex) {
		this.linkedCharacters.forEach(c -> c.setDescriptionIndex(descIndex));
	}

	public void setFaceId(int faceId) {
		this.personalData.setFaceID(faceId);
		this.linkedCharacters.forEach(c -> c.setFaceId(faceId));
	}

	public void setLord(boolean isLord) {
		this.personalData.setIsLord(isLord);
		this.linkedCharacters.forEach(c -> c.setLord(isLord));
	}
	
	/**
	 * Used to ensure that we can beat bosses (such as Wire) where there aren't really many options
	 */
	public void nerf() {
		this.linkedCharacters.forEach(c -> {
			GBAFEHolisticCharacter holisticCharacter = AbstractGBARandomizer.holisticCharacterMap.get(c.getID());
			GBAFEStatDto nerfedStats = holisticCharacter.getStats();
			nerfedStats.skl = nerfedStats.skl >> 1;
			nerfedStats.spd = nerfedStats.spd >> 1;
			nerfedStats.def = nerfedStats.def >> 1;
			nerfedStats.res = nerfedStats.res >> 1;
			holisticCharacter.setStats(nerfedStats);
		});
	}
	
	public void setPromotedPaletteIndex(int index) {
		this.personalData.setPromotedPaletteIndex(index);
	}

	public void setUnpromotedPaletteIndex(int index) {
		this.personalData.setUnpromotedPaletteIndex(index);
	}
}
