import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represent edges
class Edge {
  Vertex from;
  Vertex to;
  int weight;

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }
}

// represents player square
class Player {
  int x;
  int y;

  Player(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

// represent vertices
class Vertex {
  String vName;
  int x;
  int y;
  Vertex left;
  Vertex right;
  Vertex top;
  Vertex bottom;
  boolean visited;

  Vertex(String vName, int x, int y) {
    this.vName = vName;
    this.x = x;
    this.y = y;
  }

  // is this vertex the same as the given?
  boolean sameVertex(Vertex v) {
    return this.vName.equals(v.vName) && x == v.x && y == v.y;
  }

  // returns rectangle of a cell
  WorldImage drawVertex() {
    return new RectangleImage(Maze.MAGNIFICATION, Maze.MAGNIFICATION, OutlineMode.SOLID,
        Color.GRAY);
  }
}

// represent the maze, extends world
class Maze extends World {
  HashMap<Vertex, Vertex> representatives;
  ArrayList<ArrayList<Vertex>> vertices;
  ArrayList<Edge> edgesInTree;
  ArrayList<Edge> worklist = new ArrayList<Edge>();
  ArrayList<Vertex> solution = new ArrayList<Vertex>();
  ArrayList<Vertex> path = new ArrayList<Vertex>();
  int width;
  int height;
  static final int MAGNIFICATION = 10;
  Player p = new Player(0, 0);

  Maze(int width, int height) {
    this.width = width;
    this.height = height;
  }

  // creates vertices based on width and height given for maze
  void createVertices() {
    vertices = new ArrayList<ArrayList<Vertex>>();
    int name = 0;
    for (int row = 0; row <= height; row++) {
      ArrayList<Vertex> rowList = new ArrayList<Vertex>();
      for (int column = 0; column <= width; column++) {
        Vertex c = new Vertex("" + name, column, row);
        rowList.add(c);
        name++;
      }
      vertices.add(rowList);
    }
  }

  // returns the edge list sorted
  ArrayList<Edge> sortedEdge(Edge e, ArrayList<Edge> currWorklist) {
    int weight = e.weight;
    ArrayList<Edge> ordered = new ArrayList<Edge>();
    for (int x = 0; x < currWorklist.size(); x++) {
      Edge curr = currWorklist.get(x);
      if (curr.weight < weight) {
        ordered.add(curr);
        currWorklist.remove(curr);
      }
      else {
        x = currWorklist.size();
      }
    }
    ordered.add(e);
    ordered.addAll(currWorklist);
    return ordered;
  }

  // creates edges linking all vertices, puts in edgesInTree
  void createEdge() {
    Random r = new Random();
    for (int row = 0; row <= height; row++) {
      ArrayList<Vertex> temp = vertices.get(row);
      for (int column = 0; column <= width; column++) {
        Vertex from = temp.get(column);
        if (column == width - 1 && row == row - 1) {
          // do nothing, bottom right vertex
        }
        else if (column == width - 1) {
          Edge bottom = new Edge(from, from.bottom, r.nextInt(height * width));
          worklist = sortedEdge(bottom, worklist);
        }
        else if (row == height - 1) {
          Edge right = new Edge(from, from.right, r.nextInt(height * width));
          worklist = sortedEdge(right, worklist);
        }
        else {
          Edge right = new Edge(from, from.right, r.nextInt(height * width));
          Edge bottom = new Edge(from, from.bottom, r.nextInt(height * width));
          worklist = sortedEdge(right, worklist);
          worklist = sortedEdge(bottom, worklist);
        }
      }
    }
  }

  // links vertices to their neighbors
  void linkVertices() {
    for (int row = 0; row <= height; row++) {
      for (int column = 0; column <= width; column++) {
        Vertex v = vertices.get(row).get(column);
        if (row == 0 && column == 0) {
          v.top = v;
          v.left = v;
          v.right = vertices.get(row).get(column + 1);
          v.bottom = vertices.get(row + 1).get(column);
        }
        else if (row == 0 && column == width) {
          v.left = vertices.get(row).get(column - 1);
          v.top = v;
          v.right = v;
          v.bottom = vertices.get(row + 1).get(column);
        }
        else if (row == 0) {
          v.left = vertices.get(row).get(column - 1);
          v.top = v;
          v.right = vertices.get(row).get(column + 1);
          v.bottom = vertices.get(row + 1).get(column);
        }
        else if (row == height && column == 0) {
          v.left = v;
          v.top = vertices.get(row - 1).get(column);
          v.right = vertices.get(row).get(column + 1);
          v.bottom = v;
        }
        else if (row == height && column == width) {
          v.left = vertices.get(row).get(column - 1);
          v.top = vertices.get(row - 1).get(column);
          v.right = v;
          v.bottom = v;
        }
        else if (row == height) {
          v.left = vertices.get(row).get(column - 1);
          v.top = vertices.get(row - 1).get(column);
          v.right = vertices.get(row).get(column + 1);
          v.bottom = v;
        }
        else if (column == 0) {
          v.left = v;
          v.top = vertices.get(row - 1).get(column);
          v.right = vertices.get(row).get(column + 1);
          v.bottom = vertices.get(row + 1).get(column);
        }
        else if (column == width) {
          v.left = vertices.get(row).get(column - 1);
          v.top = vertices.get(row - 1).get(column);
          v.right = v;
          v.bottom = vertices.get(row + 1).get(column);
        }
        else {
          v.left = vertices.get(row).get(column - 1);
          v.top = vertices.get(row - 1).get(column);
          v.right = vertices.get(row).get(column + 1);
          v.bottom = vertices.get(row + 1).get(column);
        }
      }
    }
  }

  // puts vertices in a hashmap, putting themselves as their index
  void repVertices() {
    representatives = new HashMap<Vertex, Vertex>();
    for (int row = 0; row <= height; row++) {
      for (int column = 0; column <= width; column++) {
        Vertex v = vertices.get(row).get(column);
        representatives.put(v, v);
      }
    }
  }

  // runs Kruskal's algorithm with the list of edges and hashmap of vertices
  void constructTrees() {
    this.edgesInTree = new ArrayList<Edge>();
    while (worklist.size() != 0) {
      Edge cheapest = worklist.get(0);
      if (find(representatives, cheapest.from).sameVertex(find(representatives, cheapest.to))) {
        worklist.remove(cheapest);
      }
      else {
        edgesInTree.add(cheapest);
        worklist.remove(cheapest);
        this.union(representatives, find(representatives, cheapest.from),
            find(representatives, cheapest.to));
      }
    }
  }

  // return the value of the given vertex key
  Vertex find(HashMap<Vertex, Vertex> representatives, Vertex v) {
    if (representatives.get(v).sameVertex(v)) {
      return representatives.get(v);
    }
    else {
      return find(representatives, representatives.get(v));
    }
  }

  // changes value of key in hashmap
  void union(HashMap<Vertex, Vertex> representatives, Vertex from, Vertex to) {
    representatives.put(to, from);
  }

  // render scene
  public WorldScene makeScene() {
    WorldScene maze = new WorldScene(this.width * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2,
        this.height * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2);
    this.path = this.bfs();
    for (ArrayList<Vertex> a : this.vertices) {
      for (Vertex v : a) {
        if (v.x == 0 && v.y == 0) {
          maze.placeImageXY(
              new RectangleImage(Maze.MAGNIFICATION, Maze.MAGNIFICATION, OutlineMode.SOLID,
                  Color.green),
              v.x * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2,
              v.y * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2);
        }
        else if (v.visited && path.contains(v)) {
          maze.placeImageXY(
              new RectangleImage(Maze.MAGNIFICATION, Maze.MAGNIFICATION, OutlineMode.SOLID,
                  Color.blue),
              v.x * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2,
              v.y * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2);
        }
        else if (v.visited) {
          maze.placeImageXY(
              new RectangleImage(Maze.MAGNIFICATION, Maze.MAGNIFICATION, OutlineMode.SOLID,
                  Color.orange),
              v.x * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2,
              v.y * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2);
        }
        else if (v.x == width - 1 && v.y == height - 1) {
          maze.placeImageXY(
              new RectangleImage(Maze.MAGNIFICATION, Maze.MAGNIFICATION, OutlineMode.SOLID,
                  Color.MAGENTA),
              v.x * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2,
              v.y * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2);
        }
        else {
          maze.placeImageXY(v.drawVertex(), v.x * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2,
              v.y * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2);
        }
      }
    }
    if (solution.size() != 0) {
      for (Vertex v : solution) {
        maze.placeImageXY(
            new RectangleImage(Maze.MAGNIFICATION, Maze.MAGNIFICATION, OutlineMode.SOLID,
                Color.magenta),
            v.x * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2,
            v.y * Maze.MAGNIFICATION + Maze.MAGNIFICATION / 2);
      }
    }
    sceneHelp(maze);
    maze.placeImageXY(
        new RectangleImage(Maze.MAGNIFICATION, Maze.MAGNIFICATION, OutlineMode.SOLID, Color.cyan),
        p.x * Maze.MAGNIFICATION + (Maze.MAGNIFICATION / 2),
        p.y * Maze.MAGNIFICATION + (Maze.MAGNIFICATION / 2));
    return maze;
  }

  // draws edges of cells
  void sceneHelp(WorldScene maze) {
    for (ArrayList<Vertex> a : this.vertices) {
      for (Vertex v : a) {
        boolean leftFromPath = false;
        boolean leftToPath = false;
        boolean rightFromPath = false;
        boolean rightToPath = false;
        boolean topFromPath = false;
        boolean topToPath = false;
        boolean bottomFromPath = false;
        boolean bottomToPath = false;

        for (Edge e : this.edgesInTree) {
          if (e.from.sameVertex(v.left) && e.to.sameVertex(v)) {
            leftFromPath = true;
          }
          if (e.from.sameVertex(v) && e.to.sameVertex(v.left)) {
            leftToPath = true;
          }
          if (e.from.sameVertex(v.right) && e.to.sameVertex(v)) {
            rightFromPath = true;
          }
          if (e.from.sameVertex(v) && e.to.sameVertex(v.right)) {
            rightToPath = true;
          }
          if (e.from.sameVertex(v.top) && e.to.sameVertex(v)) {
            topFromPath = true;
          }
          if (e.from.sameVertex(v) && e.to.sameVertex(v.top)) {
            topToPath = true;
          }
          if (e.from.sameVertex(v.bottom) && e.to.sameVertex(v)) {
            bottomFromPath = true;
          }
          if (e.from.sameVertex(v) && e.to.sameVertex(v.bottom)) {
            bottomToPath = true;
          }
        }

        if (!leftFromPath && !leftToPath) {
          maze.placeImageXY(
              new RectangleImage(2, Maze.MAGNIFICATION, OutlineMode.SOLID, Color.LIGHT_GRAY),
              (v.x * Maze.MAGNIFICATION), (v.y * Maze.MAGNIFICATION) + Maze.MAGNIFICATION / 2);
        }
        if (!rightFromPath && !rightToPath) {
          maze.placeImageXY(
              new RectangleImage(2, Maze.MAGNIFICATION, OutlineMode.SOLID, Color.LIGHT_GRAY),
              (v.x * Maze.MAGNIFICATION) + Maze.MAGNIFICATION,
              (v.y * Maze.MAGNIFICATION) + Maze.MAGNIFICATION / 2);
        }
        if (!topFromPath && !topToPath) {
          maze.placeImageXY(
              new RectangleImage(Maze.MAGNIFICATION, 2, OutlineMode.SOLID, Color.LIGHT_GRAY),
              (v.x * Maze.MAGNIFICATION) + Maze.MAGNIFICATION / 2, (v.y * Maze.MAGNIFICATION));
        }
        if (!bottomFromPath && !bottomToPath) {
          maze.placeImageXY(
              new RectangleImage(Maze.MAGNIFICATION, 2, OutlineMode.SOLID, Color.LIGHT_GRAY),
              (v.x * Maze.MAGNIFICATION) + Maze.MAGNIFICATION / 2,
              (v.y * Maze.MAGNIFICATION) + Maze.MAGNIFICATION);
        }
      }
    }
  }

  // moves players, restarts game
  public void onKeyEvent(String key) {
    if (key.equals("left")) {
      boolean moveable = false;

      for (Edge edge : edgesInTree) {
        if (p.x == edge.from.x && edge.from.y == p.y && p.x - 1 == edge.to.x && p.y == edge.to.y) {
          moveable = true;
          edge.to.visited = true;
        }
        else if (p.x == edge.to.x && edge.to.y == p.y && p.x - 1 == edge.from.x
            && p.y == edge.from.y) {
          moveable = true;
          edge.from.visited = true;
        }
      }
      if (moveable) {
        p.x = p.x - 1;

      }
    }
    else if (key.equals("right")) {
      boolean moveable = false;
      for (Edge edge : edgesInTree) {
        if (p.x == edge.from.x && p.y == edge.from.y && p.x + 1 == edge.to.x && p.y == edge.to.y) {
          moveable = true;
          edge.to.visited = true;
        }
        else if (p.x == edge.to.x && p.y == edge.to.y && p.x + 1 == edge.from.x
            && p.y == edge.from.y) {
          moveable = true;
          edge.from.visited = true;
        }
      }
      if (moveable) {
        p.x = p.x + 1;
      }
    }
    else if (key.equals("up")) {
      boolean wu = false;
      for (Edge edge : edgesInTree) {
        if (p.y == edge.from.y && p.x == edge.from.x && p.y - 1 == edge.to.y && p.x == edge.to.x) {
          wu = true;
          edge.to.visited = true;
        }
        else if (p.y == edge.to.y && p.x == edge.to.x && p.y - 1 == edge.from.y
            && p.x == edge.from.x) {
          wu = true;
          edge.from.visited = true;
        }
      }
      if (wu) {
        p.y = p.y - 1;
      }
    }
    else if (key.equals("down")) {
      boolean wd = false;
      for (Edge edge : edgesInTree) {
        if (p.y == edge.from.y && p.x == edge.from.x && p.y + 1 == edge.to.y && p.x == edge.to.x) {
          wd = true;
          edge.to.visited = true;
        }
        else if (p.y == edge.to.y && p.x == edge.to.x && p.y + 1 == edge.from.y
            && p.x == edge.from.x) {
          wd = true;
          edge.from.visited = true;
        }
      }
      if (wd) {
        p.y = p.y + 1;
      }
    }
    else if (key.equals("r")) {
      this.createVertices();
      this.linkVertices();
      this.repVertices();
      this.createEdge();
      this.constructTrees();
      this.solution = new ArrayList<Vertex>();
      this.p = new Player(0, 0);
    }
    else if (key.equals("b")) {
      this.solution = this.bfs();
    }
    else if (key.equals("d")) {
      this.solution = this.dfs();
    }

  }

  // how to end world
  public WorldEnd worldEnds() {
    if (p.x == width - 1 && p.y == height - 1) {
      return new WorldEnd(true, this.makeAFinalSceneWon());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // the final victory scene
  WorldScene makeAFinalSceneWon() {
    int incorrect = 0;
    this.bfs();
    for (ArrayList<Vertex> a : this.vertices) {
      for (Vertex v : a) {
        if (v.visited && !solution.contains(v)) {
          incorrect++;
        }
      }
    }
    WorldScene ending = new WorldScene(width * Maze.MAGNIFICATION, height * Maze.MAGNIFICATION);
    ending.placeImageXY(new TextImage("You Won!! ", 25, FontStyle.BOLD, Color.red),
        width / 2 * Maze.MAGNIFICATION, height / 2 * Maze.MAGNIFICATION);
    ending.placeImageXY(
        new TextImage("You Made " + incorrect + " Mistakes", 25, FontStyle.BOLD, Color.red),
        width / 2 * Maze.MAGNIFICATION, Maze.MAGNIFICATION);
    return ending;
  }

  // runs breadth first search and renders path to finish
  ArrayList<Vertex> bfs() {
    HashMap<Vertex, Edge> cameFromEdge = new HashMap<Vertex, Edge>();
    Queue worklist = new Queue();
    worklist.add(vertices.get(0).get(0));
    ArrayList<Edge> edgePaths = edgesInTree;
    ArrayList<Vertex> processed = new ArrayList<Vertex>();
    Vertex next;
    while (!worklist.isEmpty()) {
      next = worklist.remove();
      if (processed.contains(next)) {
        // do nothing
      }
      else if (next.x == width - 1 && next.y == height - 1) {
        return reconstruct(cameFromEdge, next);
      }
      else {
        for (Edge n : edgePaths) {
          if (n.from.sameVertex(next)) {
            worklist.add(n.to);
            if (!cameFromEdge.containsKey(n.to)) {
              cameFromEdge.put(n.to, n);
            }
          }
          else if (n.to.sameVertex(next)) {
            worklist.add(n.from);
            if (!cameFromEdge.containsKey(n.from)) {
              cameFromEdge.put(n.from, n);
            }
          }
        }
        processed.add(next);
      }
    }
    return new ArrayList<Vertex>();
  }

  // runs depth first search and renders path to finish
  ArrayList<Vertex> dfs() {
    HashMap<Vertex, Edge> cameFromEdge = new HashMap<Vertex, Edge>();
    Stack worklist = new Stack();
    worklist.add(vertices.get(0).get(0));
    ArrayList<Edge> edgePaths = edgesInTree;
    ArrayList<Vertex> processed = new ArrayList<Vertex>();
    Vertex next;
    while (!worklist.isEmpty()) {
      next = worklist.remove();
      if (processed.contains(next)) {
        // do nothing
      }
      else if (next.x == width - 1 && next.y == height - 1) {
        return reconstruct(cameFromEdge, next);
      }
      else {
        for (Edge n : edgePaths) {
          if (n.from.sameVertex(next)) {
            worklist.add(n.to);
            if (!cameFromEdge.containsKey(n.to)) {
              cameFromEdge.put(n.to, n);
            }
          }
          else if (n.to.sameVertex(next)) {
            worklist.add(n.from);
            if (!cameFromEdge.containsKey(n.from)) {
              cameFromEdge.put(n.from, n);
            }
          }
        }
        processed.add(next);
      }
    }
    return new ArrayList<Vertex>();
  }

  // reconstructs the correct path backwards from the end of the maze
  ArrayList<Vertex> reconstruct(HashMap<Vertex, Edge> cameFromEdge, Vertex next) {
    ArrayList<Vertex> reconstructed = new ArrayList<Vertex>();
    reconstructed = this.reconstructHelp(cameFromEdge, next, reconstructed);
    return reconstructed;
  }

  // helps the recontruct method
  ArrayList<Vertex> reconstructHelp(HashMap<Vertex, Edge> cameFromEdge, Vertex next,
      ArrayList<Vertex> acc) {
    if (next.x == 0 && next.y == 0) {
      acc.add(next);
      return acc;
    }
    else {
      acc.add(next);
      Edge e = cameFromEdge.get(next);
      if (next.sameVertex(e.to)) {
        return this.reconstructHelp(cameFromEdge, e.from, acc);
      }
      else {
        return this.reconstructHelp(cameFromEdge, e.to, acc);
      }
    }
  }
}

// represents a Stack style ArrayList
class Stack {
  ArrayList<Vertex> contents;

  Stack() {
    this.contents = new ArrayList<Vertex>();
  }

  boolean isEmpty() {
    return this.contents.isEmpty();
  }

  Vertex remove() {
    return this.contents.remove(contents.size() - 1);
  }

  void add(Vertex item) {
    this.contents.add(0, item);
  }
}

// represents a Queue style ArrayList
class Queue {
  ArrayList<Vertex> contents;

  Queue() {
    this.contents = new ArrayList<Vertex>();
  }

  boolean isEmpty() {
    return this.contents.isEmpty();
  }

  Vertex remove() {
    return this.contents.remove(0);
  }

  void add(Vertex item) {
    this.contents.add(item); // NOTE: Different from Stack!
  }
}

// for examples and tests
class MazeExamples {
  Maze m1;

  // initialize maze
  void initMaze() {
    m1 = new Maze(30, 20);
    m1.createVertices();
    m1.linkVertices();
    m1.repVertices();
    m1.createEdge();
    m1.constructTrees();
  }

  // runs world
  void testBigBang(Tester t) {
    this.initMaze();
    m1.bigBang(m1.width * Maze.MAGNIFICATION, m1.height * Maze.MAGNIFICATION, .1);
  }

  boolean testcreateVertices(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    Vertex v1 = new Vertex("1", 0, 0);
    Vertex v2 = new Vertex("2", 0, 1);
    Vertex v3 = new Vertex("3", 0, 2);
    Vertex v4 = new Vertex("4", 1, 0);
    Vertex v5 = new Vertex("5", 1, 1);
    Vertex v6 = new Vertex("6", 1, 2);
    Vertex v7 = new Vertex("7", 2, 0);
    Vertex v8 = new Vertex("8", 2, 1);
    Vertex v9 = new Vertex("9", 2, 2);

    ArrayList<ArrayList<Vertex>> expected = new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(new ArrayList<Vertex>(Arrays.asList(v1, v2, v3)),
            new ArrayList<Vertex>(Arrays.asList(v4, v5, v6)),
            new ArrayList<Vertex>(Arrays.asList(v7, v8, v9))));

    return m1.vertices.equals(expected);
  }

  boolean testSameVertex(Tester t) {
    Vertex v1 = new Vertex("1", 0, 0);
    Vertex v2 = new Vertex("2", 0, 1);
    return t.checkExpect(v1.sameVertex(v1), true) && t.checkExpect(v2.sameVertex(v1), false);
  }

  boolean testCreateEdge(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.linkVertices();
    m1.repVertices();
    m1.createEdge();
    boolean noSelfLink = true;
    for (Edge e : m1.worklist) {
      if (e.from.sameVertex(e.to)) {
        noSelfLink = false;
      }
    }
    return noSelfLink;
  }

  boolean testSortedEdge(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.linkVertices();
    m1.repVertices();
    // sortedEdge is called within createEdge, resulting in sorted worklist
    m1.createEdge();
    return t.checkExpect(m1.worklist.get(0).weight <= m1.worklist.get(1).weight, true)
        && t.checkExpect(m1.worklist.get(0).weight > m1.worklist.get(1).weight, false);
  }

  boolean testlinkVertices(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.linkVertices();

    Vertex v1 = m1.vertices.get(0).get(0);
    Vertex v2 = m1.vertices.get(0).get(1);
    Vertex v3 = m1.vertices.get(0).get(2);
    Vertex v4 = m1.vertices.get(1).get(0);
    Vertex v5 = m1.vertices.get(1).get(1);
    Vertex v6 = m1.vertices.get(1).get(2);
    Vertex v7 = m1.vertices.get(2).get(0);
    Vertex v8 = m1.vertices.get(2).get(1);
    Vertex v9 = m1.vertices.get(2).get(2);

    boolean check1 = v1.left.equals(v1) && v1.right.equals(v2) && v1.top.equals(v1)
        && v1.bottom.equals(v4);
    boolean check2 = v2.left.equals(v1) && v2.right.equals(v3) && v2.top.equals(v2)
        && v2.bottom.equals(v5);
    boolean check3 = v3.left.equals(v2) && v3.right.equals(v3) && v3.top.equals(v3)
        && v3.bottom.equals(v6);
    boolean check4 = v4.left.equals(v4) && v4.right.equals(v5) && v4.top.equals(v1)
        && v4.bottom.equals(v7);
    boolean check5 = v5.left.equals(v4) && v5.right.equals(v6) && v5.top.equals(v2)
        && v5.bottom.equals(v8);
    boolean check6 = v6.left.equals(v5) && v6.right.equals(v6) && v6.top.equals(v3)
        && v6.bottom.equals(v9);
    boolean check7 = v7.left.equals(v7) && v7.right.equals(v8) && v7.top.equals(v4)
        && v7.bottom.equals(v7);
    boolean check8 = v8.left.equals(v7) && v8.right.equals(v9) && v8.top.equals(v5)
        && v8.bottom.equals(v8);
    boolean check9 = v9.left.equals(v8) && v9.right.equals(v9) && v9.top.equals(v6)
        && v9.bottom.equals(v9);

    return check1 && check2 && check3 && check4 && check5 && check6 && check7 && check8 && check9;
  }

  boolean testrepVertices(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.repVertices();
    Vertex v1 = m1.vertices.get(0).get(0);
    Vertex v2 = m1.vertices.get(0).get(1);
    Vertex v3 = m1.vertices.get(0).get(2);
    Vertex v4 = m1.vertices.get(1).get(0);
    Vertex v5 = m1.vertices.get(1).get(1);
    Vertex v6 = m1.vertices.get(1).get(2);
    Vertex v7 = m1.vertices.get(2).get(0);
    Vertex v8 = m1.vertices.get(2).get(1);
    Vertex v9 = m1.vertices.get(2).get(2);

    return t.checkExpect(m1.representatives.get(v1), v1)
        && t.checkExpect(m1.representatives.get(v2), v2)
        && t.checkExpect(m1.representatives.get(v3), v3)
        && t.checkExpect(m1.representatives.get(v4), v4)
        && t.checkExpect(m1.representatives.get(v5), v5)
        && t.checkExpect(m1.representatives.get(v6), v6)
        && t.checkExpect(m1.representatives.get(v7), v7)
        && t.checkExpect(m1.representatives.get(v8), v8)
        && t.checkExpect(m1.representatives.get(v9), v9);
  }

  boolean testconstructTrees(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.repVertices();
    Vertex v1 = m1.vertices.get(0).get(0);
    Vertex v2 = m1.vertices.get(0).get(1);
    Vertex v3 = m1.vertices.get(0).get(2);
    Vertex v6 = m1.vertices.get(1).get(2);
    Vertex v9 = m1.vertices.get(2).get(2);
    m1.worklist = new ArrayList<Edge>(Arrays.asList(new Edge(v1, v2, 5), new Edge(v2, v3, 6),
        new Edge(v3, v6, 7), new Edge(v6, v9, 8)));
    m1.constructTrees();
    return t.checkExpect(m1.edgesInTree, m1.worklist);
  }

  boolean testfind(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.repVertices();
    Vertex v1 = m1.vertices.get(0).get(0);
    Vertex v2 = m1.vertices.get(0).get(1);
    Vertex v3 = m1.vertices.get(0).get(2);
    Vertex v4 = m1.vertices.get(1).get(0);
    Vertex v5 = m1.vertices.get(1).get(1);
    Vertex v6 = m1.vertices.get(1).get(2);
    Vertex v7 = m1.vertices.get(2).get(0);
    Vertex v8 = m1.vertices.get(2).get(1);
    Vertex v9 = m1.vertices.get(2).get(2);

    return t.checkExpect(m1.find(m1.representatives, v1), v1)
        && t.checkExpect(m1.find(m1.representatives, v2), v2)
        && t.checkExpect(m1.find(m1.representatives, v3), v3)
        && t.checkExpect(m1.find(m1.representatives, v4), v4)
        && t.checkExpect(m1.find(m1.representatives, v5), v5)
        && t.checkExpect(m1.find(m1.representatives, v6), v6)
        && t.checkExpect(m1.find(m1.representatives, v7), v7)
        && t.checkExpect(m1.find(m1.representatives, v8), v8)
        && t.checkExpect(m1.find(m1.representatives, v9), v9);
  }

  boolean testUnion(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.repVertices();
    Vertex v1 = m1.vertices.get(0).get(0);
    Vertex v2 = m1.vertices.get(0).get(1);
    Vertex v3 = m1.vertices.get(0).get(2);
    Vertex v4 = m1.vertices.get(1).get(0);
    Vertex v5 = m1.vertices.get(1).get(1);
    Vertex v6 = m1.vertices.get(1).get(2);
    Vertex v7 = m1.vertices.get(2).get(0);
    Vertex v8 = m1.vertices.get(2).get(1);
    Vertex v9 = m1.vertices.get(2).get(2);
    m1.union(m1.representatives, v2, v1);
    m1.union(m1.representatives, v3, v2);
    m1.union(m1.representatives, v4, v3);
    m1.union(m1.representatives, v5, v4);
    m1.union(m1.representatives, v6, v5);
    m1.union(m1.representatives, v7, v6);
    m1.union(m1.representatives, v8, v7);
    m1.union(m1.representatives, v9, v8);

    return t.checkExpect(m1.representatives.get(v1), v2)
        && t.checkExpect(m1.representatives.get(v2), v3)
        && t.checkExpect(m1.representatives.get(v3), v4)
        && t.checkExpect(m1.representatives.get(v4), v5)
        && t.checkExpect(m1.representatives.get(v5), v6)
        && t.checkExpect(m1.representatives.get(v6), v7)
        && t.checkExpect(m1.representatives.get(v7), v8)
        && t.checkExpect(m1.representatives.get(v8), v9);
  }

  boolean testRestart(Tester t) {
    initMaze();
    m1.onKeyEvent("r");
    return t.checkExpect(m1.solution, new ArrayList<Vertex>())
        && t.checkExpect(m1.p, new Player(0, 0));
  }

  boolean testbfs(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.linkVertices();
    m1.repVertices();
    m1.createEdge();
    m1.constructTrees();
    m1.solution = m1.bfs();
    Vertex beg = m1.vertices.get(0).get(0);
    Vertex end = m1.vertices.get(m1.width - 1).get(m1.height - 1);
    return t.checkExpect(m1.solution.contains(beg) && m1.solution.contains(end), true);
  }

  boolean testdfs(Tester t) {
    m1 = new Maze(3, 3);
    m1.createVertices();
    m1.linkVertices();
    m1.repVertices();
    m1.createEdge();
    m1.constructTrees();
    m1.solution = m1.dfs();
    Vertex beg = m1.vertices.get(0).get(0);
    Vertex end = m1.vertices.get(m1.width - 1).get(m1.height - 1);
    return t.checkExpect(m1.solution.contains(beg) && m1.solution.contains(end), true);
  }
}