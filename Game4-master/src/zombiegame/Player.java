package zombiegame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
public class Player{
	
	int width = 34;
	int height = 34;
	double x;
	double y;
	double speedX;
	double speedY;
	double health = 50;
	
	Image imgPlayer1;
	
	Player(double x, double y, double speedX, double speedY) {
		this.x = x;
		this.y = y;
		this.speedX = speedX;
		this.speedY = speedY;
		imgPlayer1 = loadImage("pisotalposeOG_center.png");
		rotation(0);
		
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

	private AffineTransform transform = new AffineTransform();
	
	public void rotation(double rotAngle) {
        //add to movement in game

        transform = AffineTransform.getTranslateInstance(x, y);
        transform.rotate(rotAngle,imgPlayer1.getWidth(null)/2-35,imgPlayer1.getHeight(null)/2-35);

    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON); //antialiasing

        if (imgPlayer1 == null) {
        	g2.setColor(Color.yellow);
        	g2.fillRect((int)x, (int)y, (int)width, (int)height);
        	return;
        }
        g2.setTransform(transform);
        g2.drawImage(imgPlayer1, (int)-35, (int)-35, null);
        g2.setTransform(new AffineTransform());

    }
}
