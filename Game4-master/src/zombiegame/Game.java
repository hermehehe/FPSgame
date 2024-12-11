/**
 *
 * Widaad
 * 
 * Hermela
 * 
 * Henry
 * 
 * Josh
 *
 *
 * Date started: December 12, 2021
 */

package zombiegame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Game {

	//panel and JFrame
	int panW = 800; //width
	int panH = 600;	//height
	JFrame window;
	GamePanel panel;

	//color variables
	Color heartClr = new Color(181,40,43);
	Color groundClr = new Color(179,156,120);
	Color bulletClr = Color.decode("#444444");
	Color gameoverClr = new Color(200,0,0,100);

	//font variables
	Font helvetica = new Font ("Helvetica", Font.BOLD, 18);
	Font titleFont = new Font ("Serif", Font.BOLD, 65);
	Font enterFont = new Font ("Helvetica", Font.BOLD, 25);
	Font scoreFont = new Font ("Helvetica", Font.BOLD, 24);
	Font gameoverFont = new Font("Helvetica", Font.BOLD, 40);
	Font returnFont = new Font("Helvetica", Font.BOLD, 25);
	Font submenuFont = new Font("Serif", Font.BOLD, 40);
	Font objectiveFont = new Font("Helvetica", Font.BOLD, 20);
	Font controlsFont = new Font("Courier", Font.ITALIC, 25);

	//status booleans
	boolean gameIsRunning = false;
	boolean submenuIsRunning = false;
	boolean playerAlive = false;
	boolean roundOver = false;
	boolean hitboxOn = false; 

	//border, player, zombie, and obstacle variables
	Border border = new Border();
	Player player = new Player(400-17,300-17,0,0);
	ArrayList<Zombie> zombieList = new ArrayList<>();
	ArrayList<Bullet> bulletList = new ArrayList<>();
	ArrayList<Obstacle> obstacleList = new ArrayList<>();

	//movement variables
	boolean[] keys = {false,false,false,false};
	static final int UP=0, DOWN=1, LEFT=2, RIGHT=3; 

	//score, round, & default sleep timer variables
	int playerScore = 0;
	int round = 0;
	int SLEEP = 8; //default refresh rate

	//divide the ground into tiles 200x200 in size
	int GRID = (int)(border.width/200);
	int board[][] = new int [GRID][GRID];
	
	int mouseX, mouseY, mouseClickX, mouseClickY; //for mouse clicks and mouse movement
	
	long now, prevShot, prevHit; //to calculate delays between bullet shots and zombies hitting the player

	public static void main (String[] args) {
		new Game();
	}

	Game() {
		
		window = new JFrame("Wastelander");
		panel = new GamePanel();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.add(panel);

		//starts the threads
		new GfxThread().start();
		new LogicThread().start();

		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	void setup() {

		//game restarts if user decides to play again 
		playerAlive = true;
		player.health = 50;
		border.x = 400-1100;
		border.y = 300-1100;
		
		SLEEP = 8;
		round = 0;
		zombieList.clear();
		obstacleList.clear();
		bulletList.clear();

		//set ground tiles

		for (int i = 0; i < GRID; i++) {
			
			for (int j = 0; j < GRID; j++) {
				if (Math.random() > 0.3) board[i][j] = 1;
				else board[i][j] = 2;

			}
		}

		//create buildings

		//x pos, y pos, width, height, colour
		Obstacle shack = new Obstacle(border.x+720, border.y+820, 200, 140, "woodshack1.jpg");
		obstacleList.add(shack);

		Obstacle warehouse = new Obstacle(border.x+1700, border.y+810, 400, 700, "factory.jpg");
		obstacleList.add(warehouse);

	}

	void spawnZombies() {

		if (zombieList.size() == 0) {
			round++;
			roundOver = true;

			for (int i = 0; i < 3*(round) + 6; i++) {
				Zombie z = new Zombie ();
				z.fullHealth += 10;

				//damage dealt by zombies increases per round
				for (int j = 0; j < round-1; j++) {
					if (round > 1 && roundOver == true) {
						z.damage += 2;
					}
				}

				//spawn within borders
				z.x = (int)(Math.random()*border.width)+border.x;
				z.y = (int)(Math.random()*border.height)+border.y;

				//make sure zombies spawn off screen
				if (z.x + z.width >= 0 && z.x < panW && z.y + z.height >= 0 && z.y < panH) { //if zombie is within screen dimensions

					//50% chance of changing the x position, 50% chance of changing the y position

					if ((int)(Math.random()*2) == 1) {

						//change x position

						while (z.x + z.width >= 0 && z.x < panW) {
							z.x = (int)(Math.random()*border.width)+border.x;
						}
					}
					else {

						//change y position

						while (z.y + z.height >= 0 && z.y < panH) {
							z.y = (int)(Math.random()*border.height)+border.y;
						}
					}
				}
				//prevents zombies from spawning in buildings
				for (Obstacle b: obstacleList) {

					if (z.x + z.width >= b.x && z.x + z.width < b.x + b.width + 75) {
						if (z.y + z.height >= b.y && z.y + z.height < b.y + b.height + 75) {

							while (z.x + z.width >= b.x && z.x + z.width < b.x + b.width + 75) {
								z.x = (int)(Math.random()*border.width)+border.x;
							}
						}
						//change y position

						while (z.y + z.height >= b.y && z.y + z.height < b.y + b.height + 75) {
							z.y = (int)(Math.random()*border.height)+border.y;
						}
					}
				}
				zombieList.add(z);
			}
		}
	}

	void movePlayer() {

		//movement speed variables
		player.speedX = 0;
		player.speedY = 0;

		if (keys[UP])		player.speedY = -2;
		if (keys[DOWN])		player.speedY =  2;		
		if (keys[RIGHT]) 	player.speedX =  2;
		if (keys[LEFT])		player.speedX = -2;		

		if (keys[UP] && keys[RIGHT]) {
			player.speedX = 2*Math.cos(Math.toRadians(45));
			player.speedY = -2*Math.sin(Math.toRadians(45));
		}

		if (keys[UP] && keys[LEFT]) {
			player.speedX = -2*Math.cos(Math.toRadians(45));
			player.speedY = -2*Math.sin(Math.toRadians(45));
		}

		if (keys[DOWN] && keys[RIGHT]) {
			player.speedX = 2*Math.cos(Math.toRadians(45));
			player.speedY = 2*Math.sin(Math.toRadians(45));
		}

		if (keys[DOWN] && keys[LEFT]) {
			player.speedX = -2*Math.cos(Math.toRadians(45));
			player.speedY = 2*Math.sin(Math.toRadians(45));
		}

		if (keys[DOWN] && keys[UP]) {
			player.speedY = 0;
		}

		if (keys[LEFT] && keys[RIGHT]) {
			player.speedX = 0;
		}

		//COLLISION
		//against border
		if (player.x <= border.x && keys[LEFT]) { //left of border
			player.speedX = 0;
		}
		if (player.x + player.width >= border.x + border.width && keys[RIGHT]) { //right of border
			player.speedX = 0;
		}
		if (player.y <= border.y && keys[UP]) { //top of border
			player.speedY = 0;
		}
		if (player.y + player.height >= border.y + border.height && keys[DOWN]) { //bottom of border
			player.speedY = 0;
		}

		//against buildings
		for (Obstacle b : obstacleList) {

			//bottom of building
			if (keys[UP]) {
				if (player.x <= b.x+b.width && player.x+player.width >= b.x) {
					
					if (player.y <= b.y + b.height + 1 && player.y >= b.y + b.height -1) {
						
						player.speedY = 0;
					}
				}
			}

			//top of building
			if (keys[DOWN]) {
				if (player.x <= b.x+b.width && player.x+player.width >= b.x) {

					if (player.y + player.height <= b.y + 1 && player.y + player.height >= b.y-1) {
						
						player.speedY = 0;
					}
				}
			}
			
			//left of building
			if (keys[RIGHT]) {
				if (player.y <= b.y+b.height&& player.y+player.height >= b.y) {

					if (player.x + player.width <= b.x + 1 && player.x + player.width >= b.x-1) {

						player.speedX = 0;
					}
				}
			}

			//right of building
			if (keys[LEFT]) {
				if (player.y <= b.y+b.height&& player.y+player.height >= b.y) {

					if (player.x <= b.x + b.width +  1 && player.x>= b.x + b.width-1) {

						player.speedX = 0;
					}
				}
			}
		}

		//update the positions of all the surroundings
		for (int i = 0; i < zombieList.size(); i++) {
			Zombie z = zombieList.get(i);
			z.x -= player.speedX;
			z.y -= player.speedY;
		}

		for (int i = 0; i < bulletList.size(); i++) {
			Bullet b = bulletList.get(i);
			b.x -= player.speedX;
			b.y -= player.speedY;
		}

		for (Obstacle b : obstacleList) {
			b.x -= player.speedX;
			b.y -= player.speedY;
		}

		border.x -= player.speedX;
		border.y -= player.speedY;
		}

	void moveZombies() {

		for (int i = 0; i < zombieList.size(); i++) {
			Zombie z = zombieList.get(i);

			z.angle = Math.atan2((z.x - player.x), (z.y - player.y));

			//initial speed of zombies
			z.speedX = -0.5*Math.sin(z.angle);
			z.speedY = -0.5*Math.cos(z.angle);

			//after round 1, movement speed of zombies increases per round
			for (int j = 0; j < round; j++) {
				if (round > 1 && roundOver) {
					z.speedX += -0.25*Math.sin(z.angle);
					z.speedY += -0.25*Math.cos(z.angle);
				}
			}

			//pathfinding around buildings
			for (Obstacle b : obstacleList) {

				//if zombie is to the right of building (within a 25 pixel margin)

				if (z.x >= b.x + b.width - 1 && z.x <= b.x + b.width + 25) {

					if (z.y >= b.y - 25 && z.y <= b.y + b.height + 25) {

						//if player is to the left of zombie
						if (player.x < z.x) {

							if (player.y <= (b.height/2) + b.y) {
								z.speedX = 0;
								z.speedY = -0.5;
							}

							if (player.y > (b.height/2) + b.y) {
								z.speedX = 0;
								z.speedY = 0.5;
							}

							if (player.x >= b.x + b.width - 1 && player.x <= b.x + b.width + 25) {
								z.speedX = -0.5*Math.sin(z.angle);
								z.speedY = -0.5*Math.cos(z.angle);
							}

							//speed of zombies gets faster according to the round along w/ pathfinding
							for (int j = 0; j < round; j++) {
								if (round > 1 && roundOver) {
									if (player.y <= (b.height/2) + b.y) {
										z.speedX = 0;
										z.speedY += -0.2;
									}
									if (player.y > (b.height/2) + b.y) {
										z.speedX = 0;
										z.speedY += 0.2;
									}
									if (player.x >= b.x + b.width - 1 && player.x <= b.x + b.width + 25) {
										z.speedX += -0.2*Math.sin(z.angle);
										z.speedY += -0.2*Math.cos(z.angle);
									}
								}
							}
						}
					}
				}

				//if zombie is to the left of building (within a 25 pixel margin)
				if (z.x + z.width <= b.x + 1 && z.x + z.width >= b.x - 25) {

					if (z.y >= b.y - 25 && z.y <= b.y + b.height + 25) {

						//if player is to the right of zombie
						if (player.x > z.x) {


							if (player.y <= (b.height/2) + b.y) {
								z.speedX = 0;
								z.speedY = -0.5;
							}

							if (player.y > (b.height/2) + b.y) {
								z.speedX = 0;
								z.speedY = 0.5;
							}

							if (player.x + player.width <= b.x + 1 && player.x + player.width >= b.x - 25) {
								z.speedX = -0.5*Math.sin(z.angle);
								z.speedY = -0.5*Math.cos(z.angle);
							}

							for (int j = 0; j < round; j++) {
								if (round > 1 && roundOver) {
									if (player.y <= (b.height/2) + b.y) {
										z.speedX = 0;
										z.speedY += -0.2;
									}
									if (player.y > (b.height/2) + b.y) {
										z.speedX = 0;
										z.speedY += 0.2;
									}
									if (player.x + player.width <= b.x + 1 && player.x + player.width >= b.x - 25) {
										z.speedX += -0.2*Math.sin(z.angle);
										z.speedY += -0.2*Math.cos(z.angle);
									}
								}
							}
						}
					}
				}

				//if zombie is above building (within a 25 pixel margin)
				if (z.y + z.height <= b.y +1 && z.y + z.height >= b.y - 25) {

					if (z.x + z.width >= b.x - 25 && z.x <= b.x + b.width + 25) {


						//if player is below zombie
						if (player.y > z.y) {

							if (player.x <= (b.width/2) + b.x) {
								z.speedX = -0.5;
								z.speedY = 0;
							}

							if (player.x > (b.width/2) + b.x) {
								z.speedX = 0.5;
								z.speedY = 0;
							}

							if (player.y + player.height <= b.y +1 && player.y + player.height >= b.y - 25) {
								z.speedX = -0.5*Math.sin(z.angle);
								z.speedY = -0.5*Math.cos(z.angle);
							}

							for (int j = 0; j < round; j++) {
								if (round > 1 && roundOver) {
									if (player.x <= (b.width/2) + b.x) {
										z.speedX += -0.2;
										z.speedY = 0;
									}
									if (player.x > (b.width/2) + b.x) {
										z.speedX += 0.2;
										z.speedY = 0;
									}
									if (player.y + player.height <= b.y +1 && player.y + player.height >= b.y - 25) {
										z.speedX += -0.2*Math.sin(z.angle);
										z.speedY += -0.2*Math.cos(z.angle);
									}
								}
							}
						}
					}
				}

				//if zombie is below building (within a 25 pixel margin)
				if (z.y >= b.y + b.height -1 && z.y <= b.y + b.height + 25) {

					if (z.x + z.width >= b.x - 25 && z.x <= b.x+ b.width + 25) {

						//if player is above zombie
						if (player.y < z.y) {

							if (player.x <= (b.width/2) + b.x) {
								z.speedX = -0.5;
								z.speedY = 0;
							}

							if (player.x > (b.width/2) + b.x) {
								z.speedX = 0.5;
								z.speedY = 0;
							}

							if (player.y >= b.y + b.height -1 && player.y <= b.y + b.height + 25) {
								z.speedX = -0.5*Math.sin(z.angle);
								z.speedY = -0.5*Math.cos(z.angle);
							}
							for (int j = 0; j < round; j++) {
								if (round > 1 && roundOver) {
									if (player.x <= (b.width/2) + b.x) {
										z.speedX += -0.2;
										z.speedY = 0;
									}
									if (player.x > (b.width/2) + b.x) {
										z.speedX += 0.2;
										z.speedY = 0;
									}
									if (player.y >= b.y + b.height -1 && player.y <= b.y + b.height + 25) {
										z.speedX += -0.2*Math.sin(z.angle);
										z.speedY += -0.2*Math.cos(z.angle);
									}
								}
							}
						}
					}
				}
			}
			z.x += z.speedX;
			z.y += z.speedY;
		}
	}

	void shootBullets() {

		for (int i = 0; i < bulletList.size(); i++) {
			Bullet b = bulletList.get(i);

			for (int j = 0; j < zombieList.size(); j++) {
				Zombie z = zombieList.get(j);

				//if bullet goes off screen
				if (b.x < 0 || b.x >= panW || b.y < 0 || b.y >= panH) {
					bulletList.remove(i);
					return;
				}

				//if bullet hits a building
				for (int k = 0; k < obstacleList.size(); k++) {
					Obstacle build = obstacleList.get(k);
					if(b.x >= build.x && b.x <= build.x+build.width) {

						if (b.y >= build.y && b.y <= build.y+build.height) {
							bulletList.remove(i);
							return;
						}
					}
				}
				
				//if bullet hits the border
				if (b.x <= border.x) { //left side
					bulletList.remove(i);
					return;
				}
				if (b.x + b.width >= border.x + border.width) { //right side
					bulletList.remove(i);
					return;
				}
				if (b.y <= border.y) { //top side
					bulletList.remove(i);
					return;
				}
				if (b.y + b.height >= border.y + border.height) { //bottom side
					bulletList.remove(i);
					return;
				}

				//if bullet hits a zombie
				if (b.x >= z.x && b.x <= z.x+z.width) {

					if (b.y >= z.y && b.y <= z.y+z.height) {

						z.health -= b.damage;
						bulletList.remove(i);

						//each zombie that is killed scores 10 points
						if (z.health <= 0) {
							zombieList.remove(j);
							playerScore+=10;
						}
						return;
					}
				}
			}
			b.x += b.speedX;
			b.y += b.speedY;
		}
	}

	void gameStatus() {

		//if player reaches 0 health (or lower initially)
		if (player.health <= 0) {
			window.setTitle("Game over");
			playerAlive = false;
		}
	}

	void createBullets(int x, int y) {

		//where bullet is getting shot out from
		double deltaX = Math.abs((player.x + (player.width/2)-4)-x);
		double deltaY = Math.abs((player.y + (player.height/2)-4)-y);
		double angle = Math.atan2(deltaY, deltaX);

		//bullet angle 
		Bullet b = new Bullet(player.x + (player.width/2)-4,player.y + (player.height/2)-4,0,0);

		//bullet velocity
		if (x > player.x) b.speedX = (double)(5*Math.cos(angle));
		if (x < player.x) b.speedX = (double)(-5*Math.cos(angle));
		if (y > player.y) b.speedY = (double)(5*Math.sin(angle));
		if (y < player.y) b.speedY = (double)(-5*Math.sin(angle));

		now = System.currentTimeMillis();		
		//bullet cooldown of 300 ms
		if (now-prevShot >= 300) {
			bulletList.add(b);	
			prevShot = System.currentTimeMillis();
		}
	}

	//This method deducts health from the player if the zombie is making contact w/ a player
	void checkHealth() {
		for (int i = 0; i < zombieList.size(); i++) {
			Zombie z = zombieList.get(i);
			now = System.currentTimeMillis();

			if (z.x >= player.x-1 && z.x <= player.x+player.width +1 && z.y >= player.y-1 && z.y <= player.y + player.height+1) {

				if (now - prevHit >= 2000) { //player can only lose health ever 2 seconds
					player.health -= z.damage;
					prevHit = System.currentTimeMillis();
				}
			}
			
			if (player.health <= 0) {
				player.health = 0; //so that player health doesn't display as negative
			}
		}
	}


	class GamePanel extends JPanel {

		Image imgTextureTile;
		Image imgTextureTile2;
		Image imgHeart;

		GamePanel() {

			imgTextureTile = loadImage("texturetile1.jpg");
			imgTextureTile2 = loadImage("texturetile4.jpg");
			imgHeart = loadImage("heart.png");

			this.setBackground(Color.decode("#66c1d1"));
			this.setPreferredSize(new Dimension(panW,panH));

			this.addMouseListener(new MouseAL());
			this.addKeyListener(new WAL());
			this.addMouseMotionListener(new MotionAL());
			this.setFocusable(true);
			this.requestFocusInWindow();

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

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON); //antialiasing

			//menu
			if (!gameIsRunning) {
				g2.setColor(Color.black);
				g2.fillRect(0, 0, panW, panH);

				//main title 
				g2.setColor(Color.white);
				g2.setFont(titleFont);
				g2.drawString("NUCLEAR WARZONE", 70, 150);

				//game enter font, turns yellow if cursor is on text and white if not
				g2.setFont(enterFont);
				if (mouseX >= 270 && mouseX <= 270+260 && mouseY >= 220 && mouseY <= 220 + 40) {
					g2.setColor(Color.yellow);
				}
				else {
					g2.setColor(Color.white);
				}
				g2.drawString("Enter the WARZONE", 275, 250);

				//"how to play" font 
				g2.setFont(submenuFont);
				if (mouseX >= 285 && mouseX <= 285+510 && mouseY >= 470 && mouseY <= 470 + 40) {
					g2.setColor(Color.yellow);
				}
				else {
					g2.setColor(Color.white);
				}
				g2.drawString("How to Play", 290, 500);
			}
			
			//submenu (how to play screen)
			if (submenuIsRunning) {
				g2.setColor(Color.black);
				g2.fillRect(0, 0, panW, panH);

				g2.setColor(Color.white);
				g2.setFont(titleFont);
				g2.drawString("Objective", 265, 100);

				g2.setColor(Color.white);
				g2.setFont(objectiveFont);
				g2.drawString("Stay alive for as long as you can while accumulating a high score! ", 90, 200);
				g2.drawString("Points can be accumulated by killing zombies. One kill equals ten points.", 65, 250);

				g2.setColor(Color.white);
				g2.setFont(controlsFont);
				g2.drawString("CONTROLS", 325, 350);

				g2.setColor(Color.white);
				g2.setFont(objectiveFont);
				g2.drawString("WASD to move,", 325, 400);
				g2.drawString("Mouse1/Left Click to shoot!", 265, 450);

				g2.setFont(enterFont);
				if (mouseX >= 270 && mouseX <= 270+260 && mouseY >= 520 && mouseY <= 520 + 40) {
					g2.setColor(Color.yellow);
				}
				else {
					g2.setColor(Color.white);
				}
				g2.drawString("RETURN TO MAIN MENU", 250, 550); 
			}

			if (gameIsRunning) {
				//draw ground tiles 
				g2.setColor(groundClr);
				g2.fillRect((int)border.x, (int)border.y, (int)border.width, (int)border.height);
				int textureTileWidth = imgTextureTile.getWidth(null);
				int textureTileHeight = imgTextureTile.getHeight(null);

				if (imgTextureTile == null) return;

				//colour tiles
				for (int i = 0; i < GRID; i++) {

					for (int j = 0; j < GRID; j++) {

						if (board[i][j]==1) {
							g2.drawImage(imgTextureTile, (int)border.x+(i*textureTileWidth),(int)border.y+(j*textureTileHeight), null);
						}
						if (board[i][j] == 2) {
							g2.drawImage(imgTextureTile2, (int)border.x+(i*textureTileWidth),(int)border.y+(j*textureTileHeight), null);
						}
					}
				}

				//draw border
				g2.setColor(Color.white);
				g2.setStroke(new BasicStroke(8));
				g2.drawRect((int)border.x, (int)border.y, (int)border.width, (int)border.height);

				//draw bullets
				g2.setStroke(new BasicStroke(1));
				g2.setColor(bulletClr);
				for (int i = 0; i < bulletList.size(); i++) {
					Bullet b = bulletList.get(i);
					g2.fill(new Ellipse2D.Double(b.x, b.y, b.width, b.height));
				}

				//draw player
				player.draw(g2);	

				//draw zombies
				for (int i = 0; i < zombieList.size(); i++) {
					Zombie z = zombieList.get(i);
					z.draw(g2);
				}

				//draw buildings
				for (Obstacle b : obstacleList) {

					g2.setColor(b.shadowColour);
					g2.fill(new Rectangle2D.Double(b.x, b.y, b.width+30, b.height+30));
				}
				
				for (Obstacle b : obstacleList) {

					g2.setColor(b.colour);
					b.draw(g2);
				}

				//draw zombie health bars
				g2.setColor(Color.white);
				for (int i = 0; i < zombieList.size(); i++) {
					Zombie z = zombieList.get(i);	
					g2.fill(new Rectangle2D.Double(z.x + (z.width/2)-(12), z.y - 15, (int)(30*(z.health/z.fullHealth)),5));
				}

				//score and round displays
				g2.setFont(scoreFont);
				g2.setColor(Color.white);
				g2.drawString("Score: " + playerScore, panW-180, panH-30);
				g2.drawString("Round: " + round, 30, panH-30);

				//health display
				if (imgHeart ==null) return;
				g2.setColor(heartClr);
				g2.drawImage(imgHeart, 20, 12, null);
				g2.fillRect(55, 15, (int)(100*(player.health/50)), 12);
				g2.setColor(heartClr);
				g2.setFont(helvetica);
				g2.drawString(""+(int)player.health, 162, 29);


				if (hitboxOn) {
					//crosshair
					g2.setColor(Color.red);
					g2.drawLine(panW/2, 0, panW/2, panH);
					g2.drawLine(0, panH/2, panW, panH/2);

					//draw player hitbox
					g2.setStroke(new BasicStroke(4));
					g2.drawRect((int)player.x, (int)player.y, (int)player.width, (int)player.height);
					
					//draw zombie hitbox
					for (Zombie z : zombieList) {
						g2.drawRect((int)z.x,(int)z.y,(int)z.width,(int)z.height);
					}
				}

				//game over screen
				if (!playerAlive) {
					g2.setColor(gameoverClr);
					g2.fillRect(0,0,panW,panH);
					g2.setFont(gameoverFont);
					g2.setColor(Color.white);
					g2.drawString("GAME OVER", 280, panH/2 - 20);
					
					g2.setFont(returnFont);

					if (mouseX >= 300 && mouseX <= 300+205 && mouseY >= 290 && mouseY <= 290 + 30) {
						g2.setColor(Color.yellow);
					}
					else {
						g2.setColor(Color.white);
					}
					g2.drawString("Return to Menu", 310, 315);

					SLEEP = 32;
				}
			}
		}
	}

	/*****************************************************/
	/*			Event Listener classes					 */
	/*****************************************************/

	class MouseAL implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == MouseEvent.BUTTON1) {
				mouseClickX = e.getX();
				mouseClickY = e.getY();

				if (gameIsRunning) {
					if (playerAlive) {	
						createBullets(mouseClickX, mouseClickY); //shoot bullets
					}

					else {
						if (mouseClickX >= 300 && mouseClickX <= 300+205 && mouseClickY >= 290 && mouseClickY <= 290 + 30) {
							gameIsRunning = false; //return to menu
						}
					}
				}

				else {
					if (mouseClickX >= 270 && mouseClickX <= 270+260 && mouseClickY >= 220 && mouseClickY <= 220 + 40) {
						setup();
						gameIsRunning = true; //start game
					}
				}
			}

			if (!gameIsRunning) {
				if (mouseClickX >= 285 && mouseClickX <= 285+510 && mouseClickY >= 470 && mouseClickY <= 470 + 40) {
					submenuIsRunning = true;
				}
			}

			if (submenuIsRunning) {
				if (mouseClickX >= 270 && mouseClickX <= 270+260 && mouseClickY >= 520 && mouseClickY <= 520 + 40) {
					submenuIsRunning = false;
					gameIsRunning = false;

				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
	}

	class WAL implements KeyListener { 

		@Override
		public void keyPressed(KeyEvent e) {

			int key = e.getKeyCode();

			if (key == 'W') keys[UP] = true;
			if (key == 'A') keys[LEFT] = true;
			if (key == 'S') keys[DOWN] = true; 
			if (key == 'D') keys[RIGHT] = true; 

		}

		//stops player from moving in a direction if key is released
		@Override
		public void keyReleased(KeyEvent e) {

			int key = e.getKeyCode();

			if (key == 'W') keys[UP] = false;
			if (key == 'A') keys[LEFT] = false;
			if (key == 'S') keys[DOWN] = false; 
			if (key == 'D') keys[RIGHT] = false; 
		}

		@Override
		public void keyTyped(KeyEvent e) {

			//enable or disable hitboxes by pressing b
			if (gameIsRunning) {
				if (e.getKeyChar() == 'b') {

					if (hitboxOn) {
						hitboxOn = false;
					}
					else {
						hitboxOn = true;
					}
				}
			}
			//skip rounds by pressing g
			if (e.getKeyChar() == 'g') {
				zombieList.clear();
			}
		}
	}

	class MotionAL implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {

		}

		//player rotation in relation to mouse movement
		@Override
		public void mouseMoved(MouseEvent e) {

			mouseX = e.getX();
			mouseY = e.getY();

			if (gameIsRunning) {
				double x = Math.abs(mouseX - (panW/2));
				double y = Math.abs(mouseY - (panH/2));

				double angle = Math.atan((y/x));

				System.out.println(angle);
				
				/* 
				 * flipping was happening if the player was facing directly up or directly left
				 * something with angle calculation was off, so these two if statements are here
				 * to correct the angle
				 */
				if (angle == Math.PI/2 && mouseY < panH/2) {
					angle = 3*Math.PI/2;
				}
				
				if (angle == 0 && mouseX < panW/2) {
					angle = Math.PI;
				}

				//cast rule
				if (e.getX() > panW/2) {

					if (e.getY() > panH/2) {
						//do nothing
					}

					if (e.getY() < panH/2) {
						angle = 2*Math.PI - angle;
					}
				}
				if (e.getX() < panW/2) {

					if (e.getY() > panH/2) {
						angle = Math.PI - angle;
					}

					if (e.getY() < panH/2) {
						angle = Math.PI + angle;
					}
				}
				
				angle += Math.PI/2;
				
				player.rotation(angle);
			}
		}
	}

	/*****************************************************/
	/*				Thread classes						 */
	/*****************************************************/
	class LogicThread extends Thread {
		public void run() {
			while (true) {
				try { Thread.sleep(SLEEP);
				} catch (InterruptedException e) {}

				if (gameIsRunning) {
					movePlayer();
					moveZombies();
					shootBullets();
					spawnZombies();
					checkHealth();
					gameStatus();
				}	
			}
		}
	};

	class GfxThread extends Thread {
		public void run() {
			while(true) {
				try { Thread.sleep(8);
				} catch (InterruptedException e) {}

				panel.repaint();
			}
		}
	};
}
