package ui.tabs.gba;

import fedata.general.FEBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import ui.StatboosterView;
import ui.common.GuiUtil;
import ui.common.YuneTabItem;
import ui.views.ItemAssignmentView;
import ui.views.PrfView;
import ui.views.RewardRandomizationView;
import ui.views.WeaponsView;
import util.OptionRecorder;

/**
 * Tab Item tab for the GBAFE Games.
 *
 * This contains the views:
 * <ul>
 *     <li>Weapons</li>
 *     <li>Item Assignment</li>
 *     <li>Rewards</li>
 * </ul>
 *
 */
public class GBAItemsTab extends YuneTabItem {

    private WeaponsView weapons;
    private ItemAssignmentView itemAssignment;
    private RewardRandomizationView rewards;
    private StatboosterView statboosters;
    private PrfView prfs;

    public GBAItemsTab(CTabFolder parent, FEBase.GameType type) {
        super(parent, type);
    }

    @Override
    protected void compose() {
        weapons = addView(new WeaponsView(container, type, 2), GuiUtil.defaultGridData(2));
        setViewData(weapons, 2, 2);
        itemAssignment = addView(new ItemAssignmentView(container, type));
        statboosters = addView(new StatboosterView(container));

        // these two views are located below the weapons view which has a colspan of two.
        // But since there is a margin of 5 pixels between the two, views, they would be a bit wider and so misaligned.
        // reduce each of these views by 5px to make sure they are properly aligned with the weapons view
        rewards = addView(new RewardRandomizationView(container, type), GuiUtil.defaultGridData(1, -5));
        prfs = addView(new PrfView(container), GuiUtil.defaultGridData(1, -5));
    }

    @Override
    protected String getTabName() {
        return "Items";
    }

    @Override
    protected String getTabTooltip() {
        return "Contains all Setting related to Items. For Example Weapon Stats, Rewards, and Weapon Assignment";
    }

    @Override
    protected int numberColumns() {
        return 3;
    }

    @Override
    public void preloadOptions(OptionRecorder.GBAOptionBundle bundle) {
        this.weapons.initialize(bundle.weapons);
        this.itemAssignment.initialize(bundle.itemAssignmentOptions);
        this.rewards.initialize(bundle.rewards);
        this.prfs.initialize(bundle.prfs);
        this.statboosters.initialize(bundle.statboosters);
    }

    @Override
    public void updateOptionBundle(OptionRecorder.GBAOptionBundle bundle) {
        bundle.weapons = weapons.getOptions();
        bundle.itemAssignmentOptions = itemAssignment.getOptions();
        bundle.rewards = rewards.getOptions();
        bundle.prfs = prfs.getOptions();
        bundle.statboosters = statboosters.getOptions();
    }
}
