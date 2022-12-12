// Import Classes
import java.io.File;
import java.util.Scanner;
import java.io.PrintWriter;
import java.lang.Math;

/** 
 * [WordSearch.java]
 * Desc: Solves a given word search puzzle and a list of words
 * @author Emily Xie
 * @version Mar 2022
 */ 

public class WordSearch {
    public static void main(String[] args) throws Exception {
        
        // Read the puzzle.txt file to find the number of rows and columns of the puzzle array 
        Scanner input = readFile("puzzle.txt");
        int rowCount = 0;
        int columnCount = 0;
        String currentPuzzleLine = "";
        
        // Get the row count of the puzzle
        while (input.hasNextLine()) {
            currentPuzzleLine = input.nextLine();
            rowCount++;
        }
        // Get the column count of the puzzle
        for (int i = 0; i < currentPuzzleLine.length(); i++) {
            if (currentPuzzleLine.charAt(i) != ' ') {
                columnCount++;
            }
        }
        input.close(); 
        
        // Define the puzzle and solution arrays with the previously found rows & columns
        char[][] puzzle = new char[rowCount][columnCount];
        char[][] solution = new char[rowCount][columnCount];
        // Make the solution array empty
        for (int rowSolution = 0; rowSolution < rowCount; rowSolution++) {
            for (int columnSolution = 0; columnSolution < columnCount; columnSolution++) {
                solution[rowSolution][columnSolution] = ' ';
            }
        }
        
        // Read the puzzle.txt file again to fill the puzzle array with information
        input = readFile("puzzle.txt");
        while (input.hasNextLine()) {  
            for(int i = 0; i < puzzle.length; i++){
                String name = input.nextLine();
                int index = 0;
                char [] row = new char[columnCount];
                for (int j = 0; j < name.length(); j++) {
                    if (name.charAt(j) != ' ') {
                        row[index] = name.charAt(j);
                        index++;
                    }
                }
                puzzle[i] = row;
            }
        }
        input.close();
        
        // Find the number of words from the words.txt file
        input = readFile("words.txt");
        String currentWord = "";
        int wordNum = 0;
        
        // Get the number of words
        while(input.hasNextLine()) {
            currentWord = input.nextLine();
            wordNum++;
        }
        input.close();
        
        // Define the words array with previously found number of words
        String[] words = new String[wordNum];
        
        // Read the file again to fill the words array
        input = readFile("words.txt");
        for (int i = 0; i < wordNum; i++) {
            currentWord = input.nextLine();
            words[i] = currentWord;
        }
        input.close();
        
        // Find the words in the puzzle and save only solved words
        for(int count = 0; count < wordNum; count++) {
            String word = words[count];
            boolean wordFound = false;
            for (int currentRow = 0; currentRow < rowCount; currentRow++) {
                for (int currentColumn = 0; currentColumn < columnCount; currentColumn++) {
                    if (puzzle[currentRow][currentColumn] == word.charAt(0)) {
                        // Directions: -1 means going left/up, 0 means not moving, 1 means going right/down
                        final int DIR_LEFT = -1;
                        final int DIR_UP = -1;
                        final int DIR_RIGHT = 1;
                        final int DIR_DOWN = 1;
                        final int DIR_NONE = 0;
                        for (int rowDir = DIR_UP; rowDir <= DIR_DOWN; rowDir++) {
                            for (int columnDir = DIR_LEFT; columnDir <= DIR_RIGHT; columnDir++) {
                                // Only keep going if a solution hasn't been found
                                // When directions are 0, skip iteration
                                if ((rowDir != DIR_NONE || columnDir != DIR_NONE) && !wordFound) {
                                    wordFound = computeDirection(rowDir, columnDir, currentRow, currentColumn, word, rowCount, columnCount, puzzle);
                                    if (wordFound) {
                                        solution = updateSolution(word, currentRow, currentColumn, rowDir, columnDir, solution, rowCount, columnCount);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Create the solution file
        File solutionFile = new File("solution.txt");
        PrintWriter output = new PrintWriter(solutionFile);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                output.print(solution[i][j]);
                if (j == columnCount - 1)
                    output.print("\n");
                else
                    output.print(" ");
            }
        }
        output.close();
    }
    
    // Method: Scan text files
    public static Scanner readFile(String fileName) throws Exception{
        File file = new File(fileName);  
        Scanner input = new Scanner(file);
        return input;
    }
    
    // Method: Check if out of boundary
    public static boolean outOfBoundary(int currentRow, int currentColumn, int rowCount, int columnCount) {
        final int LOWER_BOUNDARY = 0;
        if (currentRow <  LOWER_BOUNDARY || currentRow >= rowCount) { 
            return true;
        } 
        if (currentColumn < LOWER_BOUNDARY || currentColumn >= columnCount) { 
            return true;
        }
        return false;
    }
    
    // Method: Find where words should wrap around
    public static int wrapStepCount(int rowDirection, int columnDirection, int rowCount, int columnCount, int currentRow, int currentColumn) {
        // Directions: -1 means going left/up, 0 means not moving, 1 means going right/down
        final int DIR_LEFT = -1;
        final int DIR_UP = -1;
        final int DIR_RIGHT = 1;
        final int DIR_DOWN = 1;
        final int DIR_NONE = 0;
        final int LENGTH_BUFFER = 1;
        int rowStepsAvble = 0;
        int columnStepsAvble = 0;
        int wrapStepCount;
        
        // Calculate the available rows that are needed to be wrapped around
        if (rowDirection == DIR_DOWN) {
            rowStepsAvble = Math.min(rowCount, currentRow);
        } 
        else if (rowDirection == DIR_UP) {
            rowStepsAvble = Math.min(rowCount - currentRow - LENGTH_BUFFER, rowCount);
        }
        // Calculate the available columns that are needed to be wrapped around
        if (columnDirection == DIR_RIGHT) {
            columnStepsAvble = Math.min(currentColumn, columnCount);
        } 
        else if (columnDirection == DIR_LEFT) {
            columnStepsAvble = Math.min(columnCount - currentColumn - LENGTH_BUFFER, columnCount); 
        }
        
        // If word goes straight, find their respective steps
        if (rowDirection == DIR_NONE) {
            wrapStepCount = columnStepsAvble;
        } 
        else if (columnDirection == DIR_NONE) {
            wrapStepCount = rowStepsAvble; 
        } 
        // If going in diagonals, the available steps is the minimum between available row and column steps
        else {
            wrapStepCount = Math.min(rowStepsAvble, columnStepsAvble);
        }
        return wrapStepCount;
    }
    
    // Method: Compute the direction of the steps to take to find the word
    public static boolean computeDirection(int rowDirection, int columnDirection, int initialRow, int initialColumn, String word, int rowCount, int columnCount, char[][] puzzle) {
        String compareWord = "";
        int length = word.length();
        for (int characterStep = 0; characterStep < length; characterStep++) {
            int currentRow = initialRow + characterStep * rowDirection;
            int currentColumn = initialColumn + characterStep * columnDirection;
            if (outOfBoundary(currentRow, currentColumn, rowCount, columnCount)) {
                // Check for wrap around
                int wrapStepCount = wrapStepCount(rowDirection, columnDirection, rowCount, columnCount, currentRow, currentColumn);
                // Wrap around the rows/columns if out of bound
                currentRow = currentRow - rowDirection * wrapStepCount;
                currentColumn = currentColumn - columnDirection * wrapStepCount;
                // No word is found if wrapped the whole way back to the starting point 
                // and compareWord's length is still smaller than the target word's length
                if (currentRow == initialRow && currentColumn == initialColumn) {
                    return false;
                }
            }
            compareWord += puzzle[currentRow][currentColumn];
        }
        if (compareWord.equals(word)) {
            return true;
        }
        return false;
    }
    
    // Method: Store words from the puzzle into the solution
    public static char[][] updateSolution(String word, int initialRow, int initialColumn, int rowDirection, int columnDirection, char[][] solution, int rowCount, int columnCount) {
        for (int characterStep = 0; characterStep < word.length(); characterStep++) {
            int currentRow = initialRow + rowDirection * characterStep;
            int currentColumn = initialColumn + columnDirection * characterStep;
            if (outOfBoundary(currentRow, currentColumn, rowCount, columnCount)) {
                // Update wrap around
                int wrapStepCount = wrapStepCount(rowDirection, columnDirection, rowCount, columnCount, currentRow, currentColumn);
                // Wrap around the rows/columns if out of bound
                currentRow = currentRow - rowDirection * wrapStepCount;
                currentColumn = currentColumn - columnDirection * wrapStepCount;
            }
            solution[currentRow][currentColumn] =  word.charAt(characterStep);
        }
////    
        printPuzzle(solution);
        
        return solution;
    }
    
////  
    public static void printPuzzle(char[][] puzzle){
        for (int row=0; row<puzzle.length; row++){
            for (int col=0; col<puzzle[0].length; col++){
                System.out.print(puzzle[row][col]+" ");
            }
            System.out.println();
        }
    }  
    
}