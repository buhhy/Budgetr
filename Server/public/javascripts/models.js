// Expense class
models.Expense = function (location, category, cost, items, createDate) {
  this.location = location;
  this.category = category;
  this.cost = cost || 0;
  this.items = items || [];
  this.createDate = createDate || new Date();
};

// Expense list class
models.ExpenseList = function (
    expenseListId, listName, description, expenses, createDate, members, categories) {
  this.expenseListId = expenseListId;
  this.listName = listName;
  this.description = description;
  this.expenses = expenses;
  this.createDate = createDate;
  this.members = members;
  this.categories = categories;

  this.allExpenseLocations = utils.removeArrayDuplicates(
      _.map(expenses, function (exp) {
        return exp.location;
      }));
  this.allExpenseDescriptionItems = utils.removeArrayDuplicates(
      _.flatten(
          _.map(expenses, function (exp) {
            return exp.items;
          })));
};
