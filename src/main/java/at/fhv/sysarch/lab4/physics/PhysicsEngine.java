package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import at.fhv.sysarch.lab4.rendering.FrameListener;
import org.dyn4j.dynamics.*;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhysicsEngine implements StepListener, ContactListener, FrameListener {

    private final World world;
    private BallPocketedListener ballPocketedListener;
    private BallsCollisionListener ballsCollisionListener;
    private ObjectsRestListener objectsRestListener;

    public PhysicsEngine(){
        this.world = new World();
        world.addListener(this);
        world.setGravity(World.ZERO_GRAVITY);
    }

    public void addObject(Body body){
        this.world.addBody(body);
    }

    public Optional<Ball> performStrike(Vector2 origin, Vector2 direction) {
        Ray ray = new Ray(origin, direction);
        List<RaycastResult> results = new ArrayList<>();

        boolean hit = this.world.raycast(ray, 0, true, false, results);

        if(hit){
            direction.multiply(500);
            results.get(0).getBody().applyForce(direction);
            Body hitObject = results.get(0).getBody();
            if(hitObject.getUserData() instanceof Ball)
                return Optional.of((Ball) hitObject.getUserData());
        }
        return Optional.empty();
    }

    public void removeObject(Body b) {
        world.removeBody(b);
    }


    @Override
    public void begin(Step step, World world) {

    }

    @Override
    public void updatePerformed(Step step, World world) {

    }

    @Override
    public void postSolve(Step step, World world) {

    }

    @Override
    public void end(Step step, World world) {
        //count balls in motion
        int ballsInMotion = 0;
        for (Ball b : Ball.values()) {
            if (!b.getBody().getLinearVelocity().isZero())
                ballsInMotion++;
        }

        if (ballsInMotion == 0)
            //no balls in motion -> resting (start)
            objectsRestListener.onStartAllObjectsRest();
        else
            //balls in motion -> not resting (end)
            objectsRestListener.onEndAllObjectsRest();
    }

    //contact listener begins here
    @Override
    public void sensed(ContactPoint point) {
        //not in use
    }

    @Override
    public boolean begin(ContactPoint point) {
        if(point.getBody1().getUserData() instanceof Ball && point.getBody2().getUserData() instanceof Ball){
            ballsCollisionListener.onBallsCollide((Ball) point.getBody1().getUserData(), (Ball) point.getBody2().getUserData());
        }
        return !point.isSensor();
    }

    @Override
    public void end(ContactPoint point) {
        //not in use
    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        //check which one of the objects is the ball and the other one is a circle but no ball
        if(point.getBody1().getUserData() instanceof Ball && !(point.getBody2().getUserData() instanceof Ball)
        && (point.getFixture2().getShape() instanceof Circle)) {
            ballPocketedListener.onBallPocketed((Ball) point.getBody1().getUserData());
        }
        else if(point.getBody2().getUserData() instanceof Ball && !(point.getBody1().getUserData() instanceof Ball)
                && (point.getFixture1().getShape() instanceof Circle)){
            ballPocketedListener.onBallPocketed((Ball) point.getBody2().getUserData());
        }
        return true;
    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {
        //not in use
    }

    @Override
    public void onFrame(double dt) {
        this.world.update(dt);
    }

    public void setBallsCollisionListener(BallsCollisionListener ballsCollisionListener) {
        this.ballsCollisionListener = ballsCollisionListener;
    }

    public void setObjectsRestListener(ObjectsRestListener objectsRestListener) {
        this.objectsRestListener = objectsRestListener;
    }

    public void setBallPocketedListener(BallPocketedListener ballPocketedListener) {
        this.ballPocketedListener = ballPocketedListener;
    }
}
