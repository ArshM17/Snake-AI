int fps = 300;

// Board 36x27
int horSqrs = 20;
int verSqrs = 20;
// Board 12x12
// int horSqrs = 12;
// int verSqrs = 12;

// Window mode for 36x27 board
//int scl = 22;
// Full screen, 36x27 board
 int scl = 20;
// Full screen, 12x12 board
 //int scl = 63;

// Colors
int bgcol = color(70, 70, 70);
int gridcol = color(150, 150, 150);
int snakecol = color(255, 255, 255);
int foodcol = color(255, 0, 0);
boolean gamePaused = false;
boolean justDijkstra = false;

Snake snake;
PVector food_pos = new PVector(floor(random(horSqrs))*scl, floor(random(verSqrs))*scl);

void settings() {
  size(scl*horSqrs+1, scl*verSqrs+1);
}

void setup() {
  grid(gridcol);
  snake = new Snake(false);
  updateFood();
  renderFood();
}

// int p = 0;
void draw() {
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
void grid(color col) {
  for(int i = 0; i < horSqrs + 1; i++) {
    stroke(col);
    line(scl*i, 0, scl*i, verSqrs*scl); 
  }
  for(int i = 0; i < verSqrs + 1; i++) {
    stroke(col);
    line(0, scl*i, horSqrs*scl, scl*i); 
  }
}

void updateFood() {
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
void renderFood() {
  fill(foodcol);
  noStroke();
  rect(food_pos.x + 1, food_pos.y + 1, scl - 1, scl - 1);
}

boolean isOutsideWorld(PVector pos) {
  if(pos.x >= scl*horSqrs || pos.x < 0 || pos.y >= scl*verSqrs || pos.y < 0) {
    return true;
  }
  return false;
}

// D: use only Dijkstra, K: pause, J: slow down, L: speed up
void keyPressed() {  
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
