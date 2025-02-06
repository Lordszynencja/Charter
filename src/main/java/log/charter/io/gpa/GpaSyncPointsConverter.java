package log.charter.io.gpa;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class GpaSyncPointsConverter implements SingleValueConverter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return true;
	}

	@Override
	public String toString(final Object obj) {
		return "";
	}

	@Override
	public List<GpaSyncPoint> fromString(final String str) {
		final List<GpaSyncPoint> syncPoints = new ArrayList<>();

		for (final String syncPointData : str.split("#")) {
			final String[] tokens = syncPointData.split(";");
			if (tokens.length < 4) {
				continue;
			}

			final double trackTime = Double.valueOf(tokens[0]);
			final int bar = Integer.valueOf(tokens[1]);
			final double positionInBar = Double.valueOf(tokens[2]);
			final double beatLength = Double.valueOf(tokens[3]);
			syncPoints.add(new GpaSyncPoint(trackTime, bar, positionInBar, beatLength));
		}

		return syncPoints;
	}

}
