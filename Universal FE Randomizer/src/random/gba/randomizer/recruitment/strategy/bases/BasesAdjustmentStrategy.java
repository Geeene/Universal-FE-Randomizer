package random.gba.randomizer.recruitment.strategy.bases;

import fedata.gba.GBAFEClassData;
import random.general.GBAStatDAO;

/**
 * Interface for Growth Adjustment Strategies when randomizing recruitment
 */
public interface BasesAdjustmentStrategy {
    public GBAStatDAO adjustBases(GBAFEClassData sourceClass, GBAStatDAO growths, GBAStatDAO promoGains);
}