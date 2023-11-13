/* autogenerated by Processing revision 1293 on 2023-11-13 */
import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class SnakeDijkstraAI extends PApplet {

int fps = 300;

// Board 36x27
int horSqrs = 36;
int verSqrs = 36;
// Board 12x12
// int horSqrs = 12;
// int verSqrs = 12;

// Window mode for 36x27 board
//int scl = 22;
// Full screen, 36x27 board
 int scl = 28;
// Full screen, 12x12 board
 //int scl = 63;

// Colors
int bgcol = color(70, 70, 70);
int gridcol = color(70, 70, 70);
int snakecol = color(255, 255, 255);
int foodcol = color(255, 0, 0);
boolean gamePaused = false;
boolean justDijkstra = false;

Snake snake;
PVector food_pos = new PVector(floor(random(horSqrs))*scl, floor(random(verSqrs))*scl);

public void settings() {
  size(scl*horSqrs+1, scl*verSqrs+1);
}

public void setup() {
  grid(gridcol);
  snake = new Snake(false);
  updateFood();
  renderFood();
}

// int p = 0;
public void draw() {
  if(!gamePaused) {
    frameRate(fps);
    background(bgcol);
    grid(gridcol);
    snake.update();
    updateFood();
    snake.search();
    // p = 0;
    snake.render();
    renderFood();
    // if(snake.controller.mainSearch.size() == 0 && snake.controller.inLongestPath && p==2) {
    //   delay(3000);
    // }
  }
}

// This is to draw the grid
public void grid(int col) {
  for(int i = 0; i < horSqrs + 1; i++) {
    stroke(col);
    line(scl*i, 0, scl*i, verSqrs*scl); 
  }
  for(int i = 0; i < verSqrs + 1; i++) {
    stroke(col);
    line(0, scl*i, horSqrs*scl, scl*i); 
  }
}

public void updateFood() {
  if(snake.ateFood()) {
    boolean match = true;
    while(match) {
      match = false;
      food_pos.x = floor(random(horSqrs))*scl; 
      food_pos.y = floor(random(verSqrs))*scl;
      // This is to make sure the food does not appear where the snake's body is
      for(int i = 0; i < snake.pos.length; i++) {
        if(food_pos.x == snake.pos[i].x && food_pos.y == snake.pos[i].y) {
          match = true;
        }
      }
    }
  }
}
public void renderFood() {
  fill(foodcol);
  noStroke();
  rect(food_pos.x + 1, food_pos.y + 1, scl - 1, scl - 1);
}

public boolean isOutsideWorld(PVector pos) {
  if(pos.x >= scl*horSqrs || pos.x < 0 || pos.y >= scl*verSqrs || pos.y < 0) {
    return true;
  }
  return false;
}

// D: use only Dijkstra, K: pause, J: slow down, L: speed up
public void keyPressed() {  
  if (key == 'd') {
    justDijkstra = !justDijkstra;
  }
  if (key == 'k') {
    gamePaused = !gamePaused;
  }
  if(key == 'l') {
    switch (fps) {
      case 5 :
        fps = 15;
      case 15 :
        fps = 30;
      break;
      case 30 :
        fps = 100;
      break;
      case 100 :
        fps = 200;
      break;
      case 200 :
        fps = 300;
      break;	
      default :
      break;	
    }
  }
  if(key == 'j') {
    switch (fps) {
      case 300 :
        fps = 200;
      break;
      case 200 :
        fps = 100;
      break;	
      case 100 :
        fps = 30;
      case 30 :
        fps = 15;
      case 15 :
        fps = 5;
      break;
      default :
      break;	
    }
  }
}
class Snake {
  int length = 0;
  boolean justAte = false;
  Controller controller = new Controller();
  PVector[] pos = new PVector[1]; // array of snake body positions
  PVector vel = new PVector(1,0);
  PVector prev_head = new PVector(0,0);
  
  Snake(boolean isVirtual) {
    vel.mult(scl);
    pos[0] = new PVector(floor(horSqrs/2)*scl,floor(verSqrs/2)*scl);
    // render only if it's not virtual
    if(!isVirtual) {
      this.square(pos[0].x, pos[0].y);
    }
  }
  
  /* Executed in each frame, this contains all the main logic of the Snake game */
  public void update() {
    justAte = false;
    prev_head = pos[0].get();
    pos[0].add(vel);
    this.checkEatFood();
    this.checkBoundaries();
    this.move();
    this.checkCollBody();
  }

  // Executed in each frame, this is the search
  public void render() {
    for(int i = 0; i < this.pos.length-1; i++) {
      square(this.pos[i].x, this.pos[i].y);
      if(pos[i].x == pos[i+1].x) {
        if(pos[i].y < pos[i+1].y) {
          rect(pos[i].x + 2, pos[i].y + scl - 1, scl - 3, 3);
        }
        if(pos[i].y > pos[i+1].y) {
          rect(pos[i].x + 2, pos[i+1].y + scl - 1, scl - 3, 3);
        }
      }
      if(pos[i].y == pos[i+1].y) {
        if(pos[i].x < pos[i+1].x) {
          rect(pos[i].x + scl - 1, pos[i].y + 2, 3, scl - 3);
        }
        if(pos[i].x > pos[i+1].x) {
          rect(pos[i+1].x + scl - 1, pos[i].y + 2, 3, scl - 3);
        }
      }
    }
    square(pos[pos.length-1].x, pos[pos.length-1].y); // draw the last part of the body
  }
  
  // Move the snake's body
  public void move() {
    PVector previous = this.prev_head.copy();
    PVector previous_copy = this.prev_head.copy(); 
    for(int i = 1; i < this.pos.length; i++) {
      previous = pos[i];
      pos[i] = previous_copy;
      previous_copy = previous;
    }
  }

  public void search() {
    this.controller.control();
  }
  
  public void checkEatFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) { 
      this.eatsFood();
    }
  }
  
  // Extend the snake after eating
  public void eatsFood() {
    //if(this.pos.length == 1) {
      //this.pos = (PVector[])append(this.pos, new PVector(this.prev_head.x, this.prev_head.y));
    //} else {
      this.pos = (PVector[])append(this.pos, new PVector(this.pos[this.pos.length - 1].x, this.pos[this.pos.length - 1].y));
    //}
  }
  
  public boolean ateFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) {
      justAte = true;
      return true;
    }
    return false;
  }
  
  public void died() {
    gamePaused = true;
    //this.pos = new PVector[1];
    //this.pos[0] = new PVector(floor(random(horSqrs))*scl, floor(random(verSqrs))*scl);
  }
  
  public void checkBoundaries() {
    if(isOutsideWorld(pos[0])) {
      this.died();
    }
  }
  
  public void checkCollBody() {
    if(isInBody(this.pos[0])) {
      this.died();
    }
  }

  public boolean isInBody(int x, int y) {
    for(int i = 1; i < this.pos.length; i++) {
      if(x*scl == this.pos[i].x && y*scl == this.pos[i].y) {
        return true;
      }
    }
    return false;
  }

  public boolean isInBody(PVector position) {
    return isInBody(PApplet.parseInt(position.x/scl), PApplet.parseInt(position.y/scl));
  }
  
  public void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 2, y + 2, scl - 3, scl - 3);
  }

  // To create a virtual copy of the snake
  public Snake copy() {
    Snake copy = new Snake(true);
    copy.pos[0] = pos[0].copy();
    for (int i = 1; i < pos.length; ++i) {
      copy.pos = (PVector[])append(copy.pos, pos[i].copy());
    }
    copy.vel = vel.copy();
    copy.prev_head = prev_head.copy();

    return copy;
  }
}

