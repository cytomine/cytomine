export function isRectangle(geometry) {
  if (geometry.getLinearRingCount() !== 1 || geometry.getCoordinates()[0].length !== 5) {
    return false;
  }

  const coordinates = geometry.getCoordinates(false)[0];
  let prevX = coordinates[0][0];
  let prevY = coordinates[0][1];
  for (let i = 1; i <= 4; i++) {
    let x = coordinates[i][0];
    let y = coordinates[i][1];
    let xChanged = (x !== prevX);
    let yChanged = (y !== prevY);
    if (xChanged === yChanged) {
      return false;
    }
    prevX = x;
    prevY = y;
  }

  return true;
}
