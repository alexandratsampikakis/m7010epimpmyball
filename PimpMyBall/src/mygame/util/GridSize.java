/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.util;

import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Jimmy
 */
@Serializable
public class GridSize {
    
    public int rows, cols;
    
    public GridSize() {
        rows = cols = 0;
    }
    public GridSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }
    public GridSize(GridSize size) {
        this.rows = size.rows;
        this.cols = size.cols;
    }
    public GridSize(GridSize size, float scale) {
        this.rows = Math.round(size.rows * scale);
        this.cols = Math.round(size.cols * scale);
    }
    
    public void set(GridSize size) {
        this.rows = size.rows;
        this.cols = size.cols;
    }
    public void set(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }
}
