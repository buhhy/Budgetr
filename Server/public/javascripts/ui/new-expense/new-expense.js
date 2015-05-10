/**
 * @author tlei (Terence Lei)
 */

/**
 * UI widget for the new expense form stack.
 * @param $root
 * @param currentExpenseList
 * @param eventHooks
 * @constructor
 */
ui.NewExpenseWidget = function ($root, eventHooks, currentExpenseList) {
  eventHooks = eventHooks || {};

  var self = this;
  this.$root = $root;
  this.orderedScreenList = [];
  this.currentScreenIndex = -1;
  this.$indicatorBar = $root.find(utils.idSelector("indicatorBar"));
  this.$questionGroup = $root.find(utils.idSelector("questionGroup"));
  this.$submitButton = $root.find(utils.idSelector("submitButton"));
  this.$scrollContainer = $("body,html");  // Firefox has scrollbar on body, chrome on body
  this.indicatorList = this.$indicatorBar.find(utils.idSelector("indicator")).toArray();
  this.currentExpenseList = currentExpenseList;

  var screenMetadatum = [{
    id: "questionBusiness",
    clazz: ui.NewExpenseWidgetScreen,
    handlers: {
      valueSerializer: function (screen, json) {
        json.location = screen.value();
        return json;
      }
    }
  }, {
    id: "questionCategory",
    clazz: ui.NewExpenseWidgetCategoryScreen
  }, {
    id: "questionCost",
    clazz: ui.NewExpenseWidgetCostScreen
  }, {
    id: "questionItems",
    clazz: ui.NewExpenseWidgetItemsScreen
  }];

  // Select the orderedScreenList and populate the orderedScreenList list
  for (var i = 0; i < screenMetadatum.length; i++) {
    var data = screenMetadatum[i];
    this.orderedScreenList[i] = new data.clazz(
        $root.find(utils.idSelector(data.id)),
        {
          next: function () { self.nextScreen(true); },
          submit: function () { self.submit(); }
        }, data.handlers);
    this.orderedScreenList[i].unfocus();
  }

  for (var j = 0; j < this.indicatorList.length; j++)
    this.indicatorList[j] = $(this.indicatorList[j]);

  $root.find(utils.idSelector("cancelButton")).click(function (event) {
    event.preventDefault();
    if (eventHooks.cancelButtonClick)
      eventHooks.cancelButtonClick();
  });

  $root.find(utils.idSelector("dismissTip")).click(function (event) {
    event.preventDefault();
    $(this).addClass("hidden");
  });

  this.$submitButton.click(function (event) {
    if (self.currentScreenIndex === self.orderedScreenList.length - 1)
      self.nextScreen();
  });

  // Attach scroll handler for switching screens
  $(window).scroll(function (event) {
    if (!self.$root.hasClass("hidden")) {
      // If the user scrolls more than a certain amount outside of the current screen, then switch
      // the screen.
      var curScreen = self.currentScreen();
      if (curScreen) {
        var scrollOffset = self.$questionGroup.offset().top;
        var scrollTop = $(window).scrollTop();
        var scrollBottom = scrollTop + $(window).outerHeight();

        // For each screen, extract its top offset
        var topValues = _.map(self.orderedScreenList, function (screen) {
          return screen.offsetTop();
        });

        // Convert list of offsets to distance between top screen and top scroll, filter out
        // negatives offsets
        var distances = _.filter(
            _.map(topValues, function (offset, index) {
              return { index: index, value: offset - scrollTop };
            }),
            function (group) {
              return group.value >= -1; // Account for rounding errors
            });

        // Take the closest positive top distance
        var sorted = distances.sort(function (a, b) { return a.value - b.value; });
        self.switchScreen(sorted[0].index);
      }
    }
  });

  this.switchScreen(0);
};

ui.NewExpenseWidget.prototype.setExpenseList = function (expList) {
  this.currentExpenseList = expList;
  for (var i = 0; i < this.orderedScreenList.length; i++)
    this.orderedScreenList[i].setExpenseList(expList);
};

ui.NewExpenseWidget.prototype.nextScreen = function (shouldScroll) {
  this.switchScreen(this.currentScreenIndex + 1, shouldScroll);
};

ui.NewExpenseWidget.prototype.previousScreen = function (shouldScroll) {
  this.switchScreen(this.currentScreenIndex - 1, shouldScroll);
};

ui.NewExpenseWidget.prototype.switchScreen = function (index, shouldScroll) {
  // If the user switches to a screen past the last screen, then submit
  if (index === this.orderedScreenList.length)
    this.submit();

  if (index !== this.currentScreenIndex && index < this.orderedScreenList.length && index >= 0) {
    var self = this;
    if (this.currentScreen())
      this.currentScreen().unfocus();
    this.currentScreenIndex = index;
    this.currentScreen().focus();
    if (shouldScroll) {
      this.$scrollContainer.animate(
          { scrollTop: this.currentScreen().offsetTop() }, { duration: 400 });
    }
    // Submit button is only enabled on the last screen
    if (index < this.orderedScreenList.length - 1)
      this.$submitButton.addClass("disabled");
    else
      this.$submitButton.removeClass("disabled");
    this.updateIndicator();
  }
  this.currentScreen().focusInput();
};

ui.NewExpenseWidget.prototype.updateIndicator = function () {
  for (var i = 0; i < this.indicatorList.length; i++)
    this.indicatorList[i].removeClass("current-prog");
  this.indicatorList[this.currentScreenIndex].addClass("current-prog");
};

ui.NewExpenseWidget.prototype.currentScreen = function () {
  return this.orderedScreenList[this.currentScreenIndex];
};

ui.NewExpenseWidget.prototype.show = function () {
  this.$root.removeClass("hidden");
  this.switchScreen(0);
};

ui.NewExpenseWidget.prototype.hide = function () {
  this.$root.addClass("hidden");
};

ui.NewExpenseWidget.prototype.reset = function () {
  for (var i = 0; i < this.orderedScreenList.length; i++)
    this.orderedScreenList[i].clear();
  this.switchScreen(0);
};

ui.NewExpenseWidget.prototype.submit = function () {
  // Only allow submit on the last screen
  if (this.currentScreenIndex !== this.orderedScreenList.length - 1)
    return;

  var json = {};
  for (var i = 0; i < this.orderedScreenList.length; i++)
    this.orderedScreenList[i].serialize(json);

  // Save and get the category ID
  // TODO(tlei): use the cached categories with IDs instead of making an ajax request
  $.ajax({
    type: "post",
    url: "/api/category",
    data: JSON.stringify({
      "value": {
        "name": json.categoryText,
        "parentListId": this.currentExpenseList.expenseListId
      }
    }),
    success: (function (data) {
      json.parentListId = this.currentExpenseList.expenseListId;
      json.categoryId = data.expenseCategoryId;

      console.log(json);

      $.ajax({
        type: "post",
        url: "/api/expense",
        data: JSON.stringify({
          value: json
        }),
        contentType: "application/json",
        success: function () {
          location.reload();
        }
      });
    }).bind(this),
    contentType: "application/json"
  });
};
