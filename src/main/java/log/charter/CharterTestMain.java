package log.charter;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import log.charter.util.collections.ExpiringValuesList;

public class CharterTestMain extends Application {
	private static final ExpiringValuesList<Long> framesCounter = new ExpiringValuesList<>(ExpiringValuesList.second);

	public static void main(final String[] args) {
		System.out.println("test");
		launch();
	}

	private static final String javaVersion = System.getProperty("java.version");
	private static final String javafxVersion = System.getProperty("javafx.version");
	private final Label label;

	{
		label = new Label("");
		showFPS();
	}

	private void showFPS() {
		label.setText(
				"JavaFX: " + javafxVersion + ", Java: " + javaVersion + ", FPS: " + framesCounter.getValues().size());
	}

	@Override
	public void start(final Stage stage) {
		final Scene scene = new Scene(new StackPane(label), 640, 480);
		stage.setScene(scene);
		stage.show();

		final AnimationTimer frameRateMeter = new AnimationTimer() {

			@Override
			public void handle(final long now) {
				framesCounter.addValue(now);
				showFPS();
			}
		};

		frameRateMeter.start();
	}
}
