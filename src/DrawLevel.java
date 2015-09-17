import java.awt.Graphics;


public class DrawLevel {

    
    private double x;
    private double y;
    
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

    
}

