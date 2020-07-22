package com.example.sudokucv;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CameraScreen extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    Mat mRGBA, processedImg;
    List<Mat> cells;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("Camera", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera_screen);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraPermissionGranted();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        return processImage(mRGBA, true);

    }

    public Mat processImage(Mat mRGBA, boolean rotate) {
        processedImg = new Mat();

        // Converting to Grayscale and cleaning image
        Imgproc.cvtColor(mRGBA, processedImg, Imgproc.COLOR_RGB2GRAY);
        Imgproc.medianBlur(processedImg, processedImg, 3);
        Imgproc.adaptiveThreshold(processedImg, processedImg, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 3);

        // Finding largest contour in image to find grid
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchyOutputVector = new Mat();
        Imgproc.findContours(processedImg, contours, hierarchyOutputVector,
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint largestContour = contours.get(0);
        double largestArea = 0;
        for (MatOfPoint p:contours) {
            double area = Imgproc.contourArea(p);
            if (area > largestArea) {
                largestArea = area;
                largestContour = p;
            }
        }

        // Adjusting perspective to centre the grid
        MatOfPoint2f poly = new MatOfPoint2f(largestContour.toArray());
        double peri = Imgproc.arcLength(poly, true);
        Imgproc.approxPolyDP(poly, poly, 0.02*peri, true);

        Point[] corners = (Point[]) poly.toList().toArray();
        if (corners.length != 4) {
            cells = null;
            return processedImg;
        }
        Arrays.sort(corners, new Comparator<Point>() {
            public int compare(Point p1, Point p2) {
                return Double.compare(p1.y, p2.y);
            }
        });
        if (corners[0].x > corners[1].x) {
            Point tmp = corners[1];
            corners[1] = corners[0];
            corners[0] = tmp;
        }
        if (corners[2].x > corners[3].x) {
            Point tmp = corners[3];
            corners[3] = corners[2];
            corners[2] = tmp;
        }

        Rect grid = Imgproc.boundingRect(poly);
        Point[] boxPoints = new Point[]{new Point(grid.x, grid.y),
                new Point(grid.x + grid.width, grid.y),
                new Point(grid.x, grid.y + grid.height),
                new Point(grid.x + grid.width, grid.y + grid.height)
        };

        Mat transform = Calib3d.findHomography(new MatOfPoint2f(corners), new MatOfPoint2f(boxPoints));
        Imgproc.warpPerspective(processedImg, processedImg, transform, processedImg.size());

        // Finding largest contour again in new image to extract a regular grid shape
        contours = new ArrayList<>();
        hierarchyOutputVector = new Mat();
        Imgproc.findContours(processedImg, contours, hierarchyOutputVector,
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        largestContour = contours.get(0);
        largestArea = 0;
        for (MatOfPoint p:contours) {
            double area = Imgproc.contourArea(p);
            if (area > largestArea) {
                largestArea = area;
                largestContour = p;
            }
        }
        grid = Imgproc.boundingRect(largestContour);
        processedImg = processedImg.submat(grid);
        Mat tmpImg = processedImg.clone();
        Imgproc.resize(tmpImg, tmpImg, mRGBA.size());

        if (rotate) {
            Core.rotate(processedImg, processedImg, Core.ROTATE_90_CLOCKWISE);
        }

        cells = new ArrayList<>();
        int cellHeight = (int)Math.floor(processedImg.size().height/9);
        int cellWidth = (int)Math.floor(processedImg.size().width/9);
        int padding = (int)Math.floor(Math.min(cellHeight, cellWidth)/6);

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Rect currCell = new Rect(x*cellWidth + padding, y*cellHeight + padding,
                        cellWidth - 2*padding, cellHeight - 2*padding);
                Mat newCell = processedImg.submat(currCell);
                cells.add(newCell);
            }
        }

        return tmpImg;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.i("Camera", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }else{
            Log.i("Camera", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void processCells(View v) {
        Intent intent = new Intent();
        if (cells == null) {
            Log.i("Processing", "No grid cells detected");
            setResult(RESULT_CANCELED, intent);
            finish();
        } else if (cells.size() != 81) {
            Log.i("Processing", "Insufficient grid cells detected");
            setResult(RESULT_CANCELED, intent);
            finish();
        } else {
            Bitmap bmp1 = Bitmap.createBitmap(processedImg.cols(), processedImg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(processedImg, bmp1);
            saveFile(bmp1, "board");

            Log.i("Processing", "Processing valid grid cells");
            int[] result = new int[81];
            int idx = 0;
            for (Mat c: cells) {
                Photo.fastNlMeansDenoising(c, c,10,7,21);
                Bitmap bmp = Bitmap.createBitmap(c.cols(), c.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(c, bmp);
                saveFile(bmp, Integer.toString(idx));

                int active = Core.countNonZero(c);
                String query;

                if (active < 100) {
                    query = "0";
                } else {
                    query = MainActivity.mTessOCR.getOCRResult(bmp);
                }
                Log.i("Processing", active + "|" + query);
                query = query.trim();

                if (query.length() != 1) {
                    result[idx] = 0;
                } else {
                    result[idx] = Integer.parseInt(query);
                }
                idx++;
            }

            intent.putExtra("result", result);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void saveFile(Bitmap bmp, String filename) {
        FileOutputStream out = null;

        File sd = new File(Environment.getExternalStorageDirectory() + "/frames/");
        boolean success = true;
        if (!sd.exists()) {
            success = sd.mkdir();
        }
        if (success) {
            File dest = new File(sd, filename + ".png");

            try {
                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Processing", e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        Log.d("Processing", "OK!!");
                    }
                } catch (IOException e) {
                    Log.d("Processing", e.getMessage() + "Error");
                    e.printStackTrace();
                }
            }
        }
    }

    public void importImage(View v) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Uri imageUri = data.getData();
                Bitmap bmp;
                try{
                    bmp = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(),
                            imageUri);
                } catch (java.io.IOException e) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }


                Mat obj = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC4);
                Utils.bitmapToMat(bmp, obj);

                processImage(obj, false);
                processCells(null);

            } else {
                Log.i("Processing", "Imported grid cell not detected");
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

}

//        for (Rect r:boundRect) {
//            Mat cell = displayImg.submat(r);
//        }

//        // Cleaning lines of grid
//        Mat vertKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,5));
//        Mat horKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,1));
//        Imgproc.morphologyEx(processedImg, processedImg, Imgproc.MORPH_CLOSE, vertKernel);
//        Imgproc.morphologyEx(processedImg, processedImg, Imgproc.MORPH_CLOSE, horKernel);
//
//        // Creating an invert bitmap
//        Mat invertedImg = new Mat(processedImg.rows(),processedImg.cols(), processedImg.type(), new Scalar(255,255,255));
//        Core.subtract(invertedImg, processedImg, invertedImg);
//
//        // Locating individual grid cells with contours and bounding rectangles
//        contours = new ArrayList<>();
//        hierarchyOutputVector = new Mat();
//        Imgproc.findContours(invertedImg, contours, hierarchyOutputVector,
//                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        MatOfPoint2f[] contoursPoly  = new MatOfPoint2f[contours.size()];
//        boundRect = new ArrayList<>();
//        for (int i = 0; i < contours.size(); i++) {
//            contoursPoly[i] = new MatOfPoint2f();
//            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly[i], 3, true);
//            Rect newRect = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));
//            if (newRect.area() > 500 && newRect.area() < 10000) {
//                boundRect.add(newRect);
//            }
//        }
//
//        // Extracting cells found to construct grid in top left to bottom right order
//        Collections.sort(boundRect, new Comparator<Rect> () {
//            @Override
//            public int compare(Rect rect1, Rect rect2) {
//                return Double.compare(rect1.tl().y, rect2.tl().y);
//            }
//        } );
//
//        // TO DO: Extract rows of board.
//        // Have to sort by x coords and account for noisy/false cells
//        // TO DO: Extract digits at each cell
//        List<List<Mat>> digits = new ArrayList<>();
//        List<Mat> row = new LinkedList<>();
//        for (Rect r:boundRect) {
//            Mat cell = displayImg.submat(r);
//        }
//
//        // Drawing detected cells for display
//        Imgproc.cvtColor(displayImg, displayImg, Imgproc.COLOR_GRAY2RGBA);
//        for (Rect r:boundRect) {
//            Scalar color = new Scalar(255, 0, 0);
//            Imgproc.rectangle(displayImg, r.tl(), r.br(), color, 3);
//        }
