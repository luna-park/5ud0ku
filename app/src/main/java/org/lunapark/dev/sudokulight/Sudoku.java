package org.lunapark.dev.sudokulight;

//import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by znak on 01.10.2015.
 */
public class Sudoku {

    private static final int PERMUTATIONS = 5;
    private final ArrayList<Integer> firstRow;
    private final ArrayList<Integer> hiddenCellsArray;
    private int size = 9;
    private int[][] sudoku;
    private Random random;

    public Sudoku(int size) {
        this.size = size;
        random = new Random(System.currentTimeMillis());
        sudoku = new int[size][size];
        firstRow = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            firstRow.add(i + 1);
        }

        hiddenCellsArray = new ArrayList<Integer>();
        for (int i = 0; i < size * size; i++) {
            hiddenCellsArray.add(i);
        }
    }

    public int[][] generateSudoku(int currentLevel) {

        Collections.shuffle(firstRow);
        // 1st row
        for (int i = 0; i < size; i++) {
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

        // Advanced permutation
        for (int i = 0; i < PERMUTATIONS; i++) {
            // TODO Hide permutation
            System.out.println("PERMUTATION: " + i);
            if (random.nextBoolean()) swapRowsSmall();
            if (random.nextBoolean()) swapColumnsSmall();
            if (random.nextBoolean()) swapRowsArea();
            if (random.nextBoolean()) swapColumnsArea();
        }

        //moves = currentLevel;

        // Fill user data
//        for (int i = 0; i < size; i++) {
//            System.arraycopy(sudoku[i], 0, sudokuSolution[i], 0, size);
//        }


        // Make hidden cells
        Collections.shuffle(hiddenCellsArray);

        for (int i = 0; i < currentLevel; i++) {
            int a = hiddenCellsArray.get(i);
            sudoku[a % size][a / size] = 0;
        }

        return sudoku;
    }


    private void generateSudokuRow(int rowSrc, int rowDst, int offset) {
        for (int i = 0; i < size; i++) {
            int b = i + offset;
            if (b >= size) b -= size;
            sudoku[rowDst][i] = sudoku[rowSrc][b];
        }
    }

    /**
     * Permutation
     */
    private void swapRowsSmall() {
        int row = random.nextInt(3) * 3;
        // System.out.println("Swap rows small: " + row);
        for (int i = 0; i < size; i++) {
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
        // System.out.println("Swap columns small: " + column);
        for (int i = 0; i < size; i++) {
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
        // System.out.println("Swap rows area: " + row);
        for (int i = 0; i < size; i++) {
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
        // System.out.println("Swap columns area: " + column);
        for (int i = 0; i < size; i++) {
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

    private void transponse() {

    }
}
