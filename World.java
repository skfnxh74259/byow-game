package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutograderBuddy {

    /**
     * Simulates a game, but doesn't render anything or call any StdDraw
     * methods. Instead, returns the world that would result if the input string
     * had been typed on the keyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quit and
     * save. To "quit" in this method, save the game to a file, then just return
     * the TETile[][]. Do not call System.exit(0) in this method.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public static TETile[][] getWorldFromInput(String input) {
        loadInput(input);
        return world.myWorld();
    }
    private static World world;
    private static StringBuilder seedMaker = new StringBuilder();


    private static Path file = Path.of("../SaveFile.txt");
    private static StringBuilder save = new StringBuilder();
    private static boolean qSwitch = false;


    private static void loadInput(String s) {
        for (char keyLoaded : s.toCharArray()) {
            try {
                doGame(keyLoaded);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String gameState = "main menu";
    private static void doGame(char c) throws IOException {
        c = Character.toUpperCase(c);
        if (gameState.equals("main menu")) {
            if (c == 'N') {
                save.append(c);
                gameState = "inputting seed";
            }
            if (c == 'L') {
                loadInput(Files.readString(file));
            }
            if (c == ':') {
                qSwitch = true;
            }
            if (c == 'Q' && qSwitch) {
                saveFile();
                System.exit(0);
            }


        } else if (gameState.equals("inputting seed")) {
            save.append(c);
            if (c >= '0' && c <= '9') {
                seedMaker.append(c);
            } else if (c == 'S') {
                makeWorldWithSeed();
            }

        } else if (gameState.equals("game start")) {
            if (c == 'W' || c == 'A' || c == 'S' || c == 'D') {
                save.append(c);
                if (world.movePlayer(c)) {
                    victoryAndReset();
                }
            } else if (c == 'T') {
                save.append(c);
                world.toggleLOS();
            } else if (c == ':') {
                qSwitch = true;
            } else if (c == 'Q' && qSwitch) {
                qSwitch = false;
                saveFile();
                System.exit(0);
            }
        } else if (gameState.equals("game ended")) {
            if (c == ':') {
                qSwitch = true;
            }
            if (c == 'Q' && qSwitch) {
                qSwitch = false;
                System.exit(0);
            }
        }
    }


    private static void makeWorldWithSeed() {
        System.out.println(seedMaker.toString());
        try {
            long ourSeed = Long.parseLong(seedMaker.toString());
            world = new World(ourSeed);
            world.initializeAvatar();
            gameState = "game start";
        } catch (NumberFormatException e) {
            System.out.println("Invalid seed. Please enter a new seed");
            gameState = "main menu";
            seedMaker.setLength(0);
        }
    }


    private static void victoryAndReset() {
        gameState = "game ended";
        seedMaker.setLength(0);
        try (FileWriter writer = new FileWriter(String.valueOf(file))) {
            writer.write("");
            System.out.println("File content has been successfully truncated.");
        } catch (IOException e) {
            // Handle IO exception, such as file not found
            e.printStackTrace();
        }
    }

    private static void saveFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(String.valueOf(file)))) {
            // Write the new content to the file
            writer.write(String.valueOf(save));
            System.out.println("File content has been successfully rewritten.");
        } catch (IOException e) {
            // Handle IO exception, such as file not found
            e.printStackTrace();
        }
    }


    /**
     * Used to tell the autograder which tiles are the floor/ground (including
     * any lights/items resting on the ground). Change this
     * method if you add additional tiles.
     */
    public static boolean isGroundTile(TETile t) {
        return t.character() == Tileset.FLOOR.character()
                || t.character() == Tileset.AVATAR.character()
                || t.character() == Tileset.FLOWER.character();
    }


    /**
     * Used to tell the autograder while tiles are the walls/boundaries. Change
     * this method if you add additional tiles.
     */
    public static boolean isBoundaryTile(TETile t) {
        return t.character() == Tileset.WALL.character()
                || t.character() == Tileset.LOCKED_DOOR.character()
                || t.character() == Tileset.UNLOCKED_DOOR.character();
    }
}
