import java.util.PriorityQueue;
import java.util.HashSet;

public class Controller {

  boolean inLongestPath = false;
  ArrayList<PVector> longestPath = new ArrayList<PVector>();
  ArrayList<PVector> mainPathGeneral = new ArrayList<PVector>();

  void control() {
    /* Search for a main path to the food using Dijkstra. */
    // print("Dijkstra called from line 12");
    mainPathGeneral = dijkstra(snake, int(food_pos.x/scl), int(food_pos.y/scl));
    // println(mainPathGeneral);
    // println();
    // delay(30000);
    if(mainPathGeneral.size() > 0) { // If such a path is found...
      if(justDijkstra) { // ...and if the game mode is only Dijkstra search
        int[] mainHead = {int(snake.pos[0].x/scl), int(snake.pos[0].y/scl)};
        chooseSpeed(snake, mainPathGeneral.get(1), mainHead); // Choose normal movement
      } else { // ...but if the more complex search is activated, check if the snake is trapped
        Snake virtualSnake = snake.copy(); // creating a virtual snake
        int[] currentHead = {0,0};

        // ...and sending it to the food. This loop moves it to the food
        for (int i = 1; i < mainPathGeneral.size(); ++i) {
          currentHead[0] = int(virtualSnake.pos[0].x/scl);
          currentHead[1] = int(virtualSnake.pos[0].y/scl);
          chooseSpeed(virtualSnake, mainPathGeneral.get(i), currentHead);
          if(i == mainPathGeneral.size() - 1) {
            virtualSnake.eatsFood();
          }
          virtualSnake.update();
        }

        // and having the virtual snake reached the food, find the path to the tail
        // print("Dijkstra called from line 36");
        ArrayList<PVector> tracebackBack = dijkstra(virtualSnake, int(virtualSnake.pos[virtualSnake.pos.length-1].x/scl), int(virtualSnake.pos[virtualSnake.pos.length-1].y/scl));
        int[] mainHead = {int(snake.pos[0].x/scl), int(snake.pos[0].y/scl)};

        if(tracebackBack.size() > 0) { // If it finds the path to the tail
          // It won't get trapped and can choose the normal path found at the beginning
          chooseSpeed(snake, mainPathGeneral.get(1), mainHead);
          inLongestPath = false;
        } else { // But if it doesn't find it, it's best to search and follow the longest path to the tail until it stops getting trapped
          if(inLongestPath && longestPath.size() > 1) { // If it was already following the longest path in the previous frame
            chooseSpeed(snake, longestPath.get(1), mainHead); // simply continue on it
            longestPath.remove(0);
          } else { // but if it wasn't following it
            longestPathHeadTail(); // find the longest path to the tail
          }
        }
      }
    } else { // But if it doesn't find the main path in the first Dijsktra...
      if(justDijkstra) { // ...and if it's in justDijkstra mode, simply rotate if it's about to collide
        if(snake.isInBody(PVector.add(snake.pos[0], snake.vel)) || isOutsideWorld(PVector.add(snake.pos[0], snake.vel))) {
          PVector rotateRight = snake.vel.copy().rotate(HALF_PI);
          rotateRight.x = int(rotateRight.x);
          rotateRight.y = int(rotateRight.y);
          PVector rotateLeft = snake.vel.copy().rotate(-HALF_PI);
          rotateLeft.x = int(rotateLeft.x);
          rotateLeft.y = int(rotateLeft.y);
          if(!snake.isInBody(new PVector(snake.pos[0].x + rotateRight.x, snake.pos[0].y + rotateRight.y)) && !isOutsideWorld(new PVector(snake.pos[0].x + rotateRight.x, snake.pos[0].y + rotateRight.y))) {
            //println("Rotating to the right");
            snake.vel = rotateRight;
          } else if(!snake.isInBody(new PVector(snake.pos[0].x + rotateLeft.x, snake.pos[0].y + rotateLeft.y)) && !isOutsideWorld(new PVector(snake.pos[0].x + rotateLeft.x, snake.pos[0].y + rotateLeft.y))) {
            //println("Rotating to the left");
            snake.vel = rotateLeft;
          } else {
            //println("No way to go");
          }
        }
      } else { //... and if it's not in justDijsktra mode
        int[] mainHead = {int(snake.pos[0].x/scl), int(snake.pos[0].y/scl)};
        if(inLongestPath && longestPath.size() > 1) {
          chooseSpeed(snake, longestPath.get(1), mainHead);
          longestPath.remove(0);
        } else {
          longestPathHeadTail();
        }
      }
    }
  }

