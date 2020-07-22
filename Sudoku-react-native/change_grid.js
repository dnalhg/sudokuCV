import React, {Component} from 'react';
import { StyleSheet, Text, View, Image, TouchableWithoutFeedback} from 'react-native';

class Cell extends Component {

  render() {
    var cellImage = this.props.images[this.props.cellNum];

    return (
      <TouchableWithoutFeedback
        onPress={() => this.props._handleClick(this.props.cellNum)}>
        <Image source={cellImage} style={styles.cell}/>
      </TouchableWithoutFeedback>
      );
  }
}

export default class ChangePanel extends Component {

  constructor(props) {
    super(props)
  }

  render() {

    var cells = []
    for (let i=0; i < 10; i++) {
      cells.push(
        <Cell
          cellNum={i+1}
          _handleClick={this.props._modifyCell}
          images={this.props.images}/>
      )
    }

    return (
      <View style={styles.cols}>
        { cells }
      </View>
    );
  }
}

const styles = StyleSheet.create({

  cell: {
    width: 35,
    height: 35,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: 'white'
  },

  cols: {
    flexDirection: 'row',
  },

});
