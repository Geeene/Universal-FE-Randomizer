package random.gba.randomizer.recruitment.strategy.bases;

import random.gba.randomizer.recruitment.strategy.growths.GrowthAdjustmentStrategy;
import ui.model.RecruitmentOptions;

/**
 * Holder class that can be used to get the correct Growth Adjustment Strategy based on the Options that the user selected.
 */
public class BasesAdjustmentStrategyHolder {
    public static GrowthAdjustmentStrategy getGrowthAdjustmentStrategyByOptions(RecruitmentOptions options) {
        if (options.baseMode == RecruitmentOptions.StatAdjustmentMode.AUTOLEVEL) {

        }
        return null;
    }
}