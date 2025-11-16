package random.gba.loader;

public class GBADataLoaders {
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

    public GBADataLoaders(CharacterDataLoader charData, ClassDataLoader classData, ChapterLoader chapterData, ItemDataLoader itemData, PaletteLoader paletteData, TextLoader textData, PortraitDataLoader portraitData, StatboostLoader statboostData, MapSpriteManager mapSprites, PromotionDataLoader promotionData, TerrainDataLoader terrainData) {
        this.charData = charData;
        this.classData = classData;
        this.chapterData = chapterData;
        this.itemData = itemData;
        this.paletteData = paletteData;
        this.textData = textData;
        this.portraitData = portraitData;
        this.statboostData = statboostData;
        this.mapSprites = mapSprites;
        this.promotionData = promotionData;
        this.terrainData = terrainData;
    }

    public CharacterDataLoader getCharData() {
        return charData;
    }

    public ClassDataLoader getClassData() {
        return classData;
    }

    public ChapterLoader getChapterData() {
        return chapterData;
    }

    public ItemDataLoader getItemData() {
        return itemData;
    }

    public PaletteLoader getPaletteData() {
        return paletteData;
    }

    public TextLoader getTextData() {
        return textData;
    }

    public PortraitDataLoader getPortraitData() {
        return portraitData;
    }

    public StatboostLoader getStatboostData() {
        return statboostData;
    }

    public MapSpriteManager getMapSprites() {
        return mapSprites;
    }

    public PromotionDataLoader getPromotionData() {
        return promotionData;
    }

    public TerrainDataLoader getTerrainData() {
        return terrainData;
    }
}
