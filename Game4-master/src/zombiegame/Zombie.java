package zombiegame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Image;
import java.awt.RenderingHints;

public class Zombie {

	int width = 25;
	int height = 25;
	double x;
	double y;
	double speedX;
	double speedY;
	double angle;
	double damage = 8;
	double fullHealth = 50;
	double health = fullHealth;

	Image imgZombie;

	Zombie() {
		imgZombie = loadImage("zombie1.png");

	}
	Image loadImage(String filename) {
		Image image = null;
		URL imageURL = this.getClass().getResource("/" + filename);
		if (imageURL != null) {
			ImageIcon icon = new ImageIcon(imageURL);
			image = icon.getImage();
		} else {
			JOptionPane.showMessageDialog(null, "An image failed to load: " + filename , "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		return image;
	}
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON); //antialiasing
		if (imgZombie == null) {
			g2.setColor(Color.green);
			g2.fillRect((int)x, (int)y, (int)width, (int)height);
			return;
		}
		g2.drawImage(imgZombie, (int)x-12, (int)y-7, null);

	}
}
