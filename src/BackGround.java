import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;


public class BackGround {
    private BufferedImage image;
    
    public BackGround(BufferedImage image){
        this.image = image;
    }
    public BufferedImage grabImage(int x, int y, int width, int height){
        BufferedImage img = image.getSubimage(x, y, width, height);
        return img;
    }
    
}


