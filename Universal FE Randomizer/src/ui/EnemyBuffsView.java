package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import ui.model.EnemyOptions;
import ui.model.EnemyOptions.BossStatMode;
import ui.model.EnemyOptions.MinionGrowthMode;

public class EnemyBuffsView extends AbstractYuneView {

    private Group container;

    private Button buffGrowthsButton;
    private Label minionSpinnerLabel;
    private Spinner buffSpinner;

    private Button flatBonusButton;
    private Button scalingBonusButton;

    private Button improveEnemyWeaponsButton;
    private Label minionWeaponSpinnerLabel;
    private Spinner weaponSpinner;

    private Button minionHP;
    private Button minionSTR;
    private Button minionSKL;
    private Button minionSPD;
    private Button minionLCK;
    private Button minionDEF;
    private Button minionRES;

    private Button buffBossStatButton;
    private Label bossStatSpinnerLabel;
    private Spinner bossStatSpinner;

    private Button linearBossButton;
    private Button easeInOutBossButton;

    private Button improveBossWeaponButton;
    private Label bossWeaponSpinnerLabel;
    private Spinner bossWeaponSpinner;

    private Button bossHP;
    private Button bossSTR;
    private Button bossSKL;
    private Button bossSPD;
    private Button bossLCK;
    private Button bossDEF;
    private Button bossRES;

