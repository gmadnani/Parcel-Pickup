package mycontroller;

import controller.CarController;
import world.Car;
import world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;


import tiles.MapTile;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.Arrays;

public class MyAutoController extends CarController{		

		private Map <String, Runnable> actionMap; //map command name to function execution 
		private ArrayList<Coordinate> tempPath = null;
		Iterator<String>  cycleLaunches =  Arrays.asList(new String[]{"Forward", "Left", "Left", "Left"}).iterator();
		private boolean onWayToParcel = false;
		// Car Speed to move at
		private final int CAR_MAX_SPEED = 1;
		
		// a constructing map
		protected HashMap<Coordinate, MapTile> constructedMap = new HashMap<Coordinate, MapTile>();
		
		// a searching engine
		HuristicSearch search = new HuristicSearch();
		
		
		
		public MyAutoController(Car car) {
			super(car);
			actionMap = initActionMap();
			initilize();
		}
		
		/**
		 * first action, let the car ready to move
		 */
		public void initilize() {


			//get more info about the visited area
			constructMap();
			tempPath = search.optimalInfoGain(constructedMap, getCarLoc());
			tempPath.add(null);
		}

		@Override
		// basically update function is a state-machine, we have state:
		//1. car not launched
		
		//2. car launched but mission not completed and not on the way to parcel
		
		//3. car launched but mission not completed and on the way to parcel
		
		//4.car launched and mission completed and not on the way to finish
		
		//5.car launched and mission completed and  on the way to finish
		
		//so base on those state we chose different strategy by applying the underlying A* algorithm
		public void update() {

			// Gets what the car can see in every move
			constructMap();
			
			//launch the car in 4 steps!
			if(cycleLaunches.hasNext()) {
				String cmd = cycleLaunches.next();
				actionMap.get(cmd).run();
			}

			// car get launched
			else {
				//when mission not completed (parcel not totally collected or having area unexplored, but
				//the game could terminate if it is lucky that we finished the collection and reach dest, even
				//some area not checked)
				if(numParcelsFound() != numParcels()) {
					//if planed path exhusted
					if(tempPath.get(1) == null) {
						tempPath = search.optimalInfoGain(constructedMap, getCarLoc());
						onWayToParcel = false;
					}
					
					//if on the way of a path, at anytime search parcel
					Coordinate pracelLoc = null;
					if(!onWayToParcel) {
					 pracelLoc = find(Type.TRAP);
					}
					
					// if find parcel, change current path to the parcel-oriented path
					if( search.aStarSearch(null, getCarLoc(), pracelLoc) != null && !onWayToParcel) {
						tempPath = search.aStarSearch(null, getCarLoc(), pracelLoc);
						onWayToParcel = true; //prevent distruption to the parcel-oriented path
					}

				}
				//if mission completed, plan a path to finalise the game
				else {
						Coordinate finalLoc =  find(Type.FINISH);
						//if get path to final, go final, if not, keep exploring
						tempPath = (finalLoc != null)?search.aStarSearch(null, getCarLoc(), finalLoc): search.optimalInfoGain(constructedMap, getCarLoc());
						
				}
				
				// this is to deal with "tempPath.get(1) == null" nullException, so add null manually
				if(tempPath.get(tempPath.size()-1) != null)
					tempPath.add(null); 

				
				excute();
				}
				
			
		}
			
			
			
	


		// map command to action
		public Map <String, Runnable> initActionMap() {
			Map <String, Runnable> actionMap = new HashMap<>();
			actionMap.put("Forward", () -> applyForwardAcceleration());
			actionMap.put("Backward", () -> applyReverseAcceleration());
			actionMap.put("Left", () -> turnLeft());
			actionMap.put("Right", () -> turnRight());
			
			return actionMap;
		}
		
		//control the car properly when current position and next position given
		public void excute() {
			if(tempPath.get(1) == null){
				return;
			}
			
			//deal with forward-backward shift (if next move should make the car turn the speed direction to opposite)
			//we have to stop the car first, so cannot directly reach the next position, we have to add the current
			//position to planed path as our next path for stopping
			if(getSpeed()<CAR_MAX_SPEED) {
				tempPath.add(0, getCarLoc());
			}
			
			//get curr, next position
			Coordinate curr = tempPath.remove(0);
			Coordinate next = tempPath.get(0);
			
			//if curr = next, drop the next
			//(this circumstance is weird and normally should not occur, but for bug prevention due to logic errors)
			if(curr.equals(next)) {
				tempPath.remove(0);
			    next = tempPath.get(0);
			}


			//assert localized location and planed current location consistent
			if(!curr.equals(getCarLoc())) {
				throw new AssertionError();
			}
			
			//excute command
			String cmd = getACommand(curr, next);
			actionMap.get(cmd).run();
		}
		
		
		
		
		
