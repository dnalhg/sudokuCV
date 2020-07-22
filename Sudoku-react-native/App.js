import React, {Component} from 'react';
import { StyleSheet, Text, View, Image, TouchableOpacity, Alert } from 'react-native';
import Grid from './grid.js'
import ChangePanel from './change_grid.js'
import solve from './solver.js'

export default class App extends Component {

  constructor(props) {
    super(props)
    this.board = new Array(9*9).fill(0)
    this.images = [
      require('./assets/0.png'),
      require('./assets/1.png'),
      require('./assets/2.png'),
      require('./assets/3.png'),
      require('./assets/4.png'),
      require('./assets/5.png'),
      require('./assets/6.png'),
      require('./assets/7.png'),
      require('./assets/8.png'),
      require('./assets/9.png'),
      require('./assets/cancel.png'),
    ]
    this.state = {board:this.board, selectedButton:null, images:this.images}

    this.buttonText = 'Solve'

    this._selectButton = this._selectButton.bind(this)
    this._modifyCell = this._modifyCell.bind(this)
    this.solveBoard = this.solveBoard.bind(this)
    this.resetStatus = this.resetStatus.bind(this)
  }

  _selectButton(button) {
    if (this.state.selectedButton === button) {
      this.setState({selectedButton:null})
    } else {
      this.setState({selectedButton:button})
    }
  }

  _modifyCell(number) {
    if (this.state.selectedButton !== null ) {
      this.board[this.state.selectedButton] = number%10
    }
    this.forceUpdate()
  }

  solveBoard(board) {
    var result = solve(this.state.board)
    if (result !== false) {
      this.buttonText = 'Solve'
      this.setState({board:result})
    } else {
      this.buttonText = 'No solution. Try again.'
    }
    this.forceUpdate()
  }

  resetStatus() {
    this.board.fill(0)
    this.setState({selectedButton:null})
    this.buttonText = 'Solve'
    this.forceUpdate()
  }

  render() {
    return (
      <View style={styles.container}>

        <Grid
          board={this.state.board}
          images={this.state.images}
          selectedButton={this.state.selectedButton}
          _selectButton={this._selectButton}/>

        <ChangePanel _modifyCell={this._modifyCell} images={this.state.images}/>

        <View style={styles.cols}>
          <TouchableOpacity
            style = {styles.button}
            onPress={this.solveBoard}>
            <Text>{this.buttonText}</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style = {styles.button}
            onPress={this.resetStatus}>
            <Text>Reset</Text>
          </TouchableOpacity>
        </View>

      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
  },

  button: {
    width: 175,
    height: 50,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: 'black'
  },

  cols: {
    flexDirection: 'row',
  },

});
