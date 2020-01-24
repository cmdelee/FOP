/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.bradford.spacegame;

import uk.ac.bradford.spacegame.Asteroid.Direction;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

/**
 * The GameEngine class is responsible for managing information about the game,
 * creating levels, the player, aliens and asteroids, as well as updating
 * information when a key is pressed while the game is running.
 *
 * @author prtrundl
 */
public class GameEngine {

    /**
     * An enumeration type to represent different types of tiles that make up
     * the level. Each type has a corresponding image file that is used to draw
     * the right tile to the screen for each tile in a level. Space is open for
     * the player and asteroids to move into, black holes will kill the player
     * if they move into the tile and destroy asteroids that move into them,
     * pulsars will damage the player if they are in or adjacent to a pulsar
     * tile while it is active.
     */
    public enum TileType {
        SPACE, BLACK_HOLE, PULSAR_ACTIVE, PULSAR_INACTIVE;

    }

    /**
     * The width of the level, measured in tiles. Changing this may cause the
     * display to draw incorrectly, and as a minimum the size of the GUI would
     * need to be adjusted.
     */
    public static final int GRID_WIDTH = 25;

    /**
     * The height of the level, measured in tiles. Changing this may cause the
     * display to draw incorrectly, and as a minimum the size of the GUI would
     * need to be adjusted.
     */
    public static final int GRID_HEIGHT = 18;

    /**
     * The chance of a black hole being generated instead of open space when
     * generating the level. 1.0 is 100% chance, 0.0 is 0% chance. This can be
     * changed to affect the difficulty.
     */
    private static final double BLACK_HOLE_CHANCE = 0.07;

    /**
     * The chance of a pulsar being created instead of open space when
     * generating the level. 1.0 is 100% chance, 0.0 is 0% chance. This can be
     * changed to affect the difficulty.
     */
    private static final double PULSAR_CHANCE = 0.03;

    /**
     * A random number generator that can be used to include randomised choices
     * in the creation of levels, in choosing places to spawn the player, aliens
     * and asteroids, and to randomise movement or other factors.
     */
    private Random rng = new Random();

    /**
     * The number of levels cleared by the player in this game. Can be used to
     * generate harder games as the player clears levels.
     */
    private int cleared = 0;

    /**
     * The number of points the player has gained this level. Used to track when
     * the current level is won and a new one should be generated.
     */
    private int points = 0;

    /**
     * Tracks the current turn number. Used to control pulsar activation and
     * asteroid movement.
     */
    private int turnNumber = 1;

    /**
     * The GUI associated with a GameEngine object. THis link allows the engine
     * to pass level (tiles) and entity information to the GUI to be drawn.
     */
    private GameGUI gui;

    /**
     * The 2 dimensional array of tiles the represent the current level. The
     * size of this array should use the GRID_HEIGHT and GRID_WIDTH attributes
     * when it is created.
     */
    private TileType[][] tiles;

    /**
     * An ArrayList of Point objects used to create and track possible locations
     * to spawn the player, aliens and asteroids.
     */
    private ArrayList<Point> spawns;

    /**
     * A Player object that is the current player. This object stores the state
     * information for the player, including hull strength and the current
     * position (which is a pair of co-ordinates that corresponds to a tile in
     * the current level)
     */
    private Player player;

    /**
     * An array of Alien objects that represents the aliens in the current
     * level. Elements in this array should be of the type Alien, meaning that
     * an alien is alive and needs to be drawn or moved, or should be null which
     * means nothing is drawn or processed for movement. Null values in this
     * array are skipped during drawing and movement processing.
     */
    private Alien[] aliens;

    /**
     * An array of Asteroid objects that represents the asteroids in the current
     * level. Elements in this array should be of the type Asteroid, meaning
     * that an asteroid exists and needs to be drawn or moved, or should be null
     * which means nothing is drawn or processed for movement. Null values in
     * this array are skipped during drawing and movement processing.
     */
    private Asteroid[] asteroids;

