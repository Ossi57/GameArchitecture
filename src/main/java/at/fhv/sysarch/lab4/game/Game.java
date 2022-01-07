package at.fhv.sysarch.lab4.game;

import java.util.*;

import at.fhv.sysarch.lab4.physics.*;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.geometry.Vector2;

public class Game implements BallsCollisionListener, BallPocketedListener, ObjectsRestListener {
    private final Renderer renderer;
    private final PhysicsEngine physic;

    private static final double whiteXDefault = Table.Constants.WIDTH * 0.25;
    private static final double whiteYDefault = 0d;


    //stage
    private Set<Ball> perforatedBalls = new HashSet<>();
    private Table table;

    //flags
    private boolean respawnBall = false;
    private boolean nonWhiteBallStroken = false;
    private boolean noObjectHit = true;
    private boolean roundNotFinished = true;
    private boolean ballsInMotion = false;

    //player information
    private Player currentPlayer = Player.Player1;
    private int playerCount1;
    private int playerCount2;

    enum Player{
        Player1, Player2;

        public Player switchPlayer() {
            return this.equals(Player.Player1) ? Player.Player2 : Player.Player1;
        }
    }

    public Game(Renderer renderer, PhysicsEngine physic) {
        this.renderer = renderer;
        this.physic = physic;
        this.initWorld();
    }

    public void onMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        double pX = this.renderer.screenToPhysicsX(x);
        double pY = this.renderer.screenToPhysicsY(y);

        if(!ballsInMotion && respawnBall){
            Ball.WHITE.setPosition(pX, pY);
            renderer.addBall(Ball.WHITE);
            respawnBall = false;
            return;
        }
        //only allow the start of the cue rendering when the round is finished
        if(!ballsInMotion){
            this.renderer.activateCue();
            this.renderer.setCueStartPoints(x, y);
            this.renderer.setCueEndPoints(x, y);
        }
    }

    public void onMouseReleased(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        //only allow the finishing of a cue when round is finished
        if(!ballsInMotion) {
            finishCueAndStartMotion();
        }
    }

    private void finishCueAndStartMotion() {
        this.renderer.deactivateCue();
        //todo calculate force coordinates and hand it to physics
        Vector2 origin = this.renderer.getCueStartPoints();
        origin = new Vector2(renderer.screenToPhysicsX(origin.x), renderer.screenToPhysicsY(origin.y));
        //calculate original cue *-1 as direction is the opposite of the drawn cue
        Vector2 directionEnd = renderer.getCueEndPoints();
        Vector2 direction = new Vector2(renderer.screenToPhysicsX(directionEnd.x), renderer.screenToPhysicsY(directionEnd.y)).subtract(origin).multiply(-1);

        //set balls in motion
        Optional<Ball> hitBallOpt = physic.performStrike(origin, direction);

        //check if a non-white ball was hit
        hitBallOpt.ifPresent(
                hitBall -> {
                    nonWhiteBallStroken = !hitBall.isWhite();
                }
        );
    }


    public void setOnMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        this.renderer.setCueEndPoints(x, y);
    }

    private void placeBalls(List<Ball> balls, boolean skipTopSpot) {
        Collections.shuffle(balls);

        // positioning the billard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2*Ball.Constants.RADIUS*2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            renderer.addBall(b);
            //TODO: add Object/Body to World Model
            physic.addObject(b.getBody());

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
            //skip top spot if the flag is set to true
            if(skipTopSpot && colSize == 1)
                return;
        }
    }

    private void initWorld() {
        playerCount1 = 0;
        playerCount2 = 0;

        List<Ball> balls = new ArrayList<>();
        
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
        }

        //top spot is not being skipped, therefore false as flag
        this.placeBalls(balls, false);

        Ball.WHITE.setPosition(whiteXDefault, whiteYDefault);
        renderer.addBall(Ball.WHITE);
        physic.addObject(Ball.WHITE.getBody());
        
        table = new Table();
        renderer.setTable(table);
        physic.addObject(table.getBody());
    }

    @Override
    public boolean onBallPocketed(Ball b) {
        //stop ball
        b.getBody().setLinearVelocity(0d, 0d);

        if(b.isWhite()){
            renderer.removeBall(b);
            //reposition white ball (s. onMousePressed())
            respawnBall = true;
        }
        else{
            physic.removeObject(b.getBody());
            renderer.removeBall(b);
            //increase player score if no fouls occured
            if(!respawnBall && !nonWhiteBallStroken && !noObjectHit){
                switch (currentPlayer){
                    case Player1:
                        renderer.setPlayer1Score(++playerCount1);
                        break;
                    case Player2:
                        renderer.setPlayer2Score(++playerCount2);
                        break;
                }
            }
            perforatedBalls.add(b);
        }
        return !b.isWhite();
    }

    @Override
    public void onBallsCollide(Ball b1, Ball b2) {
        if((b1.isWhite() && !b2.isWhite()) || (!b1.isWhite() && b2.isWhite())){
            noObjectHit = false;
        }
    }

    @Override
    public void onEndAllObjectsRest() {
        //reset foul message to nothing
        renderer.setFoulMessage("");
        roundNotFinished = true;
        ballsInMotion = true;
    }

    @Override
    public void onStartAllObjectsRest() {
        //check if 14 or more balls have been perforated
        if(perforatedBalls.size() >= 14){
            resetGame();
        }

        //only check for fouls if ball was in motion before
        if(ballsInMotion){
            //restart round
            roundNotFinished = false;
            ballsInMotion = false;
        }
        if(roundNotFinished){
            return;
        }

        //check fouls
        //check if white ball was pocketed
        if(respawnBall){
            announceFoul("White ball pocketed! Click on the field to respawn the ball.");
        }
        //check if a non-white ball was stroke first
        else if(nonWhiteBallStroken){
            announceFoul("Non-white ball was hit first!");
        }
        //check if no object was hit
        else if(noObjectHit){
            announceFoul("No object hit!");
        }

        //reset all flags for new round
        nonWhiteBallStroken = false;
        noObjectHit = true;
        roundNotFinished = true;
        ballsInMotion = false;
    }

    private void resetGame() {
        //reset table for resetting stage (important for physics reset)
        physic.removeObject(table.getBody());
        table = new Table();
        renderer.setTable(table);
        physic.addObject(table.getBody());

        Optional<Ball> nonWhiteBallLeft = Arrays.stream(Ball.values()).filter(
                ball -> !perforatedBalls.contains(ball)
        ).findFirst();

        //reset position of white ball to default
        renderer.removeBall(Ball.WHITE);
        Ball.WHITE.setPosition(whiteXDefault, whiteYDefault);
        renderer.addBall(Ball.WHITE);

        //place all balls except the left non white ball
        List<Ball> balls = new ArrayList<>();
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
        }
        nonWhiteBallLeft.ifPresent(balls::remove);
        placeBalls(balls, true);
        perforatedBalls = new HashSet<>();
    }

    private void announceFoul(String msg) {
        switch (currentPlayer){
            case Player1:
                renderer.setPlayer1Score(--playerCount1);
                break;
            case Player2:
                renderer.setPlayer2Score(--playerCount2);
                break;
        }
        currentPlayer = currentPlayer.switchPlayer();
        renderer.setFoulMessage(msg + " " + currentPlayer + " is the next player!");
    }
}