  //Dijkstra
  ArrayList<PVector> dijkstra(Snake currentSnake, int destinyX, int destinyY) {
    /* Node is each square on the map, its value is its Manhattan distance to the
       snake's head. */
    int[][] nodes = new int[horSqrs][verSqrs];
    ArrayList<PVector> queue = new ArrayList<PVector>();
    boolean[][] checked = new boolean[horSqrs][verSqrs];
    // First node, snake's head
    int[] firstNode = {int(currentSnake.pos[0].x/scl), int(currentSnake.pos[0].y/scl)};
    PVector currentNode = new PVector(currentSnake.pos[0].x, currentSnake.pos[0].y);
    // Initialize all nodes with an infinite value, except the first one, which is 0
    for (int i = 0; i < horSqrs; ++i) {
      for (int ii = 0; ii < verSqrs; ++ii) {
        if(firstNode[0] != i || firstNode[1] != ii) {
          nodes[i][ii] = Integer.MAX_VALUE;
          checked[i][ii] = false;
        } else {
          nodes[i][ii] = 0;
          checked[i][ii] = true;
        }
      }
    }

    // Start adding nodes to the queue that evaluates them one by one to assign values
    queue.add(new PVector(firstNode[0], firstNode[1]));
    int i = 0;

    /* In this loop, the values of all nodes are filled, that is, the distance
       Manhattan to all squares on the map it can reach is checked one by one.  */
    while(queue.size() != 0) {
      i++;

      int horIndex = int(queue.get(0).x);
      int verIndex = int(queue.get(0).y);
      
      int value = Integer.MAX_VALUE;

      /* Each node checks the four nodes on the sides, and the value is assigned depending
         on which one has the smallest value. */
      value = checkSideNode(horIndex, 0, horIndex-1, verIndex, value, nodes, queue, currentSnake); // left
      value = checkSideNode(-horIndex, 1-horSqrs, horIndex+1, verIndex, value, nodes, queue, currentSnake); // right
      value = checkSideNode(verIndex, 0, horIndex, verIndex-1, value, nodes, queue, currentSnake); // up
      value = checkSideNode(-verIndex, 1-verSqrs, horIndex, verIndex+1, value, nodes, queue, currentSnake); // down

      queue.remove(0); // remove the current node from the queue because it is being checked
      
      if(int(horIndex) != firstNode[0] || int(verIndex) != firstNode[1]) { // If the current node is not the first one...
        
        nodes[horIndex][verIndex] = value; // ...assign the value to it
        checked[horIndex][verIndex] = true;
      }
    }

    ArrayList<PVector> tracebackNodes = new ArrayList<PVector>();
    int[] tracebackNode = {destinyX, destinyY};
    // tracebackNodes = new ArrayList<PVector>();
    tracebackNodes.add(new PVector(tracebackNode[0], tracebackNode[1]));
    boolean closed = false;
  
    // It returns by searching each time for the node with the smallest value
    while(tracebackNode[0] != firstNode[0] || tracebackNode[1] != firstNode[1]) {
      PVector move = lowestNextTo(tracebackNode[0], tracebackNode[1], nodes);
      if(move.x == -1 && move.y == -1) {
        return new ArrayList<PVector>();
      }
      tracebackNodes.add(0, move);
      tracebackNode[0] = int(move.x);
      tracebackNode[1] = int(move.y);
    }
    return tracebackNodes; // If it doesn't find a path, this returns empty
  }

  int checkSideNode(int checked, int checkTo, int checkHor, int checkVer, int cValue, int[][] nodes, ArrayList<PVector> queue, Snake cSnake) {
    if(checked > checkTo) { // Check that they are inside the world
      if(nodes[checkHor][checkVer] < Integer.MAX_VALUE) { // And that its value is not infinite
        if(nodes[checkHor][checkVer] < cValue) { // If the value of the side node is less than the central node
          return nodes[checkHor][checkVer] + 1;
        }
      } else { // but if its value is infinite
        if(!cSnake.isInBody(checkHor, checkVer)) {
          if(!queue.contains(new PVector(checkHor, checkVer))) {
            queue.add(new PVector(checkHor, checkVer)); // add it to the queue, because it means it is not checked
            // with this last line, it ensures that the queue goes through all the nodes
          }
        }
      }
    }
    return cValue;
  }

