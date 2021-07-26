package swen30006.driving.desktop;

import java.util.HashMap;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import swen30006.driving.Simulation;
import tiles.MapTile;
import utilities.Coordinate;
import world.World;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();		
		config.backgroundFPS = 0;
		config.foregroundFPS = 0;

		new LwjglApplication(new Simulation(arg), config);
	}
}
