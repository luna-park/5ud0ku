package org.lunapark.dev.sudokulight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static android.view.ViewGroup.LayoutParams;

public class MainActivity extends Activity implements Button.OnClickListener {

    private final int SIZE = 9; // Field size
    private final int PERMUTATIONS = 5; // Basic grid permutation iterations
    private final int LEVEL_OFFSET = 6;
    private final String CELL_ID = "Cell", BTN_ID = "Button", PREF_LVL = "Level";
    private SharedPreferences preferences;

    private TableLayout tableLayout; // Game field
    private LinearLayout linearLayout; // Controls layout
    private TextView tvLevel;
    private Button[][] cells; // Cells in game field
    private Button[] controls;
    private int[][] sudoku; // Generated grid
    private int[][] sudokuSolution; // User grid
    private ArrayList<Integer> hiddenCellsArray, firstRow;
    private int maxLevel = 7, currentLevel;
    private int moves;

    private int currentValue = 1;

    private final Random random = new Random(System.currentTimeMillis());

    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getPreferences(MODE_PRIVATE);
        maxLevel = preferences.getInt(PREF_LVL, maxLevel);
        currentLevel = maxLevel;

        tableLayout = (TableLayout) findViewById(R.id.tlayout);
        linearLayout = (LinearLayout) findViewById(R.id.llayout);
        tvLevel = (TextView) findViewById(R.id.tvLevel);

        animation = AnimationUtils.loadAnimation(this, R.anim.anim_scale);

        cells = new Button[SIZE][SIZE];
        controls = new Button[SIZE];
        sudoku = new int[SIZE][SIZE];
        sudokuSolution = new int[SIZE][SIZE];

        hiddenCellsArray = new ArrayList<Integer>();
        for (int i = 0; i < SIZE * SIZE; i++) {
            hiddenCellsArray.add(i);
        }

        firstRow = new ArrayList<Integer>();
        for (int i = 0; i < SIZE; i++) {
            firstRow.add(i + 1);
        }

