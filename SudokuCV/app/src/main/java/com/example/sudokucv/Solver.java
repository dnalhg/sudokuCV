package com.example.sudokucv;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Solver {

    private static boolean isValidList(int[] elements) {
        int[] flag = new int[9];

        for (int i=0; i<elements.length; i++) {
            if (elements[i]!=0) {
                if (flag[elements[i]-1] == 1) {
                    return false;
                }
                flag[elements[i]-1] = 1;
            }
        }
        return true;
    }

    private static boolean isValidBoard(int[] board){
        //Checking horizontally
        for (int i=0; i<9; i++) {
            int[] row = Arrays.copyOfRange(board,i*9,i*9+9);
            if (!isValidList(row)) {
                return false;
            }
        }

        //Checking vertically
        for (int i=0; i<9; i++) {
            int[] elements = new int[9];
            for (int j=0; j<9; j++) {
                elements[j] = board[9*j+i];
            }
            if (!isValidList(elements)){
                return false;
            }
        }

        //Checking the 3x3 grids
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                int[] elements = new int[9];
                for (int x=0; x<3; x++) {
                    for (int y=0; y<3; y++) {
                        elements[3*x+y] = board[3*i+27*j+x+9*y];
                    }
                }
                if (!isValidList(elements)){
                    return false;
                }
            }
        }

        return true;

    }

    private static boolean isValidLastmove(int[] board, int lastMove) {
        //Checking the row
        int row = (int) Math.floor(lastMove/9);
        int[] boardRow = Arrays.copyOfRange(board,9*row,9*row+9);
        if (!isValidList(boardRow)){
            return false;
        }

        //Checking the column
        int colNum = lastMove%9;
        int[] col = new int[9];
        for (int i=0; i<9; i++) {
            col[i] = board[9*i+colNum];
        }
        if (!isValidList(col)){
            return false;
        }

        //Checking the 3x3 grid
        int[] corner = new int[]{(int)Math.floor(row/3), (int)Math.floor(colNum/3)};
        int[] elements = new int[9];
        for (int x=0; x<3; x++) {
            for (int y=0; y<3; y++) {
                elements[3*x+y] = board[27*corner[0]+x+3*corner[1]+9*y];
            }
        }
        if (!isValidList(elements)){
            return false;
        }

        return true;
    }

    private static int[] solve(int[] board, LinkedList<Integer> emptyEntries, final Map<Integer, List<Integer>> allowed) {
        if (emptyEntries.size() == 0) {
            return board;
        }

        int entry = emptyEntries.removeLast();
        final List<Integer> allowedEntries = allowed.get(entry);
        for (int x: allowedEntries) {
            board[entry] = x;
            if (!isValidLastmove(board, entry)) {
                board[entry] = 0;
                continue;
            }
            int[] result = solve(board,emptyEntries,allowed);
            if (result != null) {
                return result;
            } else {
                board[entry] = 0;
            }
        }
        emptyEntries.addLast(entry);
        return null;
    }

    public static int[] solve(int[] board) {
        if (!isValidBoard(board)) {
            return null;
        }

        LinkedList<Integer> emptyEntries = new LinkedList<>();
        Map<Integer, List<Integer>> allowed = new HashMap<Integer, List<Integer>>();
        for (int i=0; i<81; i++) {
            if (board[i] == 0) {
                emptyEntries.add(i);
                List<Integer> allowedEntries = new ArrayList<>();
                for (int x=1; x < 10; x++) {
                    board[i] = x;
                    if (isValidLastmove(board, i)) {
                        allowedEntries.add(x);
                    }
                }
                board[i] = 0;
                allowed.put(i, allowedEntries);
            }
        }
        return solve(board, emptyEntries, allowed);

    }

    public static int[] generate() {
        int[] board = new int[81];
        int[] finalBoard = new int[81];
        boolean finished = false;
        Random rng = new Random();
        List<Integer> shuffleIdx = new ArrayList<>();
        for (int i=0; i<81; i++) {
            shuffleIdx.add(i);
        }
        List<Integer> entries = new ArrayList<>();
        for (int i=1; i<10; i++) {
            entries.add(i);
        }

        while (!finished) {
            finished = true;
            Arrays.fill(board, 0);
            int numEntries = 10 + rng.nextInt(20);

            // Shuffle indices and fill first numEntries indices with random numbers
            Collections.shuffle(shuffleIdx);

            boolean success;
            for (int i=0; i<numEntries; i++) {
                int boardIdx = shuffleIdx.get(i);
                // Shuffling entries to randomly select entry to fill with
                Collections.shuffle(entries);
                success = false;
                for (int j=0; j<9; j++) {
                    board[boardIdx] = entries.get(j);
                    if (isValidLastmove(board, boardIdx)) {
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    finished = false;
                    break;
                }
            }
            if (finished) {
                Log.i("Generated", Arrays.toString(board));

                ExecutorService service = Executors.newFixedThreadPool(2);
                Future<int[]> futureResult = service.submit(new SolverTimeout(board));
                try{
                    int[] result = futureResult.get(5, TimeUnit.SECONDS);
                    if (result == null) {
                        finished = false;
                    } else {
                        for (int i=0; i<numEntries; i++) {
                            finalBoard[shuffleIdx.get(i)] = board[shuffleIdx.get(i)];
                        }
                    }
                } catch(TimeoutException | ExecutionException | InterruptedException timeout){
                    futureResult.cancel(true);
                    finished = false;
                }
                service.shutdownNow();
            }

        }
        return finalBoard;
    }

    static class SolverTimeout implements Callable<int[]> {
        int[] board;
        public SolverTimeout(int[] board) {
            this.board = board;
        }
        @Override
        public int[] call() throws Exception {
            return solve(board);
        }
    }

}
