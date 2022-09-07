package bullethell.scenes;

import static bullethell.Globals.DEFAULT_COLOR;
import static bullethell.Globals.GLOBAL_TIMER;
import static bullethell.Globals.HEIGHT;
import static bullethell.Globals.WIDTH;
import static bullethell.Globals.rand;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Timer;

import bullethell.Audio;
import bullethell.Entity;
import bullethell.GameObject;
import bullethell.GameSolid;
import bullethell.GameState;
import bullethell.Globals;
import bullethell.Parallax;
import bullethell.Player;
import bullethell.Projectile;
import bullethell.Spritesheet;
import bullethell.enemies.EnemyID;
import bullethell.enemies.JellyFishBoss;
import bullethell.items.weapons.ExampleSword;
import bullethell.movement.ChargePath;
import bullethell.movement.CirclePath;
import bullethell.movement.Direction;
import bullethell.movement.LinePath;
import bullethell.movement.Path;
import bullethell.movement.SeekingPath;
import bullethell.movement.StraightPath;

public final class ErnestoBoss implements Scene, Bossfight {

    private Timer switchTimer, secondSwitchTimer, switchToFirst, switchToUltimate, switchToFinal;
    private Timer slowGrid, sinkHole, bossDash, bossRadial, bossLaser, tpDash, tpSit, dashTrail, 
      ultStar, ultTP
    ;

    private static final ErnestoBoss ERNESTO_BOSS = new ErnestoBoss();

    private JellyFishBoss boss;

    private static final Player player = Player.get();

    public static ErnestoBoss get() {
        return ERNESTO_BOSS;
    }

    private ErnestoBoss() { }

    @Override
    public GameState getState() {
        return GameState.BOSS;
    }

    public int getAnchorX() {
        return 0;
    }

    public int getAnchorY() {
        return 0;
    }

