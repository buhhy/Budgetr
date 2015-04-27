/**
 * @author tlei (Terence Lei)
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
  },
  parseFloatDefault: function (str, def) {
    var p = parseFloat(str);
    if (isNaN(p))
      return def;
    return p;
  },
  /**
   * Compare with floating point error
   */
  eqE: function (v1, v2) {
    return Math.abs(v1 - v2) <= 1E-5;
  }
};
var models = {};
var api = {};
