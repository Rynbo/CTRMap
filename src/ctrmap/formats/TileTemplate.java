package ctrmap.formats;

import java.awt.Color;

public class TileTemplate {
    public int binary;
    public Color color;
    public String name;
    public int cat1;
    public int cat2;
    public TileTemplate(int binary, int color, String name, int cat1, int cat2){
        this.binary = binary;
        this.color = new Color(color);
        this.name = name;
        this.cat1 = cat1;
        this.cat2 = cat2;
    }
}