    /**
     * Constructor that creates a GameEngine object and connects it with a
     * GameGUI object.
     *
     * @param gui The GameGUI object that this engine will pass information to
     * in order to draw levels and entities to the screen.
     */
    public GameEngine(GameGUI gui) {
        this.gui = gui;
        startGame();
    }

    /**
     * Generates a new level. The method builds a 2D array of TileTypes that
     * will be used to draw tiles to the screen and to add a variety of elements
     * into each level. Tiles can be space, black holes, active pulsars or
     * inactive pulsars. This method should contain the implementation of an
     * algorithm to create an interesting and varied level each time it is
     * called.
     *
     * @return A 2D array of TileTypes representing the tiles in the current
     * level of the dungeon. The size of this array should use the width and
     * height attributes of the level specified by GRID_WIDTH and GRID_HEIGHT.
     */
    private TileType[][] generateLevel() {

        TileType[][] tile = new TileType[GRID_WIDTH][GRID_HEIGHT];
        double chance;
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                chance = rng.nextDouble(); //generates random double for use in generating obstacles in the level
                if (chance < BLACK_HOLE_CHANCE) {
                    tile[i][j] = TileType.BLACK_HOLE;
                    if (chance < PULSAR_CHANCE) {
                        tile[i][j] = TileType.PULSAR_ACTIVE;
                        if (chance < PULSAR_CHANCE / 2) {
                            tile[i][j] = TileType.PULSAR_INACTIVE;
                        }
                    }
                } else {
                    tile[i][j] = TileType.SPACE;
                }
            }
        }

        return tile;            //modify this to return a dynamically generated 2D array
    }

    /**
     * Generates spawn points for entities. The method processes the tiles array
     * and finds tiles that are suitable for spawning, i.e. space tiles.
     * Suitable tiles should be added to the ArrayList that will be returned as
     * Point objects - Points are a simple kind of object that contain an X and
     * a Y co-ordinate stored using the int primitive type.
     *
     * @return An ArrayList containing Point objects representing suitable X and
     * Y co-ordinates in the current level that entities can be spawned in.
     */
    private ArrayList<Point> getSpawns() {
        ArrayList<Point> s = new ArrayList<>();

        int arrayPosition = 0; 

        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {

                if (tiles[i][j] == TileType.SPACE) {

                    Point available = new Point(i, j);
                    s.add(arrayPosition, available);
                    arrayPosition++;
                }
            }
        }

        return s;

    }

    /**
     * Spawns aliens in suitable locations in the current level. The method uses
     * the spawns ArrayList to pick suitable positions to add aliens, removing
     * these positions from the spawns ArrayList as they are used (using the
     * remove() method) to avoid multiple entities spawning in the same
     * location. The method creates aliens by instantiating the Alien class,
     * setting health and the X and Y position for the alien using the Point
     * object removed from the spawns ArrayList.
     *
     * @return An array of Alien objects representing the aliens for the current
     * level
     */
    private Alien[] spawnAliens() {
        int alienNumbers;
        if (cleared < 10) {
            alienNumbers = cleared;
        } else {
            alienNumbers = 10; // alien numbers increase based on level up to 10 aliens (level 10)
        }
        Alien[] alien = new Alien[alienNumbers];

        for (int i = 0; i < alienNumbers; i++) {

            int n = rng.nextInt(spawns.size());

            int x = spawns.get(n).x;
            int y = spawns.get(n).y;

            alien[i] = new Alien(50, x, y); // instantiates allien with 50 health in position x, y

            spawns.remove(n);
        }
        return alien;            //modify to return array of Alien objects
    }

    /**
     * Spawns a Player entity in the game. The method uses the spawns ArrayList
     * to select a suitable location to spawn the player and removes the Point
     * from the spawns ArrayList. The method instantiates the Player class and
     * assigns values for the health and position of the player.
     *
     * @return A Player object representing the player in the game
     */
    private Player spawnPlayer() {

        int n = rng.nextInt(spawns.size());

        int x = spawns.get(n).x; // gets x coordinates of spawns for player position
        int y = spawns.get(n).y; // gets y coordinates of spawns for player position

        Player playerShip = new Player(100, x, y); // spawns player with 100 health in position x, y

        spawns.remove(n);

        return playerShip;            //modify to return a Player object

    }

    /**
     * handles collisions within the game, if the player collides with an
     * asteroid or alien, using X and Y coordinates of both the player and the
     * item that could cause possible collisions, for asteroids players score is
     * increased and the asteroid is removed from play whereas for aliens the
     * aliens ship is damaged or destroyed if the hull strength falls below or
     * equal to 0. if alien is destroyed it is removed from play
     */
    private void collisions() {

        for (int i = 0; i < asteroids.length; i++) {
            try {
                if (asteroids[i].getX() == player.getX()) {
                    if (asteroids[i].getY() == player.getY()) {

                        points++;
                        asteroids[i] = null; // removes asteroids from the game once collected
                    }
                }
            } catch (NullPointerException e) { // deals with errors caused by asteroids being taken out of play
            }
        }
        for (int i = 0; i < aliens.length; i++) {
            try {
                if (aliens[i].getX() == player.getX()) {
                    if (aliens[i].getY() == player.getY()) {
                        aliens[i].hullStrength = aliens[i].hullStrength - 20; // damages aliens if player moves into them
                        if (aliens[i].hullStrength <= 0) { // handles what to do when aliens health is 0
                            aliens[i] = null; // remves aliens from the game once defeated
                        }
                    }

                }
            } catch (NullPointerException e) { // deals with errors caused by aliens being taken out of play
            }
        }

    }

    /**
     * Handles the movement of the player when attempting to move left in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the left arrow key on the keyboard. The method checks whether the
     * tile to the left of the player is empty for movement and if it is updates
     * the player object's X and Y locations with the new position. If the tile
     * to the left of the player is not empty the method will not update the
     * player position, but could make other changes to the game.
     */
    public void movePlayerLeft() {
        int x = player.getX();
        int y = player.getY();

        if (x <= 0) {
            x += GRID_WIDTH;
        } // uses modular division to allow the ship to come onto the other side of the grid if it travels off screen

        player.setPosition(x - 1, y);

        int j = player.getX();
        int k = player.getY();

        if (tiles[j][k] == TileType.BLACK_HOLE) {
            player.setPosition(x, y);
        }
        collisions(); // calls the collision method to deal with collisions
        for (int i = 0; i < aliens.length; i++) {
            try {
                if (aliens[i].getX() == j) {
                    if (aliens[i].getY() == k) {
                        player.setPosition(x, y); // moves the player back to the original place after attacking alien
                    }
                }
            } catch (NullPointerException e) { // deals with errors caused by aliens being taken out of play

            }
        }

    }

    /**
     * Handles the movement of the player when attempting to move right in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the right arrow key on the keyboard. The method checks whether
     * the tile to the right of the player is empty for movement and if it is
     * updates the player object's X and Y locations with the new position. If
     * the tile to the right of the player is not empty the method will not
     * update the player position, but could make other changes to the game.
     */
    public void movePlayerRight() {

        int x = player.getX();
        int y = player.getY();

        x %= GRID_WIDTH - 1; // uses modular division to allow the ship to come onto the other side of the grid if it travels off screen

        player.setPosition(x + 1, y);

        int j = player.getX();
        int k = player.getY();

        if (tiles[j][k] == TileType.BLACK_HOLE) {
            player.setPosition(x, y);
        }
        collisions();
        for (int i = 0; i < aliens.length; i++) {
            try {
                if (aliens[i].getX() == j) {
                    if (aliens[i].getY() == k) {
                        player.setPosition(x, y);
                    }
                }
            } catch (NullPointerException e) {

            }
        }

    }

    /**
     * Handles the movement of the player when attempting to move up in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the up arrow key on the keyboard. The method checks whether the
     * tile above the player is empty for movement and if it is updates the
     * player object's X and Y locations with the new position. If the tile
     * above the player is not empty the method will not update the player
     * position, but could make other changes to the game.
     */
    public void movePlayerUp() {
        int x = player.getX();
        int y = player.getY();

        if (y <= 0) {
            y += GRID_HEIGHT;
        }
        player.setPosition(x, y - 1);

        int j = player.getX();
        int k = player.getY();

        if (tiles[j][k] == TileType.BLACK_HOLE) {
            player.setPosition(x, y);
        }
        collisions();
        for (int i = 0; i < aliens.length; i++) {
            try {
                if (aliens[i].getX() == j) {
                    if (aliens[i].getY() == k) {
                        player.setPosition(x, y);
                    }
                }
            } catch (NullPointerException e) {

            }
        }

    }

    /**
     * Handles the movement of the player when attempting to move right in the
     * game. This method is called by the InputHandler class when the user has
     * pressed the down arrow key on the keyboard. The method checks whether the
     * tile below the player is empty for movement and if it is updates the
     * player object's X and Y locations with the new position. If the tile
     * below the player is not empty the method will not update the player
     * position, but could make other changes to the game.
     */
    public void movePlayerDown() {
        int x = player.getX();
        int y = player.getY();

        y %= GRID_HEIGHT - 1;

        player.setPosition(x, y + 1);

        int j = player.getX();
        int k = player.getY();

        if (tiles[j][k] == TileType.BLACK_HOLE) {
            player.setPosition(x, y);
        }
        collisions();
        for (int i = 0; i < aliens.length; i++) {
            try {
                if (aliens[i].getX() == j) {
                    if (aliens[i].getY() == k) {
                        player.setPosition(x, y);
                    }
                }
            } catch (NullPointerException e) {

            }
        }

    }

    /**
     * Updates the position of Asteroid objects by altering their X and Y
     * co-ordinates according to their moveDirection attribute value. This
     * iterates over the asteroids array one element at a time, checks if the
     * current element is null (skipping it if it is null) and finding the
     * moveDirection value for the current asteroid object. Asteroids with a
     * moveDirection value other than NONE should have their position updated
     * accordingly, and if their new position puts them outside the map or
     * inside a black hole they are "destroyed". Destroyed asteroids should be
     * replaced by creating a new, randomly positioned asteroid in the same
     * index of the asteroids array that the destroyed asteroid used to occupy.
     */
    private void moveAsteroids() {
        for (int i = 0; i < asteroids.length; i++) {
            if (asteroids[i] != null) {
                int x = asteroids[i].getX();
                int y = asteroids[i].getY();
                Direction d = asteroids[i].getMovementDirection();

                if (d == Direction.UP) { // handles the direction the asteroids move in based on the conditions given at spawns asteroids
                    if (y <= 0) {
                        y += GRID_HEIGHT;
                    }
                    asteroids[i].setPosition(x, y - 1);

                } else if (d == Direction.RIGHT) {
                    x %= GRID_WIDTH - 1;
                    asteroids[i].setPosition(x + 1, y);

                } else if (d == Direction.DOWN) {
                    y %= GRID_HEIGHT - 1;
                    asteroids[i].setPosition(x, y + 1);

                } else if (d == Direction.LEFT) {
                    if (x <= 0) {
                        x += GRID_WIDTH;
                    }
                    asteroids[i].setPosition(x - 1, y);

                }
                try {
                    if (tiles[x][y] == TileType.BLACK_HOLE) {
                        asteroids[i] = null;
                        asteroids = spawnAsteroids();
                    }
                } catch (ArrayIndexOutOfBoundsException e) { // handles errors from the asteroid array being out of bounds after being taken out of play
                }
            }
        }
    }

    /**
     * Moves all aliens on the current level. The method checks for non-null
     * elements in the aliens array and calls the moveAlien method for each one
     * that is not null.
     */
    private void moveAliens() {

        for (int i = 0; i < aliens.length; i++) {

            if (aliens[i] != null) {
                moveAlien(aliens[i]); // calls the moveAlien(Alien a) method for each individual alien that is not null

            }
        }
    }

    /**
     * Moves a specific alien in the game. The method updates the X and Y
     * attributes of the alien to reflect its new position.
     *
     * @param a The Alien that needs to be moved
     */
    private void moveAlien(Alien a) {

        int movement;
        int x = a.getX();
        int y = a.getY();

        movement = rng.nextInt(4); // generates 4 random numbers for use in the case statement

        switch (movement) { // controls the direction the aliens move based on the random number generated
            case 0:
                if (x < 0) {
                    x += GRID_WIDTH + 1;
                }
                a.setPosition(x + 1, y);
                break;
            case 1:
                x %= GRID_WIDTH - 1;
                a.setPosition(x - 1, y);

                break;
            case 2:
                if (y < 0) {
                    y += GRID_HEIGHT;
                }
                a.setPosition(x, y + 1);

                break;
            case 3:
                y %= GRID_HEIGHT - 1;
                a.setPosition(x, y - 1);
                break;
        }
        for (int i = 0; i < aliens.length; i++) {
            try {
                try {
                    if (aliens[i].getX() == player.getX()) {
                        if (aliens[i].getY() == player.getY()) {
                            a.setPosition(x, y);
                            player.hullStrength = player.hullStrength - 15; // lowers the player health when aliens collide with the player
                        }
                    }
                } catch (NullPointerException e) { // deals with errors caused by aliens being taken out of play
                }

            } catch (ArrayIndexOutOfBoundsException e) { // deals with errors caused by the arrays not being full
            }
            try {
                int j = a.getX();
                int k = a.getY();

                if (tiles[j][k] == TileType.BLACK_HOLE) {
                    a.setPosition(x, y);
                }
            } catch (ArrayIndexOutOfBoundsException e) { // deals with errors caused by the arrays not being full
            } 
        }
    }

    /**
     * Spawns asteroids in suitable locations in the current level. The method
     * uses the spawns ArrayList to pick suitable positions to add asteroids,
     * removing these positions from the spawns ArrayList as they are used
     * (using the remove() method) to avoid multiple entities spawning in the
     * same location. The method creates asteroids by repeatedly instantiating
     * the Asteroid class and setting the X and Y position for the asteroid
     * using the Point object removed from the spawns ArrayList.
     *
     * @return An array of Asteroid objects representing the asteroids for the
     * current level
     */
    private Asteroid[] spawnAsteroids() {

        int spawnAmount = (5 - points); // spawns asteroids based on how many points the player has in each level for when asteroids are destroyed by black holes

        Asteroid[] asteroid = new Asteroid[spawnAmount];

        for (int i = 0; i < asteroid.length; i++) {

            int n = rng.nextInt(spawns.size());

            int x = spawns.get(n).x;
            int y = spawns.get(n).y;
            int asteroidDirection = rng.nextInt(5);

            Asteroid.Direction d = null;

            switch (asteroidDirection) { // assigns a value to the direction the asteroids will travel in 
                case 0:
                    d = Asteroid.Direction.UP;
                    break;
                case 1:
                    d = Asteroid.Direction.DOWN;
                    break;
                case 2:
                    d = Asteroid.Direction.LEFT;
                    break;
                case 3:
                    d = Asteroid.Direction.RIGHT;
                    break;
                case 4:
                    d = Asteroid.Direction.NONE;
                    break;
            }

            asteroid[i] = new Asteroid(x, y, d);

            spawns.remove(n);

        }
        return asteroid;            //modify to return an array of Asteroid objects
    }

    /**
     * Processes the tiles array to find inactive pulsars and change them to
     * active pulsars. When a tile is found of the correct type, that tile is
     * set to PULSAR_ACTIVE. When the map is drawn to the screen next the
     * inactive pulsar will now be an active pulsar.
     */
    private void activatePulsars() {
        double chance;
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                chance = rng.nextDouble(); 
                if (tiles[i][j] == TileType.PULSAR_INACTIVE) {
                    if (chance < 0.5) { // randomises if the pulsars are to be made active
                        tiles[i][j] = TileType.PULSAR_ACTIVE;

                    }
                }
            }
        }
    }

    /**
     * Processes the tiles array to find active pulsars and change them to
     * inactive pulsars. When a tile is found of the correct type, that tile is
     * set to PULSAR_INACTIVE. When the map is drawn to the screen next the
     * active pulsar will now be an inactive pulsar.
     */
    private void deactivatePulsars() {
        double chance;

        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                chance = rng.nextDouble();
                if (tiles[i][j] == TileType.PULSAR_ACTIVE) {
                    if (chance < 0.5) {  // randomises if the pulsars are to be made inactive
                        tiles[i][j] = TileType.PULSAR_INACTIVE;
                    }
                }
            }
        }
    }

    /**
     * Damages the player if the player is in an active pulsar tile, or any of
     * the eight tiles adjacent to the active pulsar, when this method is
     * called. The method uses the player's current x and y position and
     * searches around the player looking for pulsar tiles. Any pulsar tiles
     * found this way result in a call to the changeHullStrength method for the
     * player object to damage the player.
     */
    private void pulsarDamage() {
        int x = player.getX();
        int y = player.getY();

        for (int i = x - 1; i < x + 2; i++) { 
            for (int j = y - 1; j < y + 2; j++) { // scans the adjacent squares surrounding the player for pulsars
                try {
                    if (tiles[i][j].equals(TileType.PULSAR_ACTIVE)) {
                        player.hullStrength = player.hullStrength - 10;
                        if (tiles[x][y].equals(TileType.PULSAR_ACTIVE)) {
                            player.hullStrength = player.hullStrength = 0;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) { // deals with errors when the player is at the edge of the grid
                }
            }
        }
    }

    /**
     * Called in response to the player collecting enough points win the current
     * level. The method increases the valued of cleared by one, resets the
     * value of points to zero, generates a new level by calling the
     * generateLevel method, fills the spawns ArrayList with suitable spawn
     * locations, then spawns aliens and asteroids. Finally it places the player
     * in the new level by calling the placePlayer() method. Note that a new
     * player object should not be created here as this will reset the player's
     * health to maximum.
     */
    private void newLevel() {
        cleared++; 
        points = 0; // makes the current points 0

        tiles = generateLevel();
        spawns = getSpawns();
        asteroids = spawnAsteroids();
        aliens = spawnAliens();
        placePlayer();

        gui.updateDisplay(tiles, player, aliens, asteroids);
    }

    /**
     * Places the player in a level by choosing a spawn location from the spawns
     * ArrayList, removing the spawn position as it is used. The method sets the
     * players position in the level by calling its setPosition method with the
     * x and y values of the Point taken from the spawns ArrayList.
     */
    private void placePlayer() {
        int n = rng.nextInt(spawns.size());
        
            int x = spawns.get(n).x;
            int y = spawns.get(n).y;

            spawns.remove(n);

            player.setPosition(x, y); // places the player in the new level randomly with health from previous level

    }

    /**
     * Performs a single turn of the game when the user presses a key on the
     * keyboard. This method activates or deactivates pulsars periodically by
     * using the turn attribute, moves any aliens and asteroids and then checks
     * if the player is dead, exiting the game or resetting it. It checks if the
     * player has collected enough asteroids to win the level and calls the
     * method if it does. Finally it requests the GUI to redraw the game level
     * by passing it the tiles, player, aliens and asteroids for the current
     * level.
     */
    public void doTurn() {
        if (turnNumber % 20 == 0) {
            activatePulsars();
        }
        if (turnNumber % 20 == 5) {
            deactivatePulsars();
        }
        if (turnNumber % 10 == 5) {
            moveAsteroids();
        }

        moveAliens(); // calls move alien method
        pulsarDamage(); // calls pulsar damage method
        collisions(); // calls collisions method so if asteroids fly into the player they are still collected

        if (player.getHullStrength() < 1) {
            System.exit(0);
        }
        if (points >= 5) {
            newLevel();
        }
        gui.updateDisplay(tiles, player, aliens, asteroids);
        turnNumber++;

    }

    /**
     * Starts a game. This method generates a level, finds spawn positions in
     * the level, spawns aliens, asteroids and the player and then requests the
     * GUI to update the level on screen using the information on tiles, player,
     * asteroids and aliens.
     */
    public void startGame() {
        tiles = generateLevel();
        spawns = getSpawns();
        asteroids = spawnAsteroids();
        aliens = spawnAliens();
        player = spawnPlayer();
        gui.updateDisplay(tiles, player, aliens, asteroids);

    }
}
