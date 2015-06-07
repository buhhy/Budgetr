/**
 * @author tlei (Terence Lei)
 */

/**
 * Expense creation input screen for the category input.
 */
ui.NewExpenseWidgetCategoryScreen = ui.NewExpenseWidgetScreen.extend(
    function ($root, eventHooks, valueHandlers) {
      var self = this;
      this.super.constructor.call(this, $root, eventHooks,
          $.extend({}, valueHandlers, {
            valueSerializer: function (screen, json) {
              json.categoryText = screen.value();
              return json;
            }
          }));

      this.$firstInput.typeahead({
        autoselect: true,
        hint: true,
        highlight: true,
        minLength: 1
      }, {
        name: "categories",
        source: function (q, cb) {
          if (self.expList) {
            var cats = _.map(self.expList.categories, function (category) {
              return category.name;
            });
            cb(utils.searchThroughArray(q, cats));
          } else {
            cb([]);
          }
        }
      });
    });

ui.NewExpenseWidgetCategoryScreen.prototype.onFinish = function () {
  this.$firstInput.typeahead("close");
};