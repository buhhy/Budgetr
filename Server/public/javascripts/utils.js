/**
 * Created by Terry Lei on 8/4/2015.
 */

var ui = {};
var utils = {
  // Converts time
  convertTime: function (epochTime) {
    var convertedTime = new Date(epochTime);
    var options = { weekday: 'long', month: 'long', day: 'numeric' };
    return convertedTime.toLocaleDateString('en-US', options);
  },
  idSelector: function (id) {
    return "[data-id='" + id + "']";
  },
  join: function (array, delim) {
    delim = delim || ',';
    var str = "";
    for (var i = 0; i < array.length; i++) {
      if (i > 0)
        str += delim;
      str += array[i];
    }
    return str;
  }
};
var models = {};
var api = {};
