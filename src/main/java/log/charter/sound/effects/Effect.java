package log.charter.sound.effects;

public interface Effect {
	public static final Effect emptyEffect = new Effect() {
		@Override
		public float apply(final int channel, final float sample) {
			return sample;
		}
	};

	float apply(final int channel, final float sample);
}
