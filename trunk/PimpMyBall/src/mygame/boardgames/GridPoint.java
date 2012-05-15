/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.boardgames;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.network.serializing.Serializable;
import java.io.IOException;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GridPoint implements Savable {
    
    public int row, col; // byte row, col;
    
    public GridPoint(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    /*public GridPoint(byte row, byte col) {
        this.row = row;
        this.col = col;
    }*/
    
    public GridPoint() {
        this(0, 0);
    }
    
    public GridPoint(GridPoint p) {
        this(p.row, p.col);
    }
    
    public GridPoint(GridPoint p, Direction dir) {
        this(p.row + dir.dr, p.col + dir.dc);
    }
    
    /*public void set(byte row, byte col) {
        this.row = row;
        this.col = col;
    }*/
    
    public void set(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public void set(GridPoint p) {
        this.row = p.row;
        this.col = p.col;
    }
    
    public void move(Direction dir) {
        row += dir.dr;
        col += dir.dc;
    }
    
    public boolean equals(GridPoint p) {
        return (row == p.row && col == p.col);
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof GridPoint) ? 
                equals((GridPoint)o) : false;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.row;
        hash = 83 * hash + this.col;
        return hash;
    }
    
    /* Savable methods */
    @Override
    public void write(JmeExporter ex) throws IOException {}
    @Override
    public void read(JmeImporter im) throws IOException {}
}