    @Override
    public void start(int x, int y) {
        Globals.setGameState(GameState.CUTSCENE);
        Globals.main.setBackground(new java.awt.Color(22, 22, 22));
        Globals.playsound(Audio.SWITCH);
        Globals.GLOBAL_TIMER.removeActionListener(World.get());
        Thread lightfx = new Thread(() -> {
            GameObject obj = new GameObject(null, 101) {
                int timesPerformed = 0;
                int alpha = 255;
                @Override
                public void paint(Graphics g) {
                    g.setColor(new Color(0, 0, 0, alpha));
                    g.fillRect(Player.cameraX(), Player.cameraY(), Globals.SCREEN_WIDTH, Globals.SCREEN_HEIGHT);
                 
                    if (timesPerformed > 100) {
                        alpha--;
                    }
                    if (alpha < 0) {
                        alpha = 0;
                    }
                    timesPerformed++;
                }
            };
            obj.setAlwaysDraw(true);

            new Parallax("JellyBG");
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread other = new Thread(() -> {
        try {
            lightfx.join(0);
            Globals.setGameState(GameState.BOSS);
            Globals.playsound(Audio.JELLY_SONG);

            player.resetAdren();

            boss = (JellyFishBoss) EnemyID.getEnemy(EnemyID.JELLY_FISH_BOSS);
            boss.setLocation(WIDTH / 2 - boss.getWidth() / 2, HEIGHT / 2 - boss.getHeight() / 2);
            boss.kill();
    
            class LaserProj extends Projectile {
                
                static float rotateIncr;
                static int animation = 0;

                static BufferedImage baseSprite = Globals.getImage("LaserBlue");
                static BufferedImage resultSprite = new BufferedImage(baseSprite.getWidth(), 5000, BufferedImage.TYPE_INT_ARGB);
                static {
                    resultSprite.getGraphics().drawImage(baseSprite, 0, 0, resultSprite.getWidth(), resultSprite.getHeight(), null);
                }

                LaserProj(float rotationDegrees) {
                    super(new Spritesheet(resultSprite, 10, 2), Path.DEFAULT_PATH, DEFAULT_SPEED, 75);
                    setHitbox(new Rectangle(x, y, resultSprite.getWidth() / 10, WIDTH));

                    rotateIncr = 0.03f;
                    setLocation(boss.getCenterX(), boss.getCenterY());
                    rotate(rotationDegrees);

                    setAlwaysDraw(true);
                    setDrawIndicator(true);
                    setPierce(Integer.MAX_VALUE);
                    setIndicatorDelay(750);
                    setIndicatorLifespan(750);

                    setAnimation(animation);
                    getCurrentAnimation().start();
                    getCurrentAnimation().setFrameRate(3);
                }

                @Override
                public void paint(Graphics g) {
                    if (drawIndicator && (age < indicatorLifespan || indicatorLifespan == 0)) {
                        g.setColor(new Color(128,128,128,50));
                        new StraightPath((int) Math.toDegrees(rotationDeg)).drawIndicator(g, new Point(boss.getCenterX(), boss.getCenterY()));
                    }
                    if ((indicatorProjDelay == 0) || (age >= indicatorProjDelay)) {
                        if (rotationDeg > 0 || opacity != 1f) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                            g2.rotate(rotationDeg, getCenterX(), getCenterY());
                            g2.drawImage(animations[currentAnimation].getFrame(), x, y, null);
                            g2.dispose();
                            return;
                        }
                        g.drawImage(animations[currentAnimation].getFrame(), x, y, null);
                    }
                }

                @Override
                public boolean readyToKill() {
                    return false;
                }
                
                @Override
                public boolean onCollision(GameSolid obj) {
                    if (pierce < 0) {
                        permakill();
                        return false;
                    }
                    
                    if ((indicatorProjDelay != 0 && age < indicatorProjDelay) ||
                      (obj instanceof Projectile) ||
                      (obj instanceof ExampleSword.AtkBox)) {
                        return false;
                    }
            
                    if (obj instanceof GameSolid && !(obj instanceof Entity)) {
                        permakill();
                        return false;
                    }
            
                    Entity entity = (Entity) obj;
                    boolean successful = false;
                    
                    if (friendly() != entity.friendly() && !successful && !entity.isInvicible()) {
                        if (pierce != Integer.MAX_VALUE) {
                            pierce--;
                        }
                        entity.registerDMG(dmg);
                        if (friendly()) {
                            Player.get().registerDealtDMG(dmg, this);
                        }
                        hits.add(entity);
            
                        if (pierce < 0) {
                            permakill();
                            return false;
                        }
                    }
                    return !successful;
                }

                @Override
                public void update() {
                    age += GLOBAL_TIMER.getDelay();
                    if ((indicatorProjDelay != 0 && age < indicatorProjDelay) || !isAlive()) {
                        return;
                    }
                    else {
                        dmg = constDMG;
                    }
                    move();
                    if (readyToKill()) {
                        permakill();
                    } 
                    rotate(rotateIncr);
                }
            }
    
            slowGrid = new Timer(500, null);
            sinkHole = new Timer(50, null);
            bossDash = new Timer(250, null);
            bossRadial = new Timer(250, null);
            bossLaser = new Timer(50, null);
            tpDash = new Timer(50, null);
            tpSit = new Timer(50, null);
            dashTrail = new Timer(50, null);
            ultStar = new Timer(200, null);
            ultTP = new Timer(50, null);
    
            slowGrid.addActionListener(new TimerListener(slowGrid, sinkHole, 12) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 1; i <= 5; i++) {
                        Projectile proj = new ErnestoGridProj();
                        if (rand.nextBoolean()) {
                            proj.setLocation(0, i * proj.getHeight() * 4 + rand.nextInt(-50, 50));
                            proj.setPath(new StraightPath(Direction.RIGHT));
                        } else {
                            proj.setLocation(WIDTH, i * proj.getHeight() * 4 + rand.nextInt(-50, 50));
                            proj.setPath(new StraightPath(Direction.LEFT));
                        }
                    }
                    for (int i = 1; i <= 5; i++) {
                        Projectile proj;
                        proj = new ErnestoGridProj();
                    
                        if (rand.nextBoolean()) {
                            proj.setLocation(i * proj.getWidth() * 7 + rand.nextInt(-100, 100), 0);
                            proj.setPath(new StraightPath(Direction.DOWN));
                        } else {
                            proj.setLocation(i * proj.getWidth() * 7 + rand.nextInt(-100, 100), HEIGHT);
                            proj.setPath(new StraightPath(Direction.UP));
                        }
                    }
                    timesPerformed++;
                    if (timesPerformed >= maxTimes) {
                        end();
                    }
                }
    
                @Override
                public void end() {
                    timer.stop();
                    if (nextTimer != null) nextTimer.start();
                    Entity.removeAll(obj -> obj instanceof ErnestoProj);
                }
            });
            sinkHole.addActionListener(new TimerListener(sinkHole, bossDash, 140) {
                Point centerPoint;
                Point point;
                Path circlePath;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (timesPerformed == 0) {
                        centerPoint = new Point(player.getCenterX(), player.getCenterY());
                        point = new Point(centerPoint.x - WIDTH / 2, centerPoint.y - HEIGHT / 2);
                        circlePath = new CirclePath(centerPoint, point, true);
                    }

                    for (int i = 0; i < 2; i++) {
                        Point newPoint = circlePath.move(timesPerformed % 70 > 35 ? -2 : 2);
                        point.x += newPoint.x;
                        point.y += newPoint.y;
                        try {
                            Projectile proj = new ErnestoPhase2Proj();
                            proj.setRange((int) (Point.distance(point.x, point.y, centerPoint.x, centerPoint.y)));
                            proj.setLocation(point);
                            proj.setSpeed(20);
                            proj.setDrawIndicator(true);
                            proj.setIndicatorDelay(100 / Globals.TIMER_DELAY);
                            Line2D line = new Line2D.Float(point.x, point.y, centerPoint.x, centerPoint.y);
                            LinePath path = new LinePath(line, true);
                            proj.setPath(path);
                            if (i == 0) {
                                proj.move();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    timesPerformed++;
                    if (timesPerformed >= maxTimes) {
                        end();
                    }
                }
    
                @Override
                public void end() {
                    boss.revive();
                    timer.stop();
                    if (nextTimer != null) nextTimer.start();
                    Entity.removeAll(obj -> obj instanceof Projectile);
                }
            });
    
            // first phase
            bossRadial.addActionListener(new TimerListener(bossRadial, bossDash, 8) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (boss.getPath().isActive() && boss.getPath() instanceof LinePath) {
                        return;
                    }

                    if (timesPerformed == 0 && Point.distance(boss.getCenterX(), boss.getCenterY(),
                    player.getCenterX(), player.getCenterY()) < 400) {
                      boss.setPath(new LinePath(player.getCenterX(), player.getCenterY(), 
                        boss.getCenterX(), boss.getCenterY(), 300));
                      return;
                  }

                    if (timesPerformed == 0) {
                        SeekingPath path = new SeekingPath(boss, player);
                        boss.setPath(path);
                        boss.setSpeed(3);
                    }
    
                    Point point = new Point(boss.getX(), boss.getY() + boss.getHeight() / 2);;
                    Point centerPoint = new Point(boss.getCenterX(), boss.getCenterY());
                    Path circlePath = new CirclePath(centerPoint, point, true);
    
                    if (timesPerformed % 2 == 1) {
                        Point newPoint = circlePath.move(2);
                        point.x += newPoint.x;
                        point.y += newPoint.y;
                    }
                    for (int i = 0; i < 16; i++) {
                        centerPoint = new Point(boss.getCenterX(), boss.getCenterY());
                        Point newPoint = circlePath.move(4);
                            point.x += newPoint.x;
                            point.y += newPoint.y;
                            Projectile proj = new ErnestoProj();
                            proj.setPath(new LinePath(new Line2D.Float(centerPoint.x, centerPoint.y, point.x, point.y), true));
                            proj.setLocation(point);
                            proj.setSpeed(10);
                    }
    
                    timesPerformed++;
                    if (timesPerformed >= maxTimes) {
                        timesPerformed = 0;
                        timer.stop();
                        if (rand.nextBoolean()) {
                            nextTimer.start();
                        } else {
                            bossLaser.start();
                        }
                    }
                }
            
                @Override
                public void end() {
                    timesPerformed = 0;
                    timer.stop();
                    if (rand.nextBoolean()) {
                        nextTimer.start();
                    } else {
                        bossLaser.start();
                    }
                }
            });
            bossDash.addActionListener(new TimerListener(bossDash, bossRadial, 1) {
                boolean first = false;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!first) {
                        Rectangle topHitbox = new Rectangle(boss.getX() + 100, boss.getY() + 62, 123, 42);
                        Rectangle bottomHitbox = new Rectangle(boss.getX() + 66, boss.getY() + 101, 186, 76);
                        Area area = new Area(topHitbox);
                        area.add(new Area(bottomHitbox));
                        boss.setHitbox(area);
                        first = true;
                    }

                    if (timesPerformed == 0) {
                        ChargePath path = new ChargePath(boss, player, 
                          300 / Globals.TIMER_DELAY, 100 / Globals.TIMER_DELAY);
                        path.setNumOfCharges(5);
                        boss.setPath(path);
                        boss.setSpeed(20);
                    }
    
                    timesPerformed++;
                    if (!boss.getPath().isActive()) {
                        end();
                    }
                }
    
                @Override
                public void end() {
                    timesPerformed = 0;
                    timer.stop();
                    if (rand.nextBoolean()) {
                        nextTimer.start();
                    } else {
                        bossLaser.start();
                    }
                }
            });
            bossLaser.addActionListener(new TimerListener(bossLaser, bossDash, 60) {
                LaserProj[] proj = new LaserProj[4];
                            
                float numDown = 0.08f;
                boolean flipped = false;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!flipped && timesPerformed == 0) {
                        boss.setSpeed(20);
                        boss.setPath(new LinePath(new Line2D.Float(boss.getX(), boss.getY(), WIDTH / 2 - boss.getWidth() / 2, HEIGHT / 2 - boss.getHeight() / 2),
                          false));
                        flipped = !flipped;
                    }
                    if (!boss.getPath().isActive() && timesPerformed == 0) {
                        for (int i = 0; i < proj.length; i++) {
                            proj[i] = new LaserProj(i * 90);
                        }
                        flipped = !flipped;
                    } else if (boss.getPath().isActive()) return;
                    timesPerformed++;
                    
                    if (timesPerformed <= 40)
                        LaserProj.rotateIncr += numDown;
                    
                    if (timesPerformed >= maxTimes) {
                        end();
                    }
                }
    
