package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.fhv.sysarch.lab4.physics.Physic;
import at.fhv.sysarch.lab4.physics.PhysicsEngine;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.geometry.Vector2;

public class Game {
    private final Renderer renderer;
    private final PhysicsEngine physic;

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
        this.renderer.activateCue();
        this.renderer.setCueStartPoints(x, y);
        this.renderer.setCueEndPoints(x, y);
    }

    public void onMouseReleased(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        this.renderer.setCueEndPoints(x, y);

        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);

        this.renderer.deactivateCue();
        //todo calculate force coordinates and hand it to physics
        Vector2 origin = this.renderer.getCueStartPoints();
        origin = new Vector2(renderer.screenToPhysicsX(origin.x), renderer.screenToPhysicsY(origin.y));
        //calculate original cue *-1 as direction is the opposite of the drawn cue
        Vector2 directionEnd = renderer.getCueEndPoints();
        Vector2 direction = new Vector2(renderer.screenToPhysicsX(directionEnd.x), renderer.screenToPhysicsY(directionEnd.y)).subtract(origin).multiply(-1);
        physic.performStrike(origin, direction);
    }

    public void setOnMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);

        // * 100 as original length is insufficient
        this.renderer.setCueEndPoints(x, y);
    }

    private void placeBalls(List<Ball> balls) {
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
            //TODO: add Object/BOdy to World Model
            physic.addObject(b.getBody());

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {
        List<Ball> balls = new ArrayList<>();
        
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
        }
       
        this.placeBalls(balls);

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);

        renderer.addBall(Ball.WHITE);
        physic.addObject(Ball.WHITE.getBody());
        
        Table table = new Table();
        renderer.setTable(table);
        physic.addObject(table.getBody());
    }
}