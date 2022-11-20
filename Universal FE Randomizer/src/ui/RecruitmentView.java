package ui;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import fedata.general.FEBase.GameType;
import ui.model.RecruitmentOptions;
import ui.model.RecruitmentOptions.BaseStatAutolevelType;
import ui.model.RecruitmentOptions.ClassMode;
import ui.model.RecruitmentOptions.GrowthAdjustmentMode;
import ui.model.RecruitmentOptions.StatAdjustmentMode;
import util.SeedGenerator;

public class RecruitmentView extends AbstractYuneView {

	private Composite container;

	private Button enableButton;

	private Group seedContainer;
	private Button seedEnableButton;
	private Group growthContainer;
	private Button fillGrowthButton;
	private Button slotGrowthButton;
	private Button slotRelativeGrowthButton;
	private Button recSeedGenerate;
	private Text recSeedField;

	private Group basesContainer;
	private Button autolevelButton;
	private Group autolevelTypeContainer;
	private Button autolevelOriginalButton;
	private Button autolevelNewButton;
	private Button absoluteButton;
	private Button relativeButton;

	private Group classContainer;
	private Button fillClassButton;
	private Button slotClassButton;

	private Button lordsButton;
	private Button createPrfsButton;
	private Button unbreakablePrfsButton;
	private Button thievesButton;
	private Button specialButton;

	private Button crossGenderButton;
	private Button includeExtras;

	public RecruitmentView(Composite parent, int style, GameType type) {
		super(parent, style);
		setLayout(new FillLayout());

		container = createContainer(this, "Recruitment", "Randomized character join order.");
		setGroupMargins(container);

		enableButton = createButton(container, SWT.CHECK, "Randomize Recruitment", null, true, false);
		defaultLayout(enableButton);
		enableButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				growthContainer.setEnabled(enableButton.getSelection());
				seedContainer.setEnabled(enableButton.getSelection());
				basesContainer.setEnabled(enableButton.getSelection());
				classContainer.setEnabled(enableButton.getSelection());

				fillGrowthButton.setEnabled(enableButton.getSelection());
				slotGrowthButton.setEnabled(enableButton.getSelection());
				slotRelativeGrowthButton.setEnabled(enableButton.getSelection());

				autolevelButton.setEnabled(enableButton.getSelection());
				absoluteButton.setEnabled(enableButton.getSelection());
				relativeButton.setEnabled(enableButton.getSelection());

				crossGenderButton.setEnabled(enableButton.getSelection());
				lordsButton.setEnabled(enableButton.getSelection());
				thievesButton.setEnabled(enableButton.getSelection());
				specialButton.setEnabled(enableButton.getSelection());

				createPrfsButton.setEnabled(enableButton.getSelection() && lordsButton.getSelection());
				unbreakablePrfsButton.setEnabled(
						enableButton.getSelection() && lordsButton.getSelection() && createPrfsButton.getSelection());

				autolevelTypeContainer.setEnabled(enableButton.getSelection() && autolevelButton.getSelection());
				autolevelOriginalButton.setEnabled(enableButton.getSelection() && autolevelButton.getSelection());
				autolevelNewButton.setEnabled(enableButton.getSelection() && autolevelButton.getSelection());

				fillClassButton.setEnabled(enableButton.getSelection());
				slotClassButton.setEnabled(enableButton.getSelection());

				if (includeExtras != null) {
					includeExtras.setEnabled(enableButton.getSelection());
				}
			}
		});

		// CUSTOM, add logic to allow setting a different seed for recruitment
		// Randomization

		seedContainer = createContainer(container, "Seperate Seed",
				"Determines if the recruitment randomization uses a different seed than the other settings.");
		setGroupMargins(seedContainer);
		layout(seedContainer, new FormAttachment(container, 15), new FormAttachment(container, 10),
				new FormAttachment(100, -5));

		seedEnableButton = createButton(seedContainer, SWT.CHECK, "Use Seperate seed", "", true, false);
		defaultLayout(seedEnableButton);
		seedEnableButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (seedEnableButton.getSelection()) {
					recSeedField.setText(SeedGenerator.generateRandomSeed());
					recSeedField.setEnabled(true);
					recSeedGenerate.setEnabled(true);
				} else {
					recSeedField.setText("");
					recSeedField.setEnabled(false);
					recSeedGenerate.setEnabled(false);
				}
			}
		});

		recSeedField = createText(seedContainer, SWT.BORDER, "",
				"Set a seed for specifically the Recruitment Randomization. (Primarily useful for Sollinks)", true);
		layout(recSeedField, new FormAttachment(seedEnableButton, 5), new FormAttachment(seedContainer, 0));

		recSeedGenerate = createButton(seedContainer, SWT.PUSH, "Generate",
				"Set a seed for specifically the Recruitment Randomization. (Primarily useful for Sollinks)");
		layout(recSeedGenerate, new FormAttachment(seedEnableButton, 5), new FormAttachment(recSeedField, 5));

		recSeedGenerate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				recSeedField.setText(SeedGenerator.generateRandomSeed());
			}
		});
		///////////////////////////////////////////

		growthContainer = createContainer(container, "Growths", "Determines how growths are assigned.");
		setGroupMargins(growthContainer);
		layout(growthContainer, new FormAttachment(seedContainer, 10), new FormAttachment(recSeedField, 10, SWT.LEFT),
				new FormAttachment(100, -5));

		fillGrowthButton = createButton(growthContainer, SWT.RADIO, "Use Fill Growths",
				"Characters use their natural growth rates.", false, true);
		layout(fillGrowthButton, new FormAttachment(0, 0), new FormAttachment(0, 0), new FormAttachment(100, -5));

		slotGrowthButton = createButton(growthContainer, SWT.RADIO, "Use Slot Growths",
				"Characters use the growth rates of the character they replace.");
		layout(slotGrowthButton, new FormAttachment(fillGrowthButton, 5),
				new FormAttachment(fillGrowthButton, 0, SWT.LEFT));

		slotRelativeGrowthButton = createButton(growthContainer, SWT.RADIO, "Slot Relative Growths",
				"Characters use the growth values of the character they replace,\nbut retain their own growth strengths and weaknesses.",
				false, false);
		layout(slotRelativeGrowthButton, new FormAttachment(slotGrowthButton, 5),
				new FormAttachment(slotGrowthButton, 0, SWT.LEFT));

		/////////////////////////////////////////////////

		basesContainer = createContainer(container, "Bases", "Determines how bases are transferred.");
		setGroupMargins(basesContainer);
		layout(basesContainer, new FormAttachment(growthContainer, 5), new FormAttachment(growthContainer, 0, SWT.LEFT),
				new FormAttachment(100, -5));

		autolevelButton = createButton(basesContainer, SWT.RADIO, "Autolevel Base Stats",
				"Uses the character's growth rates to simulate leveling up or down from the character's original stats to their target level.",
				false, true);
		defaultLayout(autolevelButton);

		autolevelButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				autolevelTypeContainer.setEnabled(autolevelButton.getSelection() && enableButton.getSelection());
				autolevelOriginalButton.setEnabled(autolevelButton.getSelection() && enableButton.getSelection());
				autolevelNewButton.setEnabled(autolevelButton.getSelection() && enableButton.getSelection());
			}
		});

		autolevelTypeContainer = createContainer(basesContainer);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		fillLayout.spacing = 5;
		autolevelTypeContainer.setLayout(fillLayout);
		layout(autolevelTypeContainer, new FormAttachment(autolevelButton, 5),
				new FormAttachment(autolevelButton, 10, SWT.LEFT), new FormAttachment(100, -5));

		autolevelOriginalButton = createButton(autolevelTypeContainer, SWT.RADIO, "Use Original Growths",
				"Uses the character's natural growth rates to autolevel.", false, true);

		autolevelNewButton = createButton(autolevelTypeContainer, SWT.RADIO, "Use New Growths",
				"Uses the character's newly assigned growth from above to autolevel.");

		absoluteButton = createButton(basesContainer, SWT.RADIO, "Match Base Stats",
				"Sets a character's base stats to match the character they replace.");
		layout(absoluteButton, new FormAttachment(autolevelTypeContainer, 5),
				new FormAttachment(autolevelButton, 0, SWT.LEFT));

		relativeButton = createButton(basesContainer, SWT.RADIO, "Relative Base Stats",
				"Pins the character's max stat to the max stat of the character they replace and retains the character's stat spread.",
				false, false);
		layout(relativeButton, new FormAttachment(absoluteButton, 5), new FormAttachment(absoluteButton, 0, SWT.LEFT));

		classContainer = createContainer(container, "Classes", "Determines how classes are assigned.");
		setGroupMargins(classContainer);
		layout(classContainer, new FormAttachment(basesContainer, 5), new FormAttachment(basesContainer, 0, SWT.LEFT),
				new FormAttachment(100, -5));

		fillClassButton = createButton(classContainer, SWT.RADIO, "Use Fill Class", getFillClassTooltip(type), false,
				true);
		defaultLayout(fillClassButton);

		slotClassButton = createButton(classContainer, SWT.RADIO, "Use Slot Class", getSlotClassTooltip(type));
		layout(slotClassButton, new FormAttachment(fillClassButton, 5),
				new FormAttachment(fillClassButton, 0, SWT.LEFT));

		lordsButton = createButton(container, SWT.CHECK, "Include Lords",
				"Allows Lord characters to randomize their recruitment time.");
		layout(lordsButton, new FormAttachment(classContainer, 5), new FormAttachment(classContainer, 0, SWT.LEFT));
		lordsButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				createPrfsButton.setEnabled(lordsButton.getSelection());
				unbreakablePrfsButton.setEnabled(lordsButton.getSelection() && createPrfsButton.getSelection());
			}
		});

		createPrfsButton = createButton(container, SWT.CHECK, "Create Matching Prf Weapons",
				"If enabled, creates Prf weapons for characters that randomize into the lord slots.");
		layout(createPrfsButton, new FormAttachment(lordsButton, 5), new FormAttachment(lordsButton, 10, SWT.LEFT));
		createPrfsButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				unbreakablePrfsButton.setEnabled(lordsButton.getSelection() && createPrfsButton.getSelection());
			}
		});

		unbreakablePrfsButton = createButton(container, SWT.CHECK, "Make Prf Weapons Unbreakable",
				"If enabled, created Prf weapons are unbreakable.");
		layout(unbreakablePrfsButton, new FormAttachment(createPrfsButton, 5),
				new FormAttachment(createPrfsButton, 10, SWT.LEFT));

		thievesButton = createButton(container, SWT.CHECK, "Include Thieves",
				"Allows Thief characters to randomize their recruitment time.");
		layout(thievesButton, new FormAttachment(unbreakablePrfsButton, 10),
				new FormAttachment(classContainer, 0, SWT.LEFT));

		specialButton = createButton(container, SWT.CHECK, "Include Special Characters",
				"Allows Dancers, Bards, and Manaketes to randomize their recruitment time.");
		layout(specialButton, new FormAttachment(thievesButton, 5), new FormAttachment(classContainer, 0, SWT.LEFT));

		crossGenderButton = createButton(container, SWT.CHECK, "Allow Cross-gender Assignments",
				"Allows males to be assigned to female slots and vice versa.");
		layout(crossGenderButton, new FormAttachment(specialButton, 5),
				new FormAttachment(classContainer, 0, SWT.LEFT));

		if (Arrays.asList(GameType.FE8, GameType.FE6).contains(type)) {
			includeExtras = createButton(container, SWT.CHECK, "Include Creature Campaign NPCs",
					getExtraCharacters(type));
			layout(includeExtras, new FormAttachment(crossGenderButton, 5),
					new FormAttachment(classContainer, 0, SWT.LEFT));
		}
	}

	private String getExtraCharacters(GameType type) {
		String units = type.equals(GameType.FE6) ? "Hector, Brunnya, Eliwood, and Guinievere"
				: "Glen, Fado, Hayden, and Ismaire";
		return String.format("Includes NPCs from the creature campaign into the pool.\nSpecifically: %s.", units);
	}

	private String getSlotClassTooltip(GameType type) {
		if (type.equals(GameType.FE6)) {
			return "Characters take the class of the slot they fill.\n\nFor example, Percival taking the place of Wolt will be an archer.";
		} else if (type.equals(GameType.FE7)) {
			return "Characters take the class of the slot they fill.\n\nFor example, Louise taking the place of Serra will be a cleric.";
		} else if (type.equals(GameType.FE8)) {
			return "Characters take the class of the slot they fill.\n\nFor example, Duessel taking the place of Garcia will be a fighter.";
		}
		return null;
	}

	private String getFillClassTooltip(GameType type) {
		if (type.equals(GameType.FE6)) {
			return "Characters retain their original class (after necessary promotion/demotion).\n\nFor example, Percival taking the place of Wolt will be a cavalier.";
		} else if (type.equals(GameType.FE7)) {
			return "Characters retain their original class (after necessary promotion/demotion).\n\nFor example, Louise taking the place of Serra will be an archer.";
		} else if (type.equals(GameType.FE8)) {
			return "Characters retain their original class (after necessary promotion/demotion).\n\nFor example, Duessel taking the place of Garcia will be either a Cavalier or an Armor Knight (due to branched promotion).";
		}
		return null;
	}

	public RecruitmentOptions getRecruitmentOptions() {
		boolean isEnabled = enableButton.getSelection();
		StatAdjustmentMode basesMode = null;
		BaseStatAutolevelType autolevel = null;
		if (autolevelButton.getSelection()) {
			basesMode = StatAdjustmentMode.AUTOLEVEL;
			if (autolevelOriginalButton.getSelection()) {
				autolevel = BaseStatAutolevelType.USE_ORIGINAL;
			} else if (autolevelNewButton.getSelection()) {
				autolevel = BaseStatAutolevelType.USE_NEW;
			}
		} else if (absoluteButton.getSelection()) {
			basesMode = StatAdjustmentMode.MATCH_SLOT;
		} else if (relativeButton.getSelection()) {
			basesMode = StatAdjustmentMode.RELATIVE_TO_SLOT;
		}

		GrowthAdjustmentMode growthMode = null;
		if (fillGrowthButton.getSelection()) {
			growthMode = GrowthAdjustmentMode.USE_FILL;
		} else if (slotGrowthButton.getSelection()) {
			growthMode = GrowthAdjustmentMode.USE_SLOT;
		} else if (slotRelativeGrowthButton.getSelection()) {
			growthMode = GrowthAdjustmentMode.RELATIVE_TO_SLOT;
		}

		boolean extras = includeExtras != null ? includeExtras.getSelection() : false;

		ClassMode classMode = ClassMode.USE_FILL;
		if (slotClassButton.getSelection()) {
			classMode = ClassMode.USE_SLOT;
		}

		if (isEnabled && basesMode != null && growthMode != null) {
			return new RecruitmentOptions(growthMode, basesMode, autolevel, classMode,
					lordsButton.getSelection(), createPrfsButton.getSelection(), unbreakablePrfsButton.getSelection(),
					thievesButton.getSelection(), specialButton.getSelection(), crossGenderButton.getSelection(),
					extras, seedEnableButton.getSelection(), recSeedField.getText());
		} else {
			return null;
		}
	}

	public void setRecruitmentOptions(RecruitmentOptions options) {
		if (options == null) {
			enableButton.setSelection(false);

			growthContainer.setEnabled(false);
			basesContainer.setEnabled(false);
			classContainer.setEnabled(false);
			seedContainer.setEnabled(false);

			fillGrowthButton.setEnabled(false);
			slotGrowthButton.setEnabled(false);
			slotRelativeGrowthButton.setEnabled(false);

			autolevelButton.setEnabled(false);
			autolevelTypeContainer.setEnabled(false);
			autolevelOriginalButton.setEnabled(false);
			autolevelNewButton.setEnabled(false);

			absoluteButton.setEnabled(false);
			relativeButton.setEnabled(false);

			fillClassButton.setEnabled(false);
			slotClassButton.setEnabled(false);

			lordsButton.setEnabled(false);
			createPrfsButton.setEnabled(false);
			unbreakablePrfsButton.setEnabled(false);
			thievesButton.setEnabled(false);
			specialButton.setEnabled(false);
			crossGenderButton.setEnabled(false);
			if (includeExtras != null) {
				includeExtras.setEnabled(false);
			}
		} else {
			enableButton.setSelection(true);

			growthContainer.setEnabled(true);
			basesContainer.setEnabled(true);
			classContainer.setEnabled(true);
			seedContainer.setEnabled(true);
			seedEnableButton.setEnabled(true);

			fillGrowthButton.setEnabled(true);
			slotGrowthButton.setEnabled(true);
			slotRelativeGrowthButton.setEnabled(true);

			autolevelButton.setEnabled(true);
			absoluteButton.setEnabled(true);
			relativeButton.setEnabled(true);

			fillClassButton.setEnabled(true);
			slotClassButton.setEnabled(true);

			lordsButton.setEnabled(true);
			createPrfsButton.setEnabled(options.includeLords);
			unbreakablePrfsButton.setEnabled(options.includeLords && options.createPrfs);
			thievesButton.setEnabled(true);
			specialButton.setEnabled(true);
			crossGenderButton.setEnabled(true);

			fillGrowthButton
					.setSelection(options.growthMode == GrowthAdjustmentMode.USE_FILL || options.growthMode == null);
			slotGrowthButton.setSelection(options.growthMode == GrowthAdjustmentMode.USE_SLOT);
			slotRelativeGrowthButton.setSelection(options.growthMode == GrowthAdjustmentMode.RELATIVE_TO_SLOT);

			autolevelButton.setSelection(options.baseMode == StatAdjustmentMode.AUTOLEVEL || options.baseMode == null);
			absoluteButton.setSelection(options.baseMode == StatAdjustmentMode.MATCH_SLOT);
			relativeButton.setSelection(options.baseMode == StatAdjustmentMode.RELATIVE_TO_SLOT);

			autolevelOriginalButton.setSelection(
					options.autolevelMode == BaseStatAutolevelType.USE_ORIGINAL || options.autolevelMode == null);
			autolevelNewButton.setSelection(options.autolevelMode == BaseStatAutolevelType.USE_NEW);

			fillClassButton.setSelection(options.classMode == ClassMode.USE_FILL || options.classMode == null);
			slotClassButton.setSelection(options.classMode == ClassMode.USE_SLOT);

			autolevelOriginalButton.setEnabled(autolevelButton.getSelection());
			autolevelNewButton.setEnabled(autolevelButton.getSelection());

			lordsButton.setSelection(options.includeLords);
			createPrfsButton.setSelection(options.createPrfs);
			unbreakablePrfsButton.setSelection(options.unbreakablePrfs);
			thievesButton.setSelection(options.includeThieves);
			specialButton.setSelection(options.includeSpecial);
			crossGenderButton.setSelection(options.allowCrossGender);

			if (includeExtras != null) {
				includeExtras.setEnabled(true);
				includeExtras.setSelection(options.includeExtras);
			}
		}
	}
}
