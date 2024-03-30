package log.charter.services.data.selection;

public class Selection<T> {
	public final int id;
	public final T selectable;

	public Selection(final int id, final T selectable) {
		this.id = id;
		this.selectable = selectable;
	}

	@SuppressWarnings("unchecked")
	public <A> A get() {
		return (A) selectable;
	}
}