package ca.ualberta.zhihao9.ece493_zhihao9;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{

    private ImageView imageView;
    private Button take_pic;
    private Button undo;
    private Button save;
    private Button summit;
    private ProgressDialog progress;
    private TextInputEditText undo_input;
    static final int REQUEST_CAMERA_CAPTURE = 1;
    static final int REQUEST_GALLERY = 2 ;
    static final int SWIPE_MIN_DISTANCE = 120;
    static final int SWIPE_MAX_OFF_PATH = 250;
    static final int SWIPE_THRESHOLD_VELOCITY = 200;


    GestureDetectorCompat gesture_Detector;
    ca.ualberta.zhihao9.ece493_zhihao9.Iprocessor image_Processor;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        take_pic = findViewById(R.id.load_pic);
        imageView = findViewById(R.id.image_view);
        undo = findViewById(R.id.undo);
        save = findViewById(R.id.save);
        undo_input = findViewById(R.id.undo_int);
        summit = findViewById(R.id.summit);
        image_Processor = new ca.ualberta.zhihao9.ece493_zhihao9.Iprocessor(this,2);

        progress= new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        gesture_Detector = new GestureDetectorCompat(this,this);
        gesture_Detector.setOnDoubleTapListener(this);
        undo_input.setFilters( new InputFilter[]  { new ca.ualberta.zhihao9.ece493_zhihao9.inputfilter_minmax(1,100)});

        take_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_Processor.undoBitmap();
                imageView.setImageBitmap(image_Processor.getCurrentImage() );
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_Processor.saveBitmap();
                System.out.println("pic save");
            }
        });

        summit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_Processor.setNumUndo(Integer.parseInt(String.valueOf(undo_input.getText())));
                Toast.makeText(getApplicationContext(), "undo times" + image_Processor.getNumUndo(), Toast.LENGTH_SHORT).show();
            }
        });




    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose Pic from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, REQUEST_CAMERA_CAPTURE);
                }
                else if (options[item].equals("Choose Pic from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_GALLERY);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA_CAPTURE) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    image_Processor.loadNewImage(bitmap);
                    imageView.setImageBitmap(image_Processor.getCurrentImage());
                    image_Processor.saveBitmap();
                    f.delete();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_GALLERY) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.w("path of image....", picturePath+"");
                image_Processor.loadNewImage(thumbnail);
                imageView.setImageBitmap(image_Processor.getCurrentImage());
            }
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gesture_Detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //TODO ZOOM
        Toast.makeText(this, "onDoubleTap", Toast.LENGTH_SHORT).show();


        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH
                    || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY) {
                return false;
            }
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                Toast.makeText(this, "bulge", Toast.LENGTH_SHORT).show();
                if(image_Processor.imageExists()) {
                    image_Processor.bulgeImage();
                    imageView.setImageBitmap(image_Processor.getCurrentImage());
                }

            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                Toast.makeText(this, "Swirl" , Toast.LENGTH_SHORT).show();
                if(image_Processor.imageExists()) {
                    image_Processor.swirlImage();
                    imageView.setImageBitmap(image_Processor.getCurrentImage());
                }
            }
        } else {
            if (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
                return false;
            }
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                Toast.makeText(this, "Blur" , Toast.LENGTH_SHORT).show();
                if(image_Processor.imageExists()) {
                    image_Processor.blurImage();
                    imageView.setImageBitmap(image_Processor.getCurrentImage());
                }

            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                Toast.makeText(this, "darker", Toast.LENGTH_SHORT).show();
                if(image_Processor.imageExists()) {
                    image_Processor.darkerImage();
                    imageView.setImageBitmap(image_Processor.getCurrentImage());
                }
            }
        }

        return true;

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

