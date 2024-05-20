package io.github.softv.shufflecad;

import org.opencv.highgui.Highgui;
import org.opencv.core.*;

public class CameraVariable implements IVariable {
    public String name;
    public Size shape;
    private Mat value;

    public CameraVariable(String name){
        this.name = name;
        this.value = new Mat(1, 1, CvType.CV_8U);
        shape = new Size(1, 1);
    }

    public byte[] getValue(){
        MatOfByte bytes = new MatOfByte();
        Highgui.imencode(".jpg", this.value, bytes);
        return bytes.toArray();
    }

    public void setMat(Mat mat){
        if (mat != null){
            this.value = mat;
            this.shape = mat.size();
        }
    }
}
