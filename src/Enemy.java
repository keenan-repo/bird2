import java.awt.Graphics;


public class Enemy {
    private double x, y; 
    
    private Textures tex;
    
    public Enemy(double x, double y, Textures tex){      
        this.x = x;
        this.y = y;
        this.tex = tex;
    }
    
    public void tick(){
        boolean left = false;
        if( x < 300 && !left){
            x += 1;
        } else if (x >= 300) {
            left = true;
        }
        if (x > 50 && left){
            x -= 1;
        } else if (x <= 50){
            left = false;
        }
    }
    
    public void render(Graphics g){
        g.drawImage(tex.enemy, (int)x, (int)y, null);
    }
}
