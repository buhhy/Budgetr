/**
 * Created by Terry Lei on 5/4/2015.
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
      eventHooks = eventHooks || {};
      valueHandlers = valueHandlers || {};

      this.valueExtractor =
          valueHandlers.valueExtractor || function () { return this.$firstInput.val(); };
      this.valueSerializer =
          valueHandlers.valueSerializer || function (json) { return json; };
      this.valueWriter =
          valueHandlers.valueWriter || function (value) { this.$firstInput.val(value) };
      this.valueClear =
          valueHandlers.valueClear || function () { this.$firstInput.val("") };

      this.$root = $root;
      this.$firstInput = $root.find("[data-first-input]");
      this.$lastInput = $root.find("[data-last-input]");
      this.$submitInput = $root.find("[data-submit-input]");
      this.$submitButton = $root.find("[data-submit-button]");

      // Assign the next screen keypress handlers
      this.$lastInput.on("keypress", function (event) {
        // Enter key pressed
        if (event.keyCode === 13 && eventHooks.next)
          eventHooks.next();
      });

      // Assign the submit handlers if any
      this.$submitInput.on("keypress", (function (event) {
        // Enter key pressed
        if (event.keyCode === 13 && eventHooks.submit)
          eventHooks.submit(this);
      }).bind(this));

      this.$submitButton.on("click", (function (event) {
        event.preventDefault();
        if (eventHooks.submit)
          eventHooks.submit(this);
      }).bind(this));
    });

ui.NewExpenseWidgetScreen.prototype.areKeysPressed = function (keyList, event) {
  for (var i = 0; i < keyList.length; i++) {
    if (keyList[i] === event.keyCode)
      return true;
  }
  return false;
};

ui.NewExpenseWidgetScreen.prototype.show = function () {
  this.$root.removeClass('hidden');
  this.focus();
};

ui.NewExpenseWidgetScreen.prototype.hide = function () {
  this.$root.addClass('hidden');
};

ui.NewExpenseWidgetScreen.prototype.value = function (arg) {
  // If no argument, retrieve value
  if (arg === undefined)
    return this.valueExtractor();
  else
    this.valueWriter(this, arg);
};

ui.NewExpenseWidgetScreen.prototype.focus = function () {
  this.$firstInput.focus();
};

ui.NewExpenseWidgetScreen.prototype.clear = function () {
  this.valueClear(this);
};

ui.NewExpenseWidgetScreen.prototype.serialize = function (json) {
  return this.valueSerializer(json);
};



/**
 *
 */
ui.NewExpenseWidgetItemsScreen = ui.NewExpenseWidgetScreen.extend(
    function ($root, eventHooks, valueHandlers) {
      this.super.constructor.call(
          this, $root, eventHooks, $.extend({}, valueHandlers, {
            valueExtractor: function () {
              return this.$itemList
                  .children(utils.idSelector("item"))
                  .map(function () { return $(this).text(); })
                  .reverse();
            },
            valueClear: function () {
              this.$itemList.children().detach();
              this.$itemInput.val("");
            },
            valueSerializer: function (json) {
              json.description = utils.join(this.value(), ',');
              return json;
            }
          }));
      this.delimiterKeys = [44, 32];
      this.deleteKeys = [8, 127];
      this.$itemInput = this.$root.find(utils.idSelector("itemInput"));
      this.$itemList = this.$root.find(utils.idSelector("itemList"));

      this.$itemInput.on("keyup", (function (event) {
        if (this.areKeysPressed(this.delimiterKeys, event)) {
          // Add a new item when the delimiter is pressed
          event.preventDefault();
          var value = this.$itemInput.val().trim();
          if (value) {
            var $newElem = $("<li></li>").text(value).attr("data-id", "item");
            this.$itemList.prepend($newElem);
            this.$itemInput.val("");
          }
        } else if (this.areKeysPressed(this.deleteKeys, event)) {
          // If the input field is empty, then delete the last item inserted
          if (!this.$itemInput.val()) {
            event.preventDefault();
            this.$itemList.children().eq(0).detach();
          }
        }
      }).bind(this));
    });



/**
 * UI widget for the new expense form stack.
 * @param $root
 * @param eventHooks
 * @constructor
 */
