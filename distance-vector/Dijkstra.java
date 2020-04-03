import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Dijkstra {

	public static void main(String[] args) {
		Graph g = new Graph();
		//create topology(data from project2) 
		//store arrayList, <Object> is <Vertex>
		g.addVertex('1', Arrays.asList(new Vertex('2', 7), new Vertex('3', 4), new Vertex('4', 5)));
		g.addVertex('2', Arrays.asList(new Vertex('1', 7), new Vertex('3', 2)));
		g.addVertex('3', Arrays.asList(new Vertex('1', 4), new Vertex('2', 2), new Vertex('4', 1)));
		g.addVertex('4', Arrays.asList(new Vertex('1', 5), new Vertex('3', 1)));
		//show shortest path 
		System.out.println("Shortest path from 1 to 2: ");
		List<Character> shortestPath = g.getShortestPath('1', '2');
		System.out.print("1 ");
		for (int i = shortestPath.size() - 1; i >= 0; i--){
			System.out.print("-->" + shortestPath.get(i) + " ");
		}
		System.out.println("\n");
	}
	
}

//Vertex 
class Vertex implements Comparable<Vertex> {
	
	private Character v;
	private Integer distance;
	
	public Vertex(Character v, Integer distance) {
		super();
		this.v = v;
		this.distance = distance;
	}

	public Character getV() {
		return v;
	}

	public void setV(Character v) {
		this.v = v;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}


	@Override
	public int compareTo(Vertex v) {
		if (this.distance < v.distance)
			return -1;
		else if (this.distance > v.distance)
			return 1;
		else
			return this.getV().compareTo(v.getV());
	}
	
}

//Graph
class Graph {
	
	private final Map<Character, List<Vertex>> vertices;
	
	public Graph() {
		this.vertices = new HashMap<Character, List<Vertex>>();
	}
	
	public void addVertex(Character character, List<Vertex> vertex) {
		this.vertices.put(character, vertex);
	}
	
	public List<Character> getShortestPath(Character start, Character finish) {
		final Map<Character, Integer> distances = new HashMap<Character, Integer>();
		final Map<Character, Vertex> previous = new HashMap<Character, Vertex>();
		PriorityQueue<Vertex> nodes = new PriorityQueue<Vertex>();
		
		//initial 
		for(Character vertex : vertices.keySet()) {
			if (vertex.equals(start)) {
				distances.put(vertex, 0);
				nodes.add(new Vertex(vertex, 0));
			} else {
				distances.put(vertex, Integer.MAX_VALUE);
				nodes.add(new Vertex(vertex, Integer.MAX_VALUE));
			}
			previous.put(vertex, null);
		}

		
		while (!nodes.isEmpty()) {
			Vertex smallest = nodes.poll();
			if (smallest.getV().equals(finish)) {
				//store character
				final List<Character> path = new ArrayList<Character>();
				while (previous.get(smallest.getV()) != null) {
					path.add(smallest.getV());
					smallest = previous.get(smallest.getV());
				}
				return path;
			}

			if (distances.get(smallest.getV()) == Integer.MAX_VALUE) {
				break;
			}
						
			//vertex list 
			for (Vertex neighbor : vertices.get(smallest.getV())) {
				Integer newCost = distances.get(smallest.getV()) + neighbor.getDistance();
				if (newCost < distances.get(neighbor.getV())) {
					distances.put(neighbor.getV(), newCost);
					previous.put(neighbor.getV(), smallest);
					
					for(Vertex n : nodes) {
						if (n.getV().equals(neighbor.getV())) {
							nodes.remove(n);
							n.setDistance(newCost);
							nodes.add(n);
							break;
						}
					}
				}
			}
		}
		
		return new ArrayList<Character>(distances.keySet());
	}
	
}
