package ui.views;

import fedata.general.FEBase.GameType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.*;
import ui.common.GuiUtil;
import ui.model.AutolevelingParameters;
import ui.model.AutolevelingParameters.BaseStatAutolevelType;
import ui.model.AutolevelingParameters.GrowthAdjustmentMode;
import ui.model.AutolevelingParameters.StatAdjustmentMode;
import ui.model.RecruitmentOptions;
import ui.model.RecruitmentOptions.ClassMode;

public class AutoLevelingParametersView extends YuneView<AutolevelingParameters> {


	private Group growthContainer;
	private Button fillGrowthButton;
	private Button slotGrowthButton;
	private Button slotRelativeGrowthButton;

	private Group basesContainer;
	private Button autolevelButton;
	private Composite autolevelTypeContainer;
	private Button autolevelOriginalButton;
	private Button autolevelNewButton;
	private Button absoluteButton;
	private Button relativeButton;

	public AutoLevelingParametersView(Composite parent, GameType type) {
		super(parent, type);
	}

	@Override
	public String getGroupTitle() {
		return "Recruitment";
	}

	@Override
	public String getGroupTooltip() {
		return "Randomized character join order.";
	}

	@Override
	protected void compose() {
		growthContainer = new Group(group, SWT.NONE);
		growthContainer.setText("Growths");
		growthContainer.setToolTipText("Determines how growths are assigned.");
		growthContainer.setLayout(GuiUtil.formLayoutWithMargin());
		
		FormData groupData = new FormData();
		groupData.right = new FormAttachment(100, -5);
		growthContainer.setLayoutData(groupData);
		
		fillGrowthButton = new Button(growthContainer, SWT.RADIO);
		fillGrowthButton.setText("Use Fill Growths");
		fillGrowthButton.setToolTipText("Characters use their natural growth rates.");
		fillGrowthButton.setEnabled(false);
		fillGrowthButton.setSelection(true);
		
		FormData optionData = new FormData();
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
		slotRelativeGrowthButton.setToolTipText("Characters use the growth values of the character they replace,\nbut retain their own growth strengths and weaknesses.");
		slotRelativeGrowthButton.setEnabled(false);
		slotRelativeGrowthButton.setSelection(false);
		
		optionData = new FormData();
		optionData.left = new FormAttachment(slotGrowthButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(slotGrowthButton, 5);
		slotRelativeGrowthButton.setLayoutData(optionData);
		
		/////////////////////////////////////////////////
		
		basesContainer = new Group(group, SWT.NONE);
		basesContainer.setText("Bases");
		basesContainer.setToolTipText("Determines how bases are transferred.");
		basesContainer.setLayout(GuiUtil.formLayoutWithMargin());
		
		groupData = new FormData();
		groupData.left = new FormAttachment(growthContainer, 0, SWT.LEFT);
		groupData.top = new FormAttachment(growthContainer, 10);
		groupData.right = new FormAttachment(100, -5);
		basesContainer.setLayoutData(groupData);
		
		autolevelButton = new Button(basesContainer, SWT.RADIO);
		autolevelButton.setText("Autolevel Base Stats");
		autolevelButton.setToolTipText("Uses the character's growth rates to simulate leveling up or down from the character's original stats to their target level.");
		autolevelButton.setEnabled(false);
		autolevelButton.setSelection(true);
		autolevelButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				autolevelTypeContainer.setEnabled(autolevelButton.getSelection());
				autolevelOriginalButton.setEnabled(autolevelButton.getSelection());
				autolevelNewButton.setEnabled(autolevelButton.getSelection());
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
		relativeButton.setToolTipText("Pins the character's max stat to the max stat of the character they replace and retains the character's stat spread.");
		relativeButton.setEnabled(false);
		relativeButton.setSelection(false);
		
		optionData = new FormData();
		optionData.left = new FormAttachment(absoluteButton, 0, SWT.LEFT);
		optionData.top = new FormAttachment(absoluteButton, 5);
		relativeButton.setLayoutData(optionData);
		
	}

	@Override
	public AutolevelingParameters getOptions() {
		StatAdjustmentMode basesMode = null;
		BaseStatAutolevelType autolevel = null;
		if (autolevelButton.getSelection()) {
			basesMode = StatAdjustmentMode.AUTOLEVEL;
			if (autolevelOriginalButton.getSelection()) { autolevel = BaseStatAutolevelType.USE_ORIGINAL; }
			else if (autolevelNewButton.getSelection()) { autolevel = BaseStatAutolevelType.USE_NEW; }
		}
		else if (absoluteButton.getSelection()) { basesMode = StatAdjustmentMode.MATCH_SLOT; }
		else if (relativeButton.getSelection()) { basesMode = StatAdjustmentMode.RELATIVE_TO_SLOT; }

		GrowthAdjustmentMode growthMode = null;
		if (fillGrowthButton.getSelection()) { growthMode = GrowthAdjustmentMode.USE_FILL; }
		else if (slotGrowthButton.getSelection()) { growthMode = GrowthAdjustmentMode.USE_SLOT; }
		else if (slotRelativeGrowthButton.getSelection()) { growthMode = GrowthAdjustmentMode.RELATIVE_TO_SLOT; }
		
		if (basesMode != null && growthMode != null) {
			return new AutolevelingParameters(growthMode, basesMode, autolevel);
		} else {
			return null;
		}
	}

	@Override
	public void initialize(AutolevelingParameters options) {
		boolean optionsAvailable = options != null;
		growthContainer.setEnabled(optionsAvailable);
		basesContainer.setEnabled(optionsAvailable);

		fillGrowthButton.setEnabled(optionsAvailable);
		slotGrowthButton.setEnabled(optionsAvailable);
		slotRelativeGrowthButton.setEnabled(optionsAvailable);

		autolevelButton.setEnabled(optionsAvailable);
		autolevelTypeContainer.setEnabled(optionsAvailable);
		autolevelOriginalButton.setEnabled(optionsAvailable);
		autolevelNewButton.setEnabled(optionsAvailable);

		absoluteButton.setEnabled(optionsAvailable);
		relativeButton.setEnabled(optionsAvailable);


		if (optionsAvailable) {
			fillGrowthButton.setSelection(options.growthMode == GrowthAdjustmentMode.USE_FILL || options.growthMode == null);
			slotGrowthButton.setSelection(options.growthMode == GrowthAdjustmentMode.USE_SLOT);
			slotRelativeGrowthButton.setSelection(options.growthMode == GrowthAdjustmentMode.RELATIVE_TO_SLOT);
			
			autolevelButton.setSelection(options.baseMode == StatAdjustmentMode.AUTOLEVEL || options.baseMode == null);
			absoluteButton.setSelection(options.baseMode == StatAdjustmentMode.MATCH_SLOT);
			relativeButton.setSelection(options.baseMode == StatAdjustmentMode.RELATIVE_TO_SLOT);
			
			autolevelOriginalButton.setSelection(options.autolevelMode == BaseStatAutolevelType.USE_ORIGINAL || options.autolevelMode == null);
			autolevelNewButton.setSelection(options.autolevelMode == BaseStatAutolevelType.USE_NEW);
			

			autolevelOriginalButton.setEnabled(autolevelButton.getSelection());
			autolevelNewButton.setEnabled(autolevelButton.getSelection());
		}
	}
}
