package log.charter.gui.components.simple;

import java.util.Collection;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComboBox;

public class CharterSelect<T> extends JComboBox<T> {
	private static final long serialVersionUID = 1L;

	private static <T> int findSelectedIndex(final T[] items, final T item) {
		for (int i = 0; i < items.length; i++) {
			if (Objects.equals(items[i], item)) {
				return i;
			}
		}

		return 0;
	}

	private static <T> int findSelectedIndex(final Vector<T> items, final T item) {
		for (int i = 0; i < items.size(); i++) {
			if (Objects.equals(items.get(i), item)) {
				return i;
			}
		}

		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getSelectedItem() {
		return (T) super.getSelectedItem();
	}

	public CharterSelect(final Vector<T> items, final T item) {
		super(items);

		setSelectedIndex(findSelectedIndex(items, item));
	}

	public CharterSelect(final Vector<T> items, final T item, final Consumer<T> onPick) {
		this(items, item);

		addActionListener(e -> onPick.accept(getSelectedItem()));
	}

	public CharterSelect(final T[] items, final T item) {
		super(items);

		setSelectedIndex(findSelectedIndex(items, item));
	}

	public CharterSelect(final T[] items, final T item, final Consumer<T> onPick) {
		this(items, item);

		addActionListener(e -> onPick.accept(getSelectedItem()));
	}

	public CharterSelect(final Stream<T> items, final T item) {
		this((Vector<T>) items.collect(Collectors.toCollection(Vector::new)), item);
	}

	public CharterSelect(final Stream<T> items, final T item, final Consumer<T> onPick) {
		this((Vector<T>) items.collect(Collectors.toCollection(Vector::new)), item, onPick);
	}

	public CharterSelect(final Collection<T> items, final T item) {
		this(new Vector<>(items), item);
	}

	public CharterSelect(final Collection<T> items, final T item, final Consumer<T> onPick) {
		this(new Vector<>(items), item, onPick);
	}

}
