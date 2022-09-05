package bullethell.scenes;

import static bullethell.Globals.DEFAULT_FONT;
import static bullethell.Globals.SCREEN_HEIGHT;
import static bullethell.Globals.SCREEN_WIDTH;
import static bullethell.Globals.frame;
import static bullethell.Globals.freezeCursor;
import static bullethell.Globals.main;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JTextField;

import bullethell.GameState;
import bullethell.ui.Text;

/** 
 * All subclasses of {@code Scene} should be singletons.
 */
public sealed interface Scene
  permits CoreForge, ErnestoBoss, Forge, MainMenu, OptionsMenu, World {

    void start(int x, int y);

    void end();

	boolean isActive();

	GameState getState();

	/**
	 * A new {@code Thread} should be created to call this method to prevent the program from being frozen.
	 */
    static String promptString(String string) {
		AtomicBoolean ready = new AtomicBoolean(false);

		JTextField textField = new JTextField("");
		textField.setBounds(SCREEN_WIDTH / 2 - 250, SCREEN_HEIGHT / 2 - 30, 500, 60);
		textField.setFont(DEFAULT_FONT);

		Text label = new Text(string);
		label.setLocation(textField.getX(), textField.getY() - 45);

		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
				
				String text = textField.getText();
				if (text != null && !text.isBlank() &&
				  !Character.isWhitespace(text.charAt(textField.getText().length() - 1))) {
					main.requestFocusInWindow();
					ready.set(true);
				} else {
					textField.setCaretColor(Color.RED);
				}
			}
		}); 
		frame.add(textField);
		textField.requestFocusInWindow();

		freezeCursor = true;
		while (!ready.get()) {
			if (textField.getText() != null && textField.getText().length() >= 10) {
				textField.setText(textField.getText().substring(0, 9));
				textField.setCaretPosition(9);
			}
		}
		freezeCursor = false;

		main.remove(textField);
		label.kill();
		return textField.getText();
	}
}
