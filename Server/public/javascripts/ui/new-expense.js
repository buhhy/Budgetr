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
          valueHandlers.valueExtractor || function (screen) { return screen.$firstInput.val(); };
      this.valueSerializer =
          valueHandlers.valueSerializer || function (screen, json) { return json; };
      this.valueWriter =
          valueHandlers.valueWriter || function (screen, value) { screen.$firstInput.val(value) };
      this.valueClear =
          valueHandlers.valueClear || function (screen) { screen.$firstInput.val("") };

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

ui.NewExpenseWidgetScreen.prototype.setExpenseList = function (expList) {};

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
    return this.valueExtractor(this);
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
  return this.valueSerializer(this, json);
};


/**
 * Expense creation input screen for the items list data.
 */
ui.NewExpenseWidgetItemsScreen = ui.NewExpenseWidgetScreen.extend(
    function ($root, eventHooks, valueHandlers) {
      this.super.constructor.call(
          this, $root, eventHooks, $.extend({}, valueHandlers, {
            valueExtractor: function (screen) {
              return screen.$itemList
                  .children(utils.idSelector("item"))
                  .map(function () { return $(this).text(); })
                  .toArray()
                  .reverse();
            },
            valueClear: function (screen) {
              screen.$itemList.children().detach();
              screen.$itemInput.val("");
            },
            valueSerializer: function (screen, json) {
              json.description = utils.join(screen.value(), ',');
              return json;
            }
          }));
      this.delimiterKeys = [32, 188];
      this.deleteKeys = [8, 127];
      this.$itemInput = this.$root.find(utils.idSelector("itemInput"));
      this.$itemList = this.$root.find(utils.idSelector("itemList"));

      this.$itemInput.on("keyup", (function (event) {
        if (this.areKeysPressed(this.delimiterKeys, event)) {
          // Add a new item when the delimiter is pressed
          event.preventDefault();
          var value = this.$itemInput.val();
          // Remove the last character
          value = value.substr(0, value.length - 1).trim();

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
 * Expense creation input screen for the expenses and cost structure data.
 * @type {void|*}
 */
ui.NewExpenseWidgetCostScreen = ui.NewExpenseWidgetScreen.extend(
    function ($root, eventHooks, valueHandlers) {
      var extendedValueHandlers = $.extend({}, valueHandlers, {
        valueSerializer: function (screen, json) {
          var value = screen.value();
          // Amount needs to be converted to cents
          json.amount = Math.round(value.cost * 100);
          // Add each participant's spending and responsibility
          json.participants = value.members;
          return json;
        },
        valueExtractor: function (screen) {
          return {
            cost: utils.parseFloatDefault(screen.$firstInput.val(), 0),
            members: screen.$costStructureContainer
                .find(utils.idSelector("csRow"))
                .map(function () {
                  return {
                    userId: parseInt($(this).attr("data-user-id")),
                    paidAmount:
                        utils.parseFloatDefault(
                            $(this).find(utils.idSelector("csSpentInput")).val(), 0),
                    responsibleAmount:
                        utils.parseFloatDefault(
                            $(this).find(utils.idSelector("csResponsibleInput")).val(), 0)
                  }
                })
                .toArray()
          };
        }
      });

      this.super.constructor.call(this, $root, eventHooks, extendedValueHandlers);
      this.$costStructureContainer = this.$root.find(utils.idSelector("costStructureContainer"));
    });

ui.NewExpenseWidgetCostScreen.prototype.setExpenseList = function (expList) {
  // Create the list of users
  // TODO(tlei): selectively add and remove instead of doing bulk delete, then re-add
  this.$costStructureContainer.empty();

  var numMem = expList.members.length;
  var self = this;

  var findOtherElems = function ($elem, dataId) {
    var index = $elem.attr("data-index");
    // Find other inputs that aren't $elem
    return self.$root
        .find(utils.idSelector(dataId))
        .filter(function () {
          return $(this).attr("data-index") !== index;
        })
  };

  for (var i = 0; i < numMem; i++) {
    var mem = expList.members[i];
    this.$costStructureContainer.append(
        $("<li></li>")
            .addClass("cs-row")
            .attr("data-user-id", mem.userId)
            .attr("data-id", "csRow")
            .append(
                $("<span></span>")
                    .addClass("cs-column")
                    .addClass("cs-wide")
                    .append($("<span></span>").text(mem.firstName)))
            .append(
                $("<span></span>")
                    .addClass("cs-column")
                    .append(
                        $("<input/>")
                            .addClass("cs-input")
                            .attr("type", "number")
                            .attr("data-index", i)
                            .attr("data-id", "csSpentInput")
                            .blur(function () {
                              self.normalizeInputFields(
                                  $(this),
                                  findOtherElems($(this), "csSpentInput"),
                                  utils.parseFloatDefault(self.$firstInput.val(), 0));
                            })
                            .val(0)))
            .append(
                $("<span></span>")
                    .addClass("cs-column")
                    .append(
                        $("<input/>")
                            .addClass("cs-input")
                            .attr("type", "number")
                            .attr("data-index", i)
                            .attr("data-id", "csResponsibleInput")
                            .blur(function () {
                              self.normalizeInputFields(
                                  $(this),
                                  findOtherElems($(this), "csResponsibleInput"),
                                  100);
                            })
                            .val(100.0 / numMem))
                    .append("%")));
  }
};

ui.NewExpenseWidgetCostScreen.prototype.normalizeInputFields =
    function ($elem, $otherInputs, expectedValue) {
      var value = utils.parseFloatDefault($elem.val(), -1);

      // Check for invalid inputs
      if (value < 0 || value > expectedValue) {
        value = expectedValue;
        $elem.val(value);
      }

      // Find sum of other inputs
      var sum = _.reduce(
          $otherInputs.map(function () {
            return parseFloat($(this).val())
          }).toArray(),
          function (memo, num) {
            return memo + num;
          }, 0);

      var expectedSum = expectedValue - value;
      var normalizeFn;

      if (utils.eqE(sum, 0)) {
        var val = expectedSum / $otherInputs.size();
        normalizeFn = function () {
          $(this).val(val);
        };
      } else {
        var mult = expectedSum / sum;
        normalizeFn = function () {
          var $this = $(this);
          $this.val(utils.parseFloatDefault($this.val(), 0) * mult);
        };
      }
      // Normalize values
      $otherInputs.each(normalizeFn);
    };




/**
 * UI widget for the new expense form stack.
 * @param $root
 * @param currentExpenseList
 * @param eventHooks
 * @constructor
 */
ui.NewExpenseWidget = function ($root, currentExpenseList, eventHooks) {
  eventHooks = eventHooks || {};

  this.$root = $root;
  this.orderedScreenList = [];
  this.currentScreenIndex = 0;
  this.$indicatorBar = $root.find(utils.idSelector("indicatorBar"));
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
    clazz: ui.NewExpenseWidgetScreen,
    handlers: {
      valueSerializer: function (screen, json) {
        json.categoryText = screen.value();
        return json;
      }
    }
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

ui.NewExpenseWidget.prototype.setExpenseList = function (expList) {
  this.currentExpenseList = expList;
  for (var i = 0; i < this.orderedScreenList.length; i++)
    this.orderedScreenList[i].setExpenseList(expList);
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
          //location.reload();
        }
      });
    }).bind(this),
    contentType: "application/json"
  });
};
