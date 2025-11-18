package ui.model;

import fedata.gba.GBAFECharacterData;
import fedata.gba.GBAFEStatDto;
import fedata.general.FEBase.GameType;
import random.general.RelativeValueMapper;
import util.recordkeeper.RecordKeeper;

import java.util.Arrays;
import java.util.List;

public class AutolevelingParameters implements RecordableOption {
	public enum GrowthAdjustmentMode {
		USE_FILL, USE_SLOT, RELATIVE_TO_SLOT;

        public GBAFEStatDto getGrowthsByMode(GBAFECharacterData slot, GBAFECharacterData fill) {
            return getGrowthsByMode(slot.getGrowths(), fill.getGrowths());
        }
        public GBAFEStatDto getGrowthsByMode(GBAFEStatDto slot, GBAFEStatDto fill) {
            switch(this) {
                case USE_SLOT:
                    return slot;
                case RELATIVE_TO_SLOT:
                    List<Integer> mappedStats = RelativeValueMapper.mappedValues(Arrays.asList(slot.hp, slot.str, slot.skl, slot.spd, slot.def, slot.res, slot.lck),
                            Arrays.asList(fill.hp, fill.str, fill.skl, fill.spd, fill.def, fill.res, fill.lck));
                    return new GBAFEStatDto(mappedStats.get(0), mappedStats.get(1), mappedStats.get(2), mappedStats.get(3), mappedStats.get(4), mappedStats.get(5), mappedStats.get(6));
                case USE_FILL:
                default:
                    return fill;
            }
        }
	}
	public enum StatAdjustmentMode {
		AUTOLEVEL, MATCH_SLOT, RELATIVE_TO_SLOT;
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
