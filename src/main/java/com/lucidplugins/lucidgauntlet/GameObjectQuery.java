package com.lucidplugins.lucidgauntlet;

import net.runelite.api.*;

import java.util.*;
import java.util.stream.Collectors;

public class GameObjectQuery {
    private Collection<GameObject> gameObjectCollection;
    public Collection<GameObject> getGameObjectQuery(Client client)
    {
        List<Tile> tilesList = new ArrayList<>();
        Scene scene = client.getTopLevelWorldView().getScene();
        Tile[][][] tiles = scene.getTiles();
        int z = client.getTopLevelWorldView().getPlane();
        for (int x = 0; x < Constants.SCENE_SIZE; x++)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; y++)
            {
                Tile tile = tiles[z][x][y];
                if (tile == null)
                {
                    continue;
                }
                tilesList.add(tile);
            }
        }

        Collection<GameObject> gameObjs = new ArrayList<>();
        for (Tile tile : tilesList)
        {
            GameObject[] gameObjects = tile.getGameObjects();
            if (gameObjects != null)
            {
                gameObjs.addAll(Arrays.asList(gameObjects));
            }
        }

        gameObjectCollection = gameObjs.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

        return gameObjectCollection;
    }
}