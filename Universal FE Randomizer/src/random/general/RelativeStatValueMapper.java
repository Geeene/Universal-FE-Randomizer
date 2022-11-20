package random.general;

import java.util.List;

/**
 * Same as the {@link RelativeValueMapper} except it directly wraps the returned list in a {@link GBAStatDAO} for ease of use.
 */
public class RelativeStatValueMapper {
	
	public static GBAStatDAO mappedValues(List<Integer> reference, List<Integer> input) {
		if(reference.size() != 7 || input.size()!= 7)
			throw new IllegalArgumentException();
		List<Integer> integers = RelativeValueMapper.mappedValues(reference, input);
		return new GBAStatDAO(integers);
	}

}