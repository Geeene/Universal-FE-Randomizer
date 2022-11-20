package random.gba.randomizer.recruitment.strategy.growths;

import fedata.gba.GBAFECharacterData;
import random.general.GBAStatDAO;

/**
 * Interface for Growth Adjustment Strategies when randomizing recruitment
 */
public interface GrowthAdjustmentStrategy {
    public GBAStatDAO adjustGrowths(GBAFECharacterData slot, GBAFECharacterData fill);
}