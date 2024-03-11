package log.charter.util.collections;

import java.util.Objects;

public class Pair<A, B> {
	public A a;
	public B b;

	public Pair(final A a, final B b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pair<?, ?> other = (Pair<?, ?>) obj;
		return Objects.equals(a, other.a) && Objects.equals(b, other.b);
	}

}