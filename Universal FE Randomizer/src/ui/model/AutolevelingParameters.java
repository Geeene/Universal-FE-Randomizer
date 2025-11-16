package ui.model;

import fedata.general.FEBase.GameType;
import util.recordkeeper.RecordKeeper;

public class AutolevelingParameters implements RecordableOption {
	public enum GrowthAdjustmentMode {
		USE_FILL, USE_SLOT, RELATIVE_TO_SLOT
	}
	public enum StatAdjustmentMode {
		AUTOLEVEL, MATCH_SLOT, RELATIVE_TO_SLOT
	}
	public enum BaseStatAutolevelType {
		USE_ORIGINAL, USE_NEW
	}

	public final GrowthAdjustmentMode growthMode;
	public final StatAdjustmentMode baseMode;
	public final BaseStatAutolevelType autolevelMode;
	public AutolevelingParameters(GrowthAdjustmentMode growthMode, StatAdjustmentMode baseMode,
                                  BaseStatAutolevelType autolevel) {
		this.growthMode = growthMode;
		this.baseMode = baseMode;
		this.autolevelMode = autolevel;
	}

	@Override
	public void record(RecordKeeper rk, GameType type) {
		StringBuilder sb = new StringBuilder();


		switch (growthMode) {
		case USE_FILL:
			sb.append("Use Fill Growths<br>");
			break;
		case USE_SLOT:
			sb.append("Use Slot Growths<br>");
			break;
		case RELATIVE_TO_SLOT:
			sb.append("Use Slot Relative Growths<br>");
			break;
		}

		switch (baseMode) {
		case AUTOLEVEL:
			sb.append("Autolevel Base Stats<br>");
			switch (autolevelMode) {
			case USE_ORIGINAL:
				sb.append("Autolevel w/ Original Growths");
				break;
			case USE_NEW:
				sb.append("Autolevel w/ New Growths");
				break;
			}
			break;
		case RELATIVE_TO_SLOT:
			sb.append("Relative Base Stats");
			break;
		case MATCH_SLOT:
			sb.append("Match Base Stats");
			break;
		}

		rk.addHeaderItem("Randomize Recruitment", sb.toString());
	}
}