public class Controller {

  boolean inLongestPath = false;
  ArrayList<PVector> longestPath = new ArrayList<PVector>();
  ArrayList<PVector> mainPathGeneral = new ArrayList<PVector>();

  public void control() {
    /* Search for a main path to the food using Dijkstra. */
    mainPathGeneral = dijkstra(snake, PApplet.parseInt(food_pos.x/scl), PApplet.parseInt(food_pos.y/scl), true);

    if(mainPathGeneral.size() > 0) { // If such a path is found...
      if(justDijkstra) { // ...and if the game mode is only Dijkstra search
        int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
        chooseSpeed(snake, mainPathGeneral.get(1), mainHead); // Choose normal movement
      } else { // ...but if the more complex search is activated, check if the snake is trapped
        Snake virtualSnake = snake.copy(); // creating a virtual snake
        int[] currentHead = {0,0};

        // ...and sending it to the food. This loop moves it to the food
        for (int i = 1; i < mainPathGeneral.size(); ++i) {
          currentHead[0] = PApplet.parseInt(virtualSnake.pos[0].x/scl);
          currentHead[1] = PApplet.parseInt(virtualSnake.pos[0].y/scl);
          chooseSpeed(virtualSnake, mainPathGeneral.get(i), currentHead);
          if(i == mainPathGeneral.size() - 1) {
            virtualSnake.eatsFood();
          }
          virtualSnake.update();
        }

        // and having the virtual snake reached the food, find the path to the tail
        ArrayList<PVector> tracebackBack = dijkstra(virtualSnake, PApplet.parseInt(virtualSnake.pos[virtualSnake.pos.length-1].x/scl), PApplet.parseInt(virtualSnake.pos[virtualSnake.pos.length-1].y/scl), false);
        int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};

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
          rotateRight.x = PApplet.parseInt(rotateRight.x);
          rotateRight.y = PApplet.parseInt(rotateRight.y);
          PVector rotateLeft = snake.vel.copy().rotate(-HALF_PI);
          rotateLeft.x = PApplet.parseInt(rotateLeft.x);
          rotateLeft.y = PApplet.parseInt(rotateLeft.y);
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
        int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
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
  public ArrayList<PVector> dijkstra(Snake currentSnake, int destinyX, int destinyY, boolean print) {
    /* Node is each square on the map, its value is its Manhattan distance to the
       snake's head. */
    int[][] nodes = new int[horSqrs][verSqrs];
    ArrayList<PVector> queue = new ArrayList<PVector>();
    boolean[][] checked = new boolean[horSqrs][verSqrs];

    // First node, snake's head
    int[] firstNode = {PApplet.parseInt(currentSnake.pos[0].x/scl), PApplet.parseInt(currentSnake.pos[0].y/scl)};
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

      int horIndex = PApplet.parseInt(queue.get(0).x);
      int verIndex = PApplet.parseInt(queue.get(0).y);
      
      int value = Integer.MAX_VALUE;

      /* Each node checks the four nodes on the sides, and the value is assigned depending
         on which one has the smallest value. */
      value = checkSideNode(horIndex, 0, horIndex-1, verIndex, value, nodes, queue, currentSnake); // left
      value = checkSideNode(-horIndex, 1-horSqrs, horIndex+1, verIndex, value, nodes, queue, currentSnake); // right
      value = checkSideNode(verIndex, 0, horIndex, verIndex-1, value, nodes, queue, currentSnake); // up
      value = checkSideNode(-verIndex, 1-verSqrs, horIndex, verIndex+1, value, nodes, queue, currentSnake); // down

      queue.remove(0); // remove the current node from the queue because it is being checked
      
      if(PApplet.parseInt(horIndex) != firstNode[0] || PApplet.parseInt(verIndex) != firstNode[1]) { // If the current node is not the first one...
        
        nodes[horIndex][verIndex] = value; // ...assign the value to it
        checked[horIndex][verIndex] = true;
        //print(value);
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
      tracebackNode[0] = PApplet.parseInt(move.x);
      tracebackNode[1] = PApplet.parseInt(move.y);
    }

    return tracebackNodes; // If it doesn't find a path, this returns empty
  }

  //value = checkSideNode(horIndex, 0, horIndex-1, verIndex, value, nodes, queue, currentSnake); // left
  public int checkSideNode(int checked, int checkTo, int checkHor, int checkVer, int cValue, int[][] nodes, ArrayList<PVector> queue, Snake cSnake) {
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
  public PVector lowestNextTo(int x, int y, int[][] nodes) {
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
  public void longestPathHeadTail() {
    ArrayList<PVector> path = dijkstra(snake, PApplet.parseInt(snake.pos[snake.pos.length-1].x/scl), PApplet.parseInt(snake.pos[snake.pos.length-1].y/scl), false);

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
      int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
      chooseSpeed(snake, path.get(1), mainHead);
      path.remove(0);
      longestPath = path;
      inLongestPath = true;
    }
  }

// Check that the two nodes being reviewed are indeed empty
  public boolean areValidForLongestPath(float x1, float y1, float x2, float y2, ArrayList<PVector> path, Snake cSnake) {
    if (!path.contains(new PVector(x1, y1)) && 
        !path.contains(new PVector(x2, y2)) && 
        !isOutsideWorld(new PVector(x1*scl, y1*scl)) &&
        !isOutsideWorld(new PVector(x2*scl, y2*scl)) && 
        !cSnake.isInBody(PApplet.parseInt(x1), PApplet.parseInt(y1)) &&
        !cSnake.isInBody(PApplet.parseInt(x2), PApplet.parseInt(y2)) &&
        (x1 != PApplet.parseInt(food_pos.x/scl) || y1 != PApplet.parseInt(food_pos.y/scl)) &&
        (x2 != PApplet.parseInt(food_pos.x/scl) || y2 != PApplet.parseInt(food_pos.y/scl))) {
      return true;
    }
    return false;
  }

// Choose the direction of snake movement
  public void chooseSpeed(Snake cSnake, PVector move, int[] cHead) {

    int horMove = PApplet.parseInt(move.x) - cHead[0];
    int verMove = PApplet.parseInt(move.y) - cHead[1];

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


  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SnakeDijkstraAI" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
