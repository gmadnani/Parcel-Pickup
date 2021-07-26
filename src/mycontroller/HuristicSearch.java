package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import java.util.PriorityQueue;

import tiles.MapTile;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.Car;
import world.World;

import java.lang.Math;


public class HuristicSearch {
	
	private static class Node {
		Node parent;
		Coordinate coor;
		int g = 0, h = 0, f = 0;
		
		public Node(Node parent, Coordinate coor) {
			this.parent = parent;
			this.coor = coor;
			g = h = f = 0;
		}
		
		public boolean equals(Object a) {
			//if(this == null) {System.out.print("this = null");}else {System.out.print("end = null");}
			//return this.coor.x == ((Node)a).coor.x && this.coor.y == ((Node)a).coor.y;
			return coor.equals(((Node)a).coor);
		}
		

		
		public static Node inList(Node node, HashSet<Node> list) {
			for(Node n: list) {
				if(node.equals(n)) {
					return n;
				}
					
			}
			return null;
			
		}
		
		public static Node inList(Node node, PriorityQueue<Node> list) {
			for(Node n: list) {
				if(node.equals(n)) {
					return n;
				}
					
			}
			return null;
			
		}
		
		
	    @Override
	    public String toString() { 
	        return String.format("(" + coor.x + ", " + coor.y + ")" + "g: " + g); 
	    }  

		
	}
	
	class NodeComparator implements Comparator<Node> {

		@Override
		public int compare(Node a, Node b) {
			return a.f - b.f;
		}
	}
	
	
	
public ArrayList<Coordinate> aStarSearch(HashMap<Coordinate, MapTile> maze, Coordinate curr, Coordinate dest){
		
		if(maze == null)
			maze = World.getMap();
		if(dest == null || curr == null)
			return null;
		
		
		//create start and end node
		Node start = new Node(null, curr);
		Node end = new Node(null, dest);
		
		//initialize both open and close list
		PriorityQueue<Node> openList = new PriorityQueue<>(maze.size(), new NodeComparator());
		HashSet<Node> closeList = new HashSet<>();
		
		//add start node
		openList.add(start);
		
		//loop until find the end
		
		while(openList.size() > 0) {
			
			//get the current node
			Node currNode = openList.poll();

			
			//pop out the current node and add it to closed list
			closeList.add(currNode);
			
			//find the end
			if(currNode.equals(end)) {
				ArrayList<Coordinate> path = new ArrayList<>();
				Node curNode = currNode;
				while(curNode != null) {
					path.add(curNode.coor);
					curNode = curNode.parent;
					 
				}
				Collections.reverse(path);
				return path;
			}
			
			//generate children
			
			Coordinate incrementCools[] = {new Coordinate(1, 0), new Coordinate(0, 1), new Coordinate(-1, 0), new Coordinate(0, -1)};
			ArrayList<Node>children = new ArrayList<>();
			for(int i = 0; i < incrementCools.length; i++) {
				Coordinate adjacentPosition = new Coordinate(currNode.coor.x + incrementCools[i].x, currNode.coor.y + incrementCools[i].y);
				// make sure within the map
				if(maze.get(adjacentPosition) == null) {
					continue;
				}
				
				//make sure walkable terrain
				if(maze.get(adjacentPosition).isType(Type.WALL)) {
					continue;
				}
				
				children.add(new Node(currNode, adjacentPosition));
				
			}
			

			//loop children
			for(Node childNode: children) {
				if(    /*closeList.contains(childNode)*/   Node.inList(childNode, closeList)!=null) {
					continue;
				}

				//compute g, h, f values
				childNode.g = currNode.g + 1;
				//childNode.h = (int) Math.sqrt(Math.pow(childNode.coor.x - dest.x, 2) + Math.pow(childNode.coor.y - dest.y, 2));
				//childNode.h = (int) (Math.abs(childNode.coor.x - dest.x) + Math.abs(childNode.coor.y - dest.y));
				childNode.h = (Math.abs(childNode.coor.x - dest.x) < Math.abs(childNode.coor.y - dest.y))?Math.abs(childNode.coor.x - dest.x):Math.abs(childNode.coor.y - dest.y);
				childNode.f = childNode.g + childNode.h;
				
				
				
				//Child is already in the open list
				Node prev = Node.inList(childNode, openList);
				if(prev != null) {
					if(childNode.g < prev.g) {
						prev.g = childNode.g; 
					}
					
				}
				else {
					openList.add(childNode);
				}

			}

		}
		
		
		return null;
	}
	

	public ArrayList<Coordinate> optimalInfoGain(HashMap<Coordinate, MapTile> explored, Coordinate curr){
		double maxRatio = 0;
		Coordinate optimalNode = curr;
		Coordinate[] coorsToCheck = explored.keySet().toArray(new Coordinate[explored.size()]);

		ArrayList<Coordinate> coors = new ArrayList<>();
		
		for(Coordinate coor: coorsToCheck) {
			Coordinate left = new Coordinate(coor.x+1, coor.y);
			Coordinate right = new Coordinate(coor.x-1, coor.y);
			Coordinate up = new Coordinate(coor.x, coor.y+1);
			Coordinate down = new Coordinate(coor.x, coor.y-1);

			if( !explored.containsKey(left) ||
				!explored.containsKey(right) ||
				!explored.containsKey(up) ||
				!explored.containsKey(down))
				coors.add(coor);
		}
		
		coorsToCheck = null;

		for(Coordinate coor: coors) {
			double ratio;
			if(explored.get(coor).isType(Type.WALL) || aStarSearch(null, curr, coor) == null) {
				continue;
			}
			
			int len = aStarSearch(null, curr, coor).size();
			
			int countIncrement = 0;
			for(int incrementX = -Car.VIEW_SQUARE; incrementX <= Car.VIEW_SQUARE; incrementX++) {
				for(int incrementY = -Car.VIEW_SQUARE; incrementY <= Car.VIEW_SQUARE; incrementY++) {
					Coordinate adjNode = new Coordinate(coor.x+incrementX, coor.y+incrementY);
					if((adjNode.x >= 0 && adjNode.x < World.MAP_WIDTH && adjNode.y >= 0 && adjNode.y < World.MAP_HEIGHT) && !explored.containsKey(adjNode))
					{	
						countIncrement++;
					}
				}
			}

		ratio = countIncrement/len;
		maxRatio = (ratio > maxRatio)?ratio:maxRatio;

		optimalNode = (maxRatio == ratio)?coor:optimalNode;
			
		}
		
		
		
		return aStarSearch(null, curr, optimalNode);
	}
	

	
}

	
