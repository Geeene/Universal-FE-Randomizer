package random.gba.randomizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import fedata.gba.GBAFEChapterData;
import fedata.gba.GBAFEChapterUnitData;
import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEClassData;
import fedata.gba.GBAFEItemData;
import fedata.gba.GBAFEWorldMapSpriteData;
import fedata.gba.general.GBAFEChapterMetadataChapter;
import fedata.gba.general.GBAFEChapterMetadataData;
import fedata.general.FEBase;
import fedata.general.FEBase.GameType;
import io.DiffApplicator;
import io.FileHandler;
import io.UPSPatcher;
import random.exc.RandomizationStoppedException;
import random.gba.loader.ChapterLoader;
import random.gba.loader.CharacterDataLoader;
import random.gba.loader.ClassDataLoader;
import random.gba.loader.ItemDataLoader;
import random.gba.loader.PaletteLoader;
import random.gba.loader.PortraitDataLoader;
import random.gba.loader.PromotionDataLoader;
import random.gba.loader.StatboostLoader;
import random.gba.loader.TextLoader;
import random.gba.randomizer.shuffling.CharacterShuffler;
import random.general.Randomizer;
import ui.model.BaseOptions;
import ui.model.CharacterShufflingOptions;
import ui.model.ClassOptions;
import ui.model.EnemyOptions;
import ui.model.EnemyOptions.BossStatMode;
import ui.model.GrowthOptions;
import ui.model.ItemAssignmentOptions;
import ui.model.MiscellaneousOptions;
import ui.model.OtherCharacterOptions;
import ui.model.PromotionOptions;
import ui.model.PromotionOptions.Mode;
import ui.model.RecordableOption;
import ui.model.RecruitmentOptions;
import ui.model.StatboosterOptions;
import ui.model.WeaponOptions;
import util.Diff;
import util.DiffCompiler;
import util.FreeSpaceManager;
import util.SeedGenerator;
import util.recordkeeper.RecordKeeper;

/**
 * Abstract GBA Randomizer containing most logic that is shared between FE6, 7
 * and 8
 */
public abstract class AbstractGBARandomizer extends Randomizer {

	// Variables for File Operations
	protected final String sourcePath;
	protected final String targetPath;
	protected String tempPath;
	protected FileHandler sourceFileHandler;
	protected FileHandler targetFileHandler;

	protected final FEBase.GameType gameType;
	protected DiffCompiler diffCompiler;
	protected boolean paletteFixRequired;
	protected final String seedString;
	protected FreeSpaceManager freeSpace;
	protected final String gameFriendlyName;
	protected Map<GBAFECharacterData, GBAFECharacterData> characterMap; // valid with random recruitment. Maps slots to
																		// reference character.
	RecordKeeper recordKeeper;

	// OPTION MODELS
	protected GrowthOptions growths;
	protected BaseOptions bases;
	protected ClassOptions classes;
	protected WeaponOptions weapons;
	protected OtherCharacterOptions otherCharacterOptions;
	protected EnemyOptions enemies;
	protected MiscellaneousOptions miscOptions;
	protected RecruitmentOptions recruitOptions;
	protected ItemAssignmentOptions itemAssignmentOptions;
	protected CharacterShufflingOptions charShufflingOptions;
	protected StatboosterOptions statboosterOptions;
	protected PromotionOptions promoOptions;

	// DATALOADERS
	protected CharacterDataLoader charData;
	protected ClassDataLoader classData;
	protected ChapterLoader chapterData;
	protected ItemDataLoader itemData;
	protected StatboostLoader statboostData;
	protected PaletteLoader paletteData;
	protected TextLoader textData;
	protected PortraitDataLoader portraitData;
	protected PromotionDataLoader promotionData;
	
