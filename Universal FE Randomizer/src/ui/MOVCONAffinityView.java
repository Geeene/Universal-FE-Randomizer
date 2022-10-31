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

import ui.general.MinMaxControl;
import ui.model.MinMaxOption;
import ui.model.MinVarOption;
import ui.model.OtherCharacterOptions;

public class MOVCONAffinityView extends AbstractYuneView {

    private Group container;

    private Button randomizeMOVButton;
    private MinMaxControl movLimitControl;

    private Button randomizeCONButton;
    private Spinner varianceSpinner;
    private Spinner minCONSpinner;

    private Button randomizeAffinityButton;

    public MOVCONAffinityView(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        container = createContainer(this, "Other Character Settings");
        setGroupMargins(container);

        ///////////////////////////////////////////////////////

        randomizeMOVButton = createButton(container, SWT.CHECK, "Randomize MOV",
                "Assigns each class a random MOV range between the minimum and maximum specified. Male and Female versions are considered different classes.");
        layout(randomizeMOVButton, new FormAttachment(0, 5), new FormAttachment(0, 5));
        randomizeMOVButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                movLimitControl.setEnabled(randomizeMOVButton.getSelection());
            }
        });

        movLimitControl = new MinMaxControl(container, SWT.NONE, "Min MOV:", "Max MOV:");
        movLimitControl.getMinSpinner().setValues(4, 1, 8, 0, 1, 1);
        movLimitControl.getMaxSpinner().setValues(8, 4, 15, 0, 1, 1);
        movLimitControl.setEnabled(false);
        layout(movLimitControl, new FormAttachment(randomizeMOVButton, 0),
                new FormAttachment(randomizeMOVButton, 0, SWT.LEFT), new FormAttachment(100, -5));

        ///////////////////////////////////////////////////////

        randomizeCONButton = createButton(container, SWT.CHECK, "Randomize CON",
                "Randomly adjusts each character's CON offset from their class base, up to a maximum variance.");
        layout(randomizeCONButton, new FormAttachment(movLimitControl, 10),
                new FormAttachment(randomizeMOVButton, 0, SWT.LEFT));
        randomizeCONButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                varianceSpinner.setEnabled(randomizeCONButton.getSelection());
                minCONSpinner.setEnabled(randomizeCONButton.getSelection());
            }
        });

        Composite conParamContainer = createContainer(container);
        setGroupMargins(conParamContainer);
        layout(conParamContainer, new FormAttachment(randomizeCONButton, 0),
                new FormAttachment(randomizeCONButton, 0, SWT.LEFT), new FormAttachment(100, -5));

        varianceSpinner = createSpinner(conParamContainer, new int[]{3, 1, 10, 0, 1, 1}, false);
        layout(varianceSpinner, null, null, new FormAttachment(50, -5));
        Label varianceLabel = createLabel(conParamContainer, SWT.RIGHT, "Variance:");
        layout(varianceLabel, new FormAttachment(varianceSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
                new FormAttachment(varianceSpinner, -5));

        minCONSpinner = createSpinner(conParamContainer, new int[]{3, 0, 15, 0, 1, 1}, false);
        layout(minCONSpinner, null, null, new FormAttachment(100, -5));
        Label minLabel = createLabel(conParamContainer, SWT.RIGHT, "Min CON:");
        layout(minLabel, new FormAttachment(minCONSpinner, 0, SWT.CENTER), new FormAttachment(50, 5),
                new FormAttachment(minCONSpinner, -5));

        ///////////////////////////////////////////////////////

        randomizeAffinityButton = createButton(container, SWT.CHECK, "Randomize Affinity",
                "Assigns random support affinities to all characters.");
        layout(randomizeAffinityButton, new FormAttachment(conParamContainer, 10),
                new FormAttachment(randomizeCONButton, 0, SWT.LEFT));
    }

    public OtherCharacterOptions getOtherCharacterOptions() {
        MinMaxOption movementOptions = null;
        MinVarOption constitutionOptions = null;

        if (randomizeMOVButton.getSelection()) {
            movementOptions = movLimitControl.getMinMaxOption();
        }
        if (randomizeCONButton.getSelection()) {
            constitutionOptions = new MinVarOption(minCONSpinner.getSelection(), varianceSpinner.getSelection());
        }

        return new OtherCharacterOptions(movementOptions, constitutionOptions, randomizeAffinityButton.getSelection());
    }

    public void setOtherCharacterOptions(OtherCharacterOptions options) {
        if (options == null) {
            return;
        }

        if (options.movementOptions != null) {
            randomizeMOVButton.setSelection(true);
            movLimitControl.setEnabled(true);
            movLimitControl.setMin(options.movementOptions.minValue);
            movLimitControl.setMax(options.movementOptions.maxValue);
        }

        if (options.constitutionOptions != null) {
            randomizeCONButton.setSelection(true);
            minCONSpinner.setEnabled(true);
            varianceSpinner.setEnabled(true);
            minCONSpinner.setSelection(options.constitutionOptions.minValue);
            varianceSpinner.setSelection(options.constitutionOptions.variance);
        }

        randomizeAffinityButton.setSelection(options.randomizeAffinity);
    }
}
