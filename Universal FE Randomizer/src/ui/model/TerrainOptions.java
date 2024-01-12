package ui.model;

import fedata.gba.general.TerrainTable;
import fedata.general.FEBase;
import util.recordkeeper.RecordKeeper;

import java.util.Arrays;

import static fedata.gba.general.TerrainTable.TerrainTableType.*;

public class TerrainOptions implements RecordableOption {

    /*
     * Healing Related Settings
     */
    public boolean randomizeHealing = false;
    public int healingChance = 0;
    public MinMaxOption healingRange = null;

    /*
     * Status recovery Related Settings
     */
    public boolean randomizeStatusRecovery = false;
    public int statusRestoreChance = 0;

    /*
     * avoid Related Settings
     */
    public boolean randomizeAvoid = false;
    public MinMaxOption avoidRange = null;
    public int avoidChance = 0;

    /*
     * Defense Related Settings
     */
    public boolean randomizeDef = false;
    public MinMaxOption defRange = null;
    public int defChance = 0;
    /*
     * Resistence Related Settings
     */
    public boolean randomizeRes = false;
    public MinMaxOption resRange = null;
    public int resChance = 0;
    /*
     * Movement Related Settings
     */
    public final boolean randomizeMovementCost = false;
    public final MinMaxOption movementCostRange = null;

    /*
     * Flag to keep tiles that didn't use to have effects safe
     */
    public boolean keepSafeTiles = false;

    /*
     * Chance for a tile to get some kind of effect, otherwise it stays as it is in vanilla
     */
    public int effectChance = 0;

    public boolean enabled = false;

    // Constructor for Tests
    public TerrainOptions() {

    }

    /**
     * Constructor for usage from the GUI.
     */
    public TerrainOptions(boolean enabled, boolean randomizeHealing, int healingChance, MinMaxOption healingRange,
                          boolean randomizeStatusRecovery, int statusRestoreChance,
                          boolean randomizeAvoid, MinMaxOption avoidRange, int avoidChance,
                          boolean randomizeDef, MinMaxOption defRange, int defChance,
                          boolean randomizeRes, MinMaxOption resRange, int resChance,
                          boolean keepSafeTiles, int effectChance) {
        this.enabled = enabled;
        this.randomizeHealing = randomizeHealing;
        this.healingChance = healingChance;
        this.healingRange = healingRange;
        this.randomizeStatusRecovery = randomizeStatusRecovery;
        this.statusRestoreChance = statusRestoreChance;
        this.randomizeAvoid = randomizeAvoid;
        this.avoidRange = avoidRange;
        this.avoidChance = avoidChance;
        this.randomizeDef = randomizeDef;
        this.defRange = defRange;
        this.defChance = defChance;
        this.randomizeRes = randomizeRes;
        this.resRange = resRange;
        this.resChance = resChance;
        this.keepSafeTiles = keepSafeTiles;
        this.effectChance = effectChance;
    }

    @Override
    public void record(RecordKeeper rk, FEBase.GameType type) {
        if (!enabled) {
            rk.addHeaderItem("Terrain Bonus Randomization", "NO");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Effect chance: ").append(effectChance).append("<br>");
        sb.append("Keep safe tiles: ").append(keepSafeTiles ? "YES" : "NO").append("<br>");

        sb.append("Avoid Randomized? ").append(randomizeAvoid ? "YES" : "NO");
        if (randomizeAvoid) {
            sb.append(" Avoid Chance: ").append(avoidChance).append(" Min Avoid: ").append(avoidRange.minValue).append(" Max Avoid: ").append(avoidRange.minValue);
        }
        sb.append("<br>");

        sb.append("Def Randomized? ").append(randomizeDef ? "YES" : "NO");
        if (randomizeDef) {
            sb.append(" Defense Chance: ").append(defChance).append(" Min Defense: ").append(defRange.minValue).append(" Max Defense: ").append(defRange.minValue);
        }
        sb.append("<br>");

        sb.append("Res Randomized? ").append(randomizeRes ? "YES" : "NO");
        if (randomizeRes) {
            sb.append(" Resistance Chance: ").append(resChance).append(" Min Resistance: ").append(resRange.minValue).append(" Max Resistance: ").append(resRange.minValue);
        }
        sb.append("<br>");

        sb.append("Healing Randomized? ").append(randomizeHealing ? "YES" : "NO");
        if (randomizeHealing) {
            sb.append(" Healing Chance: ").append(healingChance).append(" Min Healing: ").append(healingRange.minValue).append(" Max Healing: ").append(healingRange.minValue);
        }
        sb.append("<br>");
        sb.append("Status Recovery Randomized? ").append(randomizeStatusRecovery ? "YES" : "NO");
        if (randomizeHealing) {
            sb.append(" Status Recovery Chance: ").append(statusRestoreChance);
        }

        rk.addHeaderItem("Terrain Bonus Randomization", sb.toString());
    }
}
