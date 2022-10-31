package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import fedata.general.FEBase.GameType;
import ui.general.MinMaxControl;
import ui.model.MiscellaneousOptions;
import ui.model.MiscellaneousOptions.RewardMode;

public class MiscellaneousView extends AbstractYuneView {

	private Group container;

	GameType type;

	private Button applyEnglishPatch; // pre-FE6 only
	private Button trippleEffectiveness; // FE7 only

	private Button randomizeRewards;

	private MiscellaneousOptions.RewardMode rewardMode;

	private Composite rewardModeContainer;
	private Button similarRewardsButton;
	private Button randomRewardsButton;

	public Button singleRNButton;

	private Button enemyDropsButton;
	private Label enemyDropChanceLabel;
	private Spinner enemyDropChanceSpinner;

	private Button addFogButton;
	private Label fogChanceLabel;
	private Spinner fogChanceSpinner;
	private MinMaxControl fogVisionRangeControl;

	// FE4 only.
	private Button followupRequirement;
	private Label withPursuitLabel;
	private Label withoutPursuitLabel;
	private Spinner withPursuitSpinner;
	private Spinner withoutPursuitSpinner;

	public MiscellaneousView(Composite parent, int style, GameType gameType) {
		super(parent, style);
		setLayout(new FillLayout());
		type = gameType;
		FormAttachment top = null;
		container = createContainer(this, "Miscellaneous");
		setGroupMargins(container);

		//////////////////////////////////////////////////////////////////

		Control lastControl = null;

		if (gameType.hasEnglishPatch()) {
			String toolTip = getTranslationPatchTooltip(gameType);
			applyEnglishPatch = createButton(container, SWT.CHECK, "Apply English Patch", toolTip);
			layout(applyEnglishPatch, new FormAttachment(0, 5), new FormAttachment(0, 5));

			lastControl = applyEnglishPatch;
		}

		if (gameType == GameType.FE7) {
			trippleEffectiveness = createButton(container, SWT.CHECK, "Set Effectiveness to 3x",
					"Reverts the weapon effectiveness to 3x like in the Japanese release, instead of 2x.");
			top = lastControl == null ? new FormAttachment(0, 5) : new FormAttachment(lastControl, 10);
			layout(trippleEffectiveness, top, new FormAttachment(0, 5));

			lastControl = trippleEffectiveness;
		}

		//////////////////////////////////////////////////////////////////

		top = lastControl == null ? new FormAttachment(0, 5) : new FormAttachment(lastControl, 10);
		randomizeRewards = createButton(container, SWT.CHECK, getRewardText(gameType), getRewardTooltip(gameType));
		layout(randomizeRewards, top, new FormAttachment(0, 5));

		Control previousControl = randomizeRewards;

		if (gameType == GameType.FE9) {
			rewardModeContainer = new Composite(container, SWT.NONE);
			rewardModeContainer.setLayout(new FormLayout());
			layout(rewardModeContainer, new FormAttachment(randomizeRewards, 5),
					new FormAttachment(randomizeRewards, 5, SWT.LEFT));

			similarRewardsButton = createButton(rewardModeContainer, SWT.RADIO, "Similar Replacements",
					"Replaces rewards with those of a similar type.\ne.g. Weapons are replaced with weapons, stat boosters are replaced with other stat boosters, etc.",
					false, true);
			rewardMode = RewardMode.SIMILAR;
			defaultLayout(similarRewardsButton);

			randomRewardsButton = createButton(rewardModeContainer, SWT.RADIO, "Random Replacements",
					"Replaces rewards with anything.");
			layout(randomRewardsButton, new FormAttachment(similarRewardsButton, 5), new FormAttachment(0, 0));

			similarRewardsButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					rewardMode = RewardMode.SIMILAR;
				}
			});

			randomRewardsButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					rewardMode = RewardMode.RANDOM;
				}
			});

			randomRewardsButton.setEnabled(false);

			randomizeRewards.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					similarRewardsButton.setEnabled(randomizeRewards.getSelection());
					randomRewardsButton.setEnabled(randomizeRewards.getSelection());
				}
			});

			previousControl = rewardModeContainer;
		}

		if (gameType.isGBA()) {
			singleRNButton = createButton(container, SWT.CHECK, "Enable Single RN for Hit",
					"Makes accuracy rolls based on a single random number instead of the average of two random numbers.\n\nGood for those that don't like being lied to about hit rates.",
					true, false);
			layout(singleRNButton, new FormAttachment(previousControl, 10), new FormAttachment(0, 5));

			previousControl = singleRNButton;
		}

		// Random enemy drops
		if (gameType == GameType.FE9 || gameType == GameType.FE7 || gameType == GameType.FE8) {
			enemyDropsButton = createButton(container, SWT.CHECK, "Add Random Enemy Drops",
					"Gives a chance for random minions to drop weapons or a random item.", true, false);
			layout(enemyDropsButton, new FormAttachment(previousControl, 10), new FormAttachment(0, 5));

			enemyDropChanceSpinner = createSpinner(container, new int[] { 10, 1, 100, 0, 1, 5 }, false);
			layout(enemyDropChanceSpinner, new FormAttachment(enemyDropsButton, 5), null, new FormAttachment(100, -5));

			enemyDropChanceLabel = createLabel(container, SWT.RIGHT, "Chance: ");
			layout(enemyDropChanceLabel, new FormAttachment(enemyDropChanceSpinner, 0, SWT.CENTER), null,
					new FormAttachment(enemyDropChanceSpinner, -5));

			enemyDropsButton.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					enemyDropChanceSpinner.setEnabled(enemyDropsButton.getSelection());
					enemyDropChanceLabel.setEnabled(enemyDropsButton.getSelection());
				}
			});

			previousControl = enemyDropChanceSpinner;
		}

		// Fog of War
		if (gameType.isGBA()) {
			addFogButton = createButton(container, SWT.CHECK, "Add Fog of War (Beta)", getAddFogToolTip(gameType), true,
					false);
			layout(addFogButton, new FormAttachment(previousControl, 10), new FormAttachment(0, 5));
			addFogButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					// TODO Auto-generated method stub
					fogChanceSpinner.setEnabled(addFogButton.getSelection());
					fogChanceLabel.setEnabled(addFogButton.getSelection());
					fogVisionRangeControl.setEnabled(addFogButton.getSelection());
				}
			});

			fogChanceSpinner = createSpinner(container, SWT.CHECK, new int[] { 10, 1, 100, 0, 1, 1 }, null, false);
			layout(fogChanceSpinner, new FormAttachment(addFogButton, 5), null, new FormAttachment(100, -5));

			fogChanceLabel = createLabel(container, "Fog of War Chance:");
			layout(fogChanceLabel, new FormAttachment(fogChanceSpinner, 0, SWT.CENTER), null,
					new FormAttachment(fogChanceSpinner, -5));

			fogVisionRangeControl = new MinMaxControl(container, SWT.NONE, "Vision Range", "~");
			fogVisionRangeControl.setMin(3);
			fogVisionRangeControl.getMinSpinner().setValues(3, 1, 6, 0, 1, 1);
			fogVisionRangeControl.setMax(6);
			fogVisionRangeControl.getMaxSpinner().setValues(6, 3, 15, 0, 1, 1);
			fogVisionRangeControl.setEnabled(false);
			layout(fogVisionRangeControl, new FormAttachment(fogChanceSpinner, 5),
					new FormAttachment(addFogButton, 5, SWT.LEFT), new FormAttachment(100, 0));

			previousControl = fogVisionRangeControl;
		}

		if (gameType == GameType.FE4)

		{
			followupRequirement = createButton(container, SWT.CHECK, "Remove Pursuit Follow-up Requirement",
					"Modifies the battle system so that the Pursuit skill is not needed to make follow-up attacks.",
					true, false);
			layout(followupRequirement, new FormAttachment(previousControl, 10), new FormAttachment(0, 5));
			withoutPursuitSpinner = createSpinner(container, new int[] { 6, 1, 10, 0, 1, 1 },
					"Sets the minimum Attack Speed advantage needed to perform follow-up attacks without the Pursuit skill.",
					false);
			layout(withoutPursuitSpinner, new FormAttachment(followupRequirement, 5), null,
					new FormAttachment(100, -5));

			withoutPursuitLabel = createLabel(container, "AS Threshold w/o Pursuit:");
			layout(withoutPursuitLabel, new FormAttachment(withoutPursuitSpinner, 0, SWT.CENTER), null,
					new FormAttachment(withoutPursuitSpinner, -5));

			createSpinner(container, new int[] { 3, 1, 10, 0, 1, 1 },
					"Sets the minimum Attack Speed advantage needed to perform follow-up attacks with the Pursuit skill.",
					false);
			layout(withPursuitSpinner, new FormAttachment(withoutPursuitSpinner, 5), null, new FormAttachment(100, -5));

			createLabel(container, "AS Threshold w/ Pursuit:");
			layout(withPursuitLabel, new FormAttachment(withPursuitSpinner, 0, SWT.CENTER), null,
					new FormAttachment(withPursuitSpinner, -5));
			followupRequirement.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					withoutPursuitLabel.setEnabled(followupRequirement.getSelection());
					withoutPursuitSpinner.setEnabled(followupRequirement.getSelection());
					withPursuitLabel.setEnabled(followupRequirement.getSelection());
					withPursuitSpinner.setEnabled(followupRequirement.getSelection());
				}
			});
		}
	}

	private String getAddFogToolTip(GameType gameType) {
		if (gameType == GameType.FE7) {
			return "Adds a chance for maps to feature fog of war.\n\nMaps featuring Kishuna are exempt.";
		} else {
			return "Adds a chance for maps to have fog of war.";
		}
	}

	private String getRewardTooltip(GameType gameType) {
		if (gameType == GameType.FE4) {
			return "Every instance of an obtainable ring is randomized to a different kind of ring.";
		} else {
			return "Rewards from chests, villages, and story events will now give out random rewards. Plot-important promotion items are excluded.";
		}
	}

	private String getRewardText(GameType gameType) {
		return gameType == GameType.FE4 ? "Randomize Rings" : "Randomize Rewards";

	}

	private String getTranslationPatchTooltip(GameType gameType) {
		String toolTip = null;
		switch (gameType) {
		case FE4:
			toolTip = "Applies the Project Naga localization patch.";
			break;
		case FE6:
			toolTip = "Applies the FE6 Localization Patch v1.1.1.";
			break;
		default:
			break;
		}
		return toolTip;
	}

	public void setPatchingEnabled(boolean patchingEnabled) {
		if (applyEnglishPatch == null) {
			return;
		}
		if (patchingEnabled) {
			applyEnglishPatch.setEnabled(true);
		} else {
			applyEnglishPatch.setEnabled(false);
			applyEnglishPatch.setSelection(false);
		}
	}

	public MiscellaneousOptions getMiscellaneousOptions() {
		if (type.isGBA()) {
			switch (type) {
			case FE6:
				return new MiscellaneousOptions(applyEnglishPatch.getSelection(), randomizeRewards.getSelection(),
						false, singleRNButton.getSelection(), addFogButton.getSelection(),
						fogChanceSpinner.getSelection(), fogVisionRangeControl.getMinMaxOption());
			case FE7:
			default:
				return new MiscellaneousOptions(randomizeRewards.getSelection(),
						enemyDropsButton.getSelection() ? enemyDropChanceSpinner.getSelection() : 0,
						trippleEffectiveness != null ? trippleEffectiveness.getSelection() : false,
						singleRNButton.getSelection(), addFogButton.getSelection(), fogChanceSpinner.getSelection(),
						fogVisionRangeControl.getMinMaxOption());
			}
		} else if (type.isSFC()) {
			switch (type) {
			case FE4:
				return new MiscellaneousOptions(applyEnglishPatch.getSelection(), randomizeRewards.getSelection(),
						new MiscellaneousOptions.FollowupRequirement(!followupRequirement.getSelection(),
								withPursuitSpinner.getSelection(), withoutPursuitSpinner.getSelection()));
			default:
				return new MiscellaneousOptions(false, 0, false, false, false, 0, null);
			}
		} else if (type.isGCN()) {
			return new MiscellaneousOptions(false, randomizeRewards.getSelection(), rewardMode,
					enemyDropsButton.getSelection() ? enemyDropChanceSpinner.getSelection() : 0);
		}
		return new MiscellaneousOptions(false, 0, false, false, false, 0, null);
	}

	public void setMiscellaneousOptions(MiscellaneousOptions options) {
		if (options == null) {
			return; // shouldn't happen
		}

		if (applyEnglishPatch != null) {
			applyEnglishPatch.setSelection(options.applyEnglishPatch);
		}
		if (trippleEffectiveness != null) {
			trippleEffectiveness.setSelection(options.tripleEffectiveness);
		}
		if (randomizeRewards != null) {
			randomizeRewards.setSelection(options.randomizeRewards);
		}

		if (similarRewardsButton != null) {
			similarRewardsButton.setSelection(options.rewardMode == RewardMode.SIMILAR);
			similarRewardsButton.setEnabled(options.randomizeRewards);
		}
		if (randomRewardsButton != null) {
			randomRewardsButton.setSelection(options.rewardMode == RewardMode.RANDOM);
			randomRewardsButton.setEnabled(options.randomizeRewards);
		}

		if (singleRNButton != null) {
			singleRNButton.setSelection(options.singleRNMode);
		}

		if (enemyDropsButton != null && enemyDropChanceSpinner != null) {
			enemyDropsButton.setSelection(options.enemyDropChance > 0);
			enemyDropChanceSpinner.setEnabled(options.enemyDropChance > 0);
			enemyDropChanceLabel.setEnabled(options.enemyDropChance > 0);
			if (options.enemyDropChance > 0) {
				enemyDropChanceSpinner.setSelection(options.enemyDropChance);
			}
		}

		if (addFogButton != null) {
			addFogButton.setSelection(options.randomizeFogOfWar);

			fogChanceSpinner.setEnabled(true);
			fogChanceLabel.setEnabled(true);
			fogVisionRangeControl.setEnabled(true);

			fogChanceSpinner.setSelection(options.fogOfWarChance);
			fogVisionRangeControl.setMin(options.fogOfWarVisionRange.minValue);
			fogVisionRangeControl.setMax(options.fogOfWarVisionRange.maxValue);
		}
	}
}
