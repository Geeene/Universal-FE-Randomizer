package random.gba.randomizer.recruitment.strategy.growths;

import ui.model.RecruitmentOptions;

/**
 * Holder class that can be used to get the correct Growth Adjustment Strategy based on the Options that the user selected.
 */
public class GrowthAdjustmentStrategyHolder {
    public static GrowthAdjustmentStrategy getGrowthAdjustmentStrategyByOptions(RecruitmentOptions options) {
        switch(options.growthMode){
            case USE_SLOT:
                return new GrowthAdjustmentStrategySlot();
            case RELATIVE_TO_SLOT:
                return new GrowthAdjustmentStrategyRelative();
            default:
                return new GrowthAdjustmentStrategyFill();
        }
    }
}