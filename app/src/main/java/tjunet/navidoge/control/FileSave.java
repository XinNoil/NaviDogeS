package tjunet.navidoge.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by XinNoil on 2018/9/18.
 *
 */

public class FileSave {
    private File [] Files=new File[5];
    private File appPath;
    private File filePath;
    private int dirNum=0;
    private int fileNum=0;

    FileSave(File appPath){
        this.appPath= appPath;
    }

    public int getDirNum(){
        return dirNum;
    }

    public int getFileNum(){
        return fileNum;
    }

    public String getFileName(){
        return dirNum + File.separator + fileNum + ".txt";
    }

    boolean saveData(String content,File file){
        try {
            FileOutputStream fos = new FileOutputStream(file,true);
            byte [] bytes = content.getBytes();
            fos.write(bytes);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    void setDir(){
        filePath=new File(appPath.getPath()+File.separator+dirNum);
        filePath.mkdir();
    }

    void setFiles(int index){
        Files[index] = new File(filePath, getFileMark(index)+fileNum+".txt");
        if(Files[index].exists()){
            Files[index].delete();
        }
    }

    File getFiles(int index){
        return Files[index];
    }

    private void setDirNum(int dirNum){
        this.dirNum=dirNum;
    }

    public void setDirNum(String dirNum){
        if (!dirNum.isEmpty()){
            setDirNum(Integer.parseInt(dirNum));
        }
    }

    private void setFileNum(int fileNum){
        this.fileNum=fileNum;
    }

    public void setFileNum(String fileNum){
        if (!fileNum.isEmpty()){
            setFileNum(Integer.parseInt(fileNum));
        }
    }

    public void nextDir(){
        dirNum++;
        fileNum=0;
    }

    public void nextFile() {
        fileNum++;
    }

    public String getFileMark(int index){
        switch (index){
            case 0:
                return "W";
            case 1:
                return "A";
            case 2:
                return "M";
            case 3:
                return "GYRO";
            case 4:
                return "GRA";
            default:
                return "S";
        }
    }
}
