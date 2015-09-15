
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;


public class Player {
    
    private double x, velX = 0;
    private double y, velY = 0;
    private Rectangle T, B, L, R;
    
    private BufferedImage player;
    
    
    public Player(double x, double y, GameEx game){
        this.x = x;
        this.y = y;
        
        T = new Rectangle((int)x, (int)y - 16, 32, 16);
        B = new Rectangle((int)x, (int)y + 34, 32, 16);
        R = new Rectangle((int)x+32, (int)y, 16, 32);
        L = new Rectangle((int)x-16, (int)y, 16, 32);
        
       
        
        SpriteSheet ss = new SpriteSheet(game.getSpriteSheet());
        player = ss.grabImage(1,1,32,32);
    }
    
    public void tick(){
        x += velX;
        y += velY;
        T.x=(int)x; T.y=(int)y-16;
        B.x=(int)x; B.y=(int)y+32;
        
        R.x=(int)x+32; R.y=(int)y;
        L.x=(int)x-16; L.y=(int)y;

    }
    
    public void render(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        g.drawImage(player, (int)x, (int)y, null);
        /*g2d.draw(B);
        g2d.draw(T);
        g2d.draw(R);
        g2d.draw(L);*/
    }
    /* Start getters and setters */
    public Rectangle getT(){return T;}
    public Rectangle getB(){return B;}
    public Rectangle getL(){return L;}
    public Rectangle getR(){return R;}
    
    public double getY(){return y;}
    public void setY(double y){this.y = y;}
    
    public double getX(){return x;}
    public void setX(double x){this.x = x;}
    
    public void setVelY(double velY){this.velY = velY;}
    
    public void setVelX(double velX){this.velX = velX;}
    
}
