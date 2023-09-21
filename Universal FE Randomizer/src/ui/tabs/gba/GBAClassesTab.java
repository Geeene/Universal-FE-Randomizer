package ui.tabs.gba;

import fedata.general.FEBase;
import org.eclipse.swt.custom.CTabFolder;
import ui.common.YuneTabItem;
import ui.views.CharacterShufflingView;
import ui.views.ClassesView;
import ui.views.PromotionView;
import ui.views.RecruitmentView;
import util.OptionRecorder.GBAOptionBundle;

/**
 * Tab for all Settings related to the character pool.
 *
 * This contains the views:
 * <ul>
 *     <li>Class Randomization</li>
 *     <li>Promotion Randomization</li>
 * </ul>
 *
 */
public class GBAClassesTab extends YuneTabItem {
    public GBAClassesTab(CTabFolder parent, FEBase.GameType type) {
        super(parent, type);
    }

    private ClassesView classes;
    private PromotionView promotions;

    @Override
    protected void compose() {
        classes = addView(new ClassesView(container, type));
        promotions = addView(new PromotionView(container, type));
    }

    @Override
    protected String getTabName() {
        return "Classes";
    }

    @Override
    protected String getTabTooltip() {
        return "This tab contains all settings that are related to the characters classes including randomizing the promotions.";
    }

    @Override
    protected int numberColumns() {
        return 2;
    }

    @Override
    public void preloadOptions(GBAOptionBundle bundle) {
        classes.initialize(bundle.classes);
        promotions.initialize(bundle.promotionOptions);
    }

    @Override
    public void updateOptionBundle(GBAOptionBundle bundle) {
        bundle.classes = classes.getOptions();
        bundle.promotionOptions = promotions.getOptions();
    }

}
