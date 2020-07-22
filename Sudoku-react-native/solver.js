function isValidList(elements) {
  var flag = new Array(9).fill(0)

  for (let i=0; i<elements.length; i++) {
    if (elements[i]!==0) {
      if (flag[elements[i]-1] === 1) {
        return false
      }
      flag[elements[i]-1] = 1
    }
  }
  return true
}

function isValidBoard(board){
    //Checking horizontally
    for (let i=0; i<9; i++) {
      var row = board.slice(i*9,i*9+9)
      if (isValidList(row) !== true){
        return false
      }
    }

    //Checking vertically
    for (let i=0; i<9; i++) {
      var elements = []
      for (let j=0; j<9; j++) {
        elements.push(board[9*j+i])
      }
      if (isValidList(elements) !== true){
        return false
      }
    }

    //Checking the 3x3 grids
    for (let i=0; i<3; i++) {
      for (let j=0; j<3; j++) {
        var elements = []
        for (let x=0; x<3; x++) {
          for (let y=0; y<3; y++) {
            elements.push(board[3*i+27*j+x+9*y])
          }
        }
        if (isValidList(elements) !== true){
          return false
        }
      }
    }

    return true

}

function isValidLastmove(board, lastMove) {
    //Checking the row
    var row = Math.floor(lastMove/9)
    if (isValidList(board.slice(9*row,9*row+9)) !== true){
      return false
    }

    //Checking the column
    var colNum = lastMove%9
    var col = []
    for (let i=0; i<9; i++) {
      col.push(board[9*i+colNum])
    }
    if (isValidList(col) !== true){
      return false
    }

    //Checking the 3x3 grid
    var corner = [Math.floor(row/3), Math.floor(colNum/3)]
    var elements = []
    for (let x=0; x<3; x++) {
      for (let y=0; y<3; y++) {
        elements.push(board[27*corner[0]+x+3*corner[1]+9*y])
      }
    }
    if (isValidList(elements) !== true){
      return false
    }

    return true
}

export default function solve(board, emptyEntries=null, lastMove=null) {
  if (lastMove === null) {
    if (isValidBoard(board) !== true) {
      return false
    }
  } else {
    if (isValidLastmove(board, lastMove) !== true) {
      return false
    }
  }
  if (emptyEntries === null) {
    var emptyEntries = []
    for (let i=0; i<9*9; i++) {
      if (board[i] === 0) {
        emptyEntries.push(i)
      }
    }
  }

  if (emptyEntries.length === 0) {
    return board
  }

  var entry = emptyEntries.pop()

  for (let x=1; x<10; x++) {
    board[entry] = x
    var result = solve(board,emptyEntries,entry)
    if (result !== false) {
      return result
    }
  }

  board[entry] = 0
  emptyEntries.push(entry)

  return false

}
