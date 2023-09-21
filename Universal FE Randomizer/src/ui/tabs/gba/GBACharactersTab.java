package ui.tabs.gba;

import fedata.general.FEBase;
import org.eclipse.swt.custom.CTabFolder;
import ui.common.YuneTabItem;
import ui.views.CharacterShufflingView;
import ui.views.ClassesView;
import ui.views.RecruitmentView;
import util.OptionRecorder.GBAOptionBundle;

/**
 * Tab for all Settings related to the character pool.
 *
 * This contains the views:
 * <ul>
 *     <li>Random Recruitment</li>
 *     <li>Character Shuffling</li>
 * </ul>
 *
 */
public class GBACharactersTab extends YuneTabItem {
    public GBACharactersTab(CTabFolder parent, FEBase.GameType type) {
        super(parent, type);
    }

    private RecruitmentView recruitment;
    private CharacterShufflingView shuffling;

    @Override
    protected void compose() {
        recruitment = addView(new RecruitmentView(container, type));
        shuffling = addView(new CharacterShufflingView(container, type));
    }

    @Override
    protected String getTabName() {
        return "Characters";
    }

    @Override
    protected String getTabTooltip() {
        return "This tab contains all settings that are related to the character Slots. Such as shuffling in characters from configuration or randomizing the recruitment order.";
    }

    @Override
    protected int numberColumns() {
        return 3;
    }

    @Override
    public void preloadOptions(GBAOptionBundle bundle) {
        recruitment.initialize(bundle.recruitmentOptions);
        shuffling.initialize(bundle.characterShufflingOptions);
    }

    @Override
    public void updateOptionBundle(GBAOptionBundle bundle) {
        bundle.recruitmentOptions = recruitment.getOptions();
        bundle.characterShufflingOptions = shuffling.getOptions();
    }

}
