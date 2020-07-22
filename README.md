# SudokuCV
Directory structure:
- sudoku.py: Simple sudoku solver implemented in python using back tracking.
- Sudoku-react-native: A cross-platform sudoku solving app developed using react-native. Requires users to manually input sudoku grid in order to solve it.
- SudokuCV: A sudoku solving app for android developed in Java using android studio that incorporates computer vision.

## Description
I initially implemented a sudoku solver in python using backtracking. In order to improve usability I decided to create a UI using react-native such that it was usable across all platforms. However, I noticed that manually inputting the sudoku board is extremely tedious and decided to incorporate computer vision to accelerate the process. Since react-native does not support any computer vision modules, I decided to switch to developing solely for android. The final app has the following features:
- Users can manually input the sudoku board and solve it
- Users can import a board by taking a picture or importing an image. OpenCV is used to process the images to detect the board whilst Tesseract OCR is used for the digit recognition.
- The app also features generating random boards through trial and error.

## Sudoku board processing with OpenCV and Tesseract OCR
Images of the sudoku board are processed as follows:
1. The image is converted to grayscale.
2. The image is blurred to remove noise.
3. Adaptive thresholding is used to make the black and white more distinct and further remove noise.
4. The largest contour by area in the image is found. I assumed this was the sudoku board and would hence be some quadrilateral. The 4 corners of this quadrilateral are then located.
5. We find the homography required to project this quadrilateral into a regular rectangle and then apply this homography to warp the perspective. This allows us to isolate the sudoku board and present it as a regular rectangle. This is necessary to account for when the image of the board is angled or tilted.
6. Finding the largest contour again in this warped picture and dimensions of its bounding rectangle. This gives us the dimensions of the sudoku grid.
7. We iteratively extract the number in each cell of the sudoku grid using the dimensions of the sudoku grid divided by 9 and cropping out the edges to remove noise.
8. Tesseract OCR is used to then recognise each of the digits.

## Board generation method
In order to randomly generate sudoku boards, trial and error was used. Random cells on the sudoku grid are sampled and random digits are chosen for these cells. The final board constructed is then solved using the backtracker and if a solution is not possible, we repeat. In order to improve efficiency, whenever a random digit is chosen for a cell, we check if it conflicts with any previously placed digits before proceeding.

## Comments and future work
Overall, playing around with computer vision and figuring out how to effectively generate a sudoku board were extremely interesting which made this project a fun learning experience. There are many different computer vision techniques and quite interesting maths behind each one which I think would be interesting to learn about in the future.

The current app works quite well and is able to recognise and solve most sudoku boards. However, it is not able to solve more ambiguous (and technically invalid) sudoku boards with numerous solutions. This is due to the backtracking being inefficient and requiring optimisations such as accounting for sudoku rules which could be included in the future. Whilst the computer vision works, there are times when it completely misinterprets a sudoku board due to excessive noise, particularly if the picture is one of a computer monitor is taken. A deeper understanding of computer vision may be needed to better process the images in future.
