import java.util.Collections;

ArrayList<PVector> astar(Snake currentSnake, int destinyX, int destinyY) {
  Node[][] nodes = new Node[verSqrs][horSqrs];
  PriorityQueue<Node> openList = new PriorityQueue<>((a,b)->Float.compare(a.h+a.g, b.h+b.g));
  HashSet<Node> closedList = new HashSet<>();
  int[] firstNode = {int(currentSnake.pos[0].x/scl), int(currentSnake.pos[0].y/scl)};
  for (int i = 0; i < horSqrs; ++i) {
      for (int ii = 0; ii < verSqrs; ++ii) {
          nodes[i][ii] = new Node(i, ii, currentSnake.isInBody(i, ii));
      }
  }
  
  Node start = nodes[firstNode[0]][firstNode[1]];
  Node target = nodes[destinyX][destinyY];

  openList.add(start);

  while(!openList.isEmpty()) {
      Node curr = openList.remove();
      closedList.add(curr);
      if(curr==target){
          Node temp = target;
          ArrayList<PVector> path = new ArrayList<>();
          while(temp!=start){
              path.add(new PVector(temp.x, temp.y));
              temp=temp.parent;
          }
          path.add(new PVector(temp.x, temp.y));
          Collections.reverse(path);
          return path; 
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