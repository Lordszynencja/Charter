package log.charter.services.mouseAndKeyboard;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Shortcut {
	public static Shortcut fromName(final String name) {
		final String[] nameParts = name.split(" ");
		if (nameParts.length == 4 && (nameParts[0].equals("T") || nameParts[0].equals("F"))) {
			return new Shortcut(nameParts[0].equals("T"), nameParts[1].equals("T"), nameParts[2].equals("T"), false,
					Integer.valueOf(nameParts[3]));
		}

		final Shortcut shortcut = new Shortcut();

		for (int i = 0; i < nameParts.length - 1; i++) {
			switch (nameParts[i]) {
				case "Ctrl" -> shortcut.ctrl = true;
				case "Shift" -> shortcut.shift = true;
				case "Alt" -> shortcut.alt = true;
				case "Cmd" -> shortcut.command = true;
				case "Insert" -> shortcut.insert = true;
			}
		}

		shortcut.key = Integer.valueOf(nameParts[nameParts.length - 1]);
		if (shortcut.key == KeyEvent.VK_INSERT) {
			shortcut.key = -1;
			shortcut.insert = true;
		}

		return shortcut;
	}

	public boolean ctrl = false;
	public boolean shift = false;
	public boolean alt = false;
	public boolean command = false;
	public boolean insert = false;
	public int key = -1;// key code from KeyEvent

	public Shortcut() {
	}

	public Shortcut(final Shortcut other) {
		ctrl = other.ctrl;
		shift = other.shift;
		alt = other.alt;
		command = other.command;
		insert = other.insert;
		key = other.key;
	}

	private Shortcut(final boolean ctrl, final boolean shift, final boolean alt, final boolean insert, final int key) {
		this.ctrl = ctrl;
		this.shift = shift;
		this.alt = alt;
		this.insert = insert;
		this.key = key;
	}

	public Shortcut(final int key) {
		this(false, false, false, false, key);
	}

	public Shortcut ctrl() {
		ctrl = true;
		return this;
	}

	public Shortcut shift() {
		shift = true;
		return this;
	}

	public Shortcut alt() {
		alt = true;
		return this;
	}

	public Shortcut insert() {
		insert = true;
		return this;
	}

	public Shortcut key(final int key) {
		this.key = key;
		return this;
	}

	public boolean isReady() {
		return key != -1;
	}

	public String name(final String joiner) {
		final List<String> parts = new ArrayList<>();

		if (ctrl) {
			parts.add("Ctrl");
		}
		if (shift) {
			parts.add("Shift");
		}
		if (alt) {
			parts.add("Alt");
		}
		if (insert) {
			parts.add("Insert");
		}
		if (command) {
			parts.add("Cmd");
		}
		if (key != -1) {
			parts.add(KeyEvent.getKeyText(key));
		}

		return String.join(joiner, parts);
	}

	public String saveName() {
		final StringBuilder b = new StringBuilder();
		if (ctrl) {
			b.append("Ctrl ");
		}
		if (shift) {
			b.append("Shift ");
		}
		if (alt) {
			b.append("Alt ");
		}
		if (insert) {
			b.append("Insert ");
		}
		if (command) {
			b.append("Cmd ");
		}

		b.append(key);

		return b.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(alt, ctrl, key, shift);
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

		final Shortcut other = (Shortcut) obj;
		return ctrl == other.ctrl && shift == other.shift && alt == other.alt && insert == other.insert
				&& key == other.key;
	}

	@Override
	public String toString() {
		return name("-");
	}
}
