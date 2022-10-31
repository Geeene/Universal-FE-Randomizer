package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import fedata.general.FEBase.GameType;
import ui.model.ClassOptions;
import ui.model.ClassOptions.BaseTransferOption;
import ui.model.ClassOptions.GenderRestrictionOption;
import ui.model.ClassOptions.GrowthAdjustmentOption;

public class ClassesView extends AbstractYuneView {

    private Group container;

    private Button randomizePCButton;
    private Button randomizePCLordsButton;
    private Button createNewPrfWeaponsButton;
    private Button unbreakablePrfsButton;
    private Button randomizePCThievesButton;
    private Button randomizePCSpecialButton;
    private Button evenClassesButton;

    private Button randomizeEnemiesButton;

    private Button randomizeBossesButton;

    private Button forceChangeButton;

    private Button strictGenderButton;
    private Button looseGenderButton;
    private Button noGenderButton;

    private Boolean hasMonsterOption;
    private Button mixMonsterClasses;

    private Group baseTransferGroup;
    private Button basesNoChangeButton;
    private Button basesAdjustMatchButton;
    private Button basesAdjustClassButton;

    private Group growthAdjustmentGroup;
    private Button growthNoAdjustmentButton;
    private Button personalGrowthButton;
    // Maybe enable this option in the future, but it does result in basically every
    // character of the same class have the same exact growth spread,
    // which, on reflection, doesn't sound very interesting.
//	private Button classRelativeGrowthButton;

    public ClassesView(Composite parent, int style, GameType type) {
        super(parent, style);
        setLayout(new FillLayout());

        container = createContainer(this, "Classes", "Randomize classes for all characters.");
        setGroupMargins(container);

        randomizePCButton = createButton(container, SWT.CHECK, "Randomize Playable Characters", null);
        layout(randomizePCButton, new FormAttachment(0, 5), new FormAttachment(0, 5));
        randomizePCButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                randomizePCLordsButton.setEnabled(randomizePCButton.getSelection());
                createNewPrfWeaponsButton
                        .setEnabled(randomizePCButton.getSelection() && randomizePCLordsButton.getSelection());
                unbreakablePrfsButton.setEnabled(randomizePCButton.getSelection()
                        && randomizePCLordsButton.getSelection() && createNewPrfWeaponsButton.getSelection());
                randomizePCThievesButton.setEnabled(randomizePCButton.getSelection());
                randomizePCSpecialButton.setEnabled(randomizePCButton.getSelection());
                evenClassesButton.setEnabled(randomizePCButton.getSelection());

                baseTransferGroup.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                basesNoChangeButton
                        .setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                basesAdjustMatchButton
                        .setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                basesAdjustClassButton
                        .setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());

                growthAdjustmentGroup.setEnabled(randomizePCButton.getSelection());
                growthNoAdjustmentButton.setEnabled(randomizePCButton.getSelection());
                personalGrowthButton.setEnabled(randomizePCButton.getSelection());
//				classRelativeGrowthButton.setEnabled(randomizePCButton.getSelection());

                forceChangeButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection()
                        || randomizeEnemiesButton.getSelection());
                strictGenderButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                looseGenderButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                noGenderButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());

                if (hasMonsterOption) {
                    mixMonsterClasses.setEnabled(randomizePCButton.getSelection()
                            || randomizeBossesButton.getSelection() || randomizeEnemiesButton.getSelection());
                }
            }
        });

        randomizePCLordsButton = createButton(container, SWT.CHECK, "Include Lords",
                "If enabled, allows lords to be changed to random classes, as well as adds lords to the randomizable class pool.");
        layout(randomizePCLordsButton, new FormAttachment(randomizePCButton, 5),
                new FormAttachment(randomizePCButton, 10, SWT.LEFT));

        createNewPrfWeaponsButton = createButton(container, SWT.CHECK, "Create Matching Prf Weapons",
                getPrfToolTip(type));
        layout(createNewPrfWeaponsButton, new FormAttachment(randomizePCLordsButton, 5),
                new FormAttachment(randomizePCLordsButton, 10, SWT.LEFT));

        unbreakablePrfsButton = createButton(container, SWT.CHECK, "Make Prf Weapons Unbreakable",
                "If enabled, newly created Prf weapons will have infinite durability.");
        layout(unbreakablePrfsButton, new FormAttachment(createNewPrfWeaponsButton, 5),
                new FormAttachment(createNewPrfWeaponsButton, 10, SWT.LEFT));

        randomizePCLordsButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                createNewPrfWeaponsButton
                        .setEnabled(randomizePCLordsButton.getSelection() && randomizePCButton.getSelection());
                unbreakablePrfsButton.setEnabled(randomizePCLordsButton.getSelection()
                        && createNewPrfWeaponsButton.getSelection() && randomizePCButton.getSelection());
            }
        });

        createNewPrfWeaponsButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                unbreakablePrfsButton.setEnabled(randomizePCLordsButton.getSelection()
                        && createNewPrfWeaponsButton.getSelection() && randomizePCButton.getSelection());
            }
        });

        randomizePCThievesButton = createButton(container, SWT.CHECK, "Include Thieves",
                "If enabled, allows thieves to be changed to random classes, as well as adds thieves to the randomizable class pool.");
        layout(randomizePCThievesButton, new FormAttachment(unbreakablePrfsButton, 5),
                new FormAttachment(randomizePCLordsButton, 0, SWT.LEFT));

        randomizePCSpecialButton = createButton(container, SWT.CHECK, "Include Special Classes",
                "If enabled, allows characters in special classes to be randomized, as well as adding those special classes to the class pool.");
        layout(randomizePCSpecialButton, new FormAttachment(randomizePCThievesButton, 5),
                new FormAttachment(randomizePCThievesButton, 0, SWT.LEFT));

        evenClassesButton = createButton(container, SWT.CHECK, "Assign Classes Evenly",
                "Attempts to assign classes so that the number of duplicates is minimized.");
        layout(evenClassesButton, new FormAttachment(randomizePCSpecialButton, 5),
                new FormAttachment(randomizePCSpecialButton, 0, SWT.LEFT));

        growthAdjustmentGroup = createContainer(container, "Growths");
        setGroupMargins(growthAdjustmentGroup);
        layout(growthAdjustmentGroup, new FormAttachment(evenClassesButton, 10), new FormAttachment(0, 10),
                new FormAttachment(100, 0));

        growthNoAdjustmentButton = createButton(growthAdjustmentGroup, SWT.RADIO, "No Adjustment",
                "Do not adjust growth rates.", false, true);
        defaultLayout(growthNoAdjustmentButton);

        personalGrowthButton = createButton(growthAdjustmentGroup, SWT.RADIO, "Transfer Personal Growths",
                "Apply personal growth offsets from the old class growths to the new class growths.\n\nFor example, if a character's old SPD growth was 10% higher than the old class's SPD growth,\ntheir new SPD growth would be 10% higher than the new class's SPD growth.");
        layout(personalGrowthButton, new FormAttachment(growthNoAdjustmentButton, 5), new FormAttachment(0, 0));

        //////////////////////////////////////////////////////////////////
        randomizeEnemiesButton = createButton(container, SWT.CHECK, "Randomize Regular Enemies", null);
        layout(randomizeEnemiesButton, new FormAttachment(growthAdjustmentGroup, 10),
                new FormAttachment(randomizePCButton, 0, SWT.LEFT));
        randomizeEnemiesButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                forceChangeButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection()
                        || randomizeEnemiesButton.getSelection());
                if (hasMonsterOption) {
                    mixMonsterClasses.setEnabled(randomizePCButton.getSelection()
                            || randomizeBossesButton.getSelection() || randomizeEnemiesButton.getSelection());
                }
            }
        });

        //////////////////////////////////////////////////////////////////

        randomizeBossesButton = createButton(container, SWT.CHECK, "Randomize Bosses", null);
        layout(randomizeBossesButton, new FormAttachment(randomizeEnemiesButton, 10),
                new FormAttachment(randomizeEnemiesButton, 0, SWT.LEFT));
        randomizeBossesButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                baseTransferGroup.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                basesNoChangeButton
                        .setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                basesAdjustMatchButton
                        .setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                basesAdjustClassButton
                        .setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());

                forceChangeButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection()
                        || randomizeEnemiesButton.getSelection());
                strictGenderButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                looseGenderButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                noGenderButton.setEnabled(randomizePCButton.getSelection() || randomizeBossesButton.getSelection());
                if (hasMonsterOption) {
                    mixMonsterClasses.setEnabled(randomizePCButton.getSelection()
                            || randomizeBossesButton.getSelection() || randomizeEnemiesButton.getSelection());
                }
            }
        });

        //////////////////////////////////////////////////////////////////

        baseTransferGroup = createContainer(container, "Bases");
        setGroupMargins(baseTransferGroup);
        layout(baseTransferGroup, new FormAttachment(randomizeBossesButton, 10),
                new FormAttachment(randomizeBossesButton, 0, SWT.LEFT), new FormAttachment(100, -5));

        basesNoChangeButton = createButton(baseTransferGroup, SWT.RADIO, "Retain Personal Bases",
                "Does not adjust personal base stats. Characters stats will be altered based on their target class.");
        defaultLayout(basesNoChangeButton);

        basesAdjustMatchButton = createButton(baseTransferGroup, SWT.RADIO, "Retain Final Bases",
                "Adjusts personal bases so that characters will have the same base stats as before the class change.",
                false, true);
        layout(basesAdjustMatchButton, new FormAttachment(basesNoChangeButton, 5),
                new FormAttachment(basesNoChangeButton, 0, SWT.LEFT));

        basesAdjustClassButton = createButton(baseTransferGroup, SWT.RADIO, "Adjust to Class",
                "Adjusts personal bases so that characters will have their best base stats matching their class's best stats.");
        layout(basesAdjustClassButton, new FormAttachment(basesAdjustMatchButton, 5),
                new FormAttachment(basesAdjustMatchButton, 0, SWT.LEFT));

        Group genderGroup = createContainer(container, "Gender Restriction");
        setGroupMargins(genderGroup);
        layout(genderGroup, new FormAttachment(baseTransferGroup, 10), new FormAttachment(0, 5),
                new FormAttachment(100, -5));

        noGenderButton = createButton(genderGroup, SWT.RADIO, "No RestrictionNo Restriction",
                "No gender restrictions. Any character can become any class.", false, true);
        defaultLayout(noGenderButton);

        looseGenderButton = createButton(genderGroup, SWT.RADIO, "Loose Restrictions",
                "No gender restriction, but will use the correct gender version of a selected class, if it exists.");
        layout(looseGenderButton, new FormAttachment(noGenderButton, 5), new FormAttachment(0, 0));

        strictGenderButton = createButton(genderGroup, SWT.RADIO, "Strict Restrictions",
                "Class options are restricted to those that match the character's gender.");
        layout(strictGenderButton, new FormAttachment(looseGenderButton, 5), new FormAttachment(0, 0));

        forceChangeButton = createButton(container, SWT.CHECK, "Force Class Change",
                "Attempts to force every character to change to a different class.");
        layout(forceChangeButton, new FormAttachment(genderGroup, 10), new FormAttachment(genderGroup, 0, SWT.LEFT));

        if (type == GameType.FE8) {
            mixMonsterClasses = createButton(container, SWT.CHECK, "Mix Monster Classes",
                    "If enabled, allows cross-assignment of classes between humans and monsters.\nIf disabled, ensures that units that were monsters remain monsters and units that were human remain humans when randomizing classes.\nHas no effect unless another class randomization option is enabled.");
            layout(mixMonsterClasses, new FormAttachment(forceChangeButton, 5),
                    new FormAttachment(forceChangeButton, 0, SWT.LEFT));
            hasMonsterOption = true;
        } else {
            hasMonsterOption = false;
        }
    }

    private String getPrfToolTip(GameType type) {
        if (type == GameType.FE6) {
            return "If enabled, new weapons matching Roy's new class are created and replaces the starting Rapier (with identical stats and traits).\nThis weapon will be locked to Roy only. This also updates the Binding Blade to be a weapon type Roy can use.\n\nNote: Due to a lack of weapon locks, Rapiers will no longer be in the weapon pool.";
        } else if (type == GameType.FE7) {
            return "If enabled, new weapons matching Lyn/Eliwood/Hector's new classes are created and replace the starting Prf weapons (with identical stats and traits).\nNew weapons are locked to the characters specifically. This also updates Sol Katti/Durandal/Armads to be the correct weapon type for their respective lords.";
        } else if (type == GameType.FE8) {
            return "If enabled, new weapons matching Eirika/Ephraim's new classes are created and replace the starting Prf weapons (with identical stats and traits).\nNew weapons are locked specifically to their respective characters. This also updates Sieglinde/Siegmund to be the correct weapon type for their respective lords.";
        }
        return null;
    }

    public ClassOptions getClassOptions() {
        Boolean pcsEnabled = randomizePCButton.getSelection();
        boolean lordsEnabled = false;
        boolean thievesEnabled = false;
        boolean specialEnabled = false;
        if (pcsEnabled) {
            lordsEnabled = randomizePCLordsButton.getSelection();
            thievesEnabled = randomizePCThievesButton.getSelection();
            specialEnabled = randomizePCSpecialButton.getSelection();
        }

        boolean newPrfs = false;
        boolean unbreakablePrfs = false;
        if (lordsEnabled) {
            newPrfs = createNewPrfWeaponsButton.getSelection();
            if (newPrfs) {
                unbreakablePrfs = unbreakablePrfsButton.getSelection();
            }
        }

        BaseTransferOption baseOption = BaseTransferOption.ADJUST_TO_MATCH;
        if (basesNoChangeButton.getSelection()) {
            baseOption = BaseTransferOption.NO_CHANGE;
        } else if (basesAdjustClassButton.getSelection()) {
            baseOption = BaseTransferOption.ADJUST_TO_CLASS;
        }

        GenderRestrictionOption genderOption = GenderRestrictionOption.NONE;
        if (looseGenderButton.getSelection()) {
            genderOption = GenderRestrictionOption.LOOSE;
        } else if (strictGenderButton.getSelection()) {
            genderOption = GenderRestrictionOption.STRICT;
        }

        GrowthAdjustmentOption growthOption = GrowthAdjustmentOption.NO_CHANGE;
        if (personalGrowthButton.getSelection()) {
            growthOption = GrowthAdjustmentOption.TRANSFER_PERSONAL_GROWTHS;
        }
//		else if (classRelativeGrowthButton.getSelection()) { growthOption = GrowthAdjustmentOption.CLASS_RELATIVE_GROWTHS; }

        if (hasMonsterOption) {
            return new ClassOptions(pcsEnabled, lordsEnabled, newPrfs, unbreakablePrfs, thievesEnabled, specialEnabled,
                    !mixMonsterClasses.getSelection(), forceChangeButton.getSelection(), genderOption,
                    evenClassesButton.getSelection(), randomizeEnemiesButton.getSelection(),
                    randomizeBossesButton.getSelection(), baseOption, growthOption);
        } else {
            return new ClassOptions(pcsEnabled, lordsEnabled, newPrfs, unbreakablePrfs, thievesEnabled, specialEnabled,
                    forceChangeButton.getSelection(), genderOption, evenClassesButton.getSelection(),
                    randomizeEnemiesButton.getSelection(), randomizeBossesButton.getSelection(), baseOption,
                    growthOption);
        }
    }

    public void setClassOptions(ClassOptions options) {
        if (options == null) {
            return;
        }
        if (options.randomizePCs) {
            randomizePCButton.setSelection(true);
            randomizePCLordsButton.setEnabled(true);
            randomizePCThievesButton.setEnabled(true);
            randomizePCSpecialButton.setEnabled(true);
            evenClassesButton.setEnabled(true);

            randomizePCLordsButton.setSelection(options.includeLords != null ? options.includeLords : false);
            if (Boolean.TRUE.equals(options.includeLords)) {
                createNewPrfWeaponsButton.setEnabled(true);
                createNewPrfWeaponsButton.setSelection(options.createPrfs != null ? options.createPrfs : false);
                if (Boolean.TRUE.equals(options.createPrfs)) {
                    unbreakablePrfsButton.setEnabled(true);
                    unbreakablePrfsButton
                            .setSelection(options.unbreakablePrfs != null ? options.unbreakablePrfs : false);
                }
            }

            randomizePCThievesButton.setSelection(options.includeThieves != null ? options.includeThieves : false);
            randomizePCSpecialButton.setSelection(options.includeSpecial != null ? options.includeSpecial : false);
            evenClassesButton.setSelection(options.assignEvenly);

            growthAdjustmentGroup.setEnabled(true);
            growthNoAdjustmentButton.setEnabled(true);
            personalGrowthButton.setEnabled(true);
//				classRelativeGrowthButton.setEnabled(true);

            growthNoAdjustmentButton.setSelection(
                    options.growthOptions == null || options.growthOptions == GrowthAdjustmentOption.NO_CHANGE);
            personalGrowthButton
                    .setSelection(options.growthOptions == GrowthAdjustmentOption.TRANSFER_PERSONAL_GROWTHS);
//				classRelativeGrowthButton.setSelection(options.growthOptions == GrowthAdjustmentOption.CLASS_RELATIVE_GROWTHS);
        }

        randomizeEnemiesButton.setSelection(options.randomizeEnemies);
        randomizeBossesButton.setSelection(options.randomizeBosses);

        if (options.randomizePCs || options.randomizeBosses) {
            baseTransferGroup.setEnabled(true);
            basesNoChangeButton.setEnabled(true);
            basesAdjustMatchButton.setEnabled(true);
            basesAdjustClassButton.setEnabled(true);

            noGenderButton.setEnabled(true);
            looseGenderButton.setEnabled(true);
            strictGenderButton.setEnabled(true);

            basesNoChangeButton.setSelection(options.basesTransfer == BaseTransferOption.NO_CHANGE);
            basesAdjustMatchButton.setSelection(
                    options.basesTransfer == BaseTransferOption.ADJUST_TO_MATCH || options.basesTransfer == null);
            basesAdjustClassButton.setSelection(options.basesTransfer == BaseTransferOption.ADJUST_TO_CLASS);

            noGenderButton.setSelection(options.genderOption == GenderRestrictionOption.NONE);
            looseGenderButton.setSelection(options.genderOption == GenderRestrictionOption.LOOSE);
            strictGenderButton.setSelection(options.genderOption == GenderRestrictionOption.STRICT);
        } else {
            baseTransferGroup.setEnabled(false);
            basesNoChangeButton.setEnabled(false);
            basesAdjustMatchButton.setEnabled(false);
            basesAdjustClassButton.setEnabled(false);

            noGenderButton.setEnabled(false);
            looseGenderButton.setEnabled(false);
            strictGenderButton.setEnabled(false);
        }

        if (options.randomizePCs || options.randomizeEnemies || options.randomizeBosses) {
            forceChangeButton.setEnabled(true);
            forceChangeButton.setSelection(options.forceChange);

            if (hasMonsterOption) {
                mixMonsterClasses.setEnabled(true);
                mixMonsterClasses.setSelection(!options.separateMonsters);
            }
        } else {
            forceChangeButton.setEnabled(false);
            if (hasMonsterOption) {
                mixMonsterClasses.setEnabled(false);
            }
        }
    }
}
