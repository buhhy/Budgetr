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
  }
};
var models = {};
var api = {};

// Makes the provided class a subclass of another
Object.prototype.extendClass = function (constructor) {
  constructor.prototype = Object.create(this.prototype);
  constructor.prototype.constructor = constructor;
  constructor.super = constructor.prototype;
  return constructor;
};