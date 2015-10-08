package org.lunapark.dev.sudokulight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
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

import static android.view.ViewGroup.LayoutParams;

public class MainActivity extends Activity implements Button.OnClickListener {

    private final int SIZE = 9; // Field size
    private final int LEVEL_OFFSET = 6; // Offset level value
    private final int LEVELS = SIZE * SIZE - 1; // Maximum reachable level
    private String CELL_ID = "Cell", BTN_ID = "Button", PREF_LVL = "Level";
    private SharedPreferences preferences;
    private TableLayout tableLayout; // Game field
    private LinearLayout linearLayout; // Controls layout
    private TextView tvLevel, tvRank;
    private Button[][] cells; // Cells in game field
    private Button[] controls;

    private AlertDialog.Builder messageBox;

    private Sudoku sudoku;
    private int[][] sudokuSolution; // User grid

    private String[] ranks;

    private int maxLevel = 7, currentLevel;
    private int moves, ranksLength;
    private int currentValue = 1;
    private Animation animationScale, animationRotate;
    private int cellWidth;


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
        tvRank = (TextView) findViewById(R.id.tvRank);

        messageBox = new AlertDialog.Builder(this);
        messageBox.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                    }

                });

        animationScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        animationRotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        cells = new Button[SIZE][SIZE];
        controls = new Button[SIZE];
        sudoku = new Sudoku(SIZE);

        ranksLength = getResources().getStringArray(R.array.ranks).length;
        ranks = new String[ranksLength];
        System.arraycopy(getResources().getStringArray(R.array.ranks), 0, ranks, 0, ranksLength);

        createSudoku();
        createTable();
        createControls();
        refreshTable();
        //tvLevel.setText(getString(R.string.title_level) + " " + (currentLevel - LEVEL_OFFSET));
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

        switch (item.getItemId()) {
            case R.id.action_difficulty:
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
        moves = currentLevel;

        // Fill user data
        sudokuSolution = sudoku.generateSudoku(currentLevel);
    }

    /**
     * Create game field
     */
    private void createTable() {

        Display display = getWindowManager().getDefaultDisplay();
        // Screen size
        Point point = new Point();
        display.getSize(point);
        // Screen density
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        float dpi = displayMetrics.density;
        float marginX = (getResources().getDimension(R.dimen.activity_horizontal_margin)) * dpi;
        float width = point.x - marginX;
        cellWidth = (int) (width / 9);

        for (int i = 0; i < SIZE; i++) {
            // Add row
            TableRow tableRow = new TableRow(this);
            tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
            for (int j = 0; j < SIZE; j++) {
                Button button = new Button(this);
                int value = sudokuSolution[i][j];

                if (value != 0) {
                    button.setText(String.valueOf(value));
                } else {
                    button.setText(" ");
                }
                button.setTag(CELL_ID + "," + i + j);
                button.setGravity(1);
                button.setOnClickListener(this);
                button.setWidth(cellWidth);
                button.setHeight(cellWidth);
                button.setGravity(Gravity.CENTER);
                cells[i][j] = button;
                tableRow.addView(button);
            }
            tableLayout.addView(tableRow);
        }

        // Add grid
        ShapeDrawable sdBg = new ShapeDrawable(new RectShape());
        sdBg.getPaint().setColor(getResources().getColor(R.color.field));

        ShapeDrawable sdGrid1 = new ShapeDrawable(new RectShape());
        sdGrid1.getPaint().setColor(Color.BLACK);
        sdGrid1.getPaint().setStyle(Paint.Style.STROKE);
        sdGrid1.getPaint().setStrokeWidth(5);

        ShapeDrawable sdGrid2 = new ShapeDrawable(new RectShape());
        sdGrid2.getPaint().setColor(Color.BLACK);
        sdGrid2.getPaint().setStyle(Paint.Style.STROKE);
        sdGrid2.getPaint().setStrokeWidth(5);

        Drawable[] drawables = {sdBg, sdGrid1, sdGrid2};
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        layerDrawable.setLayerInset(1, 0, cellWidth * 3, 0, cellWidth * 3);
        layerDrawable.setLayerInset(2, cellWidth * 3, 0, cellWidth * 3, 0);

        tableLayout.setBackgroundDrawable(layerDrawable);
    }

    private void refreshTable() {
        int level = currentLevel - LEVEL_OFFSET;
        tvLevel.setText(getString(R.string.title_level) + " " + (level));
        // TODO Check for range
        tvRank.setText(ranks[level / 10]);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int value = sudokuSolution[i][j];
                if (value != 0) {
                    cells[i][j].setText(String.valueOf(value));
                    cells[i][j].startAnimation(animationRotate);
                } else {
                    cells[i][j].setText(" ");
                }
                cells[i][j].setBackgroundResource(R.drawable.button_selector);
//                cells[i][j].startAnimation(animationRotate);
            }
        }
        highlights();
    }

    private void createControls() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;

        for (int i = 0; i < SIZE; i++) {
            Button button = new Button(this);
            button.setTag(BTN_ID + "," + i);
            button.setText(String.valueOf(i + 1));
            button.setOnClickListener(this);
            button.setHeight(cellWidth);
            button.setGravity(Gravity.CENTER);
            controls[i] = button;
            linearLayout.addView(button, layoutParams);
        }
        controls[0].setBackgroundResource(R.drawable.button_checked);
    }

    private void showResult(boolean result) {
        createSudoku();
        refreshTable();
        int iconId;
        String message;
        if (result) {
            // Victory
            iconId = android.R.drawable.btn_star_big_on;
            message = getString(R.string.title_victory);
        } else {
            // Fail
            iconId = android.R.drawable.ic_delete;
            message = getString(R.string.title_fail);
        }

        messageBox.setIcon(iconId).setTitle(message).show();
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


        if (moves == 0) {
            if (sudoku.checkSolution()) {
                if (currentLevel < LEVELS) currentLevel++; // Level up
                if (currentLevel > maxLevel) {
                    maxLevel = currentLevel;
                    preferences.edit().putInt(PREF_LVL, maxLevel).apply();
                }
                showResult(true);
            } else {
                showResult(false);
            }
        }

        // Highlight controls
        for (int i = 0; i < SIZE; i++) {
            Button button = controls[i];
            if (button.getTag().equals(tag1)) {
                currentValue = i + 1;
                button.setBackgroundResource(R.drawable.button_checked);
            } else {
                button.setBackgroundResource(R.drawable.button);
            }
        }
        highlights();
    }


    private void highlights() {
        for (int a = 0; a < SIZE; a++) {
            for (int b = 0; b < SIZE; b++) {
                if (sudokuSolution[a][b] == currentValue) {
                    cells[a][b].setTextColor(getResources().getColor(R.color.highlight_text));
                    cells[a][b].startAnimation(animationScale);
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