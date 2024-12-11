package zombiegame;

public class Bullet {

	int width = 6;
	double height = 6;
	double x;
	double y;
	double speedX;
	double speedY;
	double damage = 20;
	
	Bullet(double x, double y, double speedX, double speedY) {
		
		this.x = x;
		this.y = y;
		this.speedX = speedX;
		this.speedY = speedY;
	}
}
