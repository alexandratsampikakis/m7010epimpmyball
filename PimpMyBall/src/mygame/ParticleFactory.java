/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;

/**
 *
 * @author nicnys-8
 */
public class ParticleFactory {

    static private Material smokeMaterial, explosionMaterial, sunFlareMaterial,
            level1ShotMaterial, level2ShotMaterial, level3ShotMaterial;

    public static void loadImages(AssetManager assetManager) {
        smokeMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        smokeMaterial.setTexture("Texture", assetManager.loadTexture(
                "Textures/smoke.png"));
        /*explosionMaterial = new Material(assetManager,
        "Common/MatDefs/Misc/Particle.j3md");*/

        /*
        explosionMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        explosionMaterial.setTexture("Texture", assetManager.loadTexture(
                "Effects/fire.png"));
        sunFlareMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        sunFlareMaterial.setTexture("Texture", assetManager.loadTexture(
                "Effects/fire.png"));
        level1ShotMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        level1ShotMaterial.setTexture("Texture", assetManager.loadTexture(
                "Effects/shot.png"));
        level2ShotMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        level2ShotMaterial.setTexture("Texture", assetManager.loadTexture(
                "Effects/superShot.png"));
        level3ShotMaterial = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        level3ShotMaterial.setTexture("Texture", assetManager.loadTexture(
                "Effects/superDuperShot.png"));
         * 
         */

    }

