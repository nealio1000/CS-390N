package Game;

import Math.Vector3f;

public class Game {

    public Camera camera = new Camera();

    public Tile tile;
    public Coin[] coins = new Coin[25];
    public Cloud[] clouds = new Cloud[30];
    public Player player1;
    public static int coinCounter = 0;

    public Game() {
        init();
    }

    public void init() {

        // Make some coins!
        for (int i = 0; i < 5; i++) {
            coins[i] = new Coin();
            coins[i].translate(new Vector3f(i * 1.7f, 0.0f, 0.0f));
        }
        for (int i = 5; i < 10; i++) {
            coins[i] = new Coin();
            coins[i].translate(new Vector3f(i * 1.7f, 1.0f, 0.0f));
        }
        int k = 1;
        for (int i = 10; i < 15; i++) {
            coins[i] = new Coin();
            coins[i].translate(new Vector3f(1.0f, k * 1.7f, 0.0f));
            k++;
        }
        k = 2;
        int l = 2;
        for (int i = 15; i < 20; i++) {
            coins[i] = new Coin();
            coins[i].translate(new Vector3f(l * 1.0f + 3, k * 1.7f, 0.0f));
            k++;
            l++;
        }
        k = 8;
        l = 8;
        for (int i = 20; i < 25; i++) {
            coins[i] = new Coin();
            coins[i].translate(new Vector3f(l * 1.0f, k * 1.7f, 0.0f));
            k++;
            l--;
        }

        k = 1;
        // Make some fluffy clouds!
        for (int i = 0; i < 5; i++) {
            clouds[i] = new Cloud();
            clouds[i].translate((new Vector3f(k * 0.7f, 4.0f, 0.0f)));
            k++;
        }
        k = 1;

        for (int i = 5; i < 10; i++) {
            clouds[i] = new Cloud();
            clouds[i].translate((new Vector3f(k * 0.7f + 3, 5.0f, 0.0f)));
            k++;
        }
        k = 1;
        for (int i = 10; i < 15; i++) {
            clouds[i] = new Cloud();
            clouds[i].translate((new Vector3f(k * 0.7f - 7, 6.0f, 0.0f)));
            k++;
        }
        k = 1;
        for (int i = 15; i < 20; i++) {
            clouds[i] = new Cloud();
            clouds[i].translate((new Vector3f(k * 0.7f - 4, 3.0f, 0.0f)));
            k++;
        }
        k = 1;
        for (int i = 20; i < 25; i++) {
            clouds[i] = new Cloud();
            clouds[i].translate((new Vector3f(k * 0.7f + 2, 10.0f, 0.0f)));
            k++;
        }
        k = 1;
        for (int i = 25; i < 30; i++) {
            clouds[i] = new Cloud();
            clouds[i].translate((new Vector3f(k * 0.7f + 1, 25.0f, 0.0f)));
            k++;
        }
        tile = new Tile();
        player1 = new Player();
        player1.translate(new Vector3f(-8.0f, -3.6f, 0.0f));
    }

    public boolean collision() {
        // this loops round every coin that is
        // currently on the screen and checks to see
        // if there is a collision between that coin and
        // the player.
        for (int i = 0; i < coins.length; i++) {
            // This checks to see if the coin in the coins
            // array is alive. If it isn't then we shouldn't
            // waste time detecting if it's colliding with
            // anything!
            if (coins[i].alive) {

                // Gets our players "box" coordinates. Using very simple
                // collision detection here.
                float playerX = player1.position.x + Player.radius;
                float playerY = player1.position.y + Player.radius;
                float playerW = player1.position.x + 1.2f;
                float playerH = player1.position.y + 1.2f;

                // Gets our coins box coordinates
                // we'll be comparing these 2 boxes
                // against each other every frame
                // to see if there is a collision or not
                float coinX = coins[i].position.x;
                float coinY = coins[i].position.y;
                float coinW = coins[i].position.x + 2.5f;
                float coinH = coins[i].position.y + 2.5f;

                // These are the checks to see if any parts
                // of our 2 collision boxes are touching each other
                // if they are then we print out that we've collided
                // and we set this coins alive variable to false.
                // We then return true that there has been a collision
                if (playerX < coinW &&
                        playerW > coinX &&
                        playerY < coinH &&
                        playerH > coinY
                        ) {
//                    if (coinCounter != 1)
//                        System.out.println("Player1 has " + coinCounter + " coins");
//                    else
//                        System.out.println("Player1 has 1 coin");
                    coinCounter++;
                    coins[i].alive = false;
                    return true;
                }
            }

        }
        // if there hasn't been a collision then we return false
        // and the game carries on as normal.
        return false;
    }

    public void update() {
        if (collision()) {
            System.out.println("You got a coin!");
        }

        tile.update();
        player1.update();

        camera.setPosition(player1.position);

        for (int i = 0; i < coins.length; i++)
            coins[i].update();
        for (int i = 0; i < clouds.length; i++) {
            clouds[i].update();
            clouds[i].translate(new Vector3f(0.01f, 0.0f, 0.0f));
        }
    }

    public void render() {
        camera.render();
        tile.render();
        player1.render();
        for (int i = 0; i < coins.length; i++) {
            if (coins[i].alive) // only render living coins, of course.
                coins[i].render();
        }
        for (int i = 0; i < clouds.length; i++)
            clouds[i].render();
    }

}
