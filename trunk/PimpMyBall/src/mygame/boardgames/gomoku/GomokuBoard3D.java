/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames.gomoku;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import mygame.boardgames.Direction;
import mygame.boardgames.GomokuGame;
import mygame.boardgames.GridPoint;
import mygame.boardgames.GridSize;

/**
 *
 * @author Jimmy
 */
public class GomokuBoard3D extends Node implements GomokuGame.Listener {
    
    private Geometry[][] grid;
    private Material matRed, matBlue, matGreen, matWhite;
    private int numRows, numCols;
    
    public GomokuBoard3D(AssetManager manager, GomokuGame game) {
        this(manager, game.getGrid().getSize());
        game.addListener(this);
    }
    
    public GomokuBoard3D(AssetManager manager, GridSize size) {
        this(manager, size.rows, size.cols);
    }
    
    public GomokuBoard3D(AssetManager manager, int ROWS, int COLS) {
        
        initLights();
        initMaterials(manager);
        
        numRows = ROWS;
        numCols = COLS;
        grid = new Geometry[ROWS][COLS];
        
        float offset = 0.125f;
        float gridSize = 1.0f;
        float radius = gridSize / 2f * 1.125f;
        float boardEdgeWidth = 0.5f;
        float boardWidth = (gridSize * COLS + offset * (COLS - 1) + boardEdgeWidth * 2) / 2f;
        float boardHeight = (gridSize * ROWS + offset * (ROWS - 1) + boardEdgeWidth * 2) / 2f;
        
        Box b = new Box(Vector3f.ZERO, boardWidth, boardHeight, 0.25f);
        Geometry geom = new Geometry("Board", b);
        
        // Texture from:
        // http://2.bp.blogspot.com/-fIrkER0O6uE/TzB8ybd3axI/AAAAAAAAO0M/lH_Hrg5hOu0/s320/4_Seamless_Wood_Texture.jpg
        
        Material mat = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey texkey = new TextureKey("Textures/4_Seamless_Wood_Texture.jpg");
        texkey.setGenerateMips(true);
        Texture tex = manager.loadTexture(texkey);
        mat.setTexture("ColorMap", tex);
        
        geom.setMaterial(mat);
        
        attachChild(geom);
        
        Vector3f center = new Vector3f(
                boardWidth - (gridSize / 2f + boardEdgeWidth),
                boardHeight - (gridSize / 2f + boardEdgeWidth),
                0).negateLocal();
        Vector3f pos;
        Sphere s = new Sphere(25, 25, radius);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                
                pos = new Vector3f(j + offset * j, i + offset * i, 0);
                
                geom = new Geometry("Free", s);
                geom.setLocalTranslation(pos.addLocal(center));
                geom.setMaterial(matWhite);
                geom.setUserData("row", new Integer(i));
                geom.setUserData("col", new Integer(j));
                geom.setUserData("pos", new GridPoint(i, j));
                
                attachChild(geom);
                grid[i][j] = geom;
            }
        }
    }
    
    private boolean inBounds(GridPoint p) {
        return inBounds(p.row, p.col);
    }
    private boolean inBounds(int row, int col) {
        return (row < numRows && row >= 0 &&
                col < numCols && col >= 0);
    }
    
    private Material getMaterial(CellColor color) {
        switch (color) {
            case RED:
                return matRed;
            case BLUE:
                return matBlue;
            default:
                return matWhite;
        }
    }
    
    public boolean setColor(GridPoint p, CellColor color) {
        
        if (!inBounds(p))
            return false;
        
        Geometry geom = grid[p.row][p.col];
        
        if (!geom.getName().equals("Free"))
            return false;
    
        geom.setName("Taken");
        geom.setMaterial(getMaterial(color));
    
        return true;
    }
    
    public void displayWinningRow(WinningRow wr) {
        
        CellColor winner = wr.getWinningColor();
        GridPoint start = wr.getStartPoint();
        GridPoint end = wr.getEndPoint();
        Direction dir = wr.getDirection();
        
        if (winner != CellColor.NONE) {
            
            int row = start.row, col = start.col;
            Geometry geom;
            
            while (row != end.row || col != end.col) {
                geom = grid[row][col];
                geom.setMaterial(matGreen);
                row += dir.dr;
                col += dir.dc;
            }
        }
    }
    
    public void reset() {
        Geometry geom;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                geom = grid[i][j];
                geom.setName("Free");
                geom.setMaterial(matWhite);
            }
        }
    }

    private void initMaterials(AssetManager manager) {
        
        ColorRGBA[] colors = {
            ColorRGBA.White,
            ColorRGBA.Red,
            ColorRGBA.Blue,
            ColorRGBA.Green,
        };
        
        String matKey = "Common/MatDefs/Light/Lighting.j3md";
        float shininess = 24f;
        int numMats = 4;
        Material[] materials = new Material[numMats];
        
        for (int i = 0; i < numMats; i++) {
            Material newMat = new Material(manager, matKey);
            newMat.setColor("Diffuse", colors[i]);
            newMat.setColor("Ambient", colors[i].mult(0.3f));
            newMat.setColor("Specular", ColorRGBA.White.mult(0.6f));
            newMat.setFloat("Shininess", shininess);
            newMat.setBoolean("UseMaterialColors", true);
            materials[i] = newMat;
        }
        
        matWhite = materials[0];
        matRed = materials[1];
        matBlue = materials[2];
        matGreen = materials[3];
        
        /*
        String matKey = "Common/MatDefs/Light/Lighting.j3md";
        float shininess = 24f;
        
        matWhite = new Material(manager, matKey);
        matWhite.setColor("Diffuse", ColorRGBA.White);
        matWhite.setColor("Ambient", ColorRGBA.White.mult(0.3f));
        matWhite.setColor("Specular", ColorRGBA.White.mult(0.6f));
        matWhite.setFloat("Shininess", shininess);
        matWhite.setBoolean("UseMaterialColors", true);

        matRed = new Material(manager, matKey);
        matRed.setColor("Diffuse", ColorRGBA.Red);
        matRed.setColor("Ambient", ColorRGBA.Red.mult(0.3f));
        matRed.setColor("Specular", ColorRGBA.White.mult(0.6f));
        matRed.setFloat("Shininess", shininess);
        matRed.setBoolean("UseMaterialColors", true);

        matBlue = new Material(manager, matKey);
        matBlue.setColor("Diffuse", ColorRGBA.Blue);
        matBlue.setColor("Ambient", ColorRGBA.Blue.mult(0.3f));
        matBlue.setColor("Specular", ColorRGBA.White.mult(0.6f));
        matBlue.setFloat("Shininess", shininess);
        matBlue.setBoolean("UseMaterialColors", true);
        
        matGreen = new Material(manager, matKey);
        matGreen.setColor("Diffuse", ColorRGBA.Green);
        matGreen.setColor("Ambient", ColorRGBA.Green.mult(0.3f));
        matGreen.setColor("Specular", ColorRGBA.White.mult(0.6f));
        matGreen.setFloat("Shininess", shininess);
        matGreen.setBoolean("UseMaterialColors", true);
         */
    }
    
    private void initLights() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.7f));
        addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White.mult(0.5f));
        dl.setDirection(new Vector3f(0f, -1f, 1f));
        addLight(dl);
        
        dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White.mult(0.5f));
        dl.setDirection(new Vector3f(0f, -1f, -1f));
        addLight(dl);
    }

    
    
    
    @Override
    public void onMove(GomokuGame game, CellColor color, GridPoint p) {
        setColor(p, color);
    }

    @Override
    public void onWin(GomokuGame game, WinningRow wr) {
        displayWinningRow(wr);
    }

    @Override
    public void onReset(GomokuGame game) {
        reset();
    }
    
    
    
    public void positionBetween(Vector3f v1, Vector3f v2) {

        Vector3f fromTo = v2.add(v1.negate()).mult(0.5f);
        Vector3f boardPos = v1.add(fromTo).add(0, 4, 0);
 
        fromTo.y = 0;
        Quaternion q = new Quaternion();
        q.lookAt(fromTo.normalize(), Vector3f.UNIT_Y);
        
        setLocalRotation(q);
        setLocalTranslation(boardPos.add(0, -10, 0));
        setLocalScale(0.25f);
    }
}