	/**
	 * Shared constructor
	 */
	public AbstractGBARandomizer(String sourcePath, String targetPath, FEBase.GameType gameType, DiffCompiler diffs,
			GrowthOptions growths, BaseOptions bases, ClassOptions classes, WeaponOptions weapons,
			OtherCharacterOptions other, EnemyOptions enemies, MiscellaneousOptions otherOptions,
			RecruitmentOptions recruit, ItemAssignmentOptions itemAssign,
			CharacterShufflingOptions charShufflingOptions, StatboosterOptions statboosters, 
			PromotionOptions promoOptions, String seed, String friendlyName) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.seedString = seed;
		this.diffCompiler = diffs;
		this.growths = growths;
		this.bases = bases;
		this.classes = classes;
		this.weapons = weapons;
		this.otherCharacterOptions = other;
		this.enemies = enemies;
		this.miscOptions = otherOptions;
		this.recruitOptions = recruit;
		this.charShufflingOptions = charShufflingOptions;
		this.statboosterOptions = statboosters;
		this.promoOptions = promoOptions;
		this.itemAssignmentOptions = itemAssign == null ? new ItemAssignmentOptions() : itemAssign;
		this.gameType = gameType;
		this.gameFriendlyName = friendlyName;
	}

	/**
	 * GBA Randomizer Factory.
	 * 
	 * Returns an instance of the correct Randomizer based on the passed GameType
	 */
	public static AbstractGBARandomizer buildRandomizer(String sourcePath, String targetPath, FEBase.GameType gameType,
			DiffCompiler diffs, GrowthOptions growths, BaseOptions bases, ClassOptions classes, WeaponOptions weapons,
			OtherCharacterOptions other, EnemyOptions enemies, MiscellaneousOptions otherOptions,
			RecruitmentOptions recruit, ItemAssignmentOptions itemAssign,
			CharacterShufflingOptions charShufflingOptions, StatboosterOptions statboosterOptions,
			PromotionOptions promoOptions, String seed) {
		if (GameType.FE8.equals(gameType)) {
			return new FE8Randomizer(sourcePath, targetPath, gameType, diffs, growths, bases, classes, weapons, other,
					enemies, otherOptions, recruit, itemAssign, charShufflingOptions, statboosterOptions, promoOptions, seed);
		} else if (GameType.FE7.equals(gameType)) {
			return new FE7Randomizer(sourcePath, targetPath, gameType, diffs, growths, bases, classes, weapons, other,
					enemies, otherOptions, recruit, itemAssign, charShufflingOptions, statboosterOptions, promoOptions, seed);
		} else if (GameType.FE6.equals(gameType)) {
			return new FE6Randomizer(sourcePath, targetPath, gameType, diffs, growths, bases, classes, weapons, other,
					enemies, otherOptions, recruit, itemAssign, charShufflingOptions, statboosterOptions, promoOptions, seed);
		}

		throw new RandomizationStoppedException(
				"Couldn't find an applicable GBARandomizer for GameType " + gameType.name());
	}

	// Abstract methods that must be overwritten by the implementations

	/**
	 * Abstract Method
	 */
	protected abstract void makeFinalAdjustments();

	/**
	 * Abstract method.
	 * 
	 * The implementation of this method should be executing all the dataloaders
	 * which are necessary for the randomization.
	 */
	protected abstract void runDataloaders();

	/**
	 * Abstract Method.
	 * 
	 * The implementation of this method should be adding the Notes that are
	 * specific to the specific Implementation.
	 */
	protected abstract void recordNotes();

	/**
	 * Abstract Method.
	 * 
	 * The Implementation of this method should apply the diffs which are needed to
	 * enable single RN.
	 */
	protected abstract void applySingleRN();

	/**
	 * Abstract Method.
	 * 
	 * The Implementation of this method lets the subclass add more dataloader diff compilers before the free space is commited.
	 */
	protected abstract void gameSpecificDiffCompilations();
	
	
	/**
	 * Abstract Method.
	 * 
	 * The implementation of this method lets the subclass install 
	 * UPS Patches before any data is loaded. Currently this is only used for the FE6 Translation patch. 
	 */
	protected abstract void applyUpsPatches();
	
	/**
	 * The core method which executes the randomization.
	 */
	public void run() {
		try {
			// (1) Create a File Handler for the Source File
			sourceFileHandler = openFileAsHandler(sourcePath);

			// (2) Apply necessary UPS Patches pre-dataloading 
			applyUpsPatches();
			
			// (3) Run the dataloaders for the current game.
			runRandomizationStep("loading data", 1, () -> runDataloaders());

			// (4) Initialize the Record Keeper with the data from the original game
			initializeRecordKeeper();
			recordOriginalState();

			// (5) Apply some corrections needed before the Randomization.
			makePreliminaryAdjustments();

			// (6) Execute the different Randomization Steps depending on the Options
			executeRandomization();

			// (7) Apply various fixes based on the game that are necessary after the game
			// is randomized.
			makeFinalAdjustments();

			// (8) Compile the diffs to they can be applied
			compileDiffs();

			// (9) Apply the diffs to the target file.
			applyDiffs();

			// (10) cleanup the Source File Handler and potentially created Temporary files
			cleanupSourceFiles();

			// (11) Create a file Handler for the target File
			targetFileHandler = openFileAsHandler(targetPath);

			// (12) Record the State after randomization finished in the RecordKeeper.
			recordPostRandomizationState();

			// Finished.
			updateStatusString("Done!");
			updateProgress(1);
			notifyCompletion(recordKeeper, null);
		} catch (RandomizationStoppedException e) {
			notifyError(e.getMessage());
		}
	}

	// ------------------------------------------------------------------
	// HELPER METHODS & File Handling
	// ------------------------------------------------------------------
	protected FileHandler openFileAsHandler(String path) {
		try {
			return new FileHandler(path);
		} catch (IOException e) {
			notifyError("Failed to open source file.");
			return null;
		}
	}

	/**
	 * Executes a single Randomization Step.
	 * 
	 * @param stepDesc the description to print when the step throws an exception
	 * @param progress the number to update the progress to before running the step.
	 * @param step     a runnable which is the randomization step to perform
	 */
	public void runRandomizationStep(String stepDesc, Integer progress, Runnable step) {
		try {
			if (null != progress) {
				updateProgress(progress / 100d);
			}
			step.run();
		} catch (Exception e) {
			throw new RandomizationStoppedException(
					String.format("Encountered Error while Randomizing %s.%n%n%s", stepDesc, buildErrorMessage(e)));
		}
	}

	/**
	 * Builds a string containing the first five lines of the given exceptions stack
	 * trace.
	 */
	public String buildErrorMessage(Exception e) {
		return e.getClass().getSimpleName() + "\n\nStack Trace:\n\n" + Arrays.asList(e.getStackTrace()).stream()
				.limit(5).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
	}

	/**
	 * Compiles the differences to apply them later.
	 */
	protected void compileDiffs() {
		updateStatusString("Compiling changes...");
		updateProgress(0.95);
		charData.compileDiffs(diffCompiler);
		chapterData.compileDiffs(diffCompiler);
		classData.compileDiffs(diffCompiler, sourceFileHandler, freeSpace);
		itemData.compileDiffs(diffCompiler, sourceFileHandler);
		paletteData.compileDiffs(diffCompiler);
		textData.commitChanges(freeSpace, diffCompiler);
		portraitData.compileDiffs(diffCompiler);
		statboostData.compileDiffs(diffCompiler);
		
		// If the implementing game has any game specific dataloaders (such as FE8 Promotion Data), 
		// then make sure to compile the changes before we commit the freespace.
		// This should be the main position where such things should be necessary. 
		// And can help with not having to completely override this method and having to add the compile at multiple places, which is easily forgotten.
		gameSpecificDiffCompilations();
		
		freeSpace.commitChanges(diffCompiler);
	}

	/**
	 * Apply the diffs to the target file
	 */
	protected void applyDiffs() {
		if (targetPath == null) {
			return;
		}

		try {
			DiffApplicator.applyDiffs(diffCompiler, sourceFileHandler, targetPath);
		} catch (FileNotFoundException e) {
			throw new RandomizationStoppedException("Could not write to destination file.");
		}
	}

	/**
	 * Remove the Source File Handler and remove any temporary files.
	 */
	protected void cleanupSourceFiles() {
		updateStatusString("Cleaning up...");
		sourceFileHandler.close();
		sourceFileHandler = null;

		if (tempPath == null) {
			return;
		}

		File tempFile = new File(tempPath);
		if (tempFile != null) {
			Boolean success = tempFile.delete();
			if (!success) {
				System.err.println("Failed to delete temp file.");
			}
		}
	}

	// ------------------------------------------------------------------
	// Start Randomization Logic
	// ------------------------------------------------------------------

	/**
	 * Run all the Main Randomization steps
	 */
	public void executeRandomization() throws RandomizationStoppedException {
		runRandomizationStep("shuffling characters", 39, () -> shuffleCharactersIfNecessary());
		runRandomizationStep("recruitment", 40, () -> randomizeRecruitmentIfNecessary());
		runRandomizationStep("classes", 45, () -> randomizeClassesIfNecessary());
		runRandomizationStep("bases", 50, () -> randomizeBasesIfNecessary());
		runRandomizationStep("weapons", 55, () -> randomizeWeaponsIfNecessary());
		runRandomizationStep("statboosters", 57, () -> randomizeStatboostersIfNecessary());
		runRandomizationStep("other character traits", 60, () -> randomizeOtherCharacterTraitsIfNecessary());
		runRandomizationStep("growths", 65, () -> randomizeGrowthsIfNecessary());
		runRandomizationStep("miscellaneous things", 70, () -> randomizeMiscellaneousThingsIfNecessary());
		runRandomizationStep("promotions", 75, () -> randomizePromotionsIfNecessary());
	}

	protected void randomizeRecruitmentIfNecessary() {
		if (recruitOptions == null) {
			return;
		}
		updateStatusString("Randomizing recruitment...");
		Random rng = new Random(SeedGenerator.generateSeedValue(seedString, RecruitmentRandomizer.rngSalt));
		characterMap = RecruitmentRandomizer.randomizeRecruitment(recruitOptions, itemAssignmentOptions, gameType,
				charData, classData, itemData, chapterData, textData, freeSpace, rng);
		paletteFixRequired = true;
	}

	protected void randomizeGrowthsIfNecessary() {
		if (growths == null) {
			return;
		}

		Random rng = new Random(SeedGenerator.generateSeedValue(seedString, GrowthsRandomizer.rngSalt));
		switch (growths.mode) {
		case REDISTRIBUTE:
			updateStatusString("Redistributing growths...");
			GrowthsRandomizer.randomizeGrowthsByRedistribution(growths.redistributionOption.variance,
					growths.redistributionOption.minValue, growths.redistributionOption.maxValue, growths.adjustHP,
					charData, rng);
			break;
		case DELTA:
			updateStatusString("Applying random deltas to growths...");
			GrowthsRandomizer.randomizeGrowthsByRandomDelta(growths.deltaOption.variance, growths.deltaOption.minValue,
					growths.deltaOption.maxValue, growths.adjustHP, charData, rng);
			break;
		case FULL:
			updateStatusString("Randomizing growths...");
			GrowthsRandomizer.fullyRandomizeGrowthsWithRange(growths.fullOption.minValue, growths.fullOption.maxValue,
					growths.adjustHP, charData, rng);
			break;
		}
	}

	protected void randomizeBasesIfNecessary() {
		if (bases == null) {
			return;
		}

		Random rng = new Random(SeedGenerator.generateSeedValue(seedString, BasesRandomizer.rngSalt));
		switch (bases.mode) {
		case REDISTRIBUTE:
			updateStatusString("Redistributing bases...");
			BasesRandomizer.randomizeBasesByRedistribution(bases.redistributionOption.variance, charData, classData,
					rng);
			break;
		case DELTA:
			updateStatusString("Applying random deltas to growths...");
			BasesRandomizer.randomizeBasesByRandomDelta(bases.deltaOption.variance, charData, classData, rng);
			break;
		}
	}

	protected void randomizeClassesIfNecessary() {
		if (classes == null) {
			return;
		}
		if (classes.randomizePCs) {
			updateStatusString("Randomizing player classes...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, ClassRandomizer.rngSalt + 1));
			ClassRandomizer.randomizePlayableCharacterClasses(classes, itemAssignmentOptions, gameType, charData,
					classData, chapterData, itemData, textData, rng);
			paletteFixRequired = true;
		}
		if (classes.randomizeEnemies) {
			updateStatusString("Randomizing minions...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, ClassRandomizer.rngSalt + 2));
			ClassRandomizer.randomizeMinionClasses(classes, itemAssignmentOptions, gameType, charData, classData,
					chapterData, itemData, rng);
		}
		if (classes.randomizeBosses) {
			updateStatusString("Randomizing boss classes...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, ClassRandomizer.rngSalt + 3));
			ClassRandomizer.randomizeBossCharacterClasses(classes, itemAssignmentOptions, gameType, charData, classData,
					chapterData, itemData, textData, rng);
			paletteFixRequired = true;
		}
	}

	protected void randomizeOtherCharacterTraitsIfNecessary() {
		if (otherCharacterOptions == null) {
			return;
		}

		if (otherCharacterOptions.movementOptions != null) {
			updateStatusString("Randomizing class movement ranges...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, ClassRandomizer.rngSalt + 4));
			ClassRandomizer.randomizeClassMovement(otherCharacterOptions.movementOptions.minValue,
					otherCharacterOptions.movementOptions.maxValue, classData, rng);
		}
		if (otherCharacterOptions.constitutionOptions != null) {
			updateStatusString("Randomizing character constitution...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, CharacterRandomizer.rngSalt));
			CharacterRandomizer.randomizeConstitution(otherCharacterOptions.constitutionOptions.minValue,
					otherCharacterOptions.constitutionOptions.variance, charData, classData, rng);
		}
		if (otherCharacterOptions.randomizeAffinity) {
			updateStatusString("Randomizing character affinity...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, CharacterRandomizer.rngSalt + 1));
			CharacterRandomizer.randomizeAffinity(charData, rng);
		}
	}

	protected void randomizeMiscellaneousThingsIfNecessary() {
		if (miscOptions == null) {
			return;
		}

		if (miscOptions.randomizeRewards) {
			updateStatusString("Randomizing rewards...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, RandomRandomizer.rngSalt));
			RandomRandomizer.randomizeRewards(itemData, chapterData, itemAssignmentOptions.assignPromoWeapons, rng);
		}

		if (miscOptions.enemyDropChance > 0) {
			updateStatusString("Adding drops...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, RandomRandomizer.rngSalt + 1));
			RandomRandomizer.addRandomEnemyDrops(miscOptions.enemyDropChance, charData, itemData, chapterData, rng);
		}

		if (miscOptions.randomizeFogOfWar) {
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, RandomRandomizer.rngSalt + 2));
			for (GBAFEChapterMetadataChapter chapter : chapterData.getMetadataChapters()) {
				GBAFEChapterMetadataData chapterMetadata = chapterData.getMetadataForChapter(chapter);
				if (chapterMetadata.getVisionRange() > 0) {
					continue;
				}
				if (rng.nextInt(100) < miscOptions.fogOfWarChance) {
					int visionRange = rng.nextInt(
							miscOptions.fogOfWarVisionRange.maxValue - miscOptions.fogOfWarVisionRange.minValue + 1)
							+ miscOptions.fogOfWarVisionRange.minValue;
					chapterMetadata.setVisionRange(visionRange);
					chapterMetadata.commitChanges();
				}
			}
		}

		if (miscOptions.singleRNMode) {
			applySingleRN();
		}
	}

	protected void addRandomDrops() {
		updateStatusString("Adding drops...");
		Random rng = new Random(SeedGenerator.generateSeedValue(seedString, RandomRandomizer.rngSalt + 1));
		RandomRandomizer.addRandomEnemyDrops(miscOptions.enemyDropChance, charData, itemData, chapterData, rng);
	}

	protected void randomizeWeaponsIfNecessary() {
		if (weapons == null) {
			return;
		}
		if (weapons.mightOptions != null) {
			updateStatusString("Randomizing weapon power...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, WeaponsRandomizer.rngSalt));
			WeaponsRandomizer.randomizeMights(weapons.mightOptions.minValue, weapons.mightOptions.maxValue,
					weapons.mightOptions.variance, itemData, rng);
		}
		if (weapons.hitOptions != null) {
			updateStatusString("Randomizing weapon accuracy...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, WeaponsRandomizer.rngSalt + 1));
			WeaponsRandomizer.randomizeHit(weapons.hitOptions.minValue, weapons.hitOptions.maxValue,
					weapons.hitOptions.variance, itemData, rng);
		}
		if (weapons.weightOptions != null) {
			updateStatusString("Randomizing weapon weights...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, WeaponsRandomizer.rngSalt + 2));
			WeaponsRandomizer.randomizeWeight(weapons.weightOptions.minValue, weapons.weightOptions.maxValue,
					weapons.weightOptions.variance, itemData, rng);
		}
		if (weapons.durabilityOptions != null) {
			updateStatusString("Randomizing weapon durability...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, WeaponsRandomizer.rngSalt + 3));
			WeaponsRandomizer.randomizeDurability(weapons.durabilityOptions.minValue,
					weapons.durabilityOptions.maxValue, weapons.durabilityOptions.variance, itemData, rng);
		}

		if (weapons.shouldAddEffects && weapons.effectsList != null) {
			updateStatusString("Adding random effects to weapons...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seedString, WeaponsRandomizer.rngSalt + 4));
			WeaponsRandomizer.randomizeEffects(weapons.effectsList, itemData, textData, weapons.noEffectIronWeapons,
					weapons.noEffectSteelWeapons, weapons.noEffectThrownWeapons, weapons.effectChance, rng);
		}
	}

	protected void buffEnemiesIfNecessary(String seed) {
		if (enemies == null) {
			return;
		}

		if (enemies.minionMode == EnemyOptions.MinionGrowthMode.FLAT) {
			updateStatusString("Buffing enemies...");
			EnemyBuffer.buffMinionGrowthRates(enemies.minionBuff, classData, enemies.minionBuffStats);
		} else if (enemies.minionMode == EnemyOptions.MinionGrowthMode.SCALING) {
			updateStatusString("Buffing enemies...");
			EnemyBuffer.scaleEnemyGrowthRates(enemies.minionBuff, classData, enemies.minionBuffStats);
		}

		if (enemies.improveMinionWeapons) {
			updateStatusString("Upgrading enemy weapons...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seed, EnemyBuffer.rngSalt));
			EnemyBuffer.improveMinionWeapons(enemies.minionImprovementChance, charData, classData, chapterData,
					itemData, rng);
		}

		if (enemies.bossMode == BossStatMode.LINEAR) {
			updateStatusString("Buffing Bosses...");
			EnemyBuffer.buffBossStatsLinearly(enemies.bossBuff, charData, classData, enemies.bossBuffStats);
		} else if (enemies.bossMode == BossStatMode.EASE_IN_OUT) {
			updateStatusString("Buffing Bosses...");
			EnemyBuffer.buffBossStatsWithEaseInOutCurve(enemies.bossBuff, charData, classData, enemies.bossBuffStats);
		}

		if (enemies.improveBossWeapons) {
			updateStatusString("Upgrading boss weapons...");
			Random rng = new Random(SeedGenerator.generateSeedValue(seed, EnemyBuffer.rngSalt + 1));
			EnemyBuffer.improveBossWeapons(enemies.bossImprovementChance, charData, classData, chapterData, itemData,
					rng);
		}
	}

	public void shuffleCharactersIfNecessary() {
		if (charShufflingOptions == null || !charShufflingOptions.isShuffleEnabled()) {
			return;
		}
		updateStatusString("Shuffling in characters...");
		Random rng = new Random(SeedGenerator.generateSeedValue(seedString, GrowthsRandomizer.rngSalt));
		CharacterShuffler.shuffleCharacters(gameType, charData, textData, rng, sourceFileHandler, portraitData,
				freeSpace, chapterData, classData, charShufflingOptions, itemAssignmentOptions, itemData);
	}

	protected void randomizeStatboostersIfNecessary() {
		if (statboosterOptions == null || !statboosterOptions.enabled) {
			return;
		}
		
		updateStatusString("Randomizing Statboosters...");
		Random rng = new Random(SeedGenerator.generateSeedValue(seedString, StatboosterRandomizer.SALT));
		StatboosterRandomizer.randomize(statboosterOptions, statboostData, rng);
	}

	private void randomizePromotionsIfNecessary() {
		if (promoOptions == null) {
			return;
		}
		updateStatusString("Randomizing Promotions...");
		Random rng = new Random(SeedGenerator.generateSeedValue(seedString, GBAPromotionRandomizer.rngSalt));
		GBAPromotionRandomizer.randomizePromotions(promoOptions, promotionData, classData, gameType, rng);
	}

	// ------------------------------------------------------------------
	// Start common Final Adjustments
	// ------------------------------------------------------------------

	/**
	 * Abstract Method.
	 * 
	 * Create Special Lord classes, which are used to prevent Lords from promoting
	 * prematurely.
	 */
	protected abstract void createSpecialLordClasses();

	/**
	 * Abstract Method.
	 * 
	 * Create the Prf weapons for the Lords if the User selects the options to do
	 * so.
	 */
	protected abstract void createPrfs(Random rng);

	protected void applyPaletteFixes() {
		// Fix the palettes based on final classes.
		if (paletteFixRequired) {
			PaletteHelper.synchronizePalettes(gameType, recruitOptions != null ? recruitOptions.includeExtras : false,
					charData, classData, paletteData, characterMap, freeSpace);
		}
	}

	/**
	 * Loops through all ChapterUnit entries and ensures that all characters that have a staff rank have atleast one item that is a staff.
	 */
	protected void ensureHealersHaveStaves(Random rng) {
		// Make sure healing classes have at least one healing staff in their starting
		// inventory.
		for (GBAFEChapterData chapter : chapterData.allChapters()) {
			for (GBAFEChapterUnitData chapterUnit : chapter.allUnits()) {
				GBAFEClassData unitClass = classData.classForID(chapterUnit.getStartingClass());
				if (unitClass == null || unitClass.getStaffRank() == 0) {
					continue;
				}
				if (itemData.isHealingStaff(chapterUnit.getItem1()) || itemData.isHealingStaff(chapterUnit.getItem2())
						|| itemData.isHealingStaff(chapterUnit.getItem3())
						|| itemData.isHealingStaff(chapterUnit.getItem4())) {
					continue;
				}
				if (charData.isPlayableCharacterID(chapterUnit.getCharacterNumber())) {
					GBAFECharacterData character = charData.characterWithID(chapterUnit.getCharacterNumber());
					GBAFEItemData healingStaff = itemData
							.getRandomHealingStaff(itemData.rankForValue(character.getStaffRank()), rng);
					if (healingStaff != null) {
						chapterUnit.giveItem(healingStaff.getID());
					}
				}

			}
		}
	}

	/**
	 * Apply a fix to ensure that forcing a promoted unit to promote gain doesn't
	 * demote them.
	 */
	protected void applyPromotionFix() {
		// FE6 and FE7 store this on the class directly. Just switch the target
		// promotion for promoted classes to themselves.
		// Only do this if the class's demoted class promotes into it (just to make sure
		// we don't accidentally change anything we don't need to).
		for (GBAFEClassData charClass : classData.allClasses()) {
			if (classData.isPromotedClass(charClass.getID())) {
				int demotedID = charClass.getTargetPromotionID();
				GBAFEClassData demotedClass = classData.classForID(demotedID);
				if (demotedClass.getTargetPromotionID() == charClass.getID()) {
					charClass.setTargetPromotionID(charClass.getID());
				}
			}
		}
	}

	/**
	 * Update the Worldmap sprite to the new class of the given character
	 */
	protected void syncWorldMapSpriteToCharacter(GBAFEWorldMapSpriteData sprite, int characterID) {
		GBAFECharacterData character = charData.characterWithID(characterID);
		boolean spriteIsPromoted = classData.isPromotedClass(sprite.getClassID());
		int classID = character.getClassID();
		boolean characterClassIsPromoted = classData.isPromotedClass(classID);
		if (spriteIsPromoted == characterClassIsPromoted) {
			sprite.setClassID(classID);
		} else {
			if (spriteIsPromoted) {
				sprite.setClassID(classData.classForID(classID).getTargetPromotionID());
			} else {
				assert false : "This shouldn't ever be the case...";
			}
		}
	}

	// ------------------------------------------------------------------
	// Start common Final Adjustments
	// ------------------------------------------------------------------

	/**
	 * Performs necessary adjustments before the randomization takes place
	 */
	protected void makePreliminaryAdjustments() {
		// Some characters have discrepancies between character data and chapter data.
		// We'll try to address that before we get to any modifications.
		charData.applyLevelCorrectionsIfNecessary();
		itemData.prepareForRandomization();

		// Apply patch to not keep old weapon ranks if promoting to a class that can't
		// use old weapon
		prepareForPromotionRandomization();
	}

	/**
	 * If you promote and lose a weapon type, by default the old weapon type would
	 * still be available, these patches by "Vennobennu" fix that, by resetting the
	 * Weapon types to 0.
	 */
	private void prepareForPromotionRandomization() {
		String resourceFile;
		byte[] bytes;
		long offset;

		switch (gameType) {
		case FE6:
			bytes = new byte[0x49];
			resourceFile = "FE6Promotion.dmp";
			offset = 0x252F8;
			break;
		case FE7:
			bytes = new byte[0x49];
			resourceFile = "FE7Promotion.dmp";
			offset = 0x298B4;
			break;
		case FE8:
			bytes = new byte[0x35];
			resourceFile = "FE8Promotion.dmp";
			offset = 0x2BE38;
			break;

		default:
			return;

		}

		try (InputStream stream = UPSPatcher.class.getClassLoader().getResourceAsStream(resourceFile)) {
			stream.read(bytes);
		} catch (IOException e) {
			throw new RandomizationStoppedException(String.format("Encountered Error while %s.%n%n%s",
					"preparing promotion randomization", buildErrorMessage(e)));
		}

		diffCompiler.addDiff(new Diff(offset, 0x35, bytes, null));
	}

	// ------------------------------------------------------------------
	// Record Keeper
	// ------------------------------------------------------------------

	/**
	 * Records data before the randomization happens
	 */
	public void recordOriginalState() {
		charData.recordCharacters(recordKeeper, true, classData, itemData, textData);
		classData.recordClasses(recordKeeper, true, classData, textData);
		itemData.recordWeapons(recordKeeper, true, classData, textData, sourceFileHandler);
		chapterData.recordChapters(recordKeeper, true, charData, classData, itemData, textData);
		paletteData.recordReferencePalettes(recordKeeper, charData, classData, textData);
	}

	/**
	 * Records data after the randomization happened
	 */
	public void recordPostRandomizationState() {
		charData.recordCharacters(recordKeeper, false, classData, itemData, textData);
		classData.recordClasses(recordKeeper, false, classData, textData);
		itemData.recordWeapons(recordKeeper, false, classData, textData, targetFileHandler);
		chapterData.recordChapters(recordKeeper, false, charData, classData, itemData, textData);
		paletteData.recordUpdatedPalettes(recordKeeper, charData, classData, textData);

		recordKeeper.sortKeysInCategory(CharacterDataLoader.RecordKeeperCategoryKey);
		recordKeeper.sortKeysInCategory(ClassDataLoader.RecordKeeperCategoryKey);
		recordKeeper.sortKeysInCategory(ItemDataLoader.RecordKeeperCategoryWeaponKey);
	}

	/**
	 * Initialize the Record Keeper including the recording of the Selected Options.
	 */
	public void initializeRecordKeeper() {
		int index = Math.max(targetPath.lastIndexOf('/'), targetPath.lastIndexOf('\\'));
		String title = targetPath.substring(index + 1);
		recordKeeper = new RecordKeeper(title);

		recordKeeper.addHeaderItem("Game Title", gameFriendlyName);
		recordKeeper.addHeaderItem("Randomizer Seed Phrase", seedString);

		tryRecordingCategory("Randomize Growths", growths);
		tryRecordingCategory("Randomize Bases", bases);
		tryRecordingCategory("Weapons", weapons);
		tryRecordingCategory("Classes", classes);
		tryRecordingCategory("Enemies", enemies);
		tryRecordingCategory("Misc", miscOptions);
		tryRecordingCategory("Randomized Recruitment", recruitOptions);
		tryRecordingCategory("Item Assignment", itemAssignmentOptions);
		tryRecordingCategory("Promotions", promoOptions);
		tryRecordingCategory("Character Shuffling", charShufflingOptions);
		tryRecordingCategory("Statboosters", statboosterOptions);
	}

	/**
	 * if the given option is null, then this just adds a new Header to the Record
	 * keeper with the category and "NO". Indicating that this category is not being
	 * randomized.
	 * 
	 * Otherwise the responsibility for recording the options will be handed over to
	 * the method {@link RecordableOption#record(RecordKeeper, GameType)}
	 * 
	 * @param category the name of the category
	 * @param option   the option class that should be recorded
	 */
	public void tryRecordingCategory(String category, RecordableOption option) {
		if (option == null) {
			recordKeeper.addHeaderItem(category, "NO");
			return;
		}

		option.record(recordKeeper, gameType);
	}

}
