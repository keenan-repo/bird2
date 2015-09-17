import java.awt.image.BufferedImage;


public class BackGround {
    
    public BufferedImage Level_1;
    
    private BufferedImage ss;
    
    public BackGround(GameEx game){
        ss = game.getLevel_1();
        getMap();
        
    }
    
    private void getMap(){
        Level_1 = ss;
    }
}
