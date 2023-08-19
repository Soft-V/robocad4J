package io.github.crackanddie.shufflecad;

import java.util.Objects;

public class ShuffleVariable implements IVariable {
    public static final String FLOAT_TYPE = "float";
    public static final String STRING_TYPE = "string";
    public static final String BIG_STRING_TYPE = "bigstring";
    public static final String BOOL_TYPE = "bool";
    public static final String CHART_TYPE = "chart";
    public static final String SLIDER_TYPE = "slider";

    public static final String IN_VAR = "in";
    public static final String OUT_VAR = "out";

    public String name;
    public String type;
    public String direction;
    private String value;

    public ShuffleVariable(String name, String type){
        this(name, type, IN_VAR);
    }

    public ShuffleVariable(String name, String type, String direction){
        this.name = name;
        this.type = type;
        this.direction = direction;
        this.value = "";
    }

    public void setBool(boolean val){
        this.value = val ? "1" : "0";
    }

    public void setFloat(float val){
        this.value = String.valueOf(val);
    }

    public void setString(String val){
        this.value = val;
    }

    public boolean getBool(){
        return Objects.equals(this.value, "1");
    }

    public float getFloat(){
        try{
            return Float.parseFloat(this.value.replace(',', '.'));
        }
        catch (Exception e){
            return 0;
        }
    }

    public String getString(){
        return this.value;
    }
}