ui.NewExpenseWidget = function ($root, eventHooks) {
  eventHooks = eventHooks || {};

  this.$root = $root;
  this.orderedScreenList = [];
  this.currentScreenIndex = 0;
  this.$indicatorBar = $root.find(utils.idSelector("indicatorBar"));
  this.indicatorList = this.$indicatorBar.find(utils.idSelector("indicator")).toArray();

  var screenIds = [{
    id: "questionBusiness",
    clazz: ui.NewExpenseWidgetScreen,
    handlers: {
      valueSerializer: function (json) {
        json.location = this.value();
        return json;
      }
    }
  }, {
    id: "questionCategory",
    clazz: ui.NewExpenseWidgetScreen,
    handlers: {
      valueSerializer: function (json) {
        json.categoryText = this.value();
        return json;
      }
    }
  }, {
    id: "questionCost",
    clazz: ui.NewExpenseWidgetScreen,
    handlers: {
      valueSerializer: function (json) {
        json.amount = this.value();
        return json;
      },
      valueExtractor: function () {
        return parseInt(this.$firstInput.val()) || 0;
      }
    }
  }, {
    id: "questionItems",
    clazz: ui.NewExpenseWidgetItemsScreen
  }];

  // Select the orderedScreenList and populate the orderedScreenList list
  for (var i = 0; i < screenIds.length; i++) {
    var data = screenIds[i];
    this.orderedScreenList[i] =
        new data.clazz(
            $root.find(utils.idSelector(data.id)),
            {
              next: this.nextScreen.bind(this),
              submit: this.submit.bind(this)
            }, data.handlers);
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

  this.updateIndicator();
};

ui.NewExpenseWidget.prototype.nextScreen = function () {
  this.switchScreen(this.currentScreenIndex + 1);
};

ui.NewExpenseWidget.prototype.switchScreen = function (index) {
  if (index !== this.currentScreenIndex && index < this.orderedScreenList.length) {
    this.currentScreen().hide();
    this.currentScreenIndex = index;
    this.currentScreen().show();
    this.updateIndicator();
  }
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
  this.currentScreen().focus();
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
  for (var i = 0; i < this.orderedScreenList.length; i++) {
    this.orderedScreenList[i].serialize(json);
  }
  console.log(json);

  //$.ajax({
  //  type: "post",
  //  url: "/api/category",
  //  data: JSON.stringify({
  //    "value": {
  //      "name": newExpense.category,
  //      "parentListId": 1
  //    }
  //  }),
  //  success: function (data) {
  //    $.ajax({
  //      type: "post",
  //      url: "/api/expense",
  //      data: JSON.stringify({
  //        value: {
  //          location: newExpense.location,
  //          description: itemDescription,
  //          categoryId: data.expenseCategoryId,
  //          amount: newExpense.cost,
  //          parentListId: 1,
  //          participants: []
  //        }
  //      }),
  //      contentType: "application/json",
  //      success: function () {
  //        location.reload();
  //      }
  //    });
  //  },
  //  contentType: "application/json"
  //});
};







// Reset expense form
ui.NewExpenseWidget.prototype.resetExpenseForm = function () {
  this.busNameInputEl.val('');
  this.catInputEl.val('');
  this.costInputEl.val('');
  this.itemsInputEl.val('');
  this.itemListEl.html('');
  this.itemsQ.addClass('hidden');
  this.busQ.removeClass('hidden');
  this.expenseFormEl.addClass('hidden');
  this.expenseLogEl.removeClass('hidden');
  $("#prog1").removeClass('current-prog');
  $("#prog2").removeClass('current-prog');
  $("#prog3").removeClass('current-prog');
  $("#prog4").removeClass('current-prog');
};

ui.NewExpenseWidget.prototype.expensePOST = function () {
  var itemDescription = "";
  for (var j = 0; j < newExpense.items.length; j++) {
    itemDescription += newExpense.items[j];

    if (j < newExpense.items.length - 1)
      itemDescription += ", ";
  }
  console.log(itemDescription);

  $.ajax({
    type: "post",
    url: "/api/category",
    data: JSON.stringify({
      "value": {
        "name": newExpense.category,
        "parentListId": 1
      }
    }),
    success: function (data) {
      $.ajax({
        type: "post",
        url: "/api/expense",
        data: JSON.stringify({
          value: {
            location: newExpense.location,
            description: itemDescription,
            categoryId: data.expenseCategoryId,
            amount: newExpense.cost,
            parentListId: 1,
            participants: []
          }
        }),
        contentType: "application/json",
        success: function () {
          location.reload();
        }
      });
    },
    contentType: "application/json"
  });
};

//ui.NewExpenseWidget.prototype.submitBusName = function (event) {
//  if (event.keyCode === 13) {
//    newExpense.location = $(busNameInputEl).val();
//    busQ.addClass('hidden');
//    catQ.removeClass('hidden');
//    $("#prog2").addClass('current-prog');
//    catInputEl.focus();
//  }
//};
//
//ui.NewExpenseWidget.prototype.submitCat = function (event) {
//  if (event.keyCode === 13) {
//    newExpense.category = catInputEl.val();
//    catQ.addClass('hidden');
//    costQ.removeClass('hidden');
//    $("#prog3").addClass('current-prog');
//    costInputEl.focus();
//  }
//};
//
//ui.NewExpenseWidget.prototype.submitCost = function (event) {
//  if (event.keyCode === 13) {
//    newExpense.cost = Number.parseInt(costInputEl.val())*100;
//    costQ.addClass('hidden');
//    itemsQ.removeClass('hidden');
//    $("#prog4").addClass('current-prog');
//    itemsInputEl.focus();
//  }
//};
//
//ui.NewExpenseWidget.prototype.submitItems = function (event) {
//  if (itemsInputEl.val()) {
//    if (event.keyCode === 13) {
//      newExpense.items.push($(itemsInputEl).val());
//      updateList(itemsInputEl.val());
//      itemsInputEl.val("");
//    }
//  }
//};

ui.NewExpenseWidget.prototype.updateList = function (item) {
  var newLi = $("<li>"+item+"</li>");
  $("#itemList").append(newLi);
  $("#itemsInput").val('');
};
