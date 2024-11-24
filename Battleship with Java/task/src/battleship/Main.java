package battleship;

import java.util.Scanner;

public class Main {

    private static final int SIZE = 10;
    private static final char EMPTY_CELL = '~';
    private static final char SHIP_CELL = 'O';
    private static final char HIT_CELL = 'X';
    private static final char MISS_CELL = 'M';
    private static final char[] ROWS = "ABCDEFGHIJ".toCharArray();
    private static final String[] SHIP_NAMES = {
            "Aircraft Carrier", "Battleship", "Submarine", "Cruiser", "Destroyer"
    };
    private static final int[] SHIP_SIZES = {5, 4, 3, 3, 2};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialize fields for both players
        char[][] player1Field = createEmptyField();
        char[][] player2Field = createEmptyField();
        char[][] player1Fog = createEmptyField();
        char[][] player2Fog = createEmptyField();

        // Player 1 places ships
        System.out.println("Player 1, place your ships on the game field");
        printField(player1Field);
        placeAllShips(scanner, player1Field);

        promptEnterKey();

        // Player 2 places ships
        System.out.println("Player 2, place your ships on the game field");
        printField(player2Field);
        placeAllShips(scanner, player2Field);

        promptEnterKey();

        // Game loop
        boolean gameOver = false;
        while (!gameOver) {
            gameOver = takeShot(scanner, player2Fog, player1Field, player2Field, 1);
            if (gameOver) break; // Stop the game immediately if Player 1 wins
            promptEnterKey();

            gameOver = takeShot(scanner, player1Fog, player2Field, player1Field, 2);
            if (gameOver) break; // Stop the game immediately if Player 2 wins
            promptEnterKey();
        }
    }

    private static char[][] createEmptyField() {
        char[][] field = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                field[i][j] = EMPTY_CELL;
            }
        }
        return field;
    }

    private static void placeAllShips(Scanner scanner, char[][] field) {
        for (int i = 0; i < SHIP_NAMES.length; i++) {
            placeShip(scanner, field, SHIP_NAMES[i], SHIP_SIZES[i]);
            printField(field);
        }
    }

    private static void promptEnterKey() {
        System.out.println("Press Enter and pass the move to another player");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore any exceptions
        }
        System.out.println("\n".repeat(30));
    }

    private static boolean takeShot(Scanner scanner, char[][] fogField, char[][] playerField, char[][] opponentField, int playerNumber) {
        // Print the opponent's fog field and the player's own field
        printFieldWithFog(fogField);
        System.out.println("---------------------");
        printField(playerField); // Show own field

        System.out.printf("Player %d, it's your turn:%n", playerNumber);

        while (true) {
            String shot = scanner.nextLine();
            if (!isValidCoordinate(shot)) {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
                continue;
            }

            int[] shotCoord = parseCoordinate(shot);
            int row = shotCoord[0];
            int col = shotCoord[1];

            char targetCell = opponentField[row][col];
            if (targetCell == SHIP_CELL) {
                opponentField[row][col] = HIT_CELL;
                fogField[row][col] = HIT_CELL;

                if (isShipSunk(opponentField, row, col)) {
                    if (areAllShipsSunk(opponentField)) {
//                        printFieldWithFog(fogField);
                        System.out.println("You sank the last ship. You won. Congratulations!");
                        return true;
                    } else {
//                        printFieldWithFog(fogField);
                        System.out.println("You sank a ship!");
                    }
                } else {
//                    printFieldWithFog(fogField);
                    System.out.println("You hit a ship!");
                }
            } else if (targetCell == EMPTY_CELL) {
                opponentField[row][col] = MISS_CELL;
                fogField[row][col] = MISS_CELL;
//                printFieldWithFog(fogField);
                System.out.println("You missed!");
            } else if (targetCell == HIT_CELL || targetCell == MISS_CELL) {
//                System.out.println("You already shot here. Try again:");
                System.out.println("You already shot here. ");
//                continue;
            }
            break;
        }
        return false;
    }

    private static void placeShip(Scanner scanner, char[][] field, String shipName, int shipSize) {
        while (true) {
            System.out.printf("Enter the coordinates of the %s (%d cells):%n", shipName, shipSize);
            String[] coordinates = scanner.nextLine().split(" ");
            if (coordinates.length != 2 || !isValidCoordinate(coordinates[0]) || !isValidCoordinate(coordinates[1])) {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
                continue;
            }

            int[] start = parseCoordinate(coordinates[0]);
            int[] end = parseCoordinate(coordinates[1]);

            // Check alignment (vertical or horizontal)
            if (start[0] != end[0] && start[1] != end[1]) {
                System.out.println("Error! Wrong ship location! Try again:");
                continue;
            }

            // Check ship length
            int length = Math.abs(start[0] - end[0]) + Math.abs(start[1] - end[1]) + 1;
            if (length != shipSize) {
                System.out.printf("Error! Wrong length of the %s! Try again:%n", shipName);
                continue;
            }

            // Check for overlapping ships and adjacent ships
            if (!isValidPlacement(field, start, end)) {
                continue;
            }

            // Place the ship
            placeShipOnField(field, start, end);
            break;
        }
    }

    private static boolean isValidCoordinate(String coordinate) {
        if (coordinate.length() < 2 || coordinate.length() > 3) {
            return false;
        }
        char row = coordinate.charAt(0);
        String colStr = coordinate.substring(1);
        if (row < 'A' || row > 'J') {
            return false;
        }
        try {
            int col = Integer.parseInt(colStr);
            return col >= 1 && col <= 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int[] parseCoordinate(String coordinate) {
        int row = coordinate.charAt(0) - 'A';
        int col = Integer.parseInt(coordinate.substring(1)) - 1;
        return new int[]{row, col};
    }

    private static boolean isValidPlacement(char[][] field, int[] start, int[] end) {
        // Determine the range for rows and columns
        int rowStart = Math.min(start[0], end[0]);
        int rowEnd = Math.max(start[0], end[0]);
        int colStart = Math.min(start[1], end[1]);
        int colEnd = Math.max(start[1], end[1]);

        // Check for overlapping ships and adjacent ships
        for (int i = rowStart - 1; i <= rowEnd + 1; i++) {
            for (int j = colStart - 1; j <= colEnd + 1; j++) {
                if (i >= 0 && i < SIZE && j >= 0 && j < SIZE) {
                    if (field[i][j] == SHIP_CELL) {
                        System.out.println("Error! You placed it too close to another one. Try again:");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void placeShipOnField(char[][] field, int[] start, int[] end) {
        int rowStart = Math.min(start[0], end[0]);
        int rowEnd = Math.max(start[0], end[0]);
        int colStart = Math.min(start[1], end[1]);
        int colEnd = Math.max(start[1], end[1]);
        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = colStart; j <= colEnd; j++) {
                field[i][j] = SHIP_CELL;
            }
        }
    }

    private static void printField(char[][] field) {
        System.out.print("  ");
        for (int i = 1; i <= SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int i = 0; i < SIZE; i++) {
            System.out.print(ROWS[i] + " ");
            for (int j = 0; j < SIZE; j++) {
                System.out.print(field[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static void printFieldWithFog(char[][] field) {
        System.out.print("  ");
        for (int i = 1; i <= SIZE; i++) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int i = 0; i < SIZE; i++) {
            System.out.print(ROWS[i] + " ");
            for (int j = 0; j < SIZE; j++) {
                char cell = field[i][j];
                if (cell == HIT_CELL || cell == MISS_CELL) {
                    System.out.print(cell + " ");
                } else {
                    System.out.print(EMPTY_CELL + " ");
                }
            }
            System.out.println();
        }
    }

    private static boolean isShipSunk(char[][] field, int row, int col) {
        // Check horizontally to the left
        for (int j = col; j >= 0; j--) {
            if (field[row][j] == SHIP_CELL) {
                return false;
            } else if (field[row][j] == EMPTY_CELL) {
                break;
            }
        }
        // Check horizontally to the right
        for (int j = col; j < SIZE; j++) {
            if (field[row][j] == SHIP_CELL) {
                return false;
            } else if (field[row][j] == EMPTY_CELL) {
                break;
            }
        }
        // Check vertically upwards
        for (int i = row; i >= 0; i--) {
            if (field[i][col] == SHIP_CELL) {
                return false;
            } else if (field[i][col] == EMPTY_CELL) {
                break;
            }
        }
        // Check vertically downwards
        for (int i = row; i < SIZE; i++) {
            if (field[i][col] == SHIP_CELL) {
                return false;
            } else if (field[i][col] == EMPTY_CELL) {
                break;
            }
        }
        return true;
    }

    private static boolean areAllShipsSunk(char[][] field) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (field[i][j] == SHIP_CELL) { // Any ship cell found means not all are sunk
                    return false;
                }
            }
        }
        return true; // No ship cells remain, all are sunk
    }

}