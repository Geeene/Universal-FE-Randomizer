package random.gba.randomizer.recruitment.strategy.growths;

import fedata.gba.GBAFECharacterData;
import random.general.GBAStatDAO;

public class GrowthAdjustmentStrategyFill implements GrowthAdjustmentStrategy{
    @Override
    public GBAStatDAO adjustGrowths(GBAFECharacterData slot, GBAFECharacterData fill) {
        return new GBAStatDAO(fill);
    }
}