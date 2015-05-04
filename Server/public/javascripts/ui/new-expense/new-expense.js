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

  this.$root = $root;
  this.orderedScreenList = [];
  this.currentScreenIndex = -1;
  this.$indicatorBar = $root.find(utils.idSelector("indicatorBar"));
  this.$questionGroup = $root.find(utils.idSelector("questionGroup"));
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
    this.orderedScreenList[i] =
        new data.clazz(
            $root.find(utils.idSelector(data.id)),
            {
              next: this.nextScreen.bind(this),
              submit: this.submit.bind(this)
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

  this.switchScreen(0);
};

ui.NewExpenseWidget.prototype.setExpenseList = function (expList) {
  this.currentExpenseList = expList;
  for (var i = 0; i < this.orderedScreenList.length; i++)
    this.orderedScreenList[i].setExpenseList(expList);
};

ui.NewExpenseWidget.prototype.nextScreen = function () {
  this.switchScreen(this.currentScreenIndex + 1);
};

ui.NewExpenseWidget.prototype.switchScreen = function (index) {
  if (index !== this.currentScreenIndex && index < this.orderedScreenList.length && index >= 0) {
    if (this.currentScreen())
      this.currentScreen().unfocus();
    this.currentScreenIndex = index;
    this.currentScreen().focus();
    this.$scrollContainer.animate({
      scrollTop: this.currentScreen().offsetTop()
    }, 400);
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
