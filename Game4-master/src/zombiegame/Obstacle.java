package zombiegame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Obstacle {

	double x;
	double y;
	double width;
	double height;
	Color colour = Color.gray; //backup color for if images don't load
	Image obstImage;
	Image imgShack;
	Image imgFactory;
	Color shadowColour = new Color (0,0,0,40);

	Obstacle (double x, double y, double width, double height, String img) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		this.obstImage = loadImage(img);

	}
	
	Image loadImage(String filename) {

		Image image = null;
		URL imageURL = this.getClass().getResource("/" + filename);
		//	InputStream inputStr = GamePanel.class.getClassLoader().getResourceAsStream(filename);
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

	        if (obstImage == null) {
	        g2.setColor(colour);
			g2.fill(new Rectangle2D.Double(x, y, width, height));
			return;
	        }

	        g.drawImage(obstImage, (int)x,(int)y,(int)width, (int)height, null);
	          
	  }
}
