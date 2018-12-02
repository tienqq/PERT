// change p4 to your netid
package txq170130;
import java.util.*;

import rbk.Graph;
import rbk.Graph.Vertex;
import rbk.Graph.Edge;
import rbk.Graph.GraphAlgorithm;
import rbk.Graph.Factory;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    LinkedList<Vertex> finishList;
    int topNum;
    int time;
    boolean acyclic;
    int CPL;
    public static class PERTVertex implements Factory {
	// Add fields to represent attributes of vertices here
    	String color;
    	int dis;
    	int fin;
    	Vertex parent;
    	int top;
    	int ec;
    	int lc;
    	int slack;
    int duration;
	public PERTVertex(Vertex u) {
		color = "white";
		dis = 0;
		fin = 0;
		parent = null;
		top = 0;
		ec = 0;
		lc = 0;
		slack = 0;
		duration = 0;
	}
	public PERTVertex make(Vertex u) { return new PERTVertex(u); }
    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g) {
	super(g, new PERTVertex(null));
	topNum = 0;
	time = 0;
	acyclic = false;
	CPL = 0;
    }

    public void setDuration(Vertex u, int d) {
    		//d is the duration of u
    		get(u).duration = d;
    }

    // Implement the PERT algorithm. Returns false if the graph g is not a DAG.
    public boolean pert() {
    			//1. get topological ordering of nodes, helps determine if g is a DAG
    			topologicalOrder();
    			//returns false if graph isn't a DAG
    			if(!g.isDirected() || !acyclic) {
    				return false;
    			}
    			else {
    			//2. for u in g -> u.ec = duration of u
    			for(Vertex u:g) {
    				get(u).ec = get(u).duration;
    			}
    			//3. LI: u.ec = LC(u). Propagate successors of u
    			for(Vertex u:finishList) {
    				for(Edge e:g.incident(u)) {
    					Vertex v = e.otherEnd(u);
    					if(get(v).ec < get(u).ec + get(v).duration) {
    						get(v).ec = get(u).ec + get(v).duration;
    					}
    				}
    			}
    			//4. CPL = max{u.ec} for u in g
    			LinkedList<Integer> list = new LinkedList<Integer>();
    			for(Vertex u:g) {
    				list.add(get(u).ec);
    			}
    			CPL = Collections.max(list);
    			//5. for u in g, u.lc = CPL
    			for(Vertex u:g) {
    				get(u).lc = CPL;
    			}
    			//6. for u in reverse topological order, useing descending iterator of list. LI: all successors v of u, v.lc = LC(v)
    			Iterator<Vertex> it = finishList.descendingIterator();
    			while(it.hasNext()) {
    				Vertex u = it.next();
    				for(Edge e: g.incident(u)) {
    					Vertex v = e.otherEnd(u);
    					if(get(u).lc > get(v).lc - get(v).duration) {
    						get(u).lc = get(v).lc - get(v).duration;
    					}
    					get(u).slack = get(u).lc - get(u).ec;
    				}
    			}
    			return true;
    		}
    }
    // Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder() {
    		//graph isn't directed
    		if(!g.isDirected()) {
    			throw new java.lang.IllegalArgumentException("Graph is not directed");
    		}
    		acyclic = true;
    		topNum = g.size();
    		dfs();
    		if(acyclic) {
    			return finishList;
    		}
    		else {
    			throw new java.lang.IllegalArgumentException("Graph is not acyclic");
    		}
    }
    
    void dfs() {
    		time = 0;
    		finishList = new LinkedList<Vertex>();
    		for(Vertex u: g) {
    			get(u).color = "white";
    			get(u).parent = null;
    		}
    		for(Vertex u:g) {
    			if(get(u).color == "white") {
    				dfsVisit(u);
    			}
    		}
    }
    void dfsVisit(Vertex u) {
    		get(u).color = "gray";
    		get(u).dis = ++time;
    		for(Edge e: g.incident(u)) {
    			Vertex v = e.otherEnd(u);
    			if(get(v).color == "white") {
    				get(v).parent = u;
    				dfsVisit(v);
    			}
    			else if (get(v).color == "gray") {
    				acyclic = false;
    			}
    		}
    		get(u).fin = ++time;
    		get(u).color = "black";
    		get(u).top = topNum;
    		topNum = topNum - 1;
    		finishList.addFirst(u);
    }

    // The following methods are called after calling pert().

    // Earliest time at which task u can be completed
    public int ec(Vertex u) {
	return get(u).ec;
    }

    // Latest completion time of u
    public int lc(Vertex u) {
	return get(u).lc;
    }

    // Slack of u
    public int slack(Vertex u) {
	return get(u).slack;
    }

    // Length of a critical path (time taken to complete project)
    public int criticalPath() {
	return CPL;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u) {
    	//vertex is critcal is it's slack is 0
    	if(get(u).slack==0) {
	return true;
    	}
    	else return false;
    }

    // Number of critical vertices of g
    public int numCritical() {
    	int crit = 0;
    	for(Vertex u:g) {
    		if(critical(u)) {
    			crit++;
    		}
    	}
	return crit;
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration) {
	PERT p = new PERT(g);
	for(Vertex u: g) {
	    p.setDuration(u, duration[u.getIndex()]);
	}
	// Run PERT algorithm.  Returns false if g is not a DAG
	if(p.pert()) {
	    return p;
	} else {
	    return null;
	}
    }
    
    public static void main(String[] args) throws Exception {
    	boolean details = false;
	String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
	Scanner in;
	// If there is a command line argument, use it as file from which
	// input is read, otherwise use input from string.
	in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
	Graph g = Graph.readDirectedGraph(in);
	g.printGraph(false);

	int[] duration = new int[g.size()];
	for(int i=0; i<g.size(); i++) {
	    duration[i] = in.nextInt();
	}
	PERT p = pert(g, duration);
	if(p == null) {
	    System.out.println("Invalid graph: not a DAG");
	} else {
	    System.out.println("Number of critical vertices: " + p.numCritical());
	    System.out.println("u\tEC\tLC\tSlack\tCritical");
	    for(Vertex u: g) {
		System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
	    }
	}
    }
}