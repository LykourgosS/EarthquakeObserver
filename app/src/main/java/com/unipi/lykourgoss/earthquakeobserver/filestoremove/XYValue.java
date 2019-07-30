package com.unipi.lykourgoss.earthquakeobserver.filestoremove;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 28,July,2019.
 */

public class XYValue {
    private float x;
    private float y;

    public XYValue(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
