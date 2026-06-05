package core;
import java.util.Random;
import tileengine.TETile;
import tileengine.Tileset;
import java.util.ArrayList;
import java.util.List;
import edu.princeton.cs.algs4.StdDraw;


import static utils.RandomUtils.uniform;




public class World {
    private TETile[][] tiles;
    int startX;
    int startY;
    private static final int MAX_ROOM_SIZE = 6;
    private static final int MIN_ROOM_SIZE = 2;
    private static final int MAX_HALL_SIZE = 8;
    private static final int MIN_HALL_SIZE = 3;
    private static final long DEFAULT_SEED = 1234L;
    private static final int RADIUS = 3;
    private long seed;
    private int occupiedSpaces = 0;
    private double occupancyRatio = 0.25;
    private double occupancyThreshold;
    private Random r;
    private int pd;
    private TETile myAvatar;


    public static final int MAP_WIDTH = 80;
    public static final int MAP_HEIGHT = 40;
    private int xPos;
    private int yPos;
    public TETile[][] myWorld() {
        return tiles;
    }
    private boolean lightsOn = false;


    public World() {
        this.seed = DEFAULT_SEED;
        this.r = new Random(this.seed);
        this.pd = uniform(r, 4);
        this.occupancyThreshold = Math.floor(MAP_HEIGHT * MAP_WIDTH * occupancyRatio);
        tiles = new TETile[MAP_WIDTH][MAP_HEIGHT];
        makeNewWorld(Tileset.NOTHING);
        roomHelper(MAP_WIDTH / 2, MAP_HEIGHT / 2);
        wallMaker(MAP_WIDTH, MAP_HEIGHT);
        myAvatar = Tileset.AVATAR;
        lineOfSightMap();
    }
    public World(long seed) {
        this.seed = seed;
        this.r = new Random(this.seed);
        this.pd = uniform(r, 4);
        this.occupancyThreshold = Math.floor(MAP_HEIGHT * MAP_WIDTH * occupancyRatio);
        tiles = new TETile[MAP_WIDTH][MAP_HEIGHT];
        makeNewWorld(Tileset.NOTHING);
        roomHelper(MAP_WIDTH / 2, MAP_HEIGHT / 2);
        wallMaker(MAP_WIDTH, MAP_HEIGHT);
        myAvatar = Tileset.AVATAR;
        lineOfSightMap();
    }
    //implement line of sight rendering
    public TETile[][] lineOfSightMap() {
        if (lightsOn) {
            return tiles;
        }
        TETile[][] sightMap = new TETile[MAP_WIDTH][MAP_HEIGHT];
        //Fill sightMap with NOTHING tile type
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                sightMap[x][y] = Tileset.NOTHING;
            }
        }
        int leftX = Math.max(0, xPos - RADIUS);
        int rightX = Math.min(MAP_WIDTH - 1, xPos + RADIUS);
        int topY = Math.min(MAP_HEIGHT - 1, yPos + RADIUS);
        int botY = Math.max(0, yPos - RADIUS);
        for (int x = leftX; x <= rightX; x++) {
            for (int y = botY; y <= topY; y++) {
                sightMap[x][y] = tiles[x][y];
            }
        }
        return sightMap;
    }
    //toggling line of sight via button press
    public void toggleLOS() {
        lightsOn = !lightsOn;
    }
    //Place avatar in a specific spot (randomized from all floor tiles)
    public void initializeAvatar() {
        List<int[]> floorTiles = collectFloorTiles();
        if (!floorTiles.isEmpty()) {
            int[] avatarStart = floorTiles.get(uniform(r, floorTiles.size()));
            xPos = avatarStart[0];
            yPos = avatarStart[1];
            tiles[xPos][yPos] = Tileset.AVATAR;
        }
    }
    // Add menu option to cycle appearance between 2 options
    public void changeAvatar() {
        if (myAvatar == Tileset.AVATAR) {
            myAvatar = Tileset.FLOWER;
        } else {
            myAvatar = Tileset.AVATAR;
        }
        tiles[xPos][yPos] = myAvatar;
    }
    //Store all floor tiles aka possible starting positions
    private List<int[]> collectFloorTiles() {
        List<int[]> floorTiles = new ArrayList<>();
        for (int x = 1; x < MAP_WIDTH - 1; x++) {
            for (int y = 1; y < MAP_HEIGHT - 1; y++) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    floorTiles.add(new int[]{x, y});
                }
            }
        }
        return floorTiles;
    }
    //method to move avatar & return if this move led to victory or not
    public boolean movePlayer(char input) {
        int xPosNew = xPos;
        int yPosNew = yPos;
        switch (input) {
            case 'W':
                yPosNew += 1;
                break;
            case 'A':
                xPosNew -= 1;
                break;
            case 'S':
                yPosNew -= 1;
                break;
            case 'D':
                xPosNew += 1;
                break;
            default:
                break;
        }
        if (isMoveValid(xPosNew, yPosNew)) {
            //Win condition = projected move takes them into unlocked door tile
            if (tiles[xPosNew][yPosNew] == Tileset.UNLOCKED_DOOR) {
                //openVictoryScreen arbitrary method that produces victory screen (incomplete)
                return true;
            } else {
                //old position becomes floor
                tiles[xPos][yPos] = Tileset.FLOOR;
                //update avatar coordinates as new coordinates
                xPos = xPosNew;
                yPos = yPosNew;
                //projected position becomes avatar
                tiles[xPos][yPos] = myAvatar;
            }
        }
        return false;
    }
    //check if tile from projected movement is a valid floor tile
    private boolean isMoveValid(int x, int y) {
        return tiles[x][y] == Tileset.FLOOR || tiles[x][y] == Tileset.UNLOCKED_DOOR;
    }
    public void makeHUD(int mouseOverX, int mouseOverY) {
        //current tile mouse is hovering over
        TETile hoveredTile;
        if (lightsOn) {
            hoveredTile = tiles[mouseOverX][mouseOverY];
        } else {
            TETile[][] sightMap = lineOfSightMap();
            hoveredTile = sightMap[mouseOverX][mouseOverY];
        }
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledRectangle(MAP_WIDTH / 2, MAP_HEIGHT + MAP_HEIGHT / 2, MAP_WIDTH / 2, MAP_HEIGHT / 2);
        StdDraw.setPenColor(StdDraw.MAGENTA);
        StdDraw.text(10, MAP_HEIGHT + 3, "Tile: " + hoveredTile.character() + "," + " " + moreDescription(hoveredTile));
        StdDraw.show();
    }
    //helper to provide HUD info
    private String moreDescription(TETile tile) {
        if (tile.character() == '@' || tile.character() == '❀') {
            return "You";
        } else {
            return tile.description();
        }
    }






    //helper to indicate whether we should stop creating spaces
    private boolean isTooFull() {
        return occupiedSpaces > occupancyThreshold;
    }


    private void makeNewWorld(TETile tile) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = tile;
            }
        }
    }


    // build your own world!
    private void roomHelper(int x, int y) {
        int width = uniform(r, MIN_ROOM_SIZE, MAX_ROOM_SIZE);
        int height = uniform(r, MIN_ROOM_SIZE,  MAX_ROOM_SIZE);
        if (occupiedSpaces > 0 && uniform(r, 8) == 0) {
            // if not first room && in 1 in 8 chance
            // build 1x1 room
            width = 1;
            height = 1;
        } else {
            // build normal room
            int offset;
            int initx = x;
            int inity = y;

            int inBoundsCounter = 0;
            if (pd == 0) {
                offset = uniform(r, width);
                x -= offset;
            }
            if (pd == 1) {
                offset = uniform(r, height);
                y -= offset;
            }
            if (pd == 2) {
                offset = uniform(r, width);
                x -= offset;
                y -= height;
            }
            if (pd == 3) {
                offset = uniform(r, height);
                x -= width;
                y -= offset;
            }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int currX = x + i;
                    int currY = y + j;
                    if (currX >= 1 && currX < MAP_WIDTH - 1 && currY >= 1 && currY < MAP_HEIGHT - 1) {
                        inBoundsCounter += 1;
                    }
                }
            }
            if (inBoundsCounter == width || inBoundsCounter == height) {
                width = 1;
                height = 1;
                x = initx;
                y = inity;
            }


            //normal room
        }


        if (x < 1) {
            width -= 1 - x;
            x = 1;
        }
        if (y < 1) {
            height -= 1 - y;
            y = 1;
        }
        if (x + width > MAP_WIDTH - 1) {
            width = MAP_WIDTH - x - 1;
        }
        if (y + height > MAP_HEIGHT - 1) {
            height = MAP_HEIGHT - y - 1;
        }


        if (width < 1 || height < 1) {
            width = 1;
            height = 1;
        }
        roomBuilder(width, height, x, y);
        if (isTooFull() && width != 1 && height != 1) {
            return;
        }
        hallWayMaker(width, height, x, y, uniform(r, 4));
    }


    private void hallWayMaker(int w, int h, int x, int y, int cd) {
        int[] output = hallHelper(w, h, x, y, cd);
        int nextx = output[0];
        int nexty = output[1];
        int hallLength = output[2];
        if (pd == 0) {
            nexty = nexty + hallLength - 1;
        }
        if (pd == 1) {
            nextx = nextx + hallLength - 1;
        }
        if (pd == 2) {
            nexty = nexty - hallLength + 1;
        }
        if (pd == 3) {
            nextx = nextx - hallLength + 1;
        }
        roomHelper(nextx, nexty);
    }


    private boolean isOpposite(int d1, int d2) {
        if (d1 == 0 && d2 == 2) {
            return true;
        }
        if (d1 == 1 && d2 == 3) {
            return true;
        }
        if (d1 == 2 && d2 == 0) {
            return true;
        }
        if (d1 == 3 && d2 == 1) {
            return true;
        }
        return false;
    }


    private int[] hallHelper(int w, int h, int x, int y, int cd) {
        if (isOpposite(cd, pd)) {
            return hallHelper(w, h, x, y, uniform(r, 4));
        }
        int hallLength = uniform(r, MIN_HALL_SIZE, MAX_HALL_SIZE);
        if (cd == 0) {
            if (y + h + hallLength > MAP_HEIGHT - 2) {
                return hallHelper(w, h, x, y, uniform(r, 4));
            }
            y += h;
            x += uniform(r, w);
        } else if (cd == 1) {
            if (x + w + hallLength > MAP_WIDTH - 2) {
                return hallHelper(w, h, x, y, uniform(r, 4));
            }
            x += w;
            y += uniform(r, h);
        } else if (cd == 2) {
            if (y - hallLength < 1) {
                return hallHelper(w, h, x, y, uniform(r, 4));
            }
            x += uniform(r, w);
        } else {
            if (x - hallLength < 1) {
                return hallHelper(w, h, x, y, uniform(r, 4));
            }
            y += uniform(r, h);
        }
        pd = cd;
        hallBuilder(x, y, hallLength);
        int[] result = {x, y, hallLength};
        return result;
    }


    private void roomBuilder(int w, int h, int x, int y) { //need to 1.check oob 2.shift
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                if (tiles[i][j] != Tileset.FLOOR) {
                    tiles[i][j] = Tileset.FLOOR;
                    occupiedSpaces += 1;
                }
            }
        }
    }


    private void hallBuilder(int x, int y, int l) {
        if (pd == 0) {
            for (int j = y; j < y + l; j++) {
                if (tiles[x][j] != Tileset.FLOOR) {
                    tiles[x][j] = Tileset.FLOOR;
                    occupiedSpaces += 1;
                }
            }
        } else if (pd == 1) {
            for (int i = x; i < x + l; i++) {
                if (tiles[i][y] != Tileset.FLOOR) {
                    tiles[i][y] = Tileset.FLOOR;
                    occupiedSpaces += 1;
                }
            }
        } else if (pd == 2) {
            for (int j = y; j > y - l; j--) {
                if (tiles[x][j] != Tileset.FLOOR) {
                    tiles[x][j] = Tileset.FLOOR;
                    occupiedSpaces += 1;
                }
            }
        } else {
            for (int i = x; i > x - l; i--) {
                if (tiles[i][y] != Tileset.FLOOR) {
                    tiles[i][y] = Tileset.FLOOR;
                    occupiedSpaces += 1;
                }
            }
        }


    }
    private void wallMaker(int w, int h) {
        //Added some code like storing wall coords to generate random location as door
        List<int[]> storedWalls = new ArrayList<>();
        for (int i = 1; i < w - 1; i++) {
            for (int j = 1; j < h - 1; j++) {
                if (tiles[i][j] == Tileset.FLOOR) {
                    if (tiles[i - 1][j] != Tileset.FLOOR) {
                        tiles[i - 1][j] = Tileset.WALL;
                        storedWalls.add(new int[]{i - 1, j});
                    }
                    if (tiles[i + 1][j] != Tileset.FLOOR) {
                        tiles[i + 1][j] = Tileset.WALL;
                        storedWalls.add(new int[]{i + 1, j});
                    }
                    if (tiles[i][j - 1] != Tileset.FLOOR) {
                        tiles[i][j - 1] = Tileset.WALL;
                        storedWalls.add(new int[]{i, j - 1});
                    }
                    if (tiles[i][j + 1] != Tileset.FLOOR) {
                        tiles[i][j + 1] = Tileset.WALL;
                        storedWalls.add(new int[]{i, j + 1});
                    }
                }
            }


        }
        makeVictoryDoor(storedWalls);
    }
    private void makeVictoryDoor(List<int[]> storedWalls) {
        if (!storedWalls.isEmpty()) {
            int[] theDoor = storedWalls.get(uniform(r, storedWalls.size()));
            tiles[theDoor[0]][theDoor[1]] = Tileset.UNLOCKED_DOOR;
        }
    }


}
