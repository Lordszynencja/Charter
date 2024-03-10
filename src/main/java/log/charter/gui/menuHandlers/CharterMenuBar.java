package log.charter.gui.menuHandlers;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.gui.CharterFrame;
import log.charter.util.CollectionUtils.ArrayList2;

public class CharterMenuBar extends JMenuBar implements Initiable {
	private static final long serialVersionUID = -5784270027920161709L;

	public static final ColorLabel backgroundColor = ColorLabel.BASE_BG_2;

	private CharterContext charterContext;
	private CharterFrame charterFrame;

	private final ArrangementMenuHandler arrangementMenuHandler = new ArrangementMenuHandler();
	private final EditMenuHandler editMenuHandler = new EditMenuHandler();
	private final FileMenuHandler fileMenuHandler = new FileMenuHandler();
	private final GuitarMenuHandler guitarMenuHandler = new GuitarMenuHandler();
	private final InfoMenuHandler infoMenuHandler = new InfoMenuHandler();
	private final MusicMenuHandler musicMenuHandler = new MusicMenuHandler();
	private final NotesMenuHandler notesMenuHandler = new NotesMenuHandler();
	private final VocalsMenuHandler vocalsMenuHandler = new VocalsMenuHandler();

	private final ArrayList2<CharterMenuHandler> menus = new ArrayList2<>(//
			fileMenuHandler, //
			editMenuHandler, //
			musicMenuHandler, //
			arrangementMenuHandler, //
			vocalsMenuHandler, //
			notesMenuHandler, //
			guitarMenuHandler, //
			infoMenuHandler);

	@Override
	public void init() {
		charterContext.initObject(arrangementMenuHandler);
		charterContext.initObject(editMenuHandler);
		charterContext.initObject(fileMenuHandler);
		charterContext.initObject(guitarMenuHandler);
		charterContext.initObject(infoMenuHandler);
		charterContext.initObject(musicMenuHandler);
		charterContext.initObject(notesMenuHandler);
		charterContext.initObject(vocalsMenuHandler);

		final Dimension size = new Dimension(1, 20);
		setMinimumSize(size);
		setSize(size);
		setMaximumSize(size);
		setPreferredSize(size);

		setBackground(backgroundColor.color());

		refreshMenus();

		charterFrame.setJMenuBar(this);
	}

	public void refreshMenus() {
		final List<JMenu> menusToAdd = menus.stream()//
				.filter(menu -> menu.isApplicable())//
				.map(menu -> menu.prepareMenu())//
				.collect(Collectors.toList());

		removeAll();
		menusToAdd.forEach(this::add);
		validate();
	}
}
