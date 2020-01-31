package ca.ualberta.zhihao9.ece493_zhihao9;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;


public class Iprocessor {
    private Context context;
    private String currentImagePath;
    private ArrayList<Bitmap> warpedBitmaps;
    private int currentShowingIndex;
    private int numUndo;
    private Bitmap mainBitmap;

    public Iprocessor(Context context,int undos) {
        this.context = context;
        this.currentImagePath = "";
        this.currentShowingIndex = 0;
        this.numUndo = undos;
        this.mainBitmap = null;
        warpedBitmaps = new ArrayList<>();
    }

    public void loadNewImage(Bitmap newImage) {
        this.mainBitmap = newImage;
        if (true) {
            mainBitmap = Bitmap.createScaledBitmap(mainBitmap,
                    mainBitmap.getWidth() / 5,
                    mainBitmap.getHeight() / 5, true);
        }
        if(warpedBitmaps != null) warpedBitmaps.clear();

        currentShowingIndex = 0;
        warpedBitmaps.add(mainBitmap.copy(mainBitmap.getConfig(), true));

    }
    public Boolean imageExists() {
        if(mainBitmap != null) {
            return true;
        }else {
            return false;
        }
    }
    public void setNumUndo(int num) {
        if (num > 10) {
            this.numUndo = 10;
        }else{
         this.numUndo = num;
        }
    }
    public int getNumUndo(){
        return this.numUndo;
    }
    public void setCurrentImagePath(String path){
        this.currentImagePath = path;
    }

    public String getCurrentImagePath() {
        return currentImagePath;
    }

    public Bitmap getCurrentImage(){
        return mainBitmap;
    }

    //undo list from: https://blog.fossasia.org/implementing-undo-and-redo-in-image-editor-of-phimpme-android/
    private void addToList() {
        if (warpedBitmaps.size() <= numUndo) {
            currentShowingIndex++;
            warpedBitmaps.add(mainBitmap.copy(mainBitmap.getConfig(), true));
        }else {
            warpedBitmaps.get(0).recycle();
            warpedBitmaps.remove(0);
            warpedBitmaps.add(mainBitmap.copy(mainBitmap.getConfig(), true));
        }
    }


    public void undoBitmap(){

        if (currentShowingIndex - 1 >= 0) {
            warpedBitmaps.get(currentShowingIndex).recycle();
            warpedBitmaps.remove(currentShowingIndex);
            currentShowingIndex -= 1;
        }
        else currentShowingIndex = 0;

        mainBitmap = warpedBitmaps.get(currentShowingIndex);


    }

