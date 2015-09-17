import java.awt.Graphics;


public class DrawLevel {

    
    private double x, velX;
    private double y, velY;
    
    private BackGround bg;

    
    public DrawLevel(double x, double y, BackGround bg){
        this.x = x;
        this.y = y;
        this.bg = bg;
        
    }
    
    public void tick(){
       // x -= 5;
    }
    
    public void render(Graphics g){
        g.drawImage(bg.Level_1, (int)x, (int)y, null);
    }

    
    public double getY(){return y;}
    public void setY(double y){this.y = y;}
    
    public double getX(){return x;}
    public void setX(double x){this.x = x;}
    
    public void setVelY(double velY){this.velY = velY;}
    
    public void setVelX(double velX){this.velX = velX;}
    
}

