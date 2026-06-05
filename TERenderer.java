package core;
import edu.princeton.cs.algs4.StdDraw;




import tileengine.TERenderer;
import tileengine.TETile;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


public class Main {
    private static World world;
    private static StringBuilder seedMaker = new StringBuilder();


    private static TERenderer ter;
    private static Path file = Path.of("../SaveFile.txt");
    private static StringBuilder save = new StringBuilder();
    private static boolean tileDidChange;
    private static boolean qSwitch = false;
    private static TETile previousMouseTile = null;


    public static void main(String[] args) {
        displayMainMenu();
        while (true) {
            handleInput();
            if (Objects.equals(gameState, "game start")) {
                updateTileChange();
            }
        }
    }
    private static void displayMainMenu() {
        StdDraw.clear();
        StdDraw.text(0.5, 0.6, "New Game (N)");
        StdDraw.text(0.5, 0.5, "Load Game (L)");
        StdDraw.text(0.5, 0.4, "Quit (:Q)");
        StdDraw.show();
    }


    private static void loadInput(String s) {
        for (char keyLoaded : s.toCharArray()) {
            try {
                doGame(keyLoaded);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static void handleInput() {
        if (StdDraw.hasNextKeyTyped()) {
            char keyTyped = Character.toUpperCase(StdDraw.nextKeyTyped());
            try {
                doGame(keyTyped);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static void updateTileChange() {
        int mouseX = 0;
        int mouseY = 0;
        if (world != null) {
            mouseX = (int) Math.floor(StdDraw.mouseX());
            mouseY = (int) Math.floor(StdDraw.mouseY());
            if (mouseX >= 0 && mouseX < World.MAP_WIDTH && mouseY >= 0 && mouseY < World.MAP_HEIGHT) {
                TETile currentMouseTile = world.myWorld()[mouseX][mouseY];
                if (previousMouseTile == null || !currentMouseTile.equals(previousMouseTile)) {
                    tileDidChange = true;
                    previousMouseTile = currentMouseTile;
                }
            }
        }
        if (tileDidChange) {
            world.makeHUD(mouseX, mouseY);
            tileDidChange = false;
        }
    }
    private static String gameState = "main menu";
    private static void doGame(char c) throws IOException {
        c = Character.toUpperCase(c);
        if (gameState.equals("main menu")) {
            if (c == 'N') {
                save.append(c);
                newGame();
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


        } else if (gameState.equals("choose avatar")) {
            save.append(c);
            if (c == '1' || c == '2') {
                pickAvatar(c);


                // init renderer and make map to start game
                ter = new TERenderer();
                ter.initialize(World.MAP_WIDTH, World.MAP_HEIGHT + 5, 0, 0);
                ter.renderFrame(world.lineOfSightMap());


                gameState = "game start";
            }
        } else if (gameState.equals("game start")) {
            updateTileChange();
            tileDidChange = false;
            ter.renderFrame(world.lineOfSightMap());
            if (c == 'W' || c == 'A' || c == 'S' || c == 'D') {
                save.append(c);
                if (world.movePlayer(c)) {
                    victoryAndReset();
                } else {
                    tileDidChange = true;
                    ter.renderFrame(world.lineOfSightMap());
                }
            } else if (c == 'T') {
                save.append(c);
                world.toggleLOS();
                tileDidChange = true;
                ter.renderFrame(world.lineOfSightMap());
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
            avatarMenu();
            gameState = "choose avatar";
        } catch (NumberFormatException e) {
            System.out.println("Invalid seed. Please enter a new seed");
            displayMainMenu();
            gameState = "main menu";
            seedMaker.setLength(0);
        }
    }


    private static void newGame() {
        StdDraw.clear();
        StdDraw.text(0.5, 0.7, "Type in a seed ending in 'S' like 1234S:");
        //updating gamestate
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
        StdDraw.clear();
        StdDraw.text(World.MAP_WIDTH / 2, World.MAP_HEIGHT, "VICTORY! Press :Q to exit the game.");
        StdDraw.show();
    }


    private static void avatarMenu() {
        StdDraw.clear();
        StdDraw.text(0.5, 0.6, "Choose your character (1 or 2):");
        StdDraw.show();
    }


    private static void pickAvatar(char option) {
        // Process character selection...
        // You can add more logic here based on the chosen character.
        // For simplicity, let's assume characters 1 and 2 for now.
        switch (option) {
            case '1':
                world.initializeAvatar();
                break;
            case '2':
                world.initializeAvatar();
                world.changeAvatar();
                break;
            default:
                System.out.println("Invalid character choice. Please choose 1 or 2.");
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
}
