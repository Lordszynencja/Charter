package log.charter.gui.components.simple;

import java.awt.font.TextAttribute;

import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;
import javax.swing.text.StyleConstants;
import javax.swing.undo.UndoableEdit;

/**
 * copied from FonrUtilities
 *
 */
public class ComponentDocument extends PlainDocument {
	public static final int MIN_LAYOUT_CHARCODE = 0x0300;
	public static final int MAX_LAYOUT_CHARCODE = 0x206F;
	public static final int HI_SURROGATE_START = 0xD800;
	public static final int LO_SURROGATE_END = 0xDFFF;

	static boolean isComposedTextElement(final Document doc, final int offset) {
		Element elem = doc.getDefaultRootElement();
		while (!elem.isLeaf()) {
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		return isComposedTextElement(elem);
	}

	static boolean isComposedTextElement(final Element elem) {
		final AttributeSet as = elem.getAttributes();
		return isComposedTextAttributeDefined(as);
	}

	static boolean isComposedTextAttributeDefined(final AttributeSet as) {
		return ((as != null) && (as.isDefined(StyleConstants.ComposedTextAttribute)));
	}

	public static boolean isComplexText(final char[] chs, final int start, final int limit) {

		for (int i = start; i < limit; i++) {
			if (chs[i] < MIN_LAYOUT_CHARCODE) {
				continue;
			} else if (isNonSimpleChar(chs[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNonSimpleChar(final char ch) {
		return isComplexCharCode(ch) || (ch >= HI_SURROGATE_START && ch <= LO_SURROGATE_END);
	}

	public static boolean isComplexCharCode(final int code) {

		if (code < MIN_LAYOUT_CHARCODE || code > MAX_LAYOUT_CHARCODE) {
			return false;
		} else if (code <= 0x036f) {
			// Trigger layout for combining diacriticals 0x0300->0x036f
			return true;
		} else if (code < 0x0590) {
			// No automatic layout for Greek, Cyrillic, Armenian.
			return false;
		} else if (code <= 0x06ff) {
			// Hebrew 0590 - 05ff
			// Arabic 0600 - 06ff
			return true;
		} else if (code < 0x0900) {
			return false; // Syriac and Thaana
		} else if (code <= 0x0e7f) {
			// if Indic, assume shaping for conjuncts, reordering:
			// 0900 - 097F Devanagari
			// 0980 - 09FF Bengali
			// 0A00 - 0A7F Gurmukhi
			// 0A80 - 0AFF Gujarati
			// 0B00 - 0B7F Oriya
			// 0B80 - 0BFF Tamil
			// 0C00 - 0C7F Telugu
			// 0C80 - 0CFF Kannada
			// 0D00 - 0D7F Malayalam
			// 0D80 - 0DFF Sinhala
			// 0E00 - 0E7F if Thai, assume shaping for vowel, tone marks
			return true;
		} else if (code < 0x0f00) {
			return false;
		} else if (code <= 0x0fff) { // U+0F00 - U+0FFF Tibetan
			return true;
		} else if (code < 0x10A0) { // U+1000 - U+109F Myanmar
			return true;
		} else if (code < 0x1100) {
			return false;
		} else if (code < 0x11ff) { // U+1100 - U+11FF Old Hangul
			return true;
		} else if (code < 0x1780) {
			return false;
		} else if (code <= 0x17ff) { // 1780 - 17FF Khmer
			return true;
		} else if (code < 0x200c) {
			return false;
		} else if (code <= 0x200d) { // zwj or zwnj
			return true;
		} else if (code >= 0x202a && code <= 0x202e) { // directional control
			return true;
		} else if (code >= 0x206a && code <= 0x206f) { // directional control
			return true;
		}
		return false;
	}

	private static final long serialVersionUID = 1L;

	public ComponentDocument() {
		super(new StringContent());
	}

	private void quietRemove(final int offset, final int length) throws BadLocationException {
		if (length > 0) {
			if (offset < 0 || (offset + length) > getLength()) {
				throw new BadLocationException("Invalid remove", getLength() + 1);
			}
			final DefaultDocumentEvent chng = new DefaultDocumentEvent(offset, length, EventType.REMOVE);

			boolean isComposedTextElement;
			isComposedTextElement = isComposedTextElement(this, offset);

			removeUpdate(chng);
			final UndoableEdit u = getContent().remove(offset, length);
			if (u != null) {
				chng.addEdit(u);
			}
			postRemoveUpdate(chng);

			chng.end();

			if ((u != null) && !isComposedTextElement) {
				fireUndoableEditUpdate(new UndoableEditEvent(this, chng));
			}
		}
	}

	private String filterNewLines(String str) {
		final Object filterNewlines = getProperty("filterNewlines");
		if ((filterNewlines instanceof Boolean) && filterNewlines.equals(Boolean.TRUE)) {
			if ((str != null) && (str.indexOf('\n') >= 0)) {
				final StringBuilder filtered = new StringBuilder(str);
				final int n = filtered.length();
				for (int i = 0; i < n; i++) {
					if (filtered.charAt(i) == '\n') {
						filtered.setCharAt(i, ' ');
					}
				}
				str = filtered.toString();
			}
		}

		return str;
	}

	private void putI18NProperty(final String str) {
		if (getProperty("i18n").equals(Boolean.FALSE)) {
			final Object d = getProperty(TextAttribute.RUN_DIRECTION);
			if ((d != null) && (d.equals(TextAttribute.RUN_DIRECTION_RTL))) {
				putProperty("i18n", Boolean.TRUE);
			} else {
				final char[] chars = str.toCharArray();
				if (isComplexText(chars, 0, chars.length)) {
					putProperty("i18n", Boolean.TRUE);
				}
			}
		}
	}

	public void quietInsertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
		str = filterNewLines(str);

		if ((str == null) || (str.length() == 0)) {
			return;
		}
		if (offs > getLength()) {
			throw new BadLocationException("Invalid insert", getLength());
		}

		writeLock();

		try {
			if ((str == null) || (str.length() == 0)) {
				return;
			}
			final UndoableEdit u = getContent().insertString(offs, str);
			final DefaultDocumentEvent e = new DefaultDocumentEvent(offs, str.length(), EventType.INSERT);
			if (u != null) {
				e.addEdit(u);
			}

			putI18NProperty(str);

			insertUpdate(e, a);

			e.end();

			if (u != null && (a == null || !a.isDefined(StyleConstants.ComposedTextAttribute))) {
				fireUndoableEditUpdate(new UndoableEditEvent(this, e));
			}
		} finally {
			writeUnlock();
		}
	}

	@Override
	public void replace(final int offset, final int length, final String text, final AttributeSet attrs)
			throws BadLocationException {
		if (length == 0 && (text == null || text.length() == 0)) {
			return;
		}

		writeLock();
		try {
			if (length > 0) {
				quietRemove(offset, length);
			}
			if (text != null && text.length() > 0) {
				quietInsertString(offset, text, attrs);
				fireInsertUpdate(new DefaultDocumentEvent(offset, text.length(), EventType.INSERT));
			} else {
				fireRemoveUpdate(new DefaultDocumentEvent(offset, length, EventType.REMOVE));
			}
		} finally {
			writeUnlock();
		}
	}
}
