/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boardgames;

/**
 *
 * @author Jimmy
 */
public enum Direction {
    
    NORTH(
            -1,  0),
    NORTH_EAST(
            -1,  1),
    NORTH_WEST(
            -1, -1),
    SOUTH(
             1,  0),
    SOUTH_EAST(
             1,  1),
    SOUTH_WEST(
             1, -1),
    WEST(
             0, -1),
    EAST(
             0,  1),
    NONE(
             0,  0);
    
    public final int dr, dc;
    
    private Direction(int dr, int dc) {
        this.dr = dr;
        this.dc = dc;
    }
    
    public Direction opposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case NORTH_EAST:
                return SOUTH_WEST;
            case NORTH_WEST:
                return SOUTH_EAST;
            case SOUTH:
                return NORTH;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH_WEST:
                return NORTH_EAST;
            case WEST:
                return EAST;
            case EAST:
                return WEST;
            default:
                return NONE;
        }
    }
}
