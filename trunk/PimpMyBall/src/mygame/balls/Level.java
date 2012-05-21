/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.balls;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;

/**
 *
 * @author nicnys-8
 */
public abstract class Level extends Node {
    TerrainQuad terrain;

    public Level(AssetManager assetManager) {
    }

    public abstract void initLighting();

    protected abstract void setUpTerrain(AssetManager assetManager);
    
    public TerrainQuad getTerrain(){
        return terrain;
    }

    /*public BulletAppState getViewAppState() {
        return viewAppState;
    }

    public BulletAppState getGhostAppState() {
        return ghostAppState;
    }
*/
}
