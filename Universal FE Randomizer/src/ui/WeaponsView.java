package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import fedata.general.FEBase.GameType;
import ui.WeaponEffectSelectionView.WeaponEffectSelectionViewListener;
import ui.general.MinMaxControl;
import ui.model.MinMaxVarOption;
import ui.model.WeaponOptions;

public class WeaponsView extends AbstractYuneView {
	private Group container;

	private Button enableMightButton;
	private Spinner mightVarianceSpinner;
	private MinMaxControl mightRangeControl;

	private Button enableHitButton;
	private Spinner hitVarianceSpinner;
	private MinMaxControl hitRangeControl;

	private Button enableWeightButton;
	private Spinner weightVarianceSpinner;
	private MinMaxControl weightRangeControl;

	private Button enableDurabilityButton;
	private Spinner durabilityVarianceSpinner;
	private MinMaxControl durabilityRangeControl;

	private Button enableRandomEffectsButton;
	private Button noEffectsForIronButton;
	private Button noEffectsForSteelButton;
	private Button noEffectsForBasicThrownButton;
	private Button includeLaguzButton;
	private Label effectChanceLabel;
	private Spinner effectChanceSpinner;
	private WeaponEffectSelectionView effectsSelectionView;

	public WeaponsView(Composite parent, int style, GameType type) {
		super(parent, style);
		setLayout(new FillLayout());

		container = createContainer(this, "Weapons");
		setGroupMargins(container);

		///////////////////////////////////////////////////////

		enableMightButton = createButton(container, SWT.CHECK, "Randomize Power (MT)",
				"Applies a random delta +/- Variance to all weapons' MT stat. All weapons are then clamped to the min and max specified.");
		layout(enableMightButton, new FormAttachment(0, 5), new FormAttachment(0, 5));
		enableMightButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				mightRangeControl.setEnabled(enableMightButton.getSelection());
				mightVarianceSpinner.setEnabled(enableMightButton.getSelection());
			}
		});

		Composite mtParamContainer = new Composite(container, SWT.NONE);
		setGroupMargins(mtParamContainer);
		layout(mtParamContainer, new FormAttachment(enableMightButton, 0),
				new FormAttachment(enableMightButton, 0, SWT.LEFT));

		Label mtVarianceLabel = createLabel(mtParamContainer, SWT.RIGHT, "Variance:");
		layout(mtVarianceLabel, new FormAttachment(mightVarianceSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
				new FormAttachment(50, -5));

		mightVarianceSpinner = createSpinner(mtParamContainer, new int[] { 3, 1, 31, 0, 1, 1 }, false);
		layout(mightVarianceSpinner, new FormAttachment(0, 5), new FormAttachment(50, 0));

		mightRangeControl = new MinMaxControl(mtParamContainer, SWT.NONE, "Min MT:", "Max MT:");
		mightRangeControl.getMinSpinner().setValues(0, 0, 31, 0, 1, 1);
		mightRangeControl.getMaxSpinner().setValues(23, 0, 31, 0, 1, 1);
		mightRangeControl.setEnabled(false);
		layout(mightRangeControl, new FormAttachment(mightVarianceSpinner, 5), new FormAttachment(0, 5),
				new FormAttachment(100, -5));

		///////////////////////////////////////////////////////

		enableHitButton = createButton(container, SWT.CHECK, "Randomize Accuracy (Hit)",
				"Applies a random delta +/- Variance to all weapons' accuracy. All weapons are then clamped to the min and max specified.");
		layout(enableHitButton, new FormAttachment(mtParamContainer, 5), new FormAttachment(0, 5));
		enableHitButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				hitRangeControl.setEnabled(enableHitButton.getSelection());
				hitVarianceSpinner.setEnabled(enableHitButton.getSelection());
			}
		});

		Composite hitParamContainer = new Composite(container, SWT.NONE);
		setGroupMargins(hitParamContainer);
		layout(hitParamContainer, new FormAttachment(enableHitButton, 0),
				new FormAttachment(enableHitButton, 0, SWT.LEFT));

		Label hitVarianceLabel = createLabel(hitParamContainer, SWT.RIGHT, "Variance:");
		layout(hitVarianceLabel, new FormAttachment(hitVarianceSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
				new FormAttachment(50, -5));

		hitVarianceSpinner = createSpinner(hitParamContainer, new int[] { 20, 1, 255, 0, 1, 5 }, false);
		layout(hitVarianceSpinner, new FormAttachment(0, 5), new FormAttachment(50, 0));

		hitRangeControl = new MinMaxControl(hitParamContainer, SWT.NONE, "Min Hit:", "Max Hit:");
		hitRangeControl.getMinSpinner().setValues(55, 0, 255, 0, 1, 5);
		hitRangeControl.getMaxSpinner().setValues(100, 0, 255, 0, 1, 5);
		hitRangeControl.setEnabled(false);
		layout(hitRangeControl, new FormAttachment(hitVarianceSpinner, 5), new FormAttachment(0, 5),
				new FormAttachment(100, -5));

		///////////////////////////////////////////////////////

		enableWeightButton = createButton(container, SWT.CHECK, "Randomize Weights (WT)",
				"Applies a random delta +/- Variance to all weapons' weight. All weapons are then clamped to the min and max specified.", true, false);
		layout(enableWeightButton, new FormAttachment(hitParamContainer, 5), new FormAttachment(0, 5));
		enableWeightButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				weightRangeControl.setEnabled(enableWeightButton.getSelection());
				weightVarianceSpinner.setEnabled(enableWeightButton.getSelection());
			}
		});

		Composite wtParamContainer = new Composite(container, SWT.NONE);
		setGroupMargins(wtParamContainer);
		layout(wtParamContainer, new FormAttachment(enableWeightButton, 0),
				new FormAttachment(enableWeightButton, 0, SWT.LEFT));

		weightVarianceSpinner = createSpinner(wtParamContainer, new int[] { 5, 1, 255, 0, 1, 5 }, false);
		layout(weightVarianceSpinner, new FormAttachment(0, 5), new FormAttachment(50, 0));

		Label wtVarianceLabel = new Label(wtParamContainer, SWT.RIGHT);
		wtVarianceLabel.setText("Variance:");
		layout(wtVarianceLabel, new FormAttachment(weightVarianceSpinner, 0, SWT.CENTER), new FormAttachment(0, 5),
				new FormAttachment(50, -5));


		weightRangeControl = new MinMaxControl(wtParamContainer, SWT.NONE, "Min WT:", "Max WT:");
		weightRangeControl.getMinSpinner().setValues(2, 1, 30, 0, 1, 5);
		weightRangeControl.getMaxSpinner().setValues(20, 1, 30, 0, 1, 5);
		weightRangeControl.setEnabled(false);
		layout(weightRangeControl, new FormAttachment(weightVarianceSpinner, 5), new FormAttachment(0, 5),
				new FormAttachment(100, -5));


		///////////////////////////////////////////////////////

		enableDurabilityButton = createButton(container, SWT.CHECK, "Randomize Durability",
				"Applies a random delta +/- Variance to all weapons' durability. All weapons are then clamped to the min and max specified. Siege tomes are limited to a 1-use minimum.");
		layout(enableDurabilityButton, new FormAttachment(wtParamContainer, 5), new FormAttachment(0, 5));
		enableDurabilityButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				durabilityRangeControl.setEnabled(enableDurabilityButton.getSelection());
				durabilityVarianceSpinner.setEnabled(enableDurabilityButton.getSelection());
			}
		});

		Composite durabilityParamContainer = new Composite(container, SWT.NONE);
		setGroupMargins(durabilityParamContainer);
		layout(durabilityParamContainer, new FormAttachment(enableDurabilityButton, 0),
				new FormAttachment(enableDurabilityButton, 0, SWT.LEFT));

		Label durabilityVarianceLabel = createLabel(durabilityParamContainer, SWT.RIGHT, "Variance:");
		layout(durabilityVarianceLabel, new FormAttachment(durabilityVarianceSpinner, 0, SWT.CENTER),
				new FormAttachment(0, 5), new FormAttachment(50, -5));

		durabilityVarianceSpinner = createSpinner(durabilityParamContainer, new int[] { 20, 1, 255, 0, 1, 5 }, false);
		layout(durabilityVarianceSpinner, new FormAttachment(0, 5), new FormAttachment(50, 0));

		durabilityRangeControl = new MinMaxControl(durabilityParamContainer, SWT.NONE, "Min Uses:", "Max Uses:");
		durabilityRangeControl.getMinSpinner().setValues(15, 1, 63, 0, 1, 5);
		durabilityRangeControl.getMaxSpinner().setValues(60, 1, 63, 0, 1, 5);
		durabilityRangeControl.setEnabled(false);
		layout(durabilityRangeControl, new FormAttachment(durabilityVarianceSpinner, 5), new FormAttachment(0, 5),
				new FormAttachment(100, -5));



		///////////////////////////////////////////////////////

		enableRandomEffectsButton = createButton(container, SWT.CHECK, "Add Random Effects",
				"Adds a random effect to all weapons. Effects includes stat bonuses, effectiveness, weapon triangle reversal, brave, magic damge, etc. Weapons that already have effects get a second effect added on.");
		layout(enableRandomEffectsButton, new FormAttachment(durabilityParamContainer, 5), new FormAttachment(0, 5));

		noEffectsForIronButton = createButton(container, SWT.CHECK, "Safe Basic Weapons", getSafeIronTooltip(type), false, false);
		layout(noEffectsForIronButton, new FormAttachment(enableRandomEffectsButton, 5),
				new FormAttachment(enableRandomEffectsButton, 10, SWT.LEFT));
		noEffectsForIronButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				noEffectsForSteelButton.setEnabled(noEffectsForIronButton.getSelection());
				noEffectsForBasicThrownButton.setEnabled(noEffectsForIronButton.getSelection());
				noEffectsForSteelButton.setSelection(noEffectsForIronButton.getSelection());
				noEffectsForBasicThrownButton.setSelection(noEffectsForIronButton.getSelection());
			}
		});

		Control lastControl = noEffectsForIronButton;

		if (type.isGBA()) {
			noEffectsForSteelButton = createButton(container, SWT.CHECK, "Safe Steel Weapons",
					"Steel Weapons (and Thunder) remain unchanged.", false, false);
			layout(noEffectsForSteelButton, new FormAttachment(noEffectsForIronButton, 5),
					new FormAttachment(noEffectsForIronButton, 10, SWT.LEFT));

			noEffectsForBasicThrownButton = createButton(container, SWT.CHECK, "Safe Basic Thrown Weapons",
					"Thrown Weapons (Javelin, Hand Axe) remain unchanged.", false, false);
			layout(noEffectsForBasicThrownButton, new FormAttachment(noEffectsForSteelButton, 5),
					new FormAttachment(noEffectsForSteelButton, 0, SWT.LEFT));

			lastControl = noEffectsForBasicThrownButton;
		} else if (type == GameType.FE9) {
			includeLaguzButton = createButton(container, SWT.CHECK, "Include Laguz Weapons",
					"Adds a random effect to claws, fangs, beaks, and breaths. All laguz of the same type share the same weapon trait.\nSome effects (like extended range) are not eligible for laguz weapons.", false, false);
			layout(includeLaguzButton, new FormAttachment(noEffectsForIronButton, 5),
					new FormAttachment(noEffectsForIronButton, 0, SWT.LEFT));
			lastControl = includeLaguzButton;
		}

		effectChanceSpinner = createSpinner(container, new int[] { 25, 1, 100, 0, 1, 5 },
				"Sets the chance of an effect being added to a weapon.", false);
		layout(effectChanceSpinner, new FormAttachment(lastControl, 10), new FormAttachment(effectChanceLabel, 10));

		effectChanceLabel = createLabel(container, "Effect Chance:");
		layout(effectChanceLabel, new FormAttachment(effectChanceSpinner, 0, SWT.CENTER),
				new FormAttachment(noEffectsForIronButton, 0, SWT.LEFT));

		updateWeaponEffectSelectionViewForGame(type);
	}

	private String getSafeIronTooltip(GameType type) {
		if (type == GameType.FE9) {
			return "Iron Weapons (inc. Knife, Fire, Wind, Thunder, and Light) remain unchanged. This establishes a safe-zone for weapons to not be broken.";
		} else {
			return "Iron Weapons (inc. Fire, Lightning, and Flux) remain unchanged. This establishes a safe-zone for weapons to not be broken.";
		}
	}

	public void updateWeaponEffectSelectionViewForGame(GameType type) {
		if (effectsSelectionView != null) {
			effectsSelectionView.dispose();
		}

		effectsSelectionView = new WeaponEffectSelectionView(container, SWT.NONE, type);
		layout(effectsSelectionView, 280, 0, new FormAttachment(effectChanceSpinner, 5),
				new FormAttachment(noEffectsForIronButton, 10, SWT.LEFT), null,new FormAttachment(100, -5));

		effectsSelectionView.setSelectionListener(new WeaponEffectSelectionViewListener() {
			@Override
			public void onSelectionChanged() {
				if (effectsSelectionView.isAllDisabled()) {
					enableRandomEffectsButton.setSelection(false);
					noEffectsForIronButton.setEnabled(false);
					if (noEffectsForSteelButton != null) {
						noEffectsForSteelButton.setEnabled(false);
					}
					if (noEffectsForBasicThrownButton != null) {
						noEffectsForBasicThrownButton.setEnabled(false);
					}
					effectChanceLabel.setEnabled(false);
					effectChanceSpinner.setEnabled(false);
					effectsSelectionView.setEnabled(false);
					if (includeLaguzButton != null) {
						includeLaguzButton.setEnabled(false);
					}
				}
			}
		});

		for (Listener listener : enableRandomEffectsButton.getListeners(SWT.Selection)) {
			enableRandomEffectsButton.removeListener(SWT.Selection, listener);
		}

		enableRandomEffectsButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Boolean enabled = enableRandomEffectsButton.getSelection();
				effectsSelectionView.setEnabled(enabled);
				noEffectsForIronButton.setEnabled(enabled);
				if (noEffectsForSteelButton != null) {
					noEffectsForSteelButton.setEnabled(enabled);
				}
				if (noEffectsForBasicThrownButton != null) {
					noEffectsForBasicThrownButton.setEnabled(enabled);
				}
				effectChanceLabel.setEnabled(enabled);
				effectChanceSpinner.setEnabled(enabled);
				if (includeLaguzButton != null) {
					includeLaguzButton.setEnabled(enabled);
				}
				if (enabled) {
					effectsSelectionView.selectAll();
					noEffectsForIronButton.setSelection(true);
					if (noEffectsForSteelButton != null) {
						noEffectsForSteelButton.setSelection(true);
					}
					if (noEffectsForBasicThrownButton != null) {
						noEffectsForBasicThrownButton.setSelection(true);
					}
				} else {
					effectsSelectionView.deselectAll();
					noEffectsForIronButton.setSelection(false);
					if (noEffectsForSteelButton != null) {
						noEffectsForSteelButton.setSelection(false);
					}
					if (noEffectsForBasicThrownButton != null) {
						noEffectsForBasicThrownButton.setSelection(false);
					}
				}
			}
		});
	}

	public WeaponOptions getWeaponOptions() {
		MinMaxVarOption mightOptions = null;
		MinMaxVarOption hitOptions = null;
		MinMaxVarOption weightOptions = null;
		MinMaxVarOption durabilityOptions = null;

		if (enableMightButton.getSelection()) {
			mightOptions = new MinMaxVarOption(mightRangeControl.getMinMaxOption(),
					mightVarianceSpinner.getSelection());
		}
		if (enableHitButton.getSelection()) {
			hitOptions = new MinMaxVarOption(hitRangeControl.getMinMaxOption(), hitVarianceSpinner.getSelection());
		}
		if (enableWeightButton.getSelection()) {
			weightOptions = new MinMaxVarOption(weightRangeControl.getMinMaxOption(),
					weightVarianceSpinner.getSelection());
		}
		if (enableDurabilityButton.getSelection()) {
			durabilityOptions = new MinMaxVarOption(durabilityRangeControl.getMinMaxOption(),
					durabilityVarianceSpinner.getSelection());
		}

		return new WeaponOptions(mightOptions, hitOptions, weightOptions, durabilityOptions,
				enableRandomEffectsButton.getSelection(), effectChanceSpinner.getSelection(),
				effectsSelectionView.getOptions(), noEffectsForIronButton.getSelection(),
				noEffectsForIronButton.getSelection() && (noEffectsForSteelButton != null && noEffectsForSteelButton.getSelection()),
				noEffectsForIronButton.getSelection() && (noEffectsForBasicThrownButton != null && noEffectsForBasicThrownButton.getSelection()),
				includeLaguzButton != null && includeLaguzButton.getSelection());
	}

	public void setWeaponOptions(WeaponOptions options) {
		if (options == null) {
			return;
		}

		if (options.mightOptions != null) {
			enableMightButton.setSelection(true);
			mightRangeControl.setEnabled(true);
			mightRangeControl.setMin(options.mightOptions.minValue);
			mightRangeControl.setMax(options.mightOptions.maxValue);
			mightVarianceSpinner.setEnabled(true);
			mightVarianceSpinner.setSelection(options.mightOptions.variance);
		}

		if (options.hitOptions != null) {
			enableHitButton.setSelection(true);
			hitRangeControl.setEnabled(true);
			hitRangeControl.setMin(options.hitOptions.minValue);
			hitRangeControl.setMax(options.hitOptions.maxValue);
			hitVarianceSpinner.setEnabled(true);
			hitVarianceSpinner.setSelection(options.hitOptions.variance);
		}

		if (options.weightOptions != null) {
			enableWeightButton.setSelection(true);
			weightRangeControl.setEnabled(true);
			weightRangeControl.setMin(options.weightOptions.minValue);
			weightRangeControl.setMax(options.weightOptions.maxValue);
			weightVarianceSpinner.setEnabled(true);
			weightVarianceSpinner.setSelection(options.weightOptions.variance);
		}

		if (options.durabilityOptions != null) {
			enableDurabilityButton.setSelection(true);
			durabilityRangeControl.setEnabled(true);
			durabilityRangeControl.setMin(options.durabilityOptions.minValue);
			durabilityRangeControl.setMax(options.durabilityOptions.maxValue);
			durabilityVarianceSpinner.setEnabled(true);
			durabilityVarianceSpinner.setSelection(options.durabilityOptions.variance);
		}

		if (options.shouldAddEffects) {
			enableRandomEffectsButton.setSelection(true);
			effectsSelectionView.setEnabled(true);
			noEffectsForIronButton.setEnabled(true);
			effectChanceSpinner.setEnabled(true);
			effectChanceLabel.setEnabled(true);
			effectsSelectionView.setOptions(options.effectsList);
			noEffectsForIronButton.setSelection(options.noEffectIronWeapons);
			if (noEffectsForSteelButton != null) {
				noEffectsForSteelButton.setEnabled(true);
				noEffectsForSteelButton.setSelection(options.noEffectSteelWeapons);
			}
			if (noEffectsForBasicThrownButton != null) {
				noEffectsForBasicThrownButton.setEnabled(true);
				noEffectsForBasicThrownButton.setSelection(options.noEffectThrownWeapons);
			}
			effectChanceSpinner.setSelection(options.effectChance);
			if (includeLaguzButton != null) {
				includeLaguzButton.setEnabled(true);
				includeLaguzButton.setSelection(options.includeLaguzWeapons);
			}
		}
	}
}