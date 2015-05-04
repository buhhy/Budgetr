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
      var self = this;

      this.$firstInput.blur(function () {
        var value = $(this).val();
        self.normalizeInputFields(
            $(), self.$root.find(utils.idSelector("csSpentInput")), value);
        self.normalizeInputFields(
            $(), self.$root.find(utils.idSelector("csResponsibleInput")), 100);
      });
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
                    .on("keyup", function (event) {
                      // Enter key pressed
                      if (event.keyCode === 13 && self.eventHooks.next)
                        self.eventHooks.next(this);
                    })
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
                    .on("keyup", function (event) {
                      // Enter key pressed
                      if (event.keyCode === 13 && self.eventHooks.next)
                        self.eventHooks.next(this);
                    })
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
