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
          this, $root, eventHooks,
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
              screen.$itemInput.typeahead("val", "");
            },
            valueSerializer: function (screen, json) {
              json.description = utils.join(screen.value(), ',');
              return json;
            }
          }));
      this.delimiterKeys = [188, 13];
      this.deleteKeys = [8, 127];
      this.$itemInput = this.$root.find(utils.idSelector("itemInput"));
      this.$itemList = this.$root.find(utils.idSelector("itemList"));

      this.$itemInput.on("keyup", function (event) {
        // Ctrl + enter will submit
        if (event.keyCode === 13 && event.ctrlKey) {
          if (self.eventHooks.submit)
            self.eventHooks.submit();
        }


        if (self.areKeysPressed(self.delimiterKeys, event)) {
          // Add a new item when the delimiter is pressed
          event.preventDefault();
          var value = self.$itemInput.typeahead("val");
          // Remove the last character if it is equal to the key pressed, this is to differentiate
          // between enter presses vs comma presses.
          // TODO(tlei): make this less hacky
          if (event.keyCode === 188)
            value = value.substr(0, value.length - 1);
          self.addNewItem(value.trim());
        } else if (self.areKeysPressed(self.deleteKeys, event)) {
          // If the input field is empty, then delete the last item inserted
          if (!self.$itemInput.typeahead("val")) {
            event.preventDefault();
            self.$itemList.children().eq(0).detach();
          }
        }
      });

      this.$itemInput.typeahead({
        autoselect: true,
        hint: true,
        highlight: true,
        minLength: 1
      }, {
        name: "items",
        source: function (q, cb) {
          cb(utils.searchThroughArray(q, self.expList.allExpenseDescriptionItems));
        }
      });
    });

ui.NewExpenseWidgetItemsScreen.prototype.addNewItem = function (value) {
  if (value) {
    var $newElem = $("<li></li>").text(value).attr("data-id", "item");
    this.$itemList.prepend($newElem);
    this.$itemInput.typeahead("val", "");
    this.$itemInput.typeahead("close");
  }
};

ui.NewExpenseWidgetItemsScreen.prototype.onSubmit = function () {
  this.addNewItem(this.$itemInput.typeahead("val"));
};

ui.NewExpenseWidgetItemsScreen.prototype.onFinish = function () {
  this.$itemInput.typeahead("close");
};
