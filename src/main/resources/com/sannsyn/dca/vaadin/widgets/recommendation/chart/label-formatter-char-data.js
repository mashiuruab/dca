function labelFormatter() {
  var labelValue = parseInt(this.value) + 1;
  if(labelValue >= 1 && labelValue <= 60 && (labelValue % 10) == 0) {
    return labelValue + 's';
  } else if(labelValue >= 61 && labelValue <= 120 && ((labelValue - 60) % 10) == 0) {
    return labelValue - 60 + 'm';
  } else if(labelValue >= 121 && labelValue <= 144 && ((labelValue - 120) % 8) == 0) {
    return labelValue - 120 + 'h';
  } else if(labelValue >= 145 && labelValue <= 174 && ((labelValue - 144) % 10) == 0) {
    return labelValue - 144 + 'd';
  } else if(labelValue >= 175 && labelValue <= 186 && ((labelValue - 174) % 6) == 0) {
    return labelValue - 174 + 'M';
  } else if(labelValue >= 187 && labelValue <= 198 && ((labelValue - 186) % 10) == 0) {
    return labelValue - 186 + 'y';
  } else {
    return '';
  }
}

function yFormatter() {
  var yLabelValue = parseFloat(this.value);
  if(yLabelValue == 0.01) {
    return 0;
  } else if (yLabelValue == 0.0001){
    return '';
  } else {
    return this.value;
  }
}

function toolTipFormatter() {
  var label = (this.y < 1) ? 0 : this.y;
  return '<b>' + label + '</b> - ' + this.key;
}