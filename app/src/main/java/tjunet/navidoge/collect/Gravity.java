package tjunet.navidoge.collect;

/**
 * Created by XinNoil on 2018/9/19.
 *
 */

public class Gravity {
    double [] ex;
    double [] ey;
    double [] ez;
    double [] exz;
    double gx,gy,gz,gxz;
    double tx,ty,tz;
    Gravity(){
        ex=new double[3];
        ey=new double[3];
        ez=new double[3];
        exz=new double[3];
    }
    public void calElements(float [] gra){
        gx=gra[0];
        gy=gra[1];
        gz=gra[2];
        gxz=Math.sqrt(gx * gx + gz * gz);
        ty=Math.atan(gy / gxz);
        tx=Math.atan(gz/Math.abs(gx));
        tz=Math.atan(Math.abs(gx)/gz);
        ey[0]=0;
        ey[1]=Math.cos(ty);
        ey[2]=Math.sin(ty);
        exz[0]=0;
        exz[1]=-Math.sin(ty);
        exz[2]=Math.cos(ty);
        ex[2]=Math.cos(tx)/(exz[2]-ey[2]*exz[1]/ey[1]);
        ez[2]=Math.cos(tz)/(exz[2]-ey[2]*exz[1]/ey[1]);
        ex[1]=-ey[2]*ex[2]/ey[1];
        ez[1]=-ey[2]*ez[2]/ey[1];
        ex[0]=Math.sqrt(1 - ex[1] * ex[1] - ex[2] * ex[2]);
        ez[0]=-Math.sqrt(1 - ez[1] * ez[1] - ez[2] * ez[2]);
        if (gx<0){
            ex[0]=-ex[0];
            ex[1]=-ex[1];
            ex[2]=-ex[2];
        }
        if (gz<0){
            ez[0]=-ez[0];
            ez[1]=-ez[1];
            ez[2]=-ez[2];
        }
    }
    public double[] getEx(){
        return ex;
    }
    public double[] getEy(){
        return ey;
    }
    public double[] getEz(){
        return ez;
    }
    public double[] getTransform(double vec[]){
        double [] newVec=new double[3];
        newVec[0]=vec[0]*ex[0]+vec[1]*ey[0]+vec[2]*ez[0];
        newVec[1]=vec[0]*ex[1]+vec[1]*ey[1]+vec[2]*ez[1];
        newVec[2]=vec[0]*ex[2]+vec[1]*ey[2]+vec[2]*ez[2];
        return newVec;
    }
}
