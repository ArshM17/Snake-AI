let inputDirection = { x: 0, y: 0 };
let lastInputDirection = { x: 0, y: 0 };
let directions = [
  { x: 0, y: 1 },
  { x: 0, y: -1 },
  { x: 1, y: 0 },
  { x: -1, y: 0 },
];

// window.addEventListener("keydown", (e) => {
//   switch (e.key) {
//     case "ArrowUp":
//       if (lastInputDirection.y !== 0) break;
//       inputDirection = { x: 0, y: -1 };
//       break;
//     case "ArrowDown":
//       if (lastInputDirection.y !== 0) break;
//       inputDirection = { x: 0, y: 1 };
//       break;
//     case "ArrowLeft":
//       if (lastInputDirection.x !== 0) break;
//       inputDirection = { x: -1, y: 0 };
//       break;
//     case "ArrowRight":
//       if (lastInputDirection.x !== 0) break;
//       inputDirection = { x: 1, y: 0 };
//       break;
//   }
// });

export function getInputDirection() {
  let dir = Math.floor(Math.random() * 4);
  switch (dir) {
    case 1:
      if (lastInputDirection.y !== 0) break;
      inputDirection = { x: 0, y: -1 };
      break;
    case 0:
      if (lastInputDirection.y !== 0) break;
      inputDirection = { x: 0, y: 1 };
      break;
    case 3:
      if (lastInputDirection.x !== 0) break;
      inputDirection = { x: -1, y: 0 };
      break;
    case 2:
      if (lastInputDirection.x !== 0) break;
      inputDirection = { x: 1, y: 0 };
      break;
  }
  lastInputDirection = inputDirection;
  return inputDirection;
}
