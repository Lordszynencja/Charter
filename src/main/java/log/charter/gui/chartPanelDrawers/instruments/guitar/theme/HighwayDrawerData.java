package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.awt.Graphics;

import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;

public class HighwayDrawerData {
	public final int strings;
	public final int time;

	public final DrawableShapeList sectionsAndPhrases;
	public final DrawableShapeList toneChanges;
	public final DrawableShapeList anchors;
	public final DrawableShapeList chordNames;
	public final DrawableShapeList noteTails;
	public final DrawableShapeList notes;
	public final DrawableShapeList slideFrets;
	public final DrawableShapeList bendValues;
	public final DrawableShapeList handShapes;
	public final DrawableShapeList highlights;
	public final DrawableShapeList noteIds;

	public final Graphics g;

	public HighwayDrawerData(final Graphics g, final int strings, final int time) {
		this.strings = strings;
		this.time = time;

		sectionsAndPhrases = new DrawableShapeList();
		toneChanges = new DrawableShapeList();
		anchors = new DrawableShapeList();
		chordNames = new DrawableShapeList();
		highlights = new DrawableShapeList();
		noteTails = new DrawableShapeList();
		notes = new DrawableShapeList();
		slideFrets = new DrawableShapeList();
		bendValues = new DrawableShapeList();
		handShapes = new DrawableShapeList();
		noteIds = new DrawableShapeList();

		this.g = g;
	}

	public void draw() {
		sectionsAndPhrases.draw(g);
		toneChanges.draw(g);
		anchors.draw(g);
		chordNames.draw(g);
		handShapes.draw(g);
		noteTails.draw(g);
		notes.draw(g);
		slideFrets.draw(g);
		bendValues.draw(g);
		highlights.draw(g);
		noteIds.draw(g);
	}
}
