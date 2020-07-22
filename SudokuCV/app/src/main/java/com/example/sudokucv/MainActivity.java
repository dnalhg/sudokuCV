package com.example.sudokucv;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Camera;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button[] board = new Button[81];
    private int activeCell = -1;
    public static TessOCR mTessOCR;

    Drawable cellImg;
    Drawable cellImgActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        cellImg = ResourcesCompat.getDrawable(res, R.drawable.cell, null);
        cellImgActive = ResourcesCompat.getDrawable(res, R.drawable.cellactive, null);

        TableLayout table = (TableLayout)findViewById(R.id.tableLayout);
        mTessOCR = new TessOCR(this);

        TableRow currRow = new TableRow(this);
        for (int i=0; i<81; i++) {
            if (i%9==0 && i!=0) {
                table.addView(currRow);
                currRow = new TableRow(this);
                if (i%27==0) {
                    View line = new View(this);
                    line.setBackgroundColor(0xFFFF0000);
                    table.addView(line,830, 10);
                }
            } else if (i%3==0 && i!=0) {
                View line = new View(this);
                line.setBackgroundColor(0xFFFF0000);
                currRow.addView(line,10,90);
            }

            Button cell = new Button(this);
            cell.setText("");
            cell.setTextSize(8);
            cell.setTextColor(0xFFFFFFFF);
            cell.setBackground(cellImg);
            final int finalI = i;
            View.OnClickListener buttonListener = new View.OnClickListener() {
                int id = finalI;

                @Override
                public void onClick(View v) {
                    if (activeCell == id) {
                        activeCell = -1;
                        v.setBackground(cellImg);
                    } else {
                        v.setBackground(cellImgActive);
                        if (activeCell != -1) {
                            Button oldCell = board[activeCell];
                            oldCell.setBackground(cellImg);
                        }
                        activeCell = id;
                    }
                }
            };
            cell.setOnClickListener(buttonListener);

            board[i] = cell;

            currRow.addView(cell,90,90);
        }
        table.addView(currRow);

        LinearLayout changePanel = (LinearLayout)findViewById(R.id.changePanel);
        for (int i=1; i<11; i++) {
            Button cell = new Button(this);
            cell.setBackground(cellImg);
            cell.setTextSize(8);
            if (i == 10) {
                cell.setText(R.string.delete);
            } else {
                cell.setText(Integer.toString(i));
            }
            cell.setTextColor(0xFFFFFFFF);

            final int finalI = i;
            View.OnClickListener buttonListener = new View.OnClickListener() {
                int id = finalI;
                @Override
                public void onClick(View v) {
                    if (activeCell != -1) {
                        Button oldCell = board[activeCell];
                        if (id == 10) {
                            oldCell.setText("");
                        } else {
                            oldCell.setText(Integer.toString(id));
                        }
                    }
                }
            };
            cell.setOnClickListener(buttonListener);
            changePanel.addView(cell, 90, 90);
        }

    }

    public void resetButton(View view) {
        for (Button b: board) {
            b.setText("");
        }
        if (activeCell != -1) {
            Button oldCell = board[activeCell];
            oldCell.setBackground(cellImg);
            activeCell = -1;
        }
        Button solveButton = (Button)findViewById(R.id.solveButton);
        solveButton.setText(R.string.solve);
    }

    public void solveButton(View view) {
        Toast.makeText(this, "Solving...", Toast.LENGTH_SHORT).show();
        int[] boardInt = new int[81];
        for (int i=0; i<board.length; i++) {
            String entry = board[i].getText().toString();
            if (entry.isEmpty()) {
                boardInt[i] = 0;
            } else {
                boardInt[i] = Integer.parseInt(entry);
            }
        }
        int[] solution = Solver.solve(boardInt);
        if (solution == null) {
            Toast.makeText(this, "No solution found", Toast.LENGTH_SHORT).show();
        } else {
            for (int i=0; i<solution.length; i++) {
                board[i].setText(Integer.toString(solution[i]));
            }
        }
    }

    public void generateButton(View view) {
        Toast.makeText(this, "Generating...", Toast.LENGTH_SHORT).show();
        new Thread() {
            public void run() {
                int[] boardInt = Solver.generate();
                for (int i=0; i<boardInt.length; i++) {
                    if (boardInt[i] == 0) {
                        board[i].setText("");
                    } else {
                        board[i].setText(Integer.toString(boardInt[i]));
                    }
                }
            }
        }.start();

    }

    public void cameraButton(View view) {
        Intent intent = new Intent(this, CameraScreen.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int[] result = data.getIntArrayExtra("result");
                for (int i=0; i<result.length; i++) {
                    if (result[i] == 0) {
                        board[i].setText("");
                    } else {
                        board[i].setText(Integer.toString(result[i]));
                    }
                }
            } else {
                Toast.makeText(this, "No valid grid found", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
