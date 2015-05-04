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
        hint: true,
        highlight: true,
        minLength: 1
      }, {
        name: "categories",
        source: function (q, cb) {
          // regex used to determine if a string contains the substring `q`
          var substrRegex = new RegExp(q, 'i');

          var categories = [];
          if (self.expList)
            categories = self.expList.categories;

          // iterate through the pool of strings and filter out any strings that  do not contain the
          // substring `q`
          var matches = _.map(_.filter(categories, function (category) {
            return substrRegex.test(category.name);
          }), function (category) {
            return category.name;
          });

          cb(matches);
        }
      });
    });

ui.NewExpenseWidgetCategoryScreen.prototype.setExpenseList = function (expList) {
  this.expList = expList;
};