ArrayList<PVector> dijkstra(Snake currentSnake, int destinyX, int destinyY) {
  /* Node is each square on the map, its value is its Manhattan distance to the
      snake's head. */
  Node[][] nodes = new Node[verSqrs][horSqrs];
  // PriorityQueue<Node> openList = new PriorityQueue<Node>();
  PriorityQueue<Node> openList = new PriorityQueue<>((a,b)->Float.compare(a.h+a.g, b.h+b.g));
  HashSet<Node> closedList = new HashSet<>();
  // First node, snake's head
  int[] firstNode = {int(currentSnake.pos[0].x/scl), int(currentSnake.pos[0].y/scl)};
  // Initialize all nodes with an infinite value, except the first one, which is 0
  for (int i = 0; i < horSqrs; ++i) {
      for (int ii = 0; ii < verSqrs; ++ii) {
          nodes[ii][i] = new Node(i, ii, currentSnake.isInBody(i, ii));
      }
  }
  
  Node start = nodes[firstNode[0]][firstNode[1]];
  Node target = nodes[destinyX][destinyY];

  // Start adding nodes to the queue that evaluates them one by one to assign values
  openList.add(start);

  /* In this loop, the values of all nodes are filled, that is, the distance
      Manhattan to all squares on the map it can reach is checked one by one.  */
  while(!openList.isEmpty()) {
      Node curr = openList.remove();
      closedList.add(curr);
      if(curr==target){
          //target found
          Node temp = target;
          ArrayList<PVector> path = new ArrayList<>();
          while(temp!=start){
              path.add(new PVector(temp.x, temp.y));
              temp=temp.parent;
          }
          return path; // If it doesn't find a path, this returns empty
      }

      for(Node neighbour : getNeighbours(curr, nodes)){
          if(neighbour.isObstacle || closedList.contains(neighbour)) continue;
          float path;
          path = curr.g + manhattanDistance(curr, neighbour);
          if(neighbour.g>path || !openList.contains(neighbour)){
              neighbour.g = path;
              neighbour.h = manhattanDistance(neighbour, target);
              neighbour.parent = curr;
              if(!openList.contains(neighbour)){
                  openList.add(neighbour);
              }
          }
    }
  }
  return new ArrayList<PVector>();
}


ArrayList<Node> getNeighbours(Node node, Node[][] maze){
    ArrayList<Node> list = new ArrayList<>();
    int i = node.x;
    int j = node.y;
    if(i!=0) list.add(maze[i-1][j]);
    if(i!=maze.length-1) list.add(maze[i+1][j]);
    if(j!=0) list.add(maze[i][j-1]);
    if(j!=maze[0].length-1) list.add(maze[i][j+1]);
    return list;
}

float manhattanDistance(Node a, Node b){
	return Math.abs(a.x-b.x)+Math.abs(a.y-b.y);
}