package log.charter.gui.components.simple;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComboBox;

import log.charter.data.config.ChartPanelColors.ColorLabel;

public class CharterSelect<T> extends JComboBox<CharterSelect.ItemHolder<T>> {
	public static class ItemHolder<T> {
		public final T item;
		private final String label;

		public ItemHolder(final T item) {
			this.item = item;
			this.label = item.toString();
		}

		public ItemHolder(final T item, final String label) {
			this.item = item;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static <T> Vector<ItemHolder<T>> pack(final List<T> collection, final Function<T, String> labelGenerator) {
		return collection.stream().map(e -> new ItemHolder<>(e, labelGenerator.apply(e)))
				.collect(Collectors.toCollection(Vector::new));
	}

	private static <T> Function<T, String> defaultLabelGenerator() {
		return v -> v == null ? "" : v.toString();
	}

	private static final long serialVersionUID = 1L;

	private final List<T> items;

	public CharterSelect(final List<T> items, final T item, final Function<T, String> labelGenerator,
			final Consumer<T> onPick) {
		super(pack(items, labelGenerator == null ? defaultLabelGenerator() : labelGenerator));

		this.items = items;
		setSelectedIndex(findSelectedIndex(item));

		if (onPick != null) {
			addActionListener(e -> onPick.accept(getSelectedValue()));
		}

		setBackground(ColorLabel.BASE_BUTTON.color());
	}

	public CharterSelect(final List<T> items, final T item) {
		this(items, item, null, null);
	}

	public CharterSelect(final Stream<T> items, final T item, final Function<T, String> labelGenerator,
			final Consumer<T> onPick) {
		this(items.toList(), item, labelGenerator, onPick);
	}

	public CharterSelect(final T[] items, final T item, final Function<T, String> labelGenerator,
			final Consumer<T> onPick) {
		this(asList(items), item, labelGenerator, onPick);
	}

	private int findSelectedIndex(final T item) {
		for (int i = 0; i < items.size(); i++) {
			if (Objects.equals(items.get(i), item)) {
				return i;
			}
		}

		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemHolder<T> getSelectedItem() {
		return (ItemHolder<T>) super.getSelectedItem();
	}

	public T getSelectedValue() {
		final ItemHolder<T> selected = getSelectedItem();
		return selected == null ? null : selected.item;
	}

	public void setSelectedValue(final T value) {
		setSelectedIndex(findSelectedIndex(value));
	}
}
