package tjunet.navidoge.collect;

/**
 * Created by XinNoil on 2018/9/19.
 */

public class Compass {
    private double[][] R;
    private double [] values=new double[3];
    Compass(){
        R=new double[3][3];
    }
    public void setValues(){
        values[0] = Math.atan2(R[0][1], R[1][1]);
        values[1] = Math.asin(-R[2][1]);
        values[2] = Math.atan2(-R[2][0], R[2][2]);
    }
    public double[] getValues(){
        return values;
    }
    public void setR(float []Rm){
        for (int i=0;i<3;i++)
            for (int j=0;j<3;j++){
                R[i][j]=(double)Rm[i*3+j];
            }
        setValues();
    }
    public double[][] getR(){
        return R;
    }
    public double sq(double x){
        return x*x;
    }

}

    /*
    public double [][]calR(double []gra,double t){
        double g=Math.sqrt(gra[0]*gra[0]+gra[1]*gra[1]+gra[2]*gra[2]);
        double a=-gra[0]/g;
        double b=-gra[1]/g;
        double c=-gra[2]/g;
        double a_cos=a*Math.cos(t);
        double b_sin=b*Math.sin(t);
        double c_cos=c*Math.cos(t);
        double K=sq((a_cos - a * b) * (a_cos - b_sin)) / (a * a * sq(a_cos - b_sin))+sq((b * b - c_cos)*(a_cos - a * b))+sq((a_cos - a * b)*(a_cos - b_sin));
        double sqrtK=Math.sqrt(K);
        R[0][0]=(b*b-c_cos)*sqrtK/(a_cos-b_sin);
        R[0][1]=Math.sin(t);
        R[0][2]=a;
        R[1][0]=a*c*sqrtK/(a_cos-b_sin);
        R[1][1]=Math.cos(t);
        R[1][2]=b;
        R[2][0]=sqrtK;
        R[2][1]=b;
        R[2][2]=c;
        return R;
    }
    */