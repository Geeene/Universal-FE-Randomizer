package ui;

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
import org.eclipse.swt.widgets.Spinner;

import ui.general.MinMaxControl;
import ui.model.GrowthOptions;
import ui.model.MinMaxOption;
import ui.model.MinMaxVarOption;

public class GrowthsView extends AbstractYuneView {

    private Boolean isEnabled = false;
    private GrowthOptions.Mode currentMode = GrowthOptions.Mode.REDISTRIBUTE;

    private Group container;

    private Button enableButton;

    private Group modeContainer;

    private Button redistributeOption;
    private Spinner varianceSpinner;

    private Button byDeltaOption;
    private Spinner deltaSpinner;

    private Button fullRandomOption;
    private MinMaxControl growthRangeControl;

    private Button adjustHPGrowths;
    private Button adjustSTRMAGSplit;

    public GrowthsView(Composite parent, int style, boolean hasSTRMAGSplit) {
        super(parent, style);
        setLayout(new FillLayout());

        container = createContainer(this, "Growths", "Randomizes the growths of all playable characters.");
        setGroupMargins(container);

        enableButton = createButton(container, SWT.CHECK, "Enable Growths Randomization", "");
        enableButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setEnableGrowths(enableButton.getSelection());
            }
        });

        growthRangeControl = new MinMaxControl(container, SWT.NONE, "Min Growth:", "Max Growth:");
        growthRangeControl.getMinSpinner().setValues(5, 0, 255, 0, 1, 5);
        growthRangeControl.getMaxSpinner().setValues(80, 0, 255, 0, 1, 5);
        growthRangeControl.setEnabled(false);
        layout(growthRangeControl, new FormAttachment(enableButton, 5), new FormAttachment(0, 5),
                new FormAttachment(100, -5));

        modeContainer = createContainer(container, "Mode");
        setGroupMargins(modeContainer);
        layout(modeContainer, new FormAttachment(growthRangeControl, 10), new FormAttachment(enableButton, 5, SWT.LEFT),
                new FormAttachment(100, -5));

        /////////////////////////////////////////////////////////////
        redistributeOption = createButton(modeContainer, SWT.RADIO, "Redistribute",
                "Randomly redistributes a character's total growths.", false, true);
        defaultLayout(redistributeOption);
        redistributeOption.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setMode(GrowthOptions.Mode.REDISTRIBUTE);
            }
        });

        Composite redistParamContainer = new Composite(modeContainer, 0);
        setGroupMargins(redistParamContainer);
        layout(redistParamContainer, new FormAttachment(redistributeOption, 0), new FormAttachment(redistributeOption, 0, SWT.LEFT), new FormAttachment(100, -5));

        Label redistParamLabel = createLabel(redistParamContainer, SWT.RIGHT, "Growth Variance:");
        layout(redistParamLabel, new FormAttachment(varianceSpinner, 0, SWT.CENTER), new FormAttachment(0, 5), new FormAttachment(varianceSpinner, -5));

        varianceSpinner = createSpinner(redistParamContainer, new int[]{30, 0, 255, 0, 1, 5}, false);
        layout(varianceSpinner, null, null, new FormAttachment(100, -5));


        /////////////////////////////////////////////////////////////

        byDeltaOption = createButton(modeContainer, SWT.RADIO, "Randomize Delta", "Applies a random delta between +X and -X to all growth areas.");
        layout(byDeltaOption, new FormAttachment(redistParamContainer, 0), new FormAttachment(redistParamContainer, 0, SWT.LEFT));
        byDeltaOption.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setMode(GrowthOptions.Mode.DELTA);
            }
        });


        Composite deltaParamContainer = new Composite(modeContainer, 0);
        setGroupMargins(deltaParamContainer);
        layout(deltaParamContainer, new FormAttachment(byDeltaOption, 0), new FormAttachment(byDeltaOption, 0, SWT.LEFT), new FormAttachment(100, -5));

        Label deltaParamLabel = createLabel(deltaParamContainer, SWT.RIGHT, "Max Delta:");
        layout(deltaParamLabel, new FormAttachment(deltaSpinner, 0, SWT.CENTER), new FormAttachment(0, 5), new FormAttachment(deltaSpinner, -5));

        deltaSpinner = createSpinner(deltaParamContainer, new int[]{20, 0, 255, 0, 1, 5}, false);
        layout(deltaSpinner, null, null, new FormAttachment(100, -5));

        /////////////////////////////////////////////////////////////

        fullRandomOption = createButton(modeContainer, SWT.RADIO, "Randomize Absolute", "Generates fully random growth rates between the specified minimum and maximum.");
        layout(fullRandomOption, new FormAttachment(deltaParamContainer, 0), new FormAttachment(deltaParamContainer, 0, SWT.LEFT));
        fullRandomOption.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                setMode(GrowthOptions.Mode.FULL);
            }
        });

        adjustHPGrowths = createButton(container, SWT.CHECK, "Adjust HP Growths", "Puts extra emphasis on HP growths relative to other stats.");
        layout(adjustHPGrowths, new FormAttachment(modeContainer, 10), new FormAttachment(enableButton, 10, SWT.LEFT));

        if (hasSTRMAGSplit) {
            adjustSTRMAGSplit = createButton(container, SWT.CHECK, "Adjust STR/MAG by Class", "Ensures that characters that primarily use magic randomize a higher or equal magic growth than strength and that\ncharacters that primarily use physical attacks randomize a higher or equal strength growth than magic.\n\nCharacters that use both will not be weighted in either direction.");
            layout(adjustSTRMAGSplit, new FormAttachment(adjustHPGrowths, 5), new FormAttachment(adjustHPGrowths, 0, SWT.LEFT));
        }
    }

    public void overrideMaxGrowthAllowed(int maxGrowth) {
        growthRangeControl.getMaxSpinner().setMaximum(maxGrowth);
    }

    private void setEnableGrowths(Boolean enabled) {
        redistributeOption.setEnabled(enabled);
        byDeltaOption.setEnabled(enabled);
        fullRandomOption.setEnabled(enabled);
        varianceSpinner.setEnabled(enabled && currentMode == GrowthOptions.Mode.REDISTRIBUTE);
        deltaSpinner.setEnabled(enabled && currentMode == GrowthOptions.Mode.DELTA);
        growthRangeControl.setEnabled(enabled);
        adjustHPGrowths.setEnabled(enabled);
        if (adjustSTRMAGSplit != null) {
            adjustSTRMAGSplit.setEnabled(enabled && currentMode != GrowthOptions.Mode.DELTA);
        }

        isEnabled = enabled;
    }

    private void setMode(GrowthOptions.Mode newMode) {
        currentMode = newMode;
        if (!isEnabled) {
            return;
        }
        switch (newMode) {
            case REDISTRIBUTE:
                varianceSpinner.setEnabled(true);
                deltaSpinner.setEnabled(false);
                growthRangeControl.setEnabled(true);
                break;
            case DELTA:
                varianceSpinner.setEnabled(false);
                deltaSpinner.setEnabled(true);
                growthRangeControl.setEnabled(true);
                break;
            case FULL:
                varianceSpinner.setEnabled(false);
                deltaSpinner.setEnabled(false);
                growthRangeControl.setEnabled(true);
                break;
        }
    }

    public GrowthOptions getGrowthOptions() {
        if (!isEnabled) {
            return null;
        }

        MinMaxVarOption redistributionOption = null;
        MinMaxVarOption deltaOption = null;
        MinMaxOption fullOption = null;

        switch (currentMode) {
            case REDISTRIBUTE:
                redistributionOption = new MinMaxVarOption(growthRangeControl.getMinMaxOption(),
                        varianceSpinner.getSelection());
                break;
            case DELTA:
                deltaOption = new MinMaxVarOption(growthRangeControl.getMinMaxOption(), deltaSpinner.getSelection());
                break;
            case FULL:
                fullOption = growthRangeControl.getMinMaxOption();
                break;
        }

        boolean adjustSTRMAG = adjustSTRMAGSplit != null ? adjustSTRMAGSplit.getSelection() : false;

        return new GrowthOptions(currentMode, redistributionOption, deltaOption, fullOption,
                adjustHPGrowths.getSelection(), adjustSTRMAG);
    }

    public void setGrowthOptions(GrowthOptions options) {
        if (options == null) {
            enableButton.setSelection(false);
            setEnableGrowths(false);
        } else {
            enableButton.setSelection(true);
            setEnableGrowths(true);
            setMode(options.mode);

            switch (options.mode) {
                case REDISTRIBUTE:
                    redistributeOption.setSelection(true);
                    byDeltaOption.setSelection(false);
                    fullRandomOption.setSelection(false);
                    varianceSpinner.setSelection(options.redistributionOption.variance);
                    if (options.redistributionOption.minValue < growthRangeControl.getMinSpinner().getMaximum()) {
                        growthRangeControl.setMin(options.redistributionOption.minValue);
                        growthRangeControl.setMax(options.redistributionOption.maxValue);
                    } else {
                        growthRangeControl.setMax(options.redistributionOption.maxValue);
                        growthRangeControl.setMin(options.redistributionOption.minValue);
                    }
                    break;
                case DELTA:
                    redistributeOption.setSelection(false);
                    byDeltaOption.setSelection(true);
                    fullRandomOption.setSelection(false);
                    deltaSpinner.setSelection(options.deltaOption.variance);
                    if (options.deltaOption.minValue < growthRangeControl.getMinSpinner().getMaximum()) {
                        growthRangeControl.setMin(options.deltaOption.minValue);
                        growthRangeControl.setMax(options.deltaOption.maxValue);
                    } else {
                        growthRangeControl.setMax(options.deltaOption.maxValue);
                        growthRangeControl.setMin(options.deltaOption.minValue);
                    }
                    break;
                case FULL:
                    redistributeOption.setSelection(false);
                    byDeltaOption.setSelection(false);
                    fullRandomOption.setSelection(true);
                    if (options.fullOption.minValue < growthRangeControl.getMinSpinner().getMaximum()) {
                        growthRangeControl.setMin(options.fullOption.minValue);
                        growthRangeControl.setMax(options.fullOption.maxValue);
                    } else {
                        growthRangeControl.setMax(options.fullOption.maxValue);
                        growthRangeControl.setMin(options.fullOption.minValue);
                    }
                    break;
            }

            adjustHPGrowths.setSelection(options.adjustHP);
            if (adjustSTRMAGSplit != null) {
                adjustSTRMAGSplit.setSelection(options.adjustSTRMAGSplit);
            }
        }
    }
}
