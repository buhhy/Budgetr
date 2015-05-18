// Dashboard
$(document).ready(function () {
  var newExpenseWidget = new ui.NewExpenseWidget($("#newExpenseForm"), {
    cancelButtonClick: function () {
      newExpenseWidget.hide();
      newExpenseWidget.reset();
      expenseListWidget.show();
    }
  });
  var expenseListWidget = new ui.ExpenseListWidget($("#expenseLog"), {
    newExpenseButtonClick: function () {
      expenseListWidget.hide();
      newExpenseWidget.show();
    }
  });
  api.getExpenseListById(1, function (expList) {
    newExpenseWidget.setExpenseList(expList);
    expenseListWidget.setExpenseList(expList);
  });
});

api.getExpenseListById = function (id, callback) {
  $.get("/api/expenselist/" + id, function (data) {
    callback(api.parseExpenseListJson(data));
  });
};

api.parseExpenseListJson = function (json) {
  var categories = {};
  var expenses = [];

  for (var i = 0; i < json.categories.length; i++) {
    var cat = json.categories[i];
    categories[cat.expenseCategoryId] = cat.name;
  }

  for (var j = 0; j < json.expenses.length; j++) {
    var exp = json.expenses[j];
    expenses.push(
        new models.Expense(
            exp.location, categories[exp.categoryId], exp.amount,
            exp.description.split(","), exp.createDate));
  }
  return new models.ExpenseList(
      json.expenseListId, json.name, json.description, expenses, new Date(json.createDate),
      json.members, json.categories);
};