  // Check which node has the lowest value next to the one with coords. x, y
  PVector lowestNextTo(int x, int y, int[][] nodes) {
    int lowestXInd = 0;
    int lowestYInd = 0;
    int lowestValue = Integer.MAX_VALUE;
    boolean closed = true;

    if(x > 0) {
      if(nodes[x-1][y] < lowestValue) {
        closed = false;
        lowestValue = nodes[x-1][y] + 1;
        lowestXInd = x-1;
        lowestYInd = y;
      }
    }
    if(x < horSqrs - 1) {
      if(nodes[x+1][y] < lowestValue - 1) {
        closed = false;
        lowestValue = nodes[x+1][y] + 1;
        lowestXInd = x+1;
        lowestYInd = y;
      }
    }
    if(y > 0) {
      if(nodes[x][y-1] < lowestValue - 1) {
        closed = false;
        lowestValue = nodes[x][y-1] + 1;
        lowestXInd = x;
        lowestYInd = y-1;
      }
    }
    if(y < verSqrs - 1) {
      if(nodes[x][y+1] < lowestValue - 1) {
        closed = false;
        lowestValue = nodes[x][y+1] + 1;
        lowestXInd = x;
        lowestYInd = y+1;
      }
    }
    
    if(closed) {
      return new PVector(-1, -1);
    }
    return new PVector(lowestXInd, lowestYInd);
    
  }

  // Find the longest path to the tail.
  void longestPathHeadTail() {
    ArrayList<PVector> path = dijkstra(snake, int(snake.pos[snake.pos.length-1].x/scl), int(snake.pos[snake.pos.length-1].y/scl));

    if(path.size() > 0) {
      //path extension algorithm
      boolean aPairFound = true;
      while(aPairFound) {
        aPairFound = false;
        for (int i = 0; i < path.size()-1; ++i) {
          // Two nodes in the same column
          if(path.get(i).x == path.get(i+1).x) {
            // Expand to the left
            if(areValidForLongestPath(path.get(i).x-1, path.get(i).y, path.get(i+1).x-1, path.get(i+1).y, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x-1, path.get(i).y));
              path.add(i+2, new PVector(path.get(i+2).x-1, path.get(i+2).y));
              aPairFound = true;
              break;
            // Expand to the right
            } else if(areValidForLongestPath(path.get(i).x+1, path.get(i).y, path.get(i+1).x+1, path.get(i+1).y, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x+1, path.get(i).y));
              path.add(i+2, new PVector(path.get(i+2).x+1, path.get(i+2).y));
              aPairFound = true;
              break;
            }
          // Two nodes in the same row
          } else if(path.get(i).y == path.get(i+1).y) {
            // Expand downward
            if(areValidForLongestPath(path.get(i).x, path.get(i).y+1, path.get(i+1).x, path.get(i+1).y+1, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x, path.get(i).y+1));
              path.add(i+2, new PVector(path.get(i+2).x, path.get(i+2).y+1));
              aPairFound = true;
              break;
            // Expand upward
            } else if(areValidForLongestPath(path.get(i).x, path.get(i).y-1, path.get(i+1).x, path.get(i+1).y-1, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x, path.get(i).y-1));
              path.add(i+2, new PVector(path.get(i+2).x, path.get(i+2).y-1));
              aPairFound = true;
              break;
            }
          }
        }
      }

      // Having the longest path, move in the direction that follows
      int[] mainHead = {int(snake.pos[0].x/scl), int(snake.pos[0].y/scl)};
      chooseSpeed(snake, path.get(1), mainHead);
      path.remove(0);
      longestPath = path;
      inLongestPath = true;
    }
  }

// Check that the two nodes being reviewed are indeed empty
  boolean areValidForLongestPath(float x1, float y1, float x2, float y2, ArrayList<PVector> path, Snake cSnake) {
    if (!path.contains(new PVector(x1, y1)) && 
        !path.contains(new PVector(x2, y2)) && 
        !isOutsideWorld(new PVector(x1*scl, y1*scl)) &&
        !isOutsideWorld(new PVector(x2*scl, y2*scl)) && 
        !cSnake.isInBody(int(x1), int(y1)) &&
        !cSnake.isInBody(int(x2), int(y2)) &&
        (x1 != int(food_pos.x/scl) || y1 != int(food_pos.y/scl)) &&
        (x2 != int(food_pos.x/scl) || y2 != int(food_pos.y/scl))) {
      return true;
    }
    return false;
  }

// Choose the direction of snake movement
  void chooseSpeed(Snake cSnake, PVector move, int[] cHead) {

    int horMove = int(move.x) - cHead[0];
    int verMove = int(move.y) - cHead[1];

    if(horMove == -1 && verMove == 0) {
      cSnake.vel.x = -scl;
      cSnake.vel.y = 0;
    } else if(horMove == 1 && verMove == 0) {
      cSnake.vel.x = scl;
      cSnake.vel.y = 0;
    } else if(horMove == 0 && verMove == -1) {
      cSnake.vel.x = 0;
      cSnake.vel.y = -scl;
    } else if(horMove == 0 && verMove == 1) {
      cSnake.vel.x = 0;
      cSnake.vel.y = scl;
    }
  }
}
