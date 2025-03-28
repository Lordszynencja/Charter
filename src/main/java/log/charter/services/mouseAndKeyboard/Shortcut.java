package log.charter.services.mouseAndKeyboard;

import java.awt.event.KeyEvent;
import java.util.Objects;

public class Shortcut {
	public static Shortcut fromName(final String name) {
		final String[] nameParts = name.split(" ");
		if (nameParts.length == 4 && (nameParts[0].equals("T") || nameParts[0].equals("F"))) {
			return new Shortcut(nameParts[0].equals("T"), nameParts[1].equals("T"), nameParts[2].equals("T"),
					Integer.valueOf(nameParts[3]));
		}

		final Shortcut shortcut = new Shortcut();

		for (int i = 0; i < nameParts.length - 1; i++) {
			switch (nameParts[i]) {
				case "Ctrl" -> shortcut.ctrl = true;
				case "Shift" -> shortcut.shift = true;
				case "Alt" -> shortcut.alt = true;
				case "Cmd" -> shortcut.command = true;
			}
		}

		shortcut.key = Integer.valueOf(nameParts[nameParts.length - 1]);

		return shortcut;
	}

	public boolean ctrl = false;
	public boolean shift = false;
	public boolean alt = false;
	public boolean command = false;
	public int key = -1;// key code from KeyEvent

	public Shortcut() {
	}

	public Shortcut(final Shortcut other) {
		ctrl = other.ctrl;
		shift = other.shift;
		alt = other.alt;
		command = other.command;
		key = other.key;
	}

	public Shortcut(final boolean ctrl, final boolean shift, final boolean alt, final int key) {
		this.ctrl = ctrl;
		this.shift = shift;
		this.alt = alt;
		this.key = key;
	}

	public Shortcut(final int key) {
		this(false, false, false, key);
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

	public Shortcut key(final int key) {
		this.key = key;
		return this;
	}

	public boolean isReady() {
		return key != -1;
	}

	public String name(final String joiner) {
		final StringBuilder nameBuilder = new StringBuilder();
		if (ctrl) {
			nameBuilder.append("Ctrl").append(joiner);
		}
		if (shift) {
			nameBuilder.append("Shift").append(joiner);
		}
		if (alt) {
			nameBuilder.append("Alt").append(joiner);
		}
		if (command) {
			nameBuilder.append("Cmd").append(joiner);
		}
		if (key != -1) {
			nameBuilder.append(KeyEvent.getKeyText(key));
		}

		return nameBuilder.toString();
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
		return ctrl == other.ctrl && shift == other.shift && alt == other.alt && key == other.key;
	}

	@Override
	public String toString() {
		return name("-");
	}
}
