package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.instancing.InstancedGeometry;
import com.jme3.scene.instancing.InstancedNode;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.water.WaterFilter;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Ilya Mirzazhanov
 */
public class Recarea extends SimpleApplication implements ActionListener, PhysicsCollisionListener, RagdollCollisionListener, AnimEventListener {

    public static void main(String[] args) {
        Recarea app = new Recarea();
        app.start();
    }
    private PhysicsSpace space;
    private BulletAppState bulletAppState;
    private static Box box;
    private static Sphere bullet;
    private static BoxCollisionShape blockCollisionShape;
    private boolean left = false, right = false, up = false, down = false;
    private final Vector3f camDir = new Vector3f();
    private final Vector3f camLeft = new Vector3f();
    private final Vector3f walkDirection = new Vector3f();
    private CharacterControl player;
    private Node level;
    private CompoundCollisionShape levelShape;
    private CompoundCollisionShape movingShape;
    private Geometry block;
    private CollisionResults results;
    private CollisionResult closest;
    private SphereCollisionShape sphereCollisionShape;
    Material materialYellow;
    Material materialRed;
    Material materialGreen;
    Material materialBlue;
    Ray ray;
    private RigidBodyControl landscape;
    private Spatial sceneModel;
    private static final boolean useHttp = true;
    private final float time = 0;
    private WaterFilter water;
    private final Vector3f lightDir = new Vector3f(-80.9236743f, -100.27054665f, 5.896916f);
    boolean end = false;
    private Node moving;
    BitmapText ch;
    Geometry smasher;
    ArrayList<Geometry> br = new ArrayList<>();
    ArrayList<Geometry> bn = new ArrayList<>();
    ArrayList<BoxCollisionShape> bx = new ArrayList<>();
    int x;
    int y;
    int z;
    int SCORE = 0;
    BoundingVolume pVol;
    BoundingVolume vVol;
    Geometry pb;
    Vector3f startPosition = new Vector3f(2.6854258f, 125.03817f, 54.26886f);
    Vector3f dwPos = new Vector3f(-120.6854258f, -10.03817f, 4.26886f);
    Geometry deathWall;
    BitmapText ch2;
    
    ColorRGBA brc;
    ColorRGBA nrc;
    DirectionalLight sun;
    DirectionalLight sun2;
    DirectionalLight sun3;
    
    private boolean INSTANCING = true;
    Geometry g;
    BoxCollisionShape gs;
    @Override
    public void simpleInitApp() {
        setDisplayFps(false);
        setDisplayStatView(false);
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        space = bulletAppState.getPhysicsSpace();
        level = new Node("level");
        levelShape = new CompoundCollisionShape();
        movingShape = new CompoundCollisionShape();
            
        moving = new InstancedNode("instanced_node");
        
        
        
        
        setUpKeys();
        setUpWorld();
        setUpLight();
        setUpMaterials();
        initStartBrick();
        levelOptions();
        addPlayer();
        makePlayerSkeleton();
        makeBricks();
        makeBonuses();
        
        
        
        /*g = new Geometry("g", new Box(5, 2, 10));
        g.setShadowMode(RenderQueue.ShadowMode.Receive);
        g.setMaterial(materialRed);
        g.setLocalTranslation(new Vector3f(13.6854258f, 125.03817f, 58.26886f));
        //g.addControl(new RigidBodyControl(0));
        //moving.attachChild(g);
        gs = new BoxCollisionShape(new Vector3f(5, 2, 10));
        
        movingShape.addChildShape(gs, g.getLocalTranslation());
        
        float height = (smoothstep(0, 1, FastMath.nextRandomFloat()) * 2.5f) - 1.25f;
                g.setUserData("height", height);
                g.setUserData("dir", 1f);
                
                moving.attachChild(g);
        
          if (INSTANCING) {
            ((InstancedNode)moving).instance();
          }  
          level.attachChild(moving);*/
          //space.add(g);
        space.add(player);
        space.add(level);
        rootNode.attachChild(level);
        initScore();
        //bulletAppState.setDebugEnabled(true);
    }
    
