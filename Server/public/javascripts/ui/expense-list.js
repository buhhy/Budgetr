/**
 * @author tlei (Terence Lei)
 */

ui.ExpenseListWidget = function($root, eventHooks) {
  eventHooks = eventHooks || {};
  this.$root = $root;
  this.$expenseList = $root.find(utils.idSelector("expenseList"));
  this.$newExpenseButton = $root.find(utils.idSelector("newExpenseButton"));
  this.currentExpenseList = undefined;

  this.$newExpenseButton.on("click", (function (event) {
    event.preventDefault();
    if (this.currentExpenseList !== undefined && eventHooks.newExpenseButtonClick) {
      eventHooks.newExpenseButtonClick(event);
    }
  }).bind(this));
  this.$newExpenseButton.addClass("disabled");
};

ui.ExpenseListWidget.prototype.setExpenseList = function (expenseList) {
  this.currentExpenseList = expenseList;

  var activeDate;
  var activeExpenseUl;

  var expenses = expenseList.expenses;

  /**
   * Builds each expense item li element.
   */
  var createLi = function (name, cls) {
    return $("<div></div>").addClass(cls).append($("<span></span>").html(name));
  };

  /**
   * Converts the list of items into a readable string.
   */
  var itemListString = function (items) {
    var str = "";
    for (var i = 0; i < items.length; i++) {
      str += items[i];
      if (i < items.length - 1)
        str += ", ";
    }
    return str;
  };

  var columns = [
    { title: "Business", class: "business" }, { title: "Description", class: "items" },
    { title: "Category", class: "category" }, { title: "Cost", class: "cost" }
  ];

  for (var i = expenses.length - 1; i >= 0; i--) {
    var currentExpense = expenses[i];
    var currentConvertedDate = utils.convertTime(currentExpense.createDate);

    if (currentConvertedDate !== activeDate) {
      // Create a header for a new day
      activeDate = utils.convertTime(currentExpense.createDate);
      activeExpenseUl = $("<ul class='expense-list'></ul>");

      var newDayContainerEl = $("<div class='day-log'></div>");
      var newDateHeading = $("<h2 class='date-heading'>" + activeDate + "</h2>");
      var tableHeadings = $("<li class='table-headings'></li>");
      var tableColumn = $("<div class='table-column'></div>");

      // Add all table headings
      for (var k = 0; k < columns.length; k++) {
        $("<div></div>").append(
            $("<span></span>").html(columns[k].title))
            .addClass("table-column")
            .addClass(columns[k].class)
            .appendTo(tableHeadings);
      }

      activeExpenseUl.append(tableHeadings);

      newDayContainerEl.append(newDateHeading);
      newDayContainerEl.append(activeExpenseUl);

      // Add the new element to the document DOM tree
      this.$expenseList.append(newDayContainerEl);
    }

    var expenseLi = $("<li class='expense'></li>");
    expenseLi.append(createLi(currentExpense.location, "business"));
    expenseLi.append(createLi(itemListString(currentExpense.items), "items"));
    expenseLi.append(createLi(currentExpense.category, "category"));
    expenseLi.append(createLi(parseFloat(currentExpense.cost / 100).toFixed(2), "cost"));

    activeExpenseUl.append(expenseLi);
  }

  // Enable the new expense button
  this.$newExpenseButton.removeClass("disabled");
};

ui.ExpenseListWidget.prototype.show = function () {
  this.$root.removeClass("hidden");
};

ui.ExpenseListWidget.prototype.hide = function () {
  this.$root.addClass("hidden");
};
