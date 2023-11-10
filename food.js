import { onSnake, expandSnake, gameState } from "./snake.js";
// import { randomGridPosition } from "./grid.js";

export let food = getRandomFoodPosition();
const EXPANSION_RATE = 1;

export function update() {
  if (onSnake(food)) {
    expandSnake(EXPANSION_RATE);
    food = getRandomFoodPosition();
  }
}

export function draw(gameBoard) {
  const foodElement = document.createElement("div");
  foodElement.style.gridRowStart = food.y;
  foodElement.style.gridColumnStart = food.x;
  foodElement.classList.add("food");
  gameBoard.appendChild(foodElement);
}

function getRandomFoodPosition() {
  const zeroIndices = [];
  for (let i = 0; i < gameState.length; i++) {
    for (let j = 0; j < gameState[i].length; j++) {
      if (gameState[i][j] === 0) {
        zeroIndices.push({ x: i + 1, y: j + 1 });
      }
    }
  }

  const randomIndex = Math.floor(Math.random() * zeroIndices.length);

  return zeroIndices[randomIndex];
}
