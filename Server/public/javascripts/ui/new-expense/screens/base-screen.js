/**
 * @author tlei (Terence Lei)
 */

// Makes the provided class a subclass of another
ui.extend = function (constructor) {
  var _super = this.prototype || Object.prototype;
  constructor.prototype = Object.create(_super);
  constructor.prototype.constructor = constructor;
  constructor.prototype.super = _super;
  constructor.extend = ui.extend;
  return constructor;
};

/**
 * UI for each page within the new expense stack widget.
 * @param $root
 * @param eventHooks
 * @param valueHandlers
 * @constructor
 */
ui.NewExpenseWidgetScreen = ui.extend(
    function ($root, eventHooks, valueHandlers) {
      var self = this;
      this.eventHooks = eventHooks || {};
      valueHandlers = valueHandlers || {};

      this.valueExtractor =
          valueHandlers.valueExtractor || function (screen) { return screen.$firstInput.val(); };
      this.valueSerializer =
          valueHandlers.valueSerializer || function (screen, json) { return json; };
      this.valueWriter =
          valueHandlers.valueWriter || function (screen, value) { screen.$firstInput.val(value) };
      this.valueClear =
          valueHandlers.valueClear || function (screen) { screen.$firstInput.val("") };

      this.$root = $root;
      this.$firstInput = $root.find("[data-first-input]");
      this.$nextScreenInput = $root.find("[data-next-screen-input]");
      this.$allInputs = $root.find("input");
      this.expList = null;

      // Assign the next screen keypress handlers
      this.$nextScreenInput.on("keypress", function (event) {
        // Enter key pressed
        if (event.keyCode === 13 && self.eventHooks.next)
          self.eventHooks.next();
      });

      // Screen is disabled by default
      this.$allInputs.prop("disabled", true);
    });

ui.NewExpenseWidgetScreen.prototype.setExpenseList = function (expList) {
  this.expList = expList;
};

ui.NewExpenseWidgetScreen.prototype.areKeysPressed = function (keyList, event) {
  for (var i = 0; i < keyList.length; i++) {
    if (keyList[i] === event.keyCode)
      return true;
  }
  return false;
};

ui.NewExpenseWidgetScreen.prototype.value = function (arg) {
  // If no argument, retrieve value
  if (arg === undefined)
    return this.valueExtractor(this);
  else
    this.valueWriter(this, arg);
};

ui.NewExpenseWidgetScreen.prototype.offsetTop = function () {
  return this.$root.position().top;
};

ui.NewExpenseWidgetScreen.prototype.height = function () {
  return this.$root.outerHeight();
};

ui.NewExpenseWidgetScreen.prototype.focus = function (autoscroll) {
  this.$allInputs.prop("disabled", false);
  this.$root.addClass("focus");
  // This exists because of really weird scrolling interactions on mobile web, ideally we would
  // always focus the next input.
  if (autoscroll)
    this.focusInput();
};

ui.NewExpenseWidgetScreen.prototype.unfocus = function () {
  this.$allInputs.prop("disabled", true);
  this.$root.removeClass("focus");
};

ui.NewExpenseWidgetScreen.prototype.focusInput = function () {
  this.$firstInput.focus();
};

ui.NewExpenseWidgetScreen.prototype.clear = function () {
  this.valueClear(this);
};

ui.NewExpenseWidgetScreen.prototype.serialize = function (json) {
  return this.valueSerializer(this, json);
};

/**
 * Callback on when the current screen is finished, whether by submitting the form or advancing to
 * the next screen.
 */
ui.NewExpenseWidgetScreen.prototype.onFinish = function () { };

/**
 * Callback on when this screen becomes active.
 */
ui.NewExpenseWidgetScreen.prototype.onStart = function () { };

/**
 * Callback on when the screen is submitted.
 */
ui.NewExpenseWidgetScreen.prototype.onSubmit = function () { };
