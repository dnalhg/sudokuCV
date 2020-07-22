def is_valid_list(elements):
    elements = sorted(elements)
    for i,x in enumerate(elements):
        if i == 0:
            continue
        if x == elements[i-1] and x != 0:
            return False
    return True

def is_valid_board(board):
    # Checking horizontally
    for row in board:
        if not is_valid_list(row):
            return False

    # Checking vertically
    for col in range(9):
        elements = []
        for row in board:
            elements.append(row[col])
        if not is_valid_list(elements):
            return False

    # Checking the 3x3 grids
    for i in range(3):
        for j in range(3):
            elements = []
            for x in range(3):
                for y in range(3):
                    elements.append(board[3*i+x][3*j+y])
            if not is_valid_list(elements):
                return False

    return True

def is_valid_lastmove(board, last_move):
    # Checking the row
    row = board[last_move[0]]
    if not is_valid_list(row):
        return False

    # Checking the column
    col = []
    for r in board:
        col.append(r[last_move[1]])
    if not is_valid_list(col):
        return False

    # Checking the 3x3 grid
    corner = (last_move[0]//3, last_move[1]//3)
    elements = []
    for x in range(3):
        for y in range(3):
            elements.append(board[3*corner[0]+x][3*corner[1]+y])
    if not is_valid_list(elements):
        return False

    return True


def solve(board, empty_entries=None, last_move=None):
    if last_move is None:
        if not is_valid_board(board):
            return False
    else:
        if not is_valid_lastmove(board,last_move):
            return False
    if empty_entries is None:
        empty_entries = []
        for i,row in enumerate(board):
            for j,e in enumerate(row):
                if e == 0:
                    empty_entries.append((i,j))

    if len(empty_entries) == 0:
        return board

    entry = empty_entries.pop()
    for x in range(1,10):
        board[entry[0]][entry[1]] = x
        result = solve(board, empty_entries, entry)
        if result is False:
            continue
        else:
            return result

    board[entry[0]][entry[1]] = 0
    empty_entries.append(entry)
    return False

def test():
    assert is_valid_board([ [3, 0, 6, 5, 0, 8, 4, 0, 0],
             [5, 2, 0, 0, 0, 0, 0, 0, 0],
             [0, 8, 7, 0, 0, 0, 0, 3, 1],
             [0, 0, 3, 0, 1, 0, 0, 8, 0],
             [9, 0, 0, 8, 6, 3, 0, 0, 5],
             [0, 5, 0, 0, 9, 0, 6, 0, 0],
             [1, 3, 0, 0, 0, 0, 2, 5, 0],
             [0, 0, 0, 0, 0, 0, 0, 7, 4],
             [0, 0, 5, 2, 0, 6, 3, 0, 0] ])

    assert is_valid_board([ [3, 0, 6, 5, 3, 8, 4, 0, 0],
             [5, 2, 0, 0, 0, 0, 0, 0, 0],
             [0, 8, 7, 0, 0, 0, 0, 3, 1],
             [0, 0, 3, 0, 1, 0, 0, 8, 0],
             [9, 0, 0, 8, 6, 3, 0, 0, 5],
             [0, 5, 0, 0, 9, 0, 6, 0, 0],
             [1, 3, 0, 0, 0, 0, 2, 5, 0],
             [0, 0, 0, 0, 0, 0, 0, 7, 4],
             [0, 0, 5, 2, 0, 6, 3, 0, 0] ]) is False

    board = [ [3, 0, 6, 5, 0, 8, 4, 0, 0],
             [5, 2, 0, 0, 0, 0, 0, 0, 0],
             [0, 8, 7, 0, 0, 0, 0, 3, 1],
             [0, 0, 3, 0, 1, 0, 0, 8, 0],
             [9, 0, 0, 8, 6, 3, 0, 0, 5],
             [0, 5, 0, 0, 9, 0, 6, 0, 0],
             [1, 3, 0, 0, 0, 0, 2, 5, 0],
             [0, 0, 0, 0, 0, 0, 0, 7, 4],
             [0, 0, 5, 2, 0, 6, 3, 0, 0] ]
    solution = [ [3, 1, 6, 5, 7, 8, 4, 9, 2],
              [5, 2, 9, 1, 3, 4, 7, 6, 8],
              [4, 8, 7, 6, 2, 9, 5, 3, 1],
              [2, 6, 3, 4, 1, 5, 9, 8, 7],
              [9, 7, 4, 8, 6, 3, 1, 2, 5],
              [8, 5, 1, 7, 9, 2, 6, 4, 3],
              [1, 3, 8, 9, 4, 7, 2, 5, 6],
              [6, 9, 2, 3, 5, 1, 8, 7, 4],
              [7, 4, 5, 2, 8, 6, 3, 1, 9] ]
    assert solve(board) == solution

    board2 = [ [1, 0, 0, 0, 0, 0, 0, 0, 0],
             [0, 0, 0, 0, 0, 7, 0, 0, 5],
             [0, 8, 0, 9, 0, 3, 4, 2, 0],
             [0, 0, 2, 0, 4, 0, 8, 0, 0],
             [8, 1, 0, 0, 2, 0, 0, 0, 0],
             [0, 0, 7, 0, 0, 0, 0, 5, 6],
             [6, 5, 0, 0, 0, 0, 0, 0, 0],
             [0, 0, 3, 0, 0, 0, 0, 9, 0],
             [0, 0, 0, 0, 0, 0, 7, 0, 4] ]
    solution2 = [[1, 7, 9, 2, 5, 4, 6, 8, 3],
                [3, 2, 4, 6, 8, 7, 9, 1, 5],
                [5, 8, 6, 9, 1, 3, 4, 2, 7],
                [9, 6, 2, 3, 4, 5, 8, 7, 1],
                [8, 1, 5, 7, 2, 6, 3, 4, 9],
                [4, 3, 7, 8, 9, 1, 2, 5, 6],
                [6, 5, 8, 4, 7, 9, 1, 3, 2],
                [7, 4, 3, 1, 6, 2, 5, 9, 8],
                [2, 9, 1, 5, 3, 8, 7, 6, 4]]
    assert solve(board2) == solution2

test()
