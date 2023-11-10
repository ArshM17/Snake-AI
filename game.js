import {
  update as updateSnake,
  draw as drawSnake,
  SNAKE_SPEED,
  getSnakeHead,
  snakeIntersection,
  snakeBody,
  gameState,
  expandSnake,
} from "./snake.js";
import { update as updateFood, draw as drawFood, food } from "./food.js";
import { outsideGrid } from "./grid.js";

let lastRenderTime = 0;
let gameOver = false;
const gameBoard = document.getElementById("game-board");
// const gameState = Array.from({ length: 21 }, () => Array(21).fill(0));

function main(currentTime) {
  if (gameOver) {
    if (confirm("You lost. Press ok to restart.")) {
      window.location = "/";
    } else {
      // gameState.forEach((row) => {
      //   console.log(row.join(" "));
      // });
    }
    return;
  }

  window.requestAnimationFrame(main);
  const secondsSinceLastRender = (currentTime - lastRenderTime) / 1000;
  if (secondsSinceLastRender < 1 / SNAKE_SPEED) return;

  lastRenderTime = currentTime;
  expandSnake(1);
  update();
  draw();
}

window.requestAnimationFrame(main);

function update() {
  updateSnake();
  updateFood();
  checkDeath();
  for (let i = 0; i < gameState.length; i++) {
    for (let j = 0; j < gameState[i].length; j++) {
      gameState[i][j] = 0;
    }
  }

  snakeBody.forEach(({ x, y }) => {
    if (gameState[y - 1]) gameState[y - 1][x - 1] = 1;
  });

  const head = snakeBody[0];
  if (gameState[head.y - 1]) gameState[head.y - 1][head.x - 1] = 2;
  if (gameState[food.y - 1]) gameState[food.y - 1][food.x - 1] = -1;
}

function draw() {
  gameBoard.innerHTML = "";
  drawSnake(gameBoard);
  drawFood(gameBoard);
}

function checkDeath() {
  gameOver = outsideGrid(getSnakeHead()) || snakeIntersection();
}