    public static ParticleEmitter getSmoke(AssetManager assetManager, float size) {
        ParticleEmitter smoke =
                new ParticleEmitter("Smoke", ParticleMesh.Type.Triangle, 7);
        if (smokeMaterial == null) {
            loadImages(assetManager);
        }
        smoke.setMaterial(smokeMaterial);
        smoke.setImagesX(2);
        smoke.setImagesY(2); // 2x2 texture animation
        smoke.setSelectRandomImage(true);
        smoke.setEndColor(new ColorRGBA(0f, 0f, 0f, 1f));   // red
        smoke.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f)); // yellow
        smoke.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
        smoke.setQueueBucket(Bucket.Transparent);
        smoke.setStartSize(0.1f * size);
        smoke.setEndSize(size);
        smoke.setLowLife(0.1f);
        smoke.setHighLife(0.4f);
        smoke.getParticleInfluencer().setVelocityVariation(2f);
        return smoke;
    }

    /*
    public static ParticleEmitter getExplosion(AssetManager assetManager) {
    ParticleEmitter explosion =
    new ParticleEmitter("Explosion", ParticleMesh.Type.Triangle, 15);
    if (explosionMaterial == null) loadImages(assetManager);
    explosion.setMaterial(explosionMaterial);
    explosion.setImagesX(1);
    explosion.setImagesY(1); // 2x2 texture animation
    explosion.setSelectRandomImage(true);
    
    explosion.setEndColor(new ColorRGBA(0.2f, 0.2f, 0f, 1f));   // red
    explosion.setStartColor(new ColorRGBA(1f, 1f, 0f, 1f)); // yellow
    explosion.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
    
    explosion.setQueueBucket(Bucket.Transparent);
    explosion.setStartSize(0.5f);
    explosion.setEndSize(4f);
    explosion.setLowLife(2f);
    explosion.setHighLife(0.4f);
    explosion.setParticlesPerSec(0);
    explosion.getParticleInfluencer().setVelocityVariation(1.3f);
    return explosion;
    }*/
    public static ParticleEmitter getExplosion(AssetManager assetManager) {
        //int COUNT_FACTOR = 1;
        //float COUNT_FACTOR_F = 1f;
        ParticleEmitter effect = new ParticleEmitter("Flame", Type.Triangle, 5);
        if (explosionMaterial == null) {
            loadImages(assetManager);
        }
        effect.setMaterial(explosionMaterial);
        effect.setSelectRandomImage(true);
        effect.setStartColor(new ColorRGBA(1f, 0.4f, 0.1f, 1f));
        effect.setEndColor(new ColorRGBA(1f, 0.4f, 0.1f, 1f));
        effect.setStartSize(0.1f);
        effect.setEndSize(1f);
        effect.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.2f));
        //effect.setParticlesPerSec(0);
        effect.setGravity(0, -5f, 0);
        effect.setLowLife(.02f);
        effect.setHighLife(.05f);
        //effect.setInitialVelocity(new Vector3f(0, 7, 0));
        //effect.setVelocityVariation(1f);
        effect.setImagesX(1);
        effect.setImagesY(1);
        return effect;
    }

    public static ParticleEmitter getSmallExplosion(AssetManager assetManager) {
        ParticleEmitter flame = new ParticleEmitter("fire", Type.Point, 32);
        flame.setSelectRandomImage(true);
        flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, 1f));
        flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flame.setStartSize(0.3f);
        flame.setEndSize(0.05f);
        flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        flame.setGravity(0, -5, 0);
        flame.setLowLife(.4f);
        flame.setHighLife(.5f);
        flame.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 1, 0));
        flame.getParticleInfluencer().setVelocityVariation(0.2f);
        flame.setImagesX(1);
        flame.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/fire.png"));
        flame.setMaterial(mat);
        return flame;
    }

    public static ParticleEmitter getFlares(AssetManager assetManager, float size) {
        ParticleEmitter sunFlareEmitter =
                new ParticleEmitter("SunFlares", ParticleMesh.Type.Triangle, 20);
        if (sunFlareMaterial == null) {
            loadImages(assetManager);
        }
        sunFlareEmitter.setMaterial(sunFlareMaterial);
        sunFlareEmitter.setImagesX(1);
        sunFlareEmitter.setImagesY(1); // 2x2 texture animation
        sunFlareEmitter.setEndColor(new ColorRGBA(0.1f, 0f, 0f, 1f));
        sunFlareEmitter.setStartColor(new ColorRGBA(1f, 0.9f, 0f, 0.5f));
        sunFlareEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
        sunFlareEmitter.setQueueBucket(Bucket.Transparent);
        sunFlareEmitter.setStartSize(0.1f * size);
        sunFlareEmitter.setEndSize(size);
        sunFlareEmitter.setGravity(0, 0, 0);
        sunFlareEmitter.setLowLife(1.0f);
        sunFlareEmitter.setHighLife(2.0f);
        sunFlareEmitter.getParticleInfluencer().setVelocityVariation(1.3f);
        //sunFlareEmitter.killAllParticles();
        return sunFlareEmitter;
    }

    public static ParticleEmitter getFire(AssetManager assetManager) {
        ParticleEmitter sunFlareEmitter =
                new ParticleEmitter("SunFlares", ParticleMesh.Type.Triangle, 20);
        if (sunFlareMaterial == null) {
            loadImages(assetManager);
        }
        sunFlareEmitter.setMaterial(sunFlareMaterial);
        sunFlareEmitter.setImagesX(1);
        sunFlareEmitter.setImagesY(1); // 2x2 texture animation
        sunFlareEmitter.setEndColor(new ColorRGBA(0.1f, 0f, 0f, 1f));
        sunFlareEmitter.setStartColor(new ColorRGBA(1f, 0.9f, 0f, 0.5f));
        sunFlareEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
        sunFlareEmitter.setQueueBucket(Bucket.Transparent);
        sunFlareEmitter.setStartSize(0.01f);
        sunFlareEmitter.setEndSize(0.02f);
        sunFlareEmitter.setGravity(0, 0, 0);
        sunFlareEmitter.setLowLife(1.0f);
        sunFlareEmitter.setHighLife(2.0f);
        sunFlareEmitter.getParticleInfluencer().setVelocityVariation(1.3f);
        //sunFlareEmitter.killAllParticles();
        return sunFlareEmitter;

    }

    public static ParticleEmitter getShot(AssetManager assetManager, int attackPower) {
        //int COUNT_FACTOR = 1;
        //float COUNT_FACTOR_F = 1f;
        ParticleEmitter shot = new ParticleEmitter("Shot", Type.Triangle, 100);
        if (level1ShotMaterial == null || level2ShotMaterial == null
                || level3ShotMaterial == null) {
            /*switch (attackPower) {
            case 0:
            shot.setMaterial(level1ShotMaterial);
            break;
            case 1:
            shot.setMaterial(level2ShotMaterial);
            break;
            case 2:
            shot.setMaterial(level3ShotMaterial);
            break;
            default:
            shot.setMaterial(level1ShotMaterial);
            break;
            }*/
        }
        shot.setMaterial(level1ShotMaterial);
        switch (attackPower) {
            case 2:
                shot.setMaterial(level2ShotMaterial);
                break;
            case 3:
                shot.setMaterial(level3ShotMaterial);
                break;
            default:
                break;
        }
        System.out.println(attackPower);

        shot.setSelectRandomImage(true);
        shot.setStartSize(0.2f);
        shot.setEndSize(0.2f);
        ColorRGBA color = new ColorRGBA(1f, 1f, 1f, 1f);
        shot.setStartColor(color);
        shot.setEndColor(color);
        shot.setParticlesPerSec(0);
        shot.setGravity(0f, 0f, 0f);
        shot.getParticleInfluencer().setVelocityVariation(0f);
        shot.setLowLife(2f);
        shot.setHighLife(2f);
        //effect.setInitialVelocity(new Vector3f(0, 7, 0));
        //effect.setVelocityVariation(1f);
        shot.setImagesX(1);
        shot.setImagesY(1);
        //shot.emitAllParticles();
        return shot;
    }
}
