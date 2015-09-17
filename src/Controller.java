import java.awt.Graphics;
import java.util.LinkedList;


public class Controller {

    private LinkedList<Bullet> b = new LinkedList<Bullet>();
    private LinkedList<Enemy> e = new LinkedList<Enemy>();
    
    Bullet TempBullet;
    Enemy TempEnemy;
    
    GameEx game;
    Textures tex;
    
    public Controller(GameEx game, Textures tex){
        this.game = game;
        this.tex = tex;
        
        
        for(int x = 0; x < (GameEx.WIDTH * GameEx.SCALE); x += 64){
           addEnemy(new Enemy(0, 200, tex));
        }

    }
    


    public void tick() {
        for(int i = 0; i < b.size(); i++){
            TempBullet = b.get(i);
        
            if(TempBullet.getY() < 0)
                removeBullet(TempBullet);
            
            TempBullet.tick();
        }
        
        for(int i = 0; i < e.size(); i++){
            TempEnemy = e.get(i);;
            
            TempEnemy.tick();
        }
    }
    
    public void render(Graphics g){
        for(int i = 0; i < b.size(); i++){
            TempBullet = b.get(i);
            
            TempBullet.render(g);
        }
        
        for(int i = 0; i < e.size(); i++){
            TempEnemy = e.get(i);;
            
            TempEnemy.render(g);
        }
        
    }
        
        public void addBullet(Bullet block){
            b.add(block);
        }
        
        private void addEnemy(Enemy enemy) {
            e.add(enemy);        
        }
        
        
        public void removeBullet(Bullet block){
            b.remove(block);
        }
        
        
        public void removeEnemy(Enemy enemy){
            b.remove(enemy);
        }
    
}
