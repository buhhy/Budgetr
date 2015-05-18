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
  },
  removeArrayDuplicates: function (arr) {
    return _.map(
        _.pairs(
            _.reduce(
                arr,
                function (collect, item) {
                  if (item)
                    collect[item] = (collect[item] || 0) + 1;
                  return collect;
                }, {})),
        function (pair) {
          return pair[0];
        });
  },
  searchThroughArray: function (query, array) {
    if (!array)
      return [];

    // regex used to determine if a string contains the substring `query`
    var substrRegex = new RegExp(query, 'i');

    // iterate through the pool of strings and filter out any strings that  do not contain the
    // substring `q`
    return _.filter(array, function (location) {
      return substrRegex.test(location);
    });
  }
};
var models = {};
var api = {};
