/**
 * @author tlei (Terence Lei)
 */

/**
 * Expense creation input screen for the business input.
 */
ui.NewExpenseWidgetLocationScreen = ui.NewExpenseWidgetScreen.extend(
    function ($root, eventHooks, valueHandlers) {
      var self = this;
      this.super.constructor.call(this, $root, eventHooks,
          $.extend({}, valueHandlers, {
            valueSerializer: function (screen, json) {
              json.location = screen.value();
              return json;
            }
          }));

      this.$firstInput.typeahead({
        hint: true,
        highlight: true,
        minLength: 1
      }, {
        name: "location",
        source: function (q, cb) {
          cb(utils.searchThroughArray(q, self.expList.allExpenseLocations));
        }
      });
    });
