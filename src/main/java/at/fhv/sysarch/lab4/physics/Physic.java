package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.rendering.FrameListener;
import org.dyn4j.dynamics.*;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Physic implements StepListener, ContactListener, FrameListener, PhysicsEngine {

    private final World world;
    private BallPocketedListener ballPocketedListener;
    private BallsCollisionListener ballsCollisionListener;
    private ObjectsRestListener objectsRestListener;

    public Physic(){
        this.world = new World();
        world.addListener(this);
        world.setGravity(World.ZERO_GRAVITY);
    }

    @Override
    public void addObject(Body body){
        this.world.addBody(body);
    }

    @Override
    public void performStrike(Vector2 origin, Vector2 direction) {
        Ray ray = new Ray(origin, direction);
        List<RaycastResult> results = new ArrayList<>();

        boolean hit = this.world.raycast(ray, 0, true, false, results);

        if(hit){
            direction.multiply(500);
            results.get(0).getBody().applyForce(direction);
        }
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

    }

    //contact listener begins here

    @Override
    public void sensed(ContactPoint point) {

    }

    @Override
    public boolean begin(ContactPoint point) {
        return false;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        return true;
    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }

    @Override
    public void onFrame(double dt) {
        this.world.update(dt);
    }
}
