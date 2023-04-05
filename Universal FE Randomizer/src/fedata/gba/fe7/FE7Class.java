package fedata.gba.fe7;

import java.util.Arrays;

import fedata.gba.GBAFEClassData;

import fedata.general.FEBase.GameType;

public class FE7Class extends GBAFEClassData {
	
	public FE7Class(GBAFEClassData reference) {
		super(GameType.FE7);
		this.originalData = Arrays.copyOf(reference.getData(), reference.getData().length);
		this.data = Arrays.copyOf(reference.getData(), reference.getData().length);
	}

	public FE7Class(byte[] data, long originalOffset) {
		super(GameType.FE7);
		this.originalData = data;
		this.data = data;
		this.originalOffset = originalOffset;
	}
	


	@Override
	public GBAFEClassData createClone() {
		FE7Class clone = new FE7Class(this);
		clone.setOriginalOffset(-1);
		return clone;
	}
}
