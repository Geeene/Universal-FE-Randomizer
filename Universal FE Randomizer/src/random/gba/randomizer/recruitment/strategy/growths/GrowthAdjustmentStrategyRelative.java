package random.gba.randomizer.recruitment.strategy.growths;

import fedata.gba.GBAFECharacterData;
import random.general.GBAStatDAO;
import random.general.RelativeStatValueMapper;

/**
 * Growth Assignment Strategy that maps the fill stats to the highest stats of the slot stats.
 */
public class GrowthAdjustmentStrategyRelative implements GrowthAdjustmentStrategy{
    @Override
    public GBAStatDAO adjustGrowths(GBAFECharacterData slot, GBAFECharacterData fill) {
        return RelativeStatValueMapper.mappedValues(slot.getGrowths().getAll(), fill.getGrowths().getAll());
    }
}