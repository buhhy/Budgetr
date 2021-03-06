/**
 * @author tlei (Terence Lei)
 */

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
                .find(utils.idSelector(screen.dataIds.row))
                .map(function () {
                  return {
                    userId: parseInt($(this).attr("data-user-id")),
                    paidAmount:
                        utils.parseFloatDefault(
                            $(this).find(utils.idSelector(screen.dataIds.spentInput)).val(), 0),
                    responsibleAmount:
                        utils.parseFloatDefault(
                            $(this).find(utils.idSelector(
                                screen.dataIds.responsibleInput)).val(), 0)
                  }
                })
                .toArray()
          };
        },
        valueClear: function (screen) {
          screen.areDefaultsSet = false;
          self.$firstInput.val("");
          self.$root.find(utils.idSelector(screen.dataIds.spentInput)).val("");
        }
      });

      this.super.constructor.call(this, $root, eventHooks, extendedValueHandlers);
      this.$costStructureContainer = this.$root.find(utils.idSelector("costStructureContainer"));
      this.areDefaultsSet = false;
      this.dataIds = {
        spentInput: "csSpentInput",
        responsibleInput: "csResponsibleInput",
        row: "csRow"
      };
      var self = this;

      this.$firstInput.blur(function () {
        var value = parseFloat($(this).val());
        if (isNaN(value))
          value = null;
        self.setDefaultSpentInputs(
            self.$root.find(utils.idSelector(self.dataIds.row)), value, userId);
        self.normalizeInputFields(
            $(), self.$root.find(utils.idSelector(self.dataIds.spentInput)), value);
        self.normalizeInputFields(
            $(), self.$root.find(utils.idSelector(self.dataIds.responsibleInput)), 100);
      });
    });

ui.NewExpenseWidgetCostScreen.prototype.setExpenseList = function (expList) {
  // Create the list of users
  // TODO(tlei): selectively add and remove instead of doing bulk delete, then re-add
  this.$costStructureContainer.empty();
  this.areDefaultsSet = false;

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
                    .attr("data-id", this.dataIds.spentInput)
                    .attr("placeholder", 0)
                    .on("keyup", function (event) {
                      // Enter key pressed
                      if (event.keyCode === 13 && self.eventHooks.next)
                        self.eventHooks.next(this);
                    })
                    .blur(function () {
                      self.normalizeInputFields(
                          $(this),
                          findOtherElems($(this), self.dataIds.spentInput),
                          utils.parseFloatDefault(self.$firstInput.val(), 0));
                    })))
            .append(
            $("<span></span>")
                .addClass("cs-column")
                .append(
                $("<input/>")
                    .addClass("cs-input")
                    .attr("type", "number")
                    .attr("data-index", i)
                    .attr("data-id", this.dataIds.responsibleInput)
                    .on("keyup", function (event) {
                      // Enter key pressed
                      if (event.keyCode === 13 && self.eventHooks.next)
                        self.eventHooks.next(this);
                    })
                    .blur(function () {
                      self.normalizeInputFields(
                          $(this),
                          findOtherElems($(this), self.dataIds.responsibleInput),
                          100);
                    })
                    .val(100.0 / numMem))
                .append("%")));
  }
};

ui.NewExpenseWidgetCostScreen.prototype.setDefaultSpentInputs =
    function ($rows, expectedValue, userId) {
      if (!this.areDefaultsSet && expectedValue !== null) {
        var self = this;
        $rows.each(function () {
          var $input = $(this).find(utils.idSelector(self.dataIds.spentInput));
          if (parseInt($(this).attr("data-user-id")) === userId)
            $input.val(expectedValue);
          else
            $input.val(0);
        });
        this.areDefaultsSet = true;
      }
    };

ui.NewExpenseWidgetCostScreen.prototype.normalizeInputFields =
    function ($elem, $otherInputs, expectedValue) {
      var value = 0;

      // If no input element is specified, then assume the default value is 0
      if ($elem.size() > 0)
        value = utils.parseFloatDefault($elem.val(), -1);

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