    private float smoothstep(float edge0, float edge1, float x) {
        // Scale, bias and saturate x to 0..1 range
        x = FastMath.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        // Evaluate polynomial
        return x * x * (3 - 2 * x);
    }
    
   public void levelOptions() {
       level.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        level.addControl(new RigidBodyControl(levelShape, 0));
   }
   public void setUpWorld() {
       this.cam.setLocation(new Vector3f(0, 6f, 6f));
        flyCam.setMoveSpeed(50);
        flyCam.setZoomSpeed(0);
         //0.7f, 0.8f, 1f, 1f - sky color
        viewPort.setBackgroundColor(/*new*/ ColorRGBA.randomColor()/*(0.7f, 0.8f, 1f, 1f)*/);
   }
    
    public void setUpLight() {
        sun = new DirectionalLight();
        sun2 = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.7f, -0.3f, -0.5f)).normalizeLocal());
        sun2.setDirection((new Vector3f(0.7f, 0.3f, 0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        sun2.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        rootNode.addLight(sun2);
        sun3 = new DirectionalLight();
        sun3.setDirection(lightDir);
        sun3.setColor(ColorRGBA.White.clone().multLocal(1f));
        level.addLight(sun3);
    }
    public void initStartBrick() {
        Geometry floor = new Geometry("floor", new Box(10, 0.5f, 20));
        floor.setShadowMode(RenderQueue.ShadowMode.Receive);
        floor.setLocalTranslation(0.0f, 120f, 50.0f);
        floor.setMaterial(materialYellow);
        
        level.attachChild(floor);
        

        BoxCollisionShape floorShape = new BoxCollisionShape(new Vector3f(10, 0.5f, 20));
        bx.add(floorShape);

        levelShape.addChildShape(floorShape, new Vector3f(0.0f, 120f, 50.0f));
    }
     /*brc = new ColorRGBA(0.23413175f, 0.45958066f, 0.8304797f, 1.0f);
        nrc = new ColorRGBA(0.1285702f, 0.17633164f, 0.5225241f, 1.0f);*/
    
    /*brc = new ColorRGBA(0.08149296f, 0.47600466f, 0.60408187f, 1.0f);
        nrc = new ColorRGBA(0.9025471f, 0.17458212f, 0.15811568f, 1.0f);*/
    
    /*brc = new ColorRGBA(0.09852934f, 0.23077333f, 0.93435186f, 1.0f);
        nrc = new ColorRGBA(0.52462876f, 0.951079f, 0.8884326f, 1.0f);*/
    
    /*brc = new ColorRGBA(0.718364f, 0.73131627f, 0.32483375f, 1.0f);
        nrc = new ColorRGBA(0.5911986f, 0.54708624f, 0.4619918f, 1.0f);*/
    
    /*brc = new ColorRGBA(0.57414055f, 0.6029728f, 0.87560326f, 1.0f);
        nrc = new ColorRGBA(0.3066978f, 0.3431701f, 0.29706186f, 1.0f);*/
    
    /*brc = new ColorRGBA(0.55923015f, 0.8126053f, 0.64271075f, 1.0f);
        nrc = new ColorRGBA(0.37596196f, 0.499515957//, 0.24359423, 1.0f);*/
    
    /*brc: Color[0.23426878, 0.39923012, 0.24353355, 1.0]
    nrc: Color[0.9310388, 0.36279285, 0.94119054, 1.0]*/
    
    public void setUpMaterials() {
        brc = ColorRGBA.randomColor();
        nrc = ColorRGBA.randomColor();
        materialYellow = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        materialYellow.setBoolean("UseMaterialColors", true);
        materialYellow.setBoolean("HardwareShadows", true);
        materialYellow.setColor("Diffuse", /*new*/ brc/*RGBA(0.9529f, 0.7843f, 0.0078f, 1.0f)*/);
        materialYellow.setColor("Specular", ColorRGBA.White);
        materialYellow.setFloat("Shininess", 64.0f);
        
        materialRed = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        materialRed.setBoolean("UseMaterialColors", true);
        materialRed.setBoolean("HardwareShadows", true);
        materialRed.setBoolean("UseInstancing", INSTANCING);
        materialRed.setColor("Diffuse", ColorRGBA.Red);
        materialRed.setColor("Specular", ColorRGBA.White);
        materialRed.setFloat("Shininess", 64.0f);

        materialGreen = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        materialGreen.setBoolean("UseMaterialColors", true);
        materialGreen.setBoolean("HardwareShadows", true);
        materialGreen.setColor("Diffuse", new ColorRGBA(0.0431f, 0.7725f, 0.0078f, 1.0f));
        materialGreen.setColor("Specular", ColorRGBA.White);
        materialGreen.setFloat("Shininess", 64.0f);
        
        materialBlue = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        materialBlue.setBoolean("UseMaterialColors", true);
        materialBlue.setBoolean("HardwareShadows", true);
        materialBlue.setColor("Diffuse", nrc);
        materialBlue.setColor("Specular", ColorRGBA.White);
        materialBlue.setFloat("Shininess", 64.0f);
    }
    
    public void addPlayer() {
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 10.05f);
        player.setJumpSpeed(40);
        player.setFallSpeed(45);
        player.setGravity(new Vector3f(0,-30,0));
        player.setPhysicsLocation(startPosition);
    }
    public void makeBricks() {
        Random xr = new Random();
        Random yr = new Random();
        Random zr = new Random();
        int COB = 2500;
        for(int i = 0; i < COB; i++) {
            x = xr.nextInt(13000);
            y = yr.nextInt(300);
            z = zr.nextInt(160);
            makeBrick(x,y,z);
        }
    }
    public void makeBonuses() {
        Random xb = new Random();
        Random yb = new Random();
        Random zb = new Random();
        int BC = 1000;
        for(int i = 0; i < BC; i++) {
            x = xb.nextInt(13000);
            y = yb.nextInt(300);
            z = zb.nextInt(160);
            makeBonus(x,y,z);
        }
    }

    public void makePlayerSkeleton() {
        Material mat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");
        pb = new Geometry("pb", new Box(5, 6, 5));
        pb.setMaterial(mat);
        pb.setLocalTranslation(player.getPhysicsLocation());
        level.attachChild(pb);
    }
    public void makeBrick(int x, int y, int z) {
            Geometry brick = new Geometry("brick", new Box(5, 2, 10));
            brick.setShadowMode(RenderQueue.ShadowMode.Receive);
            brick.setLocalTranslation(x, y, z);
            brick.setMaterial(materialYellow);
            level.attachChild(brick);
            BoxCollisionShape brickShape = new BoxCollisionShape(new Vector3f(5, 2, 10));
            bx.add(brickShape);
            levelShape.addChildShape(brickShape, new Vector3f(x,y,z));
    }
    public void makeBonus(int x, int y, int z) {
            Geometry bonus = new Geometry("bonus", new Box(0.7f, 0.7f, 0.7f));
            bonus.setShadowMode(RenderQueue.ShadowMode.Receive);
            bonus.setLocalTranslation(x, y, z);
            bonus.setMaterial(materialBlue);
            level.attachChild(bonus);
            br.add(bonus);
    }
   
    
    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    protected void initScore() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 3);
        ch.setText(Integer.toString(SCORE));
        ch.setColor(brc);
        ch.setLocalTranslation( // center
                0,
                settings.getHeight()+ ch.getLineHeight() / 6 , 0);
        guiNode.attachChild(ch);
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        switch (binding) {
            case "Left":
                if (value) {
                    left = true;
                } else {
                    left = false;
                }   break;
            case "Right":
                if (value) {
                    right = true;
                } else {
                    right = false;
                }   break;
            case "Up":
                if (value) {
                    up = true;
                } else {
                    up = false;
                }   break;
            case "Down":
                if (value) {
                    down = true;
                } else {
                    down = false;
                }   break;
            case "Jump":
                player.jump();
                break;
            case "Reset":
                reset();
            default:
                break;
        }
    }
    
    public void reset() {
        setDisplayStatView(false);
        SCORE = 0;
        player.setPhysicsLocation(startPosition);
        guiNode.detachAllChildren();
        initScore();
    }
    
    public void checkBonusCollision(float tpf) {
        for(Geometry bon : br) {
            bon.rotate(0.1f, 2*tpf, 0.01f);

                BoundingVolume pVol = pb.getWorldBound();
                BoundingVolume vVol = bon.getWorldBound();

                if (pVol.intersects(vVol)){
                    bon.move(0,1000,0);
                    bon.removeFromParent();
                    //new Sound(assetManager).spawn();
                    SCORE++;
                    guiNode.detachChild(ch);
                    initScore();
                    return;
                }
        }
    }
    protected void gameOver() {
        end = true;
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        ch2 = new BitmapText(guiFont, false);
        ch2.setSize(guiFont.getCharSet().getRenderedSize() * 5);
        ch2.setText("Press 'R' to restart.");
        ch2.setLocalTranslation(
                settings.getWidth() / 3,
                settings.getHeight() / 2 + ch2.getLineHeight() / 2, 0);
        guiNode.attachChild(ch2);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //initScore();
       //levelShape.addChildShape(gs, g.getLocalTranslation());
       /*for (Spatial child : moving.getChildren()) {
            if (!(child instanceof InstancedGeometry)) {
                float val = ((Float)child.getUserData("height"));
                float dir = ((Float)child.getUserData("dir"));

                val += (dir + ((FastMath.nextRandomFloat() * 0.5f) - 0.25f)) * tpf;

                if (val > 1f) {
                    val = 1f;
                    dir = -dir;
                } else if (val < 0f) {
                    val = 0f;
                    dir = -dir;
                }

                Vector3f translation = child.getLocalTranslation();
                translation.y = (smoothstep(0, 1, val) * 15.5f) - 3.25f + 130;

                child.setUserData("height", val);
                child.setUserData("dir", dir);
                
                child.setLocalTranslation(translation);
                //levelShape.
                //child.updateGeometricState();
            }
        }*/
       
       
       
       pb.setLocalTranslation(player.getPhysicsLocation());
       
        System.out.println("brc: " + brc);
         System.out.println("nrc: " + nrc);
           checkBonusCollision(tpf);
            //checkDeathWallCollision(tpf); 
        camDir.set(cam.getDirection()).multLocal(0.6f);

        camLeft.set(cam.getLeft()).multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            //new Sound(assetManager).explosion();
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            //new Sound(assetManager).explosion();
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            //new Sound(assetManager).explosion();
            walkDirection.addLocal(camDir);
        }
        if (down) {
            //new Sound(assetManager).explosion();
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }
    
     private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Reset");
    }

    /*private final ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
          
        }
    };*/

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
       
    }
    
   
    @Override
    public void collide(Bone bone, PhysicsCollisionObject object, PhysicsCollisionEvent event) {

         if (object.getUserObject() != null && object.getUserObject() instanceof Geometry) {
            Geometry geom = (Geometry) object.getUserObject();
            /*if ("Floor".equals(geom.getName())) {
                new Sound(assetManager).spawn();
                return;
            }*/
            //if("bonus".equals(geom.getName()))
                //new Sound(assetManager).spawn();
            /*for(Geometry bon : br) {
            level.detachChild(bon);
            }*/
            //new Sound(assetManager).spawn();
        }
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        
        if (channel.getAnimationName().equals("StandUpBack") || channel.getAnimationName().equals("StandUpFront")) {
            channel.setLoopMode(LoopMode.DontLoop);
            channel.setAnim("IdleTop", 5);
            channel.setLoopMode(LoopMode.Loop);
        }
    }
    

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