    public void saveBitmap(){
        if(imageExists()){

            String path = android.os.Environment
                    .getExternalStorageDirectory()
                    + File.separator + "default";
            OutputStream outFile ;
            File folder = new File(path);
            File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
            try {

                if (!folder.exists()) {
                    folder.mkdirs();
                    System.out.println("Making dirs");
                }

                outFile = new FileOutputStream(file);
                getCurrentImage().compress(Bitmap.CompressFormat.JPEG, 100, outFile);
                System.out.println("File save");
                outFile.flush();
                outFile.close();
                galleryAddPic(file);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{
        }
    }


    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }



    public void darkerImage() {
        int height = mainBitmap.getHeight();
        int width = mainBitmap.getWidth();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, mainBitmap.getConfig());
        int R, G, B;
        int pixelColor;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelColor = mainBitmap.getPixel(x, y);
                R = (int) Math.ceil(Color.red(pixelColor)*0.8);
                G = (int) Math.ceil(Color.green(pixelColor)*0.8);
                B = (int) Math.ceil(Color.blue(pixelColor)*0.8);
                resultBitmap.setPixel(x, y, Color.rgb(R, G, B));
            }
        }
        mainBitmap = resultBitmap;
        addToList();
    }

    public void blurImage() {
        int height = mainBitmap.getHeight();
        int width = mainBitmap.getWidth();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, mainBitmap.getConfig());
        Bitmap partial = Bitmap.createBitmap(width, height, mainBitmap.getConfig());

        int A, R, G, B;
        int radius = 8;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sColor;
                R=0; G=0; B=0;
                A = Color.alpha(mainBitmap.getPixel(x, y));
                for(int b= -radius; b <=radius; b++){
                    if(y+b >=0 && y+b <height) {
                        sColor= mainBitmap.getPixel(x, y+b);

                        R += Color.red(sColor);
                        G += Color.green(sColor);
                        B += Color.blue(sColor);
                    }

                }
                R = R/(2*radius +1);
                G = G/(2*radius +1);
                B = B/(2*radius +1);
                partial.setPixel(x, y, Color.argb(A, R, G ,B));

            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sColor;
                R=0; G=0; B=0;
                A = Color.alpha(partial.getPixel(x, y));
                for(int b= -radius; b <=radius; b++){
                    if(x+b >=0 && x+b <width) {
                        sColor= partial.getPixel(x + b, y);

                        R += Color.red(sColor);
                        G += Color.green(sColor);
                        B += Color.blue(sColor);
                    }
                }
                R = R/(2*radius +1);
                G = G/(2*radius +1);
                B = B/(2*radius +1);
                resultBitmap.setPixel(x, y, Color.argb(A, R, G ,B));

            }
        }
        mainBitmap = resultBitmap;
        addToList();
    }


    public  void bulgeImage() {

        int width = mainBitmap.getWidth();
        int height = mainBitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, mainBitmap.getConfig());
        int cx = width/2;
        int cy = height/2;
        double bulgeRadius = 250;
        double bulgeStrength = 1;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int dx = x - cx;
                int dy = y - cy;
                double distanceSquared = dx * dx + dy * dy;
                ;
                int sx = x;
                int sy = y;
                if (distanceSquared < bulgeRadius * bulgeRadius) {
                    double distance = Math.sqrt(distanceSquared);
                    boolean otherMethod = false;
                    //otherMethod = true;
                    if (otherMethod) {
                        double r = distance / bulgeRadius;
                        double a = Math.atan2(dy, dx);
                        double rn = Math.pow(r, bulgeStrength) * distance;
                        double newX = rn * Math.cos(a) + cx;
                        double newY = rn * Math.sin(a) + cy;
                        sx += (newX - x);
                        sy += (newY - y);
                    } else {
                        double dirX = dx / distance;
                        double dirY = dy / distance;
                        double alpha = distance / bulgeRadius;
                        double distortionFactor =
                                distance * Math.pow(1 - alpha, 1.0 / bulgeStrength);
                        sx -= distortionFactor * dirX;
                        sy -= distortionFactor * dirY;
                    }
                }
                if (sx >= 0 && sx < width && sy >= 0 && sy < height) {
                    result.setPixel(x, y, mainBitmap.getPixel(sx, sy));
                }
            }
        }
        mainBitmap = result;
        addToList();
    }

    //Modified from Image Warp Example on Eclass
    public void swirlImage() {
        int width = mainBitmap.getWidth();
        int height = mainBitmap.getHeight();
        double centerX = width/2.0;
        double centerY = height/2.0;
        double factor = 0.005;
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, mainBitmap.getConfig());

        for(int y=0; y<height;y++){
            double relativeY = centerY - y;
            for (int x=0;x<width;x++){
                double relativeX = x - centerX;
                double oAngle;
                if (relativeX !=0) {
                    oAngle = Math.atan(Math.abs(relativeY)/Math.abs(relativeX));
                    if (relativeX > 0 && relativeY < 0){
                        oAngle = 2.0*Math.PI - oAngle;
                    }else if(relativeX<=0 && relativeY>=0){
                        oAngle = Math.PI - oAngle;
                    }else if (relativeX<=0 && relativeY<0){
                        oAngle += Math.PI;
                    }
                }else {
                    if (relativeY>=0){
                        oAngle = 0.5*Math.PI;
                    }else {
                        oAngle = 1.5*Math.PI;
                    }
                }
                double radius = Math.sqrt(relativeX*relativeX + relativeY*relativeY);
                double newAngle = oAngle+factor*radius;
                //double newAngle = oAngle + (1/(factor*radius + 4.0/Math.PI));
                int sourceX = (int) Math.floor(radius*Math.cos(newAngle)+0.5);
                int sourceY = (int) Math.floor(radius*Math.sin(newAngle)+0.5);
                sourceX += centerX;
                sourceY += centerY;
                sourceY = height - sourceY;

                if (sourceX<0) {
                    sourceX = 0;
                }else if(sourceX >=width) {
                    sourceX = width - 1;
                }

                if (sourceY<0) {
                    sourceY = 0;
                }else if (sourceY>=height) {
                    sourceY=height-1;
                }
                resultBitmap.setPixel(x,y, mainBitmap.getPixel(sourceX,sourceY));
            }
        }
        mainBitmap = resultBitmap;
        addToList();
    }
}