                @Override
                public void end() {
                    timesPerformed = 0;	
                    timer.stop();
                    if (rand.nextBoolean()) {
                        nextTimer.start();
                    } else {
                        bossRadial.start();
                    }
                    for (int i = 0; i < proj.length; i++) {
                        if (proj[i] != null) proj[i].permakill();
                    }
                }
            });
            slowGrid.setInitialDelay(100);
            bossDash.setInitialDelay(500);
            
            // second phase
            switchTimer = new Timer(39 * 1000, null);
            switchTimer.addActionListener((ActionEvent e) -> {
                ((TimerListener) bossRadial.getActionListeners()[0]).end();
                ((TimerListener) bossDash.getActionListeners()[0]).end();
                ((TimerListener) bossLaser.getActionListeners()[0]).end();
                bossRadial.stop();
                bossDash.stop();
                bossLaser.stop();
                boss.setPath(Path.DEFAULT_PATH);
                tpDash.start();
                switchTimer.stop();
                ErnestoProj.animation = 1;
            });
            switchTimer.start();
    
            tpDash.addActionListener(new TimerListener(tpDash, tpSit, 2) {
                int timesCharged = 0;
    
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (boss.fade(0.1f)) {
                        Point point;
                        do {
                        switch (rand.nextInt(1, 9)) {
                            default:
                                point = new Point(player.getCenterX(), player.getCenterY() + 400);
                                break;
                            case 2:
                                point = new Point(player.getCenterX(), player.getCenterY() - 400);
                                break;
                            case 3:
                                point = new Point(player.getCenterX() + 400, player.getCenterY());
                                break;
                            case 4:
                                point = new Point(player.getCenterX() - 400, player.getCenterY());
                                break;
                            case 5:
                                point = new Point(player.getCenterX() + 300, player.getCenterY() + 300);
                                break;
                            case 6:
                                point = new Point(player.getCenterX() - 300, player.getCenterY() + 300);
                                break;
                            case 7:
                                point = new Point(player.getCenterX() + 300, player.getCenterY() - 300);
                                break;
                            case 8:
                                point = new Point(player.getCenterX() - 300, player.getCenterY() - 300);
                                break;
                        }
                        boss.setLocation(point.x - boss.getWidth() / 2, point.y - boss.getHeight() / 2);
                        } while (boss.outOfBounds());
                        SeekingPath seekingPath = new SeekingPath(boss, player);
                        seekingPath.setLoop(false);
                        boss.setSpeed(10);
                        boss.setPath(seekingPath);
                        timesCharged++;
                    }
                    timesPerformed++;
    
                    if (timesCharged > maxTimes) {
                        end();
                    }
                }
    
                @Override
                void end() {
                    boss.setPath(Path.DEFAULT_PATH);
                    timesPerformed = 0;
                    timesCharged = 0;
                    timer.stop();
                    nextTimer.start();
                }
            });
            tpSit.addActionListener(new TimerListener(tpSit, tpDash, 2) {
                int timeSinceFade = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (boss.fade(0.1f)) {
                        do {
                            boss.setLocation(rand.nextInt(0, WIDTH - boss.getWidth()), rand.nextInt(0, HEIGHT - boss.getHeight()));
                        } while (Point.distance(player.getCenterX(), player.getCenterY(), boss.getCenterX(), boss.getCenterY()) < 400 || boss.outOfBounds());
                        timeSinceFade++;
                    }
                    if (timeSinceFade == 10) {
                        Projectile proj = new ErnestoProj();
                        proj.setLocation(boss.getCenterX(), boss.getCenterY());
                        proj.setPath(new SeekingPath(proj, player));
                        proj.setSpeed(12);
                        proj.setLifeSpan(900 / Globals.TIMER_DELAY);
                        timesPerformed++;
                        timeSinceFade = 0;
                    }
                    if (timeSinceFade > 0) timeSinceFade++;
    
                    if (timesPerformed >= maxTimes) {
                        end();
                    }
                }
    
                @Override
                void end() {
                    timesPerformed = 0;
                    timer.stop();
                    nextTimer.start();
                }
            });
        
            // third phase
            secondSwitchTimer = new Timer(50 * 1000, null);
            secondSwitchTimer.addActionListener((ActionEvent e) -> {
                ((TimerListener) tpDash.getActionListeners()[0]).end();
                ((TimerListener) tpSit.getActionListeners()[0]).end();
                tpDash.stop();
                tpSit.stop();
                dashTrail.start();
                secondSwitchTimer.stop();
            });
            // secondSwitchTimer.start();
    
            dashTrail.addActionListener(new TimerListener(dashTrail, null, 1) {
                int chargeCountdown = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (timesPerformed == 0 || boss.outOfBounds()) {
                        boss.setPath(new SeekingPath(boss, player));
                        boss.setSpeed(0);
                        boss.setDrawIndicator(true);
                        chargeCountdown++;
                        if (timesPerformed == 0 && chargeCountdown < 30) return;
                    }
                    Line2D line = new Line2D.Float(boss.getCenterX(), boss.getCenterY(), player.getCenterX(), player.getCenterY());
                    if (chargeCountdown % 30 == 0) {
                        LinePath linePath = new LinePath(line, true); 
                        boss.setPath(linePath);
                        boss.setSpeed(100);
                        chargeCountdown++;
                    }
                    double slope = (line.getY2() - line.getY1()) / (line.getX2() - line.getX1());
                    slope = -1 / slope;
                    for (int i = 0; i < 2; i++) {
                        Projectile proj = new ErnestoProj() {
                            @Override
                            public void move() {
                                Point newCoords = path.move(speed);
                                int newX = x - newCoords.x;
                                int newY = y - newCoords.y;
                                distanceTraveled += Point.distance(newX, newY, x, y);
                                setLocation(newX, newY);
                            }
                        };
                        proj.setLocation(boss.getCenterX() - proj.getWidth() / 2, boss.getCenterY() - proj.getHeight() / 2);
                        
                        proj.setPath(boss.getPath());
                    }
                    timesPerformed++;
                }
    
                @Override
                void end() {
                    chargeCountdown = 0;
                    timesPerformed = 0;
                    boss.setDrawIndicator(false);
                }
            });
            
            // return to first phase temporarily
            switchToFirst = new Timer(62 * 1000, null);
            switchToFirst.addActionListener((ActionEvent e) -> {
                ((TimerListener) tpDash.getActionListeners()[0]).end();
                ((TimerListener) tpSit.getActionListeners()[0]).end();
                tpDash.stop();
                tpSit.stop();
                bossLaser.start();
                switchToFirst.stop();
    
                boss.setOpacity(1);
                ErnestoProj.animation = 0;
            });
            switchToFirst.start();
    
            // fourth phase
            switchToUltimate = new Timer(83 * 1000, null);
            switchToUltimate.addActionListener((ActionEvent e) -> {
                ((TimerListener) bossRadial.getActionListeners()[0]).end();
                ((TimerListener) bossDash.getActionListeners()[0]).end();
                ((TimerListener) bossLaser.getActionListeners()[0]).end();
                bossRadial.stop();
                bossDash.stop();
                bossLaser.stop();
                
                LaserProj.animation = 1;
                ErnestoProj.animation = 2;

                boss.switchAlpha = false;
                boss.timesPerformed = 0;
                boss.setSpeed(20);
                boss.setPath(new LinePath(new Line2D.Float(boss.getX(), boss.getY(), WIDTH / 2 - boss.getWidth() / 2, HEIGHT / 2 - boss.getHeight() / 2), false));
                boss.setAnimation(1);
                boss.getCurrentAnimation().start();
                switchTimer.stop();
                ultStar.start();
            });
            switchToUltimate.start();
    
            ultStar.addActionListener(new TimerListener(ultStar, ultTP, 57) {
                boolean done = false;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (boss.getPath().isActive()) {
                        timesPerformed++;
                        return;
                    }
                    if (!done) {
                        new LaserProj(90);
                        new LaserProj(270);
                        LaserProj.rotateIncr = 0.4f;
                        done = true;
                    }
                    
                    int[] xpoints = {9, 59, 38, 88, 109, 130, 180, 159, 209, 159, 180, 130, 109, 88, 38, 59};
                    int[] ypoints = {109, 130, 180, 159, 209, 159, 180, 130, 109, 88, 38, 59, 9, 59, 38, 88};
                    Projectile[] projs = new Projectile[xpoints.length];
    
                    class OctagramProj extends ErnestoProj {
                        int i;
                        OctagramProj(int index) throws IOException {
                            super();
                            this.i = index;
                            dmg = 100;
                        }
    
                        @Override
                        public void paint(Graphics g) { 
                            g.setColor(new Color(128,128,128,50));
                            
                            if (i > 0) {
                                g.drawLine(getCenterX(), getCenterY(), projs[i - 1].getCenterX(), projs[i - 1].getCenterY());
                            } else { 
                                g.drawLine(getCenterX(), getCenterY(), 
                                  projs[projs.length - 1].getCenterX(), projs[projs.length - 1].getCenterY());
                            }
                            super.paint(g);
                            g.setColor(DEFAULT_COLOR);
                        }
                    }
                    for (int i = 0; i < xpoints.length; i++) {
                            xpoints[i] -= 110;
                            ypoints[i] -= 110;
                            xpoints[i] *= 0.5f;
                            ypoints[i] *= 0.5f;
    
                            AffineTransform at = new AffineTransform();
                            at.setToRotation(Math.toRadians(timesPerformed * 6), 0, 0);
                            Point result = new Point(0,0);
                            at.transform(new Point(xpoints[i], ypoints[i]), result);
                            
                            xpoints[i] = result.x;
                            ypoints[i] = result.y;	
                            try {
                                Projectile proj = new OctagramProj(i);
                                proj.setLocation(WIDTH / 2 + xpoints[i] - proj.getWidth() / 2, HEIGHT / 2 + ypoints[i] - proj.getHeight() / 2);
                                proj.setPath(new LinePath(new Line2D.Float(WIDTH / 2, HEIGHT / 2, proj.getCenterX(), proj.getCenterY()), true));
                                projs[i] = proj;
                                proj.setSpeed(10);
                            } catch (IOException ioe) { ioe.printStackTrace(); }
                        }
                    timesPerformed++;
                    if (timesPerformed >= maxTimes) {
                        end();
                    }
                }
    
                @Override
                void end() {
                    timesPerformed = 0;
                    timer.stop();
                    if (nextTimer != null) {
                        nextTimer.start();
                    }
                    Entity.removeAll(obj -> obj instanceof Projectile);
                }
            });
            ultTP.addActionListener(new TimerListener(ultTP, null, 0) {
                int timeSinceFade = 0;
                static final int BOSS_DISTANCE = 900;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (boss.fade(0.2f) & timeSinceFade < 5) {
                        do {
                            boss.setLocation(rand.nextBoolean() ? rand.nextInt(player.getCenterX() - BOSS_DISTANCE, player.getCenterX())
                                            : rand.nextInt(player.getCenterX() + BOSS_DISTANCE), 
                                            rand.nextBoolean() ? rand.nextInt(player.getCenterY() - BOSS_DISTANCE, player.getCenterY())
                                            : rand.nextInt(player.getCenterY() + BOSS_DISTANCE));
                        } while (player.getLocation().distance(boss.getLocation()) < BOSS_DISTANCE || boss.outOfBounds());
                        timeSinceFade++;
                    }
                    if (timeSinceFade >= 5) {
                        int[] xpoints = {9, 59, 38, 88, 109, 130, 180, 159, 209, 159, 180, 130, 109, 88, 38, 59};
                        int[] ypoints = {109, 130, 180, 159, 209, 159, 180, 130, 109, 88, 38, 59, 9, 59, 38, 88};
                        Projectile[] projs = new Projectile[xpoints.length];
                        timesPerformed++;
                        if (timesPerformed % 2 == 0)
                            timeSinceFade = 0;
                        boss.setPath(new SeekingPath(boss, player));
                        for (int i = 0; i < xpoints.length; i += 2) {
                            xpoints[i] -= 110;
                            ypoints[i] -= 110;
                            xpoints[i] *= 2;
                            ypoints[i] *= 2;
                            Projectile proj = new ErnestoProj();
                            proj.setDMG(100);
                            proj.setSpeed(15);
                            proj.setLocation(boss.getCenterX() + xpoints[i] - proj.getWidth() / 2, boss.getCenterY() + ypoints[i] - proj.getHeight() / 2);
                            if (timeSinceFade == 5) {
                                boss.setSpeed(5);
                                proj.setPath(new SeekingPath(proj, player, false));
                            } else {
                                proj.setPath(new LinePath(new Line2D.Float(boss.getCenterX(), boss.getCenterY(), proj.getCenterX(), proj.getCenterY()), true));
                            }
                            projs[i] = proj;
                        }
                    }
                    if (timeSinceFade > 0) timeSinceFade++;
                }
    
                @Override
                void end() {
                    timeSinceFade = 0;
                    timesPerformed = 0;
                    timer.stop();
                }
            });
            ultStar.setInitialDelay(3000);
    
            switchToFinal = new Timer(109 * 1000, null);
            switchToFinal.addActionListener((ActionEvent e) -> {
                ((TimerListener) ultTP.getActionListeners()[0]).end();
                switchToFinal.stop();
                tpSit.start();
            });
            switchToFinal.start();

            slowGrid.start();
        } catch (Exception e) { e.printStackTrace(); }
        });
        lightfx.start();
        other.start();
    }

    public void end() {
        Globals.setGameState(GameState.DEFAULT);
        Globals.main.setBackground(java.awt.Color.WHITE);
        Globals.GLOBAL_TIMER.addActionListener(World.get());
        Entity.removeAll(obj -> obj instanceof Projectile);
        Globals.stopsound(Audio.JELLY_SONG);
        stopTimers();
    }

    private void stopTimers() {
        Timer[] timers = {switchTimer, secondSwitchTimer, switchToFirst, switchToUltimate, switchToFinal, slowGrid, sinkHole, 
            bossDash, bossRadial, bossLaser, tpDash, tpSit, dashTrail, ultStar, ultTP};
          for (Timer timer : timers) {
              if (timer != null) {
                  timer.stop();
              }
          }
    }

    private class ErnestoPhase2Proj extends ErnestoGridProj {

        public ErnestoPhase2Proj() throws IOException {
            super();
            dmg = 75;
        }
        
        @Override
        public boolean readyToKill() {
            return distanceTraveled >= range;
        }
    }

    private class ErnestoGridProj extends ErnestoProj {

        static int num = 0;

        public ErnestoGridProj() {
            super();
            setAnimation(num++ % 3);
            getCurrentAnimation().setFrameRate(10);
        }

        @Override
        public void update() {
            age++;
            if ((indicatorProjDelay != 0 && age < indicatorProjDelay) || !isAlive()) {
                return;
            } else {
                dmg = constDMG;
            }
            move();
            if (readyToKill()) {
                permakill();
            }
            
            rotate((float) (Math.toDegrees(Math.toRadians(360) - rotationDeg)));
            Point point = getPath().move(speed);
            rotate((float) -Math.toDegrees(Math.atan2(point.x, point.y)) + 180);
        }
    }

    private class ErnestoProj extends Projectile {

        static int animation = 0;

        public ErnestoProj() {
            super(new Spritesheet(Globals.getImage("Jellyfish"), 8, 3), Path.DEFAULT_PATH);
            dmg = 100;
            speed = 4;
            getCurrentAnimation().setFrameRate(10);
            getCurrentAnimation().start();
        }

        @Override
        public void update() {
            super.update();

            if (currentAnimation != animation) {
                setAnimation(animation);
                getCurrentAnimation().setFrameRate(10);
            }

            rotate((float) (Math.toDegrees(Math.toRadians(360) - rotationDeg)));
            Point point = getPath().move(speed);
            rotate((float) -Math.toDegrees(Math.atan2(point.x, point.y)) + 180);
        }
    }

    private abstract class TimerListener implements ActionListener {
        Timer timer;
        Timer nextTimer;
        int timesPerformed = 0;
        int maxTimes;
        TimerListener(Timer timer, Timer nextTimer, int maxTimes) {
            this.timer = timer;
            this.nextTimer = nextTimer;
            this.maxTimes = maxTimes;
        }

        abstract void end();
    }

    @Override
    public boolean isActive() {
        return boss.isAlive();
    }
}
