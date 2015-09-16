import java.awt.image.BufferedImage;


public class DrawBackGround {
    
    private BufferedImage backImg;
    
    public DrawBackGround(GameEx game) {
        
        BackGround bi = new BackGround(game.getBackGround());
        backImg = bi.grabImage(1,1,32,32);
    }

}
