package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import fedata.general.FEBase.GameType;
import ui.model.BaseOptions;
import ui.model.VarOption;

public class BasesView extends AbstractYuneView {

	private Boolean isEnabled = false;
	private BaseOptions.Mode currentMode = BaseOptions.Mode.REDISTRIBUTE;

	private Group container;
	private Button enableButton;
	private Button redistributeOption;
	private Spinner varianceSpinner;
	private Button byDeltaOption;
	private Spinner deltaSpinner;

	private Button adjustSTRMAG;

	public BasesView(Composite parent, int style, GameType type) {
		super(parent, style);
		setLayout(new FillLayout());

		container = createContainer(this, "Bases",
				"Randomizes the base stat offsets of all playable characters, relative to their class (excluding CON).");
		setGroupMargins(container);

		enableButton = createButton(container, SWT.CHECK, "Enable Bases Randomization", "Enable Bases Randomization",
				true, false);

		/////////////////////////////////////////////////////////////

		redistributeOption = createButton(container, SWT.RADIO, "Redistribute", getRedistributeTooltip(type), false,
				true);
		redistributeOption.addListener(SWT.Selection, event -> setMode(BaseOptions.Mode.REDISTRIBUTE));
		layout(redistributeOption, new FormAttachment(enableButton, 5), new FormAttachment(enableButton, 0, SWT.LEFT));

		Group redistParamContainer = createContainer(container);
		setGroupMargins(redistParamContainer);
		layout(redistParamContainer, new FormAttachment(redistributeOption, 0),
				new FormAttachment(redistributeOption, 0, SWT.LEFT), new FormAttachment(100, -5));

		Label redistParamLabel = createLabel(redistParamContainer, SWT.RIGHT, "Growth Variance:");
		layout(redistParamLabel, new FormAttachment(varianceSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
				new FormAttachment(varianceSpinner, -5));

		varianceSpinner = createSpinner(redistParamContainer, new int[] { 5, 0, 10, 0, 1, 1 }, null, false);
		layout(varianceSpinner, null, null, new FormAttachment(100, -5));

		/////////////////////////////////////////////////////////////

		byDeltaOption = createButton(container, SWT.RADIO, "Randomize Delta",
				"Applies a random delta between +X and -X to all base stats (excluding CON).", false, false);
		byDeltaOption.addListener(SWT.Selection, event -> setMode(BaseOptions.Mode.DELTA));
		layout(byDeltaOption, new FormAttachment(redistParamContainer, 0),
				new FormAttachment(redistParamContainer, 0, SWT.LEFT));

		Composite deltaParamContainer = new Composite(container, 0);
		setGroupMargins(deltaParamContainer);
		layout(deltaParamContainer, new FormAttachment(byDeltaOption, 0),
				new FormAttachment(byDeltaOption, 0, SWT.LEFT), new FormAttachment(100, -5));

		deltaSpinner = createSpinner(deltaParamContainer, false);
		layout(deltaSpinner, null, null, new FormAttachment(100, -5));

		Label deltaParamLabel = createLabel(deltaParamContainer, SWT.RIGHT, "Max Delta:", true);
		layout(deltaParamLabel, new FormAttachment(deltaSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
				new FormAttachment(deltaSpinner, -5));

		if (type.hasSTRMAGSplit()) {
			createButton(container, SWT.CHECK, "Adjust STR/MAG by Class",
					"Ensures that characters that primarily use magic randomize a higher or equal magic base than strength and that\ncharacters that primarily use physical attacks randomize a higher or equal strength base than magic.\n\nCharacters that use both will not be weighted in either direction.",
					false, false);
			layout(adjustSTRMAG, new FormAttachment(deltaParamContainer, 10),
					new FormAttachment(byDeltaOption, 0, SWT.LEFT));
		}
	}

	private String getRedistributeTooltip(GameType type) {
		String common = "Randomly redistrubtes the sum of the character's base stat offsets ";
		return GameType.FE4.equals(type) ? common + "(excluding HP)." : common + "(excluding CON).";
	}

	private void setEnableBases(Boolean enabled) {
		redistributeOption.setEnabled(enabled);
		byDeltaOption.setEnabled(enabled);
		varianceSpinner.setEnabled(enabled && currentMode == BaseOptions.Mode.REDISTRIBUTE);
		deltaSpinner.setEnabled(enabled && currentMode == BaseOptions.Mode.DELTA);
		if (adjustSTRMAG != null) {
			adjustSTRMAG.setEnabled(enabled);
		}

		isEnabled = enabled;
	}

	private void setMode(BaseOptions.Mode newMode) {
		currentMode = newMode;
		if (isEnabled) {
			switch (newMode) {
			case REDISTRIBUTE:
				varianceSpinner.setEnabled(true);
				deltaSpinner.setEnabled(false);
				break;
			case DELTA:
				varianceSpinner.setEnabled(false);
				deltaSpinner.setEnabled(true);
				break;
			}
		}
	}

	public BaseOptions getBaseOptions() {
		if (!isEnabled) {
			return null;
		}

		VarOption redistributionOption = null;
		VarOption deltaOption = null;

		switch (currentMode) {
		case REDISTRIBUTE:
			redistributionOption = new VarOption(varianceSpinner.getSelection());
			break;
		case DELTA:
			deltaOption = new VarOption(deltaSpinner.getSelection());
			break;
		}

		boolean adjustSTRMAGBases = adjustSTRMAG != null && adjustSTRMAG.getSelection();

		return new BaseOptions(currentMode, redistributionOption, deltaOption, adjustSTRMAGBases);
	}

	public void setBasesOptions(BaseOptions options) {
		if (options == null) {
			enableButton.setSelection(false);
			setEnableBases(false);
		} else {
			enableButton.setSelection(true);
			setEnableBases(true);
			setMode(options.mode);

			switch (options.mode) {
			case REDISTRIBUTE:
				redistributeOption.setSelection(true);
				byDeltaOption.setSelection(false);
				varianceSpinner.setSelection(options.redistributionOption.variance);
				break;
			case DELTA:
				redistributeOption.setSelection(false);
				byDeltaOption.setSelection(true);
				deltaSpinner.setSelection(options.deltaOption.variance);
				break;
			}

			if (adjustSTRMAG != null) {
				adjustSTRMAG.setSelection(options.adjustSTRMAGByClass);
			}
		}
	}

}
