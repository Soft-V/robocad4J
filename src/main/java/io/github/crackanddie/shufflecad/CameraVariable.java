package io.github.crackanddie.shufflecad;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

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
        var bytes = new MatOfByte();
        Imgcodecs.imencode(".jpg", this.value, bytes);
        return bytes.toArray();
    }

    public void setMat(Mat mat){
        if (mat != null){
            this.value = mat;
            this.shape = mat.size();
        }
    }
}
