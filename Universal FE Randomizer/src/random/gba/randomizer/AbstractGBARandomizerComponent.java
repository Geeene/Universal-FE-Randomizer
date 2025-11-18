package random.gba.randomizer;

import fedata.general.FEBase.GameType;
import random.gba.loader.*;
import ui.model.*;
import util.FreeSpaceManager;
import util.OptionRecorder.GBAOptionBundle;
import util.SeedGenerator;

import java.util.Random;

public abstract class AbstractGBARandomizerComponent {

    protected Random rng;
    protected GameType type;

    protected GBADataLoaders dataLoaders;
    protected CharacterDataLoader charData;
    protected ClassDataLoader classData;
    protected ChapterLoader chapterData;
    protected ItemDataLoader itemData;
    protected PaletteLoader paletteData;
    protected TextLoader textData;
    protected PortraitDataLoader portraitData;
    protected StatboostLoader statboostData;
    protected MapSpriteManager mapSprites;
    protected PromotionDataLoader promotionData;
    protected TerrainDataLoader terrainData;
    protected FreeSpaceManager freeSpace;

    protected GBAOptionBundle allOptions;
    protected GrowthOptions growths;
    protected BaseOptions bases;
    protected ClassOptions classes;
    protected WeaponOptions weapons;
    protected OtherCharacterOptions otherCharacterOptions;
    protected EnemyOptions enemies;
    protected GameMechanicOptions miscOptions;
    protected RecruitmentOptions recruitOptions;
    protected ItemAssignmentOptions itemAssignmentOptions;
    protected CharacterShufflingOptions shufflingOptions;
    protected RewardOptions rewardOptions;
    protected PrfOptions prfOptions;
    protected StatboosterOptions statboosters;
    protected PromotionOptions promotionOptions;
    protected TerrainOptions terrainOptions;
    protected AutolevelingParameters autolevelingParameters;


    public AbstractGBARandomizerComponent(GBAOptionBundle allOptions, GBADataLoaders dataLoaders, Random rng, GameType type) {
        this.allOptions = allOptions;
        this.dataLoaders = dataLoaders;
        this.rng = rng;
        this.type = type;

        growths = allOptions.growths;
        bases = allOptions.bases;
        classes = allOptions.classes;
        weapons = allOptions.weapons;
        otherCharacterOptions = allOptions.other;
        enemies = allOptions.enemies;
        miscOptions = allOptions.otherOptions;
        recruitOptions = allOptions.recruitmentOptions;
        itemAssignmentOptions = allOptions.itemAssignmentOptions;
        shufflingOptions = allOptions.characterShufflingOptions;
        rewardOptions = allOptions.rewards;
        prfOptions = allOptions.prfs;
        statboosters = allOptions.statboosters;
        promotionOptions = allOptions.promotionOptions;
        terrainOptions = allOptions.terrainOptions;
        autolevelingParameters = allOptions.autolevelingParameters;

        charData = dataLoaders.getCharData();
        classData = dataLoaders.getClassData();
        chapterData = dataLoaders.getChapterData();
        itemData = dataLoaders.getItemData();
        paletteData = dataLoaders.getPaletteData();
        textData = dataLoaders.getTextData();
        portraitData = dataLoaders.getPortraitData();
        statboostData = dataLoaders.getStatboostData();
        mapSprites = dataLoaders.getMapSprites();
        promotionData = dataLoaders.getPromotionData();
        terrainData = dataLoaders.getTerrainData();
        freeSpace = dataLoaders.getFreeSpace();
    }

    public void updateRng(int salt) {
        this.rng = new Random(SeedGenerator.generateSeedValue(allOptions.seed, salt));
    }
}
