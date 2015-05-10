/**
 * @author tlei (Terence Lei)
 */

/**
 * Expense creation input screen for the items list data.
 */
ui.NewExpenseWidgetItemsScreen = ui.NewExpenseWidgetScreen.extend(
    function ($root, eventHooks, valueHandlers) {
      var self = this;
      this.super.constructor.call(
          this, $root,
          $.extend({}, eventHooks, {
            next: function (screen) {
              self.addNewItem(self.$itemInput.val());
              if (eventHooks.next)
                eventHooks.next()
            }
          }),
          $.extend({}, valueHandlers, {
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
      this.delimiterKeys = [188];
      this.deleteKeys = [8, 127];
      this.$itemInput = this.$root.find(utils.idSelector("itemInput"));
      this.$itemList = this.$root.find(utils.idSelector("itemList"));

      this.$itemInput.on("keyup", function (event) {
        if (self.areKeysPressed(self.delimiterKeys, event)) {
          // Add a new item when the delimiter is pressed
          event.preventDefault();
          // Remove the last character
          var value = self.$itemInput.val();
          self.addNewItem(value.substr(0, value.length - 1).trim());
        } else if (self.areKeysPressed(self.deleteKeys, event)) {
          // If the input field is empty, then delete the last item inserted
          if (!self.$itemInput.val()) {
            event.preventDefault();
            self.$itemList.children().eq(0).detach();
          }
        }
      });
    });

ui.NewExpenseWidgetItemsScreen.prototype.addNewItem = function (value) {
  if (value) {
    var $newElem = $("<li></li>").text(value).attr("data-id", "item");
    this.$itemList.prepend($newElem);
    this.$itemInput.val("");
  }
};
