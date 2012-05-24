/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.effect.ParticleMesh.Type;

/**
 *
 * @author Jimmy
 */
public class Fireworks extends Node {
    
    private ParticleEmitter effect, effect2;
    
    public Fireworks(AssetManager assetManager) {
        // Load the model
        Node model = (Node) assetManager.loadModel("Models/FireWorks.j3o");
        effect2 = (ParticleEmitter) model.getChild("Emitter");
        
        initEffect(assetManager);
        
        attachChild(effect);
        attachChild(effect2);
    }
    
    public void emitAtPosition(Vector3f pos) {
        setLocalTranslation(pos);
        effect.emitAllParticles();
        effect2.emitAllParticles();
    }
    
    public void explosionAtPosition(Vector3f pos) {
        setLocalTranslation(pos);
        effect.emitAllParticles();
    }
    
    private void initEffect(AssetManager manager) {
    
        int COUNT_FACTOR = 1;
        float COUNT_FACTOR_F = 1f;
        
        effect = new ParticleEmitter("Flame", Type.Triangle, 32 * COUNT_FACTOR);
        effect.setSelectRandomImage(true);
        effect.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        effect.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        effect.setStartSize(0.25f);
        effect.setEndSize(4f);
        effect.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.1f));
        effect.setParticlesPerSec(0);
        effect.setGravity(0, -5f, 0);
        effect.setLowLife(.4f);
        effect.setHighLife(.5f);
        effect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 7, 0));
        effect.getParticleInfluencer().setVelocityVariation(1f);
        effect.setImagesX(2);
        effect.setImagesY(2);
        
        Material mat = new Material(manager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", manager.loadTexture("Effects/Explosion/flame.png"));
        effect.setMaterial(mat);
    }
}
