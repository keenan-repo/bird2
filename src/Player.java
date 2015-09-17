
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;


public class Player {
    
    private double x, velX = 0;
    private double y, velY = 0;
    private Rectangle T, B, L, R;
    
    private BufferedImage player;
    
    
    public Player(double x, double y, GameEx game){
        this.x = x;
        this.y = y;
        
        T = new Rectangle((int)x, (int)y - 8, 32, 8);
        B = new Rectangle((int)x, (int)y + 33, 32, 8);
        R = new Rectangle((int)x+32, (int)y, 8, 32);
        L = new Rectangle((int)x-8, (int)y, 8, 32);
        
       
        
        SpriteSheet ss = new SpriteSheet(game.getSpriteSheet());
        player = ss.grabImage(1,1,32,32);
    }
    
    public void tick(){
        x += velX;
        y += velY;
        T.x=(int)x; T.y=(int)y-8;
        B.x=(int)x; B.y=(int)y+33;
        
        R.x=(int)x+33; R.y=(int)y;
        L.x=(int)x-8; L.y=(int)y;

    }
    
    public void render(Graphics g){
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        
        /*if(velX>0 && (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_RIGHT])){
            tx.translate(-birdImg.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            birdImg = op.filter(birdImg, null);
            swap=false;
        } */
        
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
