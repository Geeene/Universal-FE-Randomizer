package ui;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import fedata.general.FEBase.GameType;
import ui.model.RecruitmentOptions;
import ui.model.RecruitmentOptions.BaseStatAutolevelType;
import ui.model.RecruitmentOptions.ClassMode;
import ui.model.RecruitmentOptions.GrowthAdjustmentMode;
import ui.model.RecruitmentOptions.StatAdjustmentMode;
import util.SeedGenerator;

public class RecruitmentView extends Composite {

	private Group container;

	private Button enableButton;

	private Group growthContainer;
	private Button fillGrowthButton;
	private Button slotGrowthButton;
	private Button slotRelativeGrowthButton;
	private Text recruitmentSeedField;
	private Label recruitmentSeedLabel;

	private Group basesContainer;
	private Button autolevelButton;
	private Composite autolevelTypeContainer;
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

		FillLayout layout = new FillLayout();
		setLayout(layout);

		container = new Group(this, SWT.NONE);
		container.setText("Recruitment");
		container.setToolTipText("Randomized character join order.");

		FormLayout mainLayout = new FormLayout();
		mainLayout.marginLeft = 5;
		mainLayout.marginTop = 5;
		mainLayout.marginBottom = 5;
		mainLayout.marginRight = 5;
		container.setLayout(mainLayout);

		enableButton = new Button(container, SWT.CHECK);
		enableButton.setText("Randomize Recruitment");
		enableButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				growthContainer.setEnabled(enableButton.getSelection());
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

		FormData enableData = new FormData();
		enableData.left = new FormAttachment(0, 0);
		enableData.top = new FormAttachment(0, 0);
		enableButton.setLayoutData(enableData);

		// CUSTOM, add logic to share Random Recruitment Order for different overall
		// Seeds

		FormData optionData = new FormData();
		optionData.left = new FormAttachment(container, 0, SWT.LEFT);
		optionData.top = new FormAttachment(enableButton, 10);

		recruitmentSeedLabel = new Label(container, SWT.NONE);
		recruitmentSeedLabel.setText("Recruitment Order Seed");
		recruitmentSeedLabel.setVisible(true);
		recruitmentSeedLabel.setLayoutData(optionData);

		optionData = new FormData();
		optionData.left = new FormAttachment(recruitmentSeedLabel, 0, SWT.LEFT);
		optionData.top = new FormAttachment(recruitmentSeedLabel, 10);

		recruitmentSeedField = new Text(container, SWT.BORDER);
		recruitmentSeedField.setText(SeedGenerator.generateRandomSeed());
		recruitmentSeedField.setToolTipText(
				"Set a seed for specifically the Recruitment Randomization. (Primarily useful for Sollinks)");
		recruitmentSeedField.setVisible(true);
		recruitmentSeedField.setLayoutData(optionData);

		///////////////////////////////////////////

		growthContainer = new Group(container, SWT.NONE);
		growthContainer.setText("Growths");
		growthContainer.setToolTipText("Determines how growths are assigned.");

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginLeft = 5;
		groupLayout.marginRight = 5;
		groupLayout.marginTop = 5;
		groupLayout.marginBottom = 5;
		growthContainer.setLayout(groupLayout);

		FormData groupData = new FormData();
		groupData.left = new FormAttachment(recruitmentSeedField, 10, SWT.LEFT);
		groupData.top = new FormAttachment(recruitmentSeedField, 10);
		groupData.right = new FormAttachment(100, -5);
		growthContainer.setLayoutData(groupData);

		fillGrowthButton = new Button(growthContainer, SWT.RADIO);
		fillGrowthButton.setText("Use Fill Growths");
		fillGrowthButton.setToolTipText("Characters use their natural growth rates.");
		fillGrowthButton.setEnabled(false);
		fillGrowthButton.setSelection(true);

		optionData = new FormData();
		optionData.left = new FormAttachment(0, 0);
		optionData.top = new FormAttachment(0, 0);
		optionData.right = new FormAttachment(100, -5);
		fillGrowthButton.setLayoutData(optionData);

