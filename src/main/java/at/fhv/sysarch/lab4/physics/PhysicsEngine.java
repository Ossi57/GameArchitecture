package at.fhv.sysarch.lab4.physics;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

public interface PhysicsEngine {
    public void addObject(Body body);
    public void performStrike(Vector2 origin, Vector2 direction);
}
