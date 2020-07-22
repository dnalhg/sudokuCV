import React, {Component} from 'react';
import { StyleSheet, Text, View, Image, TouchableWithoutFeedback} from 'react-native';

class Cell extends Component {

  render() {
    var value = this.props.board[this.props.cellNum]
    var cellImage;
    if (value === 0) {
      cellImage=this.props.images[0]
    } else {
      cellImage=this.props.images[value]
    }

    return (
      <TouchableWithoutFeedback
        onPress={() => this.props._handleClick(this.props.cellNum)}>
        <Image source={cellImage}
          style={{
            width: 35,
            height: 35,
            alignItems: 'center',
            justifyContent: 'center',
            borderColor: (this.props.selectedButton === this.props.cellNum ? 'red' : 'white'),
            borderWidth: 2
          }}
        />
      </TouchableWithoutFeedback>
      );
  }
}

export default class Grid extends Component {

  constructor(props) {
    super(props)
  }

  render() {

    var cells = []
    for (let i=0; i < 9*9; i++) {
      cells.push(
        <Cell cellNum={i}
        _handleClick={this.props._selectButton}
        selectedButton={this.props.selectedButton}
        board={this.props.board}
        images={this.props.images}/>
      )
    }

    var rows = []
    for (let i=0; i< 9; i++) {
      rows.push(
        <View style={styles.cols}>
          {cells.slice(i*9,i*9+9)}
        </View>
      )
    }

    return (
      <View>
        { rows }
      </View>
    );
  }
}

const styles = StyleSheet.create({
  rows: {
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1
  },

  cols: {
    flexDirection: 'row',
  },

  cell: {
    width: 35,
    height: 35,
    alignItems: 'center',
    justifyContent: 'center',
  }
});