		//get more info about the visited area
		public void constructMap() {
			HashMap<Coordinate, MapTile> currentView = getView();
			Coordinate coors[] = currentView.keySet().toArray(new Coordinate[currentView.size()]);
			for(Coordinate coor: coors) {
				if(coor.x < 0 || coor.x >= World.MAP_WIDTH || coor.y < 0 || coor.y >= World.MAP_HEIGHT)
					currentView.remove(coor);
			}
			
			constructedMap.putAll(currentView);	
		}
		
		
		
		public Coordinate find(Type type) {
			
			Coordinate coor[] = constructedMap.keySet().toArray(new Coordinate[constructedMap.size()]);
			for(int i = 0; i < constructedMap.size(); i++) {
				if(constructedMap.get(coor[i]).isType(type)) {
					constructedMap.put(coor[i], new MapTile(Type.ROAD));
					return coor[i];
				}
			}
		return null;
	}


		public String getACommand(Coordinate curr, Coordinate next) {
			String orientation = getOrientation().name();
			String command = new String();
			if (shouldForward(orientation, curr, next))
				command = "Forward";
			else if (shouldBackward(orientation, curr, next))
				command = "Backward";
			else if (shouldTurnLeft(orientation, curr, next))
				command = "Left";
			else
				command = "Right";


			return command;
			
		}
		
		
		public Coordinate getCarLoc() {
			return new Coordinate(getPosition());
			
		}


		// check current to next movement should make the command as forward
		public boolean shouldForward(String orientation, Coordinate curr, Coordinate next) {
			
			boolean northConsistancy, southConsistancy, eastConsistancy, westConsistancy;
			
			northConsistancy = (curr.x == next.x) && (curr.y - next.y == -1) && (orientation == WorldSpatial.Direction.NORTH.name());
			southConsistancy = (curr.x == next.x) && (curr.y - next.y == 1) && (orientation == WorldSpatial.Direction.SOUTH.name());
			westConsistancy = (curr.y == next.y) && (curr.x - next.x == 1) && (orientation == WorldSpatial.Direction.WEST.name());
			eastConsistancy = (curr.y == next.y) && (curr.x - next.x == -1) && (orientation == WorldSpatial.Direction.EAST.name());	
			
			return northConsistancy || southConsistancy || eastConsistancy || westConsistancy;

			
		}
		// check current to next movement should make the command as backward

		public boolean shouldBackward(String orientation, Coordinate curr, Coordinate next) {
			
			boolean northReverseConsistancy, southReverseConsistancy, eastReverseConsistancy, westReverseConsistancy;
			
			northReverseConsistancy = (curr.x == next.x) && (curr.y - next.y == -1) && (orientation == WorldSpatial.Direction.SOUTH.name());
			southReverseConsistancy = (curr.x == next.x) && (curr.y - next.y == 1) && (orientation == WorldSpatial.Direction.NORTH.name());
			eastReverseConsistancy = (curr.y == next.y) && (curr.x - next.x == 1) && (orientation == WorldSpatial.Direction.EAST.name());
			westReverseConsistancy = (curr.y == next.y) && (curr.x - next.x == -1) && (orientation == WorldSpatial.Direction.WEST.name());
			
			return northReverseConsistancy || southReverseConsistancy || eastReverseConsistancy || westReverseConsistancy;


			
		}
		// check current to next movement should make the command as trunright

		public boolean shouldTurnRight(String orientation, Coordinate curr, Coordinate next) {
			
			boolean towardsNorth, towardsSouth, towardsWest, towardsEast;
			
			towardsNorth = (next.y == curr.y) && (next.x - curr.x == 1) && (orientation == WorldSpatial.Direction.NORTH.name());
			towardsSouth = (next.y == curr.y) && (next.x - curr.x == -1) && (orientation == WorldSpatial.Direction.SOUTH.name());
			towardsWest = (next.y - curr.y == 1) && (next.x == curr.x) && (orientation == WorldSpatial.Direction.WEST.name());
			towardsEast = (next.y - curr.y == -1) && (next.x == curr.x) && (orientation == WorldSpatial.Direction.EAST.name());
			
			return towardsNorth || towardsSouth || towardsWest || towardsEast;
		}
		
		// check current to next movement should make the command as trunleft

		public boolean shouldTurnLeft(String orientation, Coordinate curr, Coordinate next) {
			
			boolean towardsNorth, towardsSouth, towardsWest, towardsEast;
			
			towardsNorth = (next.y == curr.y) && (next.x - curr.x == 1) && (orientation == WorldSpatial.Direction.SOUTH.name());
			towardsSouth = (next.y == curr.y) && (next.x - curr.x == -1) && (orientation == WorldSpatial.Direction.NORTH.name());
			towardsWest = (next.y - curr.y == 1) && (next.x == curr.x) && (orientation == WorldSpatial.Direction.EAST.name());
			towardsEast = (next.y - curr.y == -1) && (next.x == curr.x) && (orientation == WorldSpatial.Direction.WEST.name());
			
			return towardsNorth || towardsSouth || towardsWest || towardsEast;			
		}
		

	}