        createSudoku();
        createTable();
        createControls();
        tvLevel.setText(new StringBuilder().append(getString(R.string.title_level)).append(" ")
                .append(currentLevel - LEVEL_OFFSET).toString());
        highlights();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_new_game) {
//
//            return true;
//        }

        switch (item.getItemId()) {
            case R.id.action_difficulty:
                // TODO Set difficulty level
                showDifficultyDialog();
                break;
            case R.id.action_new_game:
                createSudoku();
                refreshTable();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Generate sudoku grid
     */
    private void createSudoku() {
        Collections.shuffle(firstRow);
        // 1st row
        for (int i = 0; i < SIZE; i++) {
            sudoku[0][i] = firstRow.get(i);
        }

        generateSudokuRow(0, 1, 3); // 2nd row
        generateSudokuRow(1, 2, 3);

        generateSudokuRow(0, 3, 1);
        generateSudokuRow(3, 4, 3);
        generateSudokuRow(4, 5, 3);

        generateSudokuRow(3, 6, 1);
        generateSudokuRow(6, 7, 3);
        generateSudokuRow(7, 8, 3);

        // TODO Advanced permutation
        for (int i = 0; i < PERMUTATIONS; i++) {
            Log.d("5UD0KU", "PERMUTATION: " + i);
            if (random.nextBoolean()) swapRowsSmall();
            if (random.nextBoolean()) swapColumnsSmall();
            if (random.nextBoolean()) swapRowsArea();
            if (random.nextBoolean()) swapColumnsArea();
        }

        moves = currentLevel;

        // Fill user data
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(sudoku[i], 0, sudokuSolution[i], 0, SIZE);
        }


        // Make hidden cells
        Collections.shuffle(hiddenCellsArray);

        for (int i = 0; i < currentLevel; i++) {
            int a = hiddenCellsArray.get(i);
            sudokuSolution[a % SIZE][a / SIZE] = 0;
        }
    }


    private void generateSudokuRow(int rowSrc, int rowDst, int offset) {
        for (int i = 0; i < SIZE; i++) {
            int b = i + offset;
            if (b >= SIZE) b -= SIZE;
            sudoku[rowDst][i] = sudoku[rowSrc][b];
        }
    }

    /**
     * Permutation
     */
    private void swapRowsSmall() {
        int row = random.nextInt(3) * 3;
        Log.d("5UD0KU", "Swap rows small: " + row);
        for (int i = 0; i < SIZE; i++) {
            int b = sudoku[row][i];
            sudoku[row][i] = sudoku[row + 2][i];
            sudoku[row + 2][i] = b;
        }
    }

    /**
     * Permutation
     */
    private void swapColumnsSmall() {
        int column = random.nextInt(3) * 3;
        Log.d("5UD0KU", "Swap columns small: " + column);
        for (int i = 0; i < SIZE; i++) {
            int b = sudoku[i][column];
            sudoku[i][column] = sudoku[i][column + 2];
            sudoku[i][column + 2] = b;
        }
    }

    /**
     * Permutation
     */
    private void swapRowsArea() {
        int row = random.nextInt(2) * 3;
        Log.d("5UD0KU", "Swap rows area: " + row);
        for (int i = 0; i < SIZE; i++) {
            int a = sudoku[row][i];
            int b = sudoku[row + 1][i];
            int c = sudoku[row + 2][i];
            sudoku[row][i] = sudoku[row + 3][i];
            sudoku[row + 1][i] = sudoku[row + 4][i];
            sudoku[row + 2][i] = sudoku[row + 5][i];
            sudoku[row + 3][i] = a;
            sudoku[row + 4][i] = b;
            sudoku[row + 5][i] = c;
        }
    }

    /**
     * Permutation
     */
    private void swapColumnsArea() {
        int column = random.nextInt(2) * 3;
        Log.d("5UD0KU", "Swap columns area: " + column);
        for (int i = 0; i < SIZE; i++) {
            int a = sudoku[i][column];
            int b = sudoku[i][column + 1];
            int c = sudoku[i][column + 2];
            sudoku[i][column] = sudoku[i][column + 3];
            sudoku[i][column + 1] = sudoku[i][column + 4];
            sudoku[i][column + 2] = sudoku[i][column + 5];
            sudoku[i][column + 3] = a;
            sudoku[i][column + 4] = b;
            sudoku[i][column + 5] = c;
        }
    }

    private boolean checkCell(int i, int j) {
        for (int a = 0; a < currentLevel; a++) {
            int b = j * SIZE + i;
            if (hiddenCellsArray.get(a) == b) return false;
        }
        return true;
    }


    /**
     * Create game field
     */
    private void createTable() {
        // Weight for buttons
        TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.weight = 1;



        for (int i = 0; i < SIZE; i++) {
            // Add row
            TableRow tableRow = new TableRow(this);
            tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
            for (int j = 0; j < SIZE; j++) {
                Button button = new Button(this);
                if (checkCell(i, j)) {
                    button.setText(String.valueOf(sudoku[i][j]));
                } else {
                    button.setText(" ");
                }
                button.setGravity(1);
                button.setOnClickListener(this);
                button.setTag(CELL_ID + "," + i + j);
                cells[i][j] = button;
                tableRow.addView(button, params);
            }
            tableLayout.addView(tableRow);
        }
    }

    private void refreshTable() {
        tvLevel.setText(new StringBuilder().append(getString(R.string.title_level)).append(" ")
                .append(currentLevel - LEVEL_OFFSET).toString());
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (checkCell(i, j)) {
                    cells[i][j].setText(String.valueOf(sudoku[i][j]));
                } else {
                    cells[i][j].setText(" ");
                }
                cells[i][j].setBackgroundResource(R.drawable.button_selector);
            }
        }
        highlights();
    }

    private void createControls() {
        for (int i = 0; i < SIZE; i++) {
            Button button = new Button(this);
            button.setText(String.valueOf(i + 1));
            button.setTag(BTN_ID + "," + i);
            button.setOnClickListener(this);
            controls[i] = button;
            linearLayout.addView(button);
        }
        controls[0].setBackgroundResource(R.drawable.button_checked);

    }

    /**
     * TODO Check for right solution
     *
     * @return
     */
    private boolean checkSolution() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (sudoku[i][j] != sudokuSolution[i][j]) return false;
            }
        }
        return true;
    }

    private void showResult(boolean result) {
        createSudoku();
        refreshTable();
        if (result) {
            // TODO Victory
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.title_victory))
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }

                            }).show();
        } else {
            // TODO Fail
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.title_fail))
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }

                            }).show();
        }
    }


    @Override
    public void onClick(View v) {
        Object tag1 = v.getTag();
        String[] ID = tag1.toString().split(",");
        // Highlight cell
        //if (ID[0].equals(CELL_ID)) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Button button = cells[i][j];
                if (button.getTag().equals(tag1) && sudokuSolution[i][j] == 0) {
                    button.setText(String.valueOf(currentValue));
                    button.setBackgroundResource(R.drawable.button_checked);
                    sudokuSolution[i][j] = currentValue;
                    moves--;
                } else {
                    button.setBackgroundResource(R.drawable.button_selector);
                }
            }
        }


        if (checkSolution()) {
            currentLevel++; // Level up
            if (currentLevel > maxLevel) {
                maxLevel = currentLevel;
                preferences.edit().putInt(PREF_LVL, maxLevel).apply();
            }
            showResult(true);
        } else if (moves == 0) {
            showResult(false);
        }

        //} else {
        // Highlight controls
        for (int i = 0; i < SIZE; i++) {
            Button button = controls[i];
            if (button.getTag().equals(tag1)) {
                currentValue = i + 1;
                button.setBackgroundResource(R.drawable.button_checked);

            } else {
                button.setBackgroundResource(R.drawable.button);

            }
            //highlights();

        }
        highlights();
        //}
    }

    private void highlights() {
        for (int a = 0; a < SIZE; a++) {
            for (int b = 0; b < SIZE; b++) {
                if (sudokuSolution[a][b] == currentValue) {
                    cells[a][b].setTextColor(getResources().getColor(R.color.highlight_text));
                    cells[a][b].startAnimation(animation);
                } else {
                    cells[a][b].setTextColor(Color.BLACK);
                }
            }
            for (int i = 0; i < SIZE; i++) {
                if (Integer.parseInt(controls[i].getText().toString()) == currentValue) {
                    controls[i].setTextColor(getResources().getColor(R.color.highlight_text));
                } else {
                    controls[i].setTextColor(Color.BLACK);
                }
            }
        }
    }

    /**
     * Выход из программы
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    // .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.title_quit))
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    System.exit(0);
                                }

                            }).setNegativeButton(android.R.string.no, null)
                    .show();

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void showDifficultyDialog() {
        final AlertDialog.Builder difficultyDialog = new AlertDialog.Builder(this);

        //difficultyDialog.setIcon(android.R.drawable.btn_star);
        difficultyDialog.setTitle(R.string.action_difficulty);

        View difficultyLayout = getLayoutInflater().inflate(R.layout.difficulty, null);
        difficultyDialog.setView(difficultyLayout);

        final TextView tvLvl = (TextView) difficultyLayout.findViewById(R.id.tvLvl);
        tvLvl.setText(String.valueOf(maxLevel - LEVEL_OFFSET));
        final SeekBar seekBar = (SeekBar) difficultyLayout.findViewById(R.id.seekBar);

        seekBar.setMax(maxLevel - LEVEL_OFFSET - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvLvl.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar.setProgress(currentLevel - LEVEL_OFFSET - 1);

        difficultyDialog.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        currentLevel = seekBar.getProgress() + LEVEL_OFFSET + 1;
                        createSudoku();
                        refreshTable();
                        dialog.dismiss();
                    }
                })

                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        difficultyDialog.create();
        difficultyDialog.show();
    }
}