		slotGrowthButton = new Button(growthContainer, SWT.RADIO);
		slotGrowthButton.setText("Use Slot Growths");
		slotGrowthButton.setToolTipText("Characters use the growth rates of the character they replace.");
		slotGrowthButton.setEnabled(false);
		slotGrowthButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(fillGrowthButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(fillGrowthButton, 5);
		slotGrowthButton.setLayoutData(optionData);

		slotRelativeGrowthButton = new Button(growthContainer, SWT.RADIO);
		slotRelativeGrowthButton.setText("Slot Relative Growths");
		slotRelativeGrowthButton.setToolTipText(
				"Characters use the growth values of the character they replace,\nbut retain their own growth strengths and weaknesses.");
		slotRelativeGrowthButton.setEnabled(false);
		slotRelativeGrowthButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(slotGrowthButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(slotGrowthButton, 5);
		slotRelativeGrowthButton.setLayoutData(optionData);

		/////////////////////////////////////////////////

		basesContainer = new Group(container, SWT.NONE);
		basesContainer.setText("Bases");
		basesContainer.setToolTipText("Determines how bases are transferred.");

		groupLayout = new FormLayout();
		groupLayout.marginLeft = 5;
		groupLayout.marginRight = 5;
		groupLayout.marginTop = 5;
		groupLayout.marginBottom = 5;
		basesContainer.setLayout(groupLayout);

		groupData = new FormData();
		groupData.left = new FormAttachment(growthContainer, 0, SWT.LEFT);
		groupData.top = new FormAttachment(growthContainer, 10);
		groupData.right = new FormAttachment(100, -5);
		basesContainer.setLayoutData(groupData);

		autolevelButton = new Button(basesContainer, SWT.RADIO);
		autolevelButton.setText("Autolevel Base Stats");
		autolevelButton.setToolTipText(
				"Uses the character's growth rates to simulate leveling up or down from the character's original stats to their target level.");
		autolevelButton.setEnabled(false);
		autolevelButton.setSelection(true);
		autolevelButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				autolevelTypeContainer.setEnabled(autolevelButton.getSelection() && enableButton.getSelection());
				autolevelOriginalButton.setEnabled(autolevelButton.getSelection() && enableButton.getSelection());
				autolevelNewButton.setEnabled(autolevelButton.getSelection() && enableButton.getSelection());
			}
		});

		optionData = new FormData();
		optionData.left = new FormAttachment(0, 0);
		optionData.top = new FormAttachment(0, 0);
		autolevelButton.setLayoutData(optionData);

		autolevelTypeContainer = new Composite(basesContainer, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		fillLayout.spacing = 5;
		autolevelTypeContainer.setLayout(fillLayout);

		autolevelOriginalButton = new Button(autolevelTypeContainer, SWT.RADIO);
		autolevelOriginalButton.setText("Use Original Growths");
		autolevelOriginalButton.setToolTipText("Uses the character's natural growth rates to autolevel.");
		autolevelOriginalButton.setEnabled(false);
		autolevelOriginalButton.setSelection(true);

		autolevelNewButton = new Button(autolevelTypeContainer, SWT.RADIO);
		autolevelNewButton.setText("Use New Growths");
		autolevelNewButton.setToolTipText("Uses the character's newly assigned growth from above to autolevel.");
		autolevelNewButton.setEnabled(false);
		autolevelNewButton.setSelection(false);

		FormData autolevelContainerData = new FormData();
		autolevelContainerData.left = new FormAttachment(autolevelButton, 10, SWT.LEFT);
		autolevelContainerData.top = new FormAttachment(autolevelButton, 5);
		autolevelContainerData.right = new FormAttachment(100, -5);
		autolevelTypeContainer.setLayoutData(autolevelContainerData);

		absoluteButton = new Button(basesContainer, SWT.RADIO);
		absoluteButton.setText("Match Base Stats");
		absoluteButton.setToolTipText("Sets a character's base stats to match the character they replace.");
		absoluteButton.setEnabled(false);
		absoluteButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(autolevelButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(autolevelTypeContainer, 5);
		absoluteButton.setLayoutData(optionData);

		relativeButton = new Button(basesContainer, SWT.RADIO);
		relativeButton.setText("Relative Base Stats");
		relativeButton.setToolTipText(
				"Pins the character's max stat to the max stat of the character they replace and retains the character's stat spread.");
		relativeButton.setEnabled(false);
		relativeButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(absoluteButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(absoluteButton, 5);
		relativeButton.setLayoutData(optionData);

		classContainer = new Group(container, SWT.NONE);
		classContainer.setText("Classes");
		classContainer.setToolTipText("Determines how classes are assigned.");

		groupLayout = new FormLayout();
		groupLayout.marginLeft = 5;
		groupLayout.marginRight = 5;
		groupLayout.marginTop = 5;
		groupLayout.marginBottom = 5;
		classContainer.setLayout(groupLayout);

		groupData = new FormData();
		groupData.left = new FormAttachment(basesContainer, 0, SWT.LEFT);
		groupData.top = new FormAttachment(basesContainer, 10);
		groupData.right = new FormAttachment(100, -5);
		classContainer.setLayoutData(groupData);

		fillClassButton = new Button(classContainer, SWT.RADIO);
		fillClassButton.setText("Use Fill Class");
		switch (type) {
		case FE6:
			fillClassButton.setToolTipText(
					"Characters retain their original class (after necessary promotion/demotion).\n\nFor example, Percival taking the place of Wolt will be a cavalier.");
			break;
		case FE7:
			fillClassButton.setToolTipText(
					"Characters retain their original class (after necessary promotion/demotion).\n\nFor example, Louise taking the place of Serra will be an archer.");
			break;
		case FE8:
			fillClassButton.setToolTipText(
					"Characters retain their original class (after necessary promotion/demotion).\n\nFor example, Duessel taking the place of Garcia will be either a Cavalier or an Armor Knight (due to branched promotion).");
			break;
		default:
			break;
		}
		fillClassButton.setEnabled(false);
		fillClassButton.setSelection(true);

		optionData = new FormData();
		optionData.left = new FormAttachment(0, 0);
		optionData.top = new FormAttachment(0, 0);
		fillClassButton.setLayoutData(optionData);

		slotClassButton = new Button(classContainer, SWT.RADIO);
		slotClassButton.setText("Use Slot Class");
		switch (type) {
		case FE6:
			slotClassButton.setToolTipText(
					"Characters take the class of the slot they fill.\n\nFor example, Percival taking the place of Wolt will be an archer.");
			break;
		case FE7:
			slotClassButton.setToolTipText(
					"Characters take the class of the slot they fill.\n\nFor example, Louise taking the place of Serra will be a cleric.");
			break;
		case FE8:
			slotClassButton.setToolTipText(
					"Characters take the class of the slot they fill.\n\nFor example, Duessel taking the place of Garcia will be a fighter.");
			break;
		default:
			break;
		}
		slotClassButton.setEnabled(false);
		slotClassButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(fillClassButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(fillClassButton, 5);
		slotClassButton.setLayoutData(optionData);

		lordsButton = new Button(container, SWT.CHECK);
		lordsButton.setText("Include Lords");
		lordsButton.setToolTipText("Allows Lord characters to randomize their recruitment time.");
		lordsButton.setEnabled(false);
		lordsButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(classContainer, 0, SWT.LEFT);
		optionData.top = new FormAttachment(classContainer, 10);
		lordsButton.setLayoutData(optionData);

		createPrfsButton = new Button(container, SWT.CHECK);
		createPrfsButton.setText("Create Matching Prf Weapons");
		createPrfsButton
				.setToolTipText("If enabled, creates Prf weapons for characters that randomize into the lord slots.");
		createPrfsButton.setEnabled(false);
		createPrfsButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(lordsButton, 10, SWT.LEFT);
		optionData.top = new FormAttachment(lordsButton, 5);
		createPrfsButton.setLayoutData(optionData);

		unbreakablePrfsButton = new Button(container, SWT.CHECK);
		unbreakablePrfsButton.setText("Make Prf Weapons Unbreakable");
		unbreakablePrfsButton.setToolTipText("If enabled, created Prf weapons are unbreakable.");
		unbreakablePrfsButton.setEnabled(false);
		unbreakablePrfsButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(createPrfsButton, 10, SWT.LEFT);
		optionData.top = new FormAttachment(createPrfsButton, 5);
		unbreakablePrfsButton.setLayoutData(optionData);

		lordsButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				createPrfsButton.setEnabled(lordsButton.getSelection());
				unbreakablePrfsButton.setEnabled(lordsButton.getSelection() && createPrfsButton.getSelection());
			}
		});

		createPrfsButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				unbreakablePrfsButton.setEnabled(lordsButton.getSelection() && createPrfsButton.getSelection());
			}
		});

		thievesButton = new Button(container, SWT.CHECK);
		thievesButton.setText("Include Thieves");
		thievesButton.setToolTipText("Allows Thief characters to randomize their recruitment time.");
		thievesButton.setEnabled(false);
		thievesButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(lordsButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(unbreakablePrfsButton, 10);
		thievesButton.setLayoutData(optionData);

		specialButton = new Button(container, SWT.CHECK);
		specialButton.setText("Include Special Characters");
		specialButton.setToolTipText("Allows Dancers, Bards, and Manaketes to randomize their recruitment time.");
		specialButton.setEnabled(false);
		specialButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(thievesButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(thievesButton, 5);
		specialButton.setLayoutData(optionData);

		crossGenderButton = new Button(container, SWT.CHECK);
		crossGenderButton.setText("Allow Cross-gender Assignments");
		crossGenderButton.setToolTipText("Allows males to be assigned to female slots and vice versa.");
		crossGenderButton.setEnabled(false);
		crossGenderButton.setSelection(false);

		optionData = new FormData();
		optionData.left = new FormAttachment(specialButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(specialButton, 5);
		crossGenderButton.setLayoutData(optionData);

		if (Arrays.asList(GameType.FE8, GameType.FE6).contains(type)) {
			// Option to include Creature Campaign
			includeExtras = new Button(container, SWT.CHECK);
			includeExtras.setText("Include Creature Campaign NPCs");
			String units = type.equals(GameType.FE6) ? "Hector, Brunnya, Eliwood, and Guinievere"
					: "Glen, Fado, Hayden, and Ismaire";

			includeExtras.setToolTipText(
					String.format("Includes NPCs from the creature campaign into the pool.\nSpecifically: %s.", units));
			includeExtras.setEnabled(false);
			includeExtras.setSelection(false);

			optionData = new FormData();
			optionData.left = new FormAttachment(crossGenderButton, 0, SWT.LEFT);
			optionData.top = new FormAttachment(crossGenderButton, 5);
			includeExtras.setLayoutData(optionData);
		}
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
			return new RecruitmentOptions(growthMode, basesMode, autolevel, classMode, lordsButton.getSelection(),
					createPrfsButton.getSelection(), unbreakablePrfsButton.getSelection(), thievesButton.getSelection(),
					specialButton.getSelection(), crossGenderButton.getSelection(), extras,
					recruitmentSeedField.getText());
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