    public EnemyBuffsView(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        container = createContainer(this, "Buff Enemies",
                "Options to mix up the normal enemies, generally to make the game more challenging.");
        setGroupMargins(container);

        Group minionGroup = createContainer(container, "Minions");
        setGroupMargins(minionGroup);
        layout(minionGroup, new FormAttachment(0, 0), new FormAttachment(0, 0), new FormAttachment(100, 0));

        buffGrowthsButton = createButton(minionGroup, SWT.CHECK, "Buff Enemy Growths",
                "Increases enemy growth rates.\n\nNote: Regardless of the method and amount used, the cap for\nenemy growths is 127% in any single area.");
        layout(buffGrowthsButton, new FormAttachment(0, 5), new FormAttachment(0, 5));
        buffGrowthsButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                buffSpinner.setEnabled(buffGrowthsButton.getSelection());
                minionSpinnerLabel.setEnabled(buffGrowthsButton.getSelection());

                flatBonusButton.setEnabled(buffGrowthsButton.getSelection());
                scalingBonusButton.setEnabled(buffGrowthsButton.getSelection());

                minionHP.setEnabled(buffGrowthsButton.getSelection());
                minionSTR.setEnabled(buffGrowthsButton.getSelection());
                minionSKL.setEnabled(buffGrowthsButton.getSelection());
                minionSPD.setEnabled(buffGrowthsButton.getSelection());
                minionLCK.setEnabled(buffGrowthsButton.getSelection());
                minionDEF.setEnabled(buffGrowthsButton.getSelection());
                minionRES.setEnabled(buffGrowthsButton.getSelection());
            }
        });

        Composite buffParamContainer = new Composite(minionGroup, SWT.NONE);
        setGroupMargins(buffParamContainer);
        layout(buffParamContainer, new FormAttachment(buffGrowthsButton, 0),
                new FormAttachment(buffGrowthsButton, 0, SWT.LEFT), new FormAttachment(100, -5));

        minionSpinnerLabel = createLabel(buffParamContainer, SWT.RIGHT, "Buff Amount:");
        layout(minionSpinnerLabel, new FormAttachment(buffSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
                new FormAttachment(buffSpinner, -5));

        buffSpinner = createSpinner(buffParamContainer, new int[]{10, 0, 100, 0, 1, 1}, "", false);
        layout(buffSpinner, null, null, new FormAttachment(100, -5));

        Composite buffModeContainer = new Composite(buffParamContainer, SWT.NONE);
        buffModeContainer.setLayout(new FillLayout());
        layout(buffModeContainer, new FormAttachment(buffSpinner, 5), new FormAttachment(0, 0),
                new FormAttachment(100, 0));

        flatBonusButton = createButton(buffModeContainer, SWT.RADIO, "Flat Buff",
                "The buff amount is directly added to the enemy's growth rates for all stats.", true, false);

        scalingBonusButton = createButton(buffModeContainer, SWT.RADIO, "Scaling Buff",
                "The buff amount is multiplied as a percentage to the enemy's growth rates for all stats.");

        Composite minionStatGroup = new Composite(minionGroup, SWT.NONE);
        minionStatGroup.setLayout(new RowLayout());
        layout(minionStatGroup, 200, 0, new FormAttachment(buffParamContainer, 0), new FormAttachment(0, 10),
                new FormAttachment(100, -10));

        minionHP = createButton(minionStatGroup, SWT.CHECK, "HP", "Apply buff to minion health.", false, true);
        minionSTR = createButton(minionStatGroup, SWT.CHECK, "STR/MAG", "Apply buff to attack power.", false, true);
        minionSKL = createButton(minionStatGroup, SWT.CHECK, "SKL",
                "Apply buff to minion accuracy and critical chance.", false, true);
        minionSPD = createButton(minionStatGroup, SWT.CHECK, "SPD", "Apply buff to minion speed and evasion.", false,
                true);
        minionLCK = createButton(minionStatGroup, SWT.CHECK, "LCK",
                "Apply buff to minion accuracy and critical evasion.", false, true);
        minionDEF = createButton(minionStatGroup, SWT.CHECK, "DEF", "Apply buff to minion physical defense.", false,
                true);
        minionRES = createButton(minionStatGroup, SWT.CHECK, "RES", "Apply buff to minion magical defense.", false,
                true);

        //////////////////////////////////////////////////////////////////
        improveEnemyWeaponsButton = createButton(minionGroup, SWT.CHECK, "Improve Enemy Weapons",
                "Adds a chance for enemies to spawn with a higher tier weapon than usual.");
        layout(improveEnemyWeaponsButton, new FormAttachment(minionStatGroup, 10), new FormAttachment(0, 5));
        improveEnemyWeaponsButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                weaponSpinner.setEnabled(improveEnemyWeaponsButton.getSelection());
                minionWeaponSpinnerLabel.setEnabled(improveEnemyWeaponsButton.getSelection());
            }
        });

        minionWeaponSpinnerLabel = createLabel(minionGroup, SWT.RIGHT, "Chance:");
        layout(minionWeaponSpinnerLabel, new FormAttachment(weaponSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
                new FormAttachment(weaponSpinner, -5));

        weaponSpinner = createSpinner(minionGroup, new int[]{25, 0, 100, 0, 1, 5}, false);
        layout(weaponSpinner, new FormAttachment(improveEnemyWeaponsButton, 5), null, new FormAttachment(100, -10),
                new FormAttachment(100, -5));

        //////////////////////////////////////////////////////////////////

        Group bossGroup = createContainer(container, "Bosses");
        setGroupMargins(bossGroup);
        layout(bossGroup, new FormAttachment(minionGroup, 5), new FormAttachment(0, 0), new FormAttachment(100, 0),
                new FormAttachment(100, 0));

        buffBossStatButton = createButton(bossGroup, SWT.CHECK, "Buff Boss Stats", "Increases base stats of bosses.",
                true, false);
        buffBossStatButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean enabled = buffBossStatButton.getSelection();
                bossStatSpinnerLabel.setEnabled(enabled);
                bossStatSpinner.setEnabled(enabled);
                linearBossButton.setEnabled(enabled);
                easeInOutBossButton.setEnabled(enabled);

                bossHP.setEnabled(enabled);
                bossSTR.setEnabled(enabled);
                bossSKL.setEnabled(enabled);
                bossSPD.setEnabled(enabled);
                bossLCK.setEnabled(enabled);
                bossDEF.setEnabled(enabled);
                bossRES.setEnabled(enabled);
            }
        });

        linearBossButton = createButton(bossGroup, SWT.RADIO, "Scale Linearly",
                "Bosses gradually gain stats in a linear fashion up to the max gain.", true, false);
        layout(linearBossButton, new FormAttachment(buffBossStatButton, 5),
                new FormAttachment(buffBossStatButton, 5, SWT.LEFT));

        easeInOutBossButton = createButton(bossGroup, SWT.RADIO, "Ease In/Ease Out",
                "Ramps up more slowly and eases into the max gain.");
        layout(easeInOutBossButton, new FormAttachment(linearBossButton, 5),
                new FormAttachment(linearBossButton, 0, SWT.LEFT));

        bossStatSpinner = createSpinner(bossGroup,
                "The maximum amount of stats a boss can gain in each area.\nThis value is the gain for the final regular boss of the game.",
                false);
        layout(bossStatSpinner, new FormAttachment(easeInOutBossButton, 5), null, new FormAttachment(100, -5));

        bossStatSpinnerLabel = createLabel(bossGroup, "Max Boost:");
        layout(bossStatSpinnerLabel, new FormAttachment(bossStatSpinner, 0, SWT.CENTER),
                new FormAttachment(bossStatSpinner, -5));

        Composite bossStatGroup = new Composite(bossGroup, SWT.NONE);
        bossStatGroup.setLayout(new RowLayout());
        layout(bossStatGroup, 200, 0, new FormAttachment(bossStatSpinner, 5), new FormAttachment(0, 10),
                new FormAttachment(100, -10));

        bossHP = createButton(minionStatGroup, SWT.CHECK, "HP", "Apply buff to boss health.", false, true);
        bossSTR = createButton(minionStatGroup, SWT.CHECK, "STR/MAG", "Apply buff to boss attack power.", false, true);
        bossSKL = createButton(minionStatGroup, SWT.CHECK, "SKL", "Apply buff to minion accuracy and critical chance.",
                false, true);
        bossSPD = createButton(minionStatGroup, SWT.CHECK, "SPD", "Apply buff to boss speed and evasion.", false, true);
        bossLCK = createButton(minionStatGroup, SWT.CHECK, "LCK", "Apply buff to boss accuracy and critical evasion.",
                false, true);
        bossDEF = createButton(minionStatGroup, SWT.CHECK, "DEF", "Apply buff to boss physical defense.", false, true);
        bossRES = createButton(minionStatGroup, SWT.CHECK, "RES", "Apply buff to boss magical defense.", false, true);

        improveBossWeaponButton = createButton(bossGroup, SWT.CHECK, "Improve Boss Weapons",
                "Adds a chance for bosses to spawn with a higher tier weapon than usual.", true, false);
        layout(improveBossWeaponButton, new FormAttachment(bossStatGroup, 10),
                new FormAttachment(buffBossStatButton, 0, SWT.LEFT));
        improveBossWeaponButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean enabled = improveBossWeaponButton.getSelection();
                bossWeaponSpinner.setEnabled(enabled);
                bossWeaponSpinnerLabel.setEnabled(enabled);
            }
        });

        bossWeaponSpinnerLabel = createLabel(bossGroup, "Chance:");
        layout(bossWeaponSpinnerLabel, new FormAttachment(bossWeaponSpinner, 0, SWT.CENTER), null,
                new FormAttachment(bossWeaponSpinner, -5));

        bossWeaponSpinner = createSpinner(bossGroup, new int[]{25, 0, 100, 0, 1, 5}, false);
        layout(bossWeaponSpinner, new FormAttachment(improveBossWeaponButton, 5), null, new FormAttachment(100, -10),
                new FormAttachment(100, -5));
    }

    public EnemyOptions getEnemyOptions() {

        boolean buffMinionWeapons = improveEnemyWeaponsButton.getSelection();
        int minionWeaponChance = weaponSpinner.getSelection();

        boolean buffBossWeapons = improveBossWeaponButton.getSelection();
        int bossWeaponChance = bossWeaponSpinner.getSelection();

        MinionGrowthMode minionMode = MinionGrowthMode.NONE;
        if (buffGrowthsButton.getSelection()) {
            if (flatBonusButton.getSelection()) {
                minionMode = MinionGrowthMode.FLAT;
            } else if (scalingBonusButton.getSelection()) {
                minionMode = MinionGrowthMode.SCALING;
            }
        }

        BossStatMode bossMode = BossStatMode.NONE;
        if (buffBossStatButton.getSelection()) {
            if (linearBossButton.getSelection()) {
                bossMode = BossStatMode.LINEAR;
            } else if (easeInOutBossButton.getSelection()) {
                bossMode = BossStatMode.EASE_IN_OUT;
            }
        }

        return new EnemyOptions(minionMode, buffSpinner.getSelection(), buffMinionWeapons, minionWeaponChance,
                new EnemyOptions.BuffStats(minionHP.getSelection(), minionSTR.getSelection(), minionSKL.getSelection(),
                        minionSPD.getSelection(), minionLCK.getSelection(), minionDEF.getSelection(),
                        minionRES.getSelection()),
                bossMode, bossStatSpinner.getSelection(), buffBossWeapons, bossWeaponChance,
                new EnemyOptions.BuffStats(bossHP.getSelection(), bossSTR.getSelection(), bossSKL.getSelection(),
                        bossSPD.getSelection(), bossLCK.getSelection(), bossDEF.getSelection(),
                        bossRES.getSelection()));
    }

    public void setEnemyOptions(EnemyOptions options) {
        if (options == null) {
            return;
        }
        if (options.minionMode != null) {
            switch (options.minionMode) {
                case NONE:
                    buffGrowthsButton.setSelection(false);
                    flatBonusButton.setEnabled(false);
                    scalingBonusButton.setEnabled(false);
                    buffSpinner.setEnabled(false);
                    minionSpinnerLabel.setEnabled(false);
                    break;
                case FLAT:
                    buffGrowthsButton.setSelection(true);
                    flatBonusButton.setEnabled(true);
                    scalingBonusButton.setEnabled(true);
                    flatBonusButton.setSelection(true);
                    scalingBonusButton.setSelection(false);
                    buffSpinner.setEnabled(true);
                    minionSpinnerLabel.setEnabled(true);
                    buffSpinner.setSelection(options.minionBuff);
                    break;
                case SCALING:
                    buffGrowthsButton.setSelection(true);
                    flatBonusButton.setEnabled(true);
                    scalingBonusButton.setEnabled(true);
                    flatBonusButton.setSelection(false);
                    scalingBonusButton.setSelection(true);
                    buffSpinner.setEnabled(true);
                    minionSpinnerLabel.setEnabled(true);
                    buffSpinner.setSelection(options.minionBuff);
                    break;
            }

            if (options.minionMode != EnemyOptions.MinionGrowthMode.NONE) {
                minionHP.setEnabled(true);
                minionSTR.setEnabled(true);
                minionSKL.setEnabled(true);
                minionSPD.setEnabled(true);
                minionDEF.setEnabled(true);
                minionRES.setEnabled(true);
                minionLCK.setEnabled(true);

                if (options.minionBuffStats != null) {
                    minionHP.setSelection(options.minionBuffStats.hp);
                    minionSTR.setSelection(options.minionBuffStats.str);
                    minionSKL.setSelection(options.minionBuffStats.skl);
                    minionSPD.setSelection(options.minionBuffStats.spd);
                    minionDEF.setSelection(options.minionBuffStats.def);
                    minionRES.setSelection(options.minionBuffStats.res);
                    minionLCK.setSelection(options.minionBuffStats.lck);
                }
            }
        }
        if (options.improveMinionWeapons) {
            improveEnemyWeaponsButton.setSelection(true);
            weaponSpinner.setEnabled(true);
            weaponSpinner.setSelection(options.minionImprovementChance);
            minionWeaponSpinnerLabel.setEnabled(true);
        }

        if (options.bossMode != null) {
            switch (options.bossMode) {
                case NONE:
                    buffBossStatButton.setSelection(false);
                    linearBossButton.setEnabled(false);
                    easeInOutBossButton.setEnabled(false);
                    bossStatSpinner.setEnabled(false);
                    bossStatSpinnerLabel.setEnabled(false);
                    break;
                case LINEAR:
                    buffBossStatButton.setSelection(true);
                    linearBossButton.setEnabled(true);
                    easeInOutBossButton.setEnabled(true);
                    bossStatSpinner.setEnabled(true);
                    bossStatSpinnerLabel.setEnabled(true);

                    linearBossButton.setSelection(true);
                    easeInOutBossButton.setSelection(false);

                    bossStatSpinner.setSelection(options.bossBuff);
                    break;
                case EASE_IN_OUT:
                    buffBossStatButton.setSelection(true);
                    linearBossButton.setEnabled(true);
                    easeInOutBossButton.setEnabled(true);
                    bossStatSpinner.setEnabled(true);
                    bossStatSpinnerLabel.setEnabled(true);

                    linearBossButton.setSelection(false);
                    easeInOutBossButton.setSelection(true);

                    bossStatSpinner.setSelection(options.bossBuff);
                    break;
            }

            if (options.bossMode != EnemyOptions.BossStatMode.NONE) {
                bossHP.setEnabled(true);
                bossSTR.setEnabled(true);
                bossSKL.setEnabled(true);
                bossSPD.setEnabled(true);
                bossDEF.setEnabled(true);
                bossRES.setEnabled(true);
                bossLCK.setEnabled(true);

                if (options.bossBuffStats != null) {
                    bossHP.setSelection(options.bossBuffStats.hp);
                    bossSTR.setSelection(options.bossBuffStats.str);
                    bossSKL.setSelection(options.bossBuffStats.skl);
                    bossSPD.setSelection(options.bossBuffStats.spd);
                    bossDEF.setSelection(options.bossBuffStats.def);
                    bossRES.setSelection(options.bossBuffStats.res);
                    bossLCK.setSelection(options.bossBuffStats.lck);
                }
            }
        }
        if (options.improveBossWeapons) {
            improveBossWeaponButton.setSelection(true);
            bossWeaponSpinnerLabel.setEnabled(true);
            bossWeaponSpinner.setEnabled(true);
            bossStatSpinnerLabel.setEnabled(true);
            bossWeaponSpinner.setSelection(options.bossImprovementChance);
        }
    }
}
