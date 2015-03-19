// Screens
var expenseLogEl = $("#expenseLog");
var expenseFormEl = $("#newExpenseForm");

// Buttons
var newExpBtn = $("#newExpBtn");
var cancelBtn = $("#cancelBtn");
var saveBtn = $("#saveExpBtn");
var notifBtn = $("#notif");

// Question block elements
var busQ = $("#busQ");
var catQ = $("#catQ");
var costQ = $("#costQ");
var itemsQ = $("#itemsQ");

//Input elements
var busNameInputEl = $("#busNameInput");
var catInputEl = $("#catInput");
var costInputEl = $("#costInput");
var itemsInputEl = $("#itemsInput");

// Other DOM elements
var itemListEl = $("#itemList");
var expenseLogDate = $("#expenseLogDate");

// Dashboard
$(document).ready(function () {
  $.get("/api/expenselist/1", function (data) {
    drawExpenseList(parseExpenseListAjaxData(data));
  });
});

// Expense class
var Expense = function (location, category, cost, items, createDate) {
  this.location = location;
  this.category = category;
  this.cost = cost || 0;
  this.items = items || [];
  this.createDate = createDate || new Date();
};

// Expense list class
var ExpenseList = function (listName, description, expenses, createDate) {
  this.listName = listName;
  this.description = description;
  this.expenses = expenses;
  this.createDate = createDate;
};

var newExpense = undefined;

// On-click events
newExpBtn.click(
  function () {
    expenseLogEl.addClass('hidden');
    expenseFormEl.removeClass('hidden');
    newExpense = new Expense();
    $("#prog1").addClass('current-prog');
    busNameInputEl.focus();
  }
);

cancelBtn.click(
  function () {
    var input = confirm("Your changes will not be saved. Continue?");
    if (input == true) {
      expenseLogEl.removeClass('hidden');
      expenseFormEl.addClass('hidden');
      resetExpenseForm();
    }
  }
);

saveBtn.click(
  function () {
    expensePOST();
    resetExpenseForm();
  }
);

notifBtn.click(
  function () {
    notifBtn.addClass('hidden');
  }
);

// Functions for submitting data
function submitBusName(event) {
  if (event.keyCode === 13) {
    newExpense.location = $(busNameInputEl).val();
    busQ.addClass('hidden');
    catQ.removeClass('hidden');
    $("#prog2").addClass('current-prog');
    catInputEl.focus();
  }
}

function submitCat(event) {
  if (event.keyCode === 13) {
    newExpense.category = catInputEl.val();
    catQ.addClass('hidden');
    costQ.removeClass('hidden');
    $("#prog3").addClass('current-prog');
    costInputEl.focus();
  }
}

function submitCost(event) {
  if (event.keyCode === 13) {
    newExpense.cost = Number.parseInt(costInputEl.val())*100; 
    costQ.addClass('hidden');
    itemsQ.removeClass('hidden');
    $("#prog4").addClass('current-prog');
    itemsInputEl.focus();
  }
}

function submitItems(event) {
  if (itemsInputEl.val()) {
    if (event.keyCode === 13) {
      newExpense.items.push($(itemsInputEl).val());
      updateList(itemsInputEl.val());
      itemsInputEl.val("");
    }
  }
}

function expensePOST() {
  var itemDescription = "";
  for (var j = 0; j < newExpense.items.length; j++) {
    itemDescription += newExpense.items[j];

    if (j < newExpense.items.length - 1)
      itemDescription += ", ";
  }
  console.log(itemDescription);

  $.ajax({
    type: "post",
    url: "/api/category",
    data: JSON.stringify({
      "value": {
        "name": newExpense.category,
        "parentListId": 1
      }
    }),
    success: function (data) {
      $.ajax({
        type: "post",
        url: "/api/expense",
        data: JSON.stringify({
          value: {
            location: newExpense.location,
            description: itemDescription,
            categoryId: data.expenseCategoryId,
            amount: newExpense.cost,
            parentListId: 1,
            participants: []
          }
        }),
        contentType: "application/json",
        success: function () {
          location.reload();
        }
      });
    },
    contentType: "application/json"
  });
}

// Item list 
function updateList(item) {
  var newLi = $("<li>"+item+"</li>");
  $("#itemList").append(newLi);
  $("#itemsInput").val('');
}

// Converts time
function convertTime(epochTime) {
  var convertedTime = new Date(epochTime);
  var options = { weekday: 'long', month: 'long', day: 'numeric', timeZone: 'America/Los_Angeles' };
  return convertedTime.toLocaleDateString('en-US', options);
}

// Event Listeners
busNameInputEl.on("keypress", submitBusName);
catInputEl.on("keypress", submitCat);
costInputEl.on("keypress", submitCost);
itemsInputEl.on("keypress", submitItems);

// Reset expense form 
function resetExpenseForm() {
  busNameInputEl.val('');
  catInputEl.val('');
  costInputEl.val('');
  itemsInputEl.val('');
  itemListEl.html('');
  itemsQ.addClass('hidden');
  busQ.removeClass('hidden');
  expenseFormEl.addClass('hidden');
  expenseLogEl.removeClass('hidden');  
  $("#prog1").removeClass('current-prog');
  $("#prog2").removeClass('current-prog');
  $("#prog3").removeClass('current-prog');
  $("#prog4").removeClass('current-prog');
}

function parseExpenseListAjaxData(data) {
  var expenseList = new ExpenseList(data.name, data.description, [], new Date(data.createDate));
  var categories = {};

  for (var i = 0; i < data.categories.length; i++) {
    var cat = data.categories[i];
    categories[cat.expenseCategoryId] = cat.name;
  }

  for (var i = 0; i < data.expenses.length; i++) {
    var exp = data.expenses[i];
    expenseList.expenses.push(
        new Expense(
            exp.location, categories[exp.categoryId], exp.amount,
            exp.description.split(","), exp.createDate));
  }
  return expenseList;
}

function drawExpenseList(expenseList) {
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
    var currentConvertedDate = convertTime(currentExpense.createDate);

    if (currentConvertedDate !== activeDate) {
      // Create a header for a new day
      activeDate = convertTime(currentExpense.createDate);
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
      expenseLogEl.append(newDayContainerEl);
    }

    var expenseLi = $("<li class='expense'></li>");
    var expenseBusName =
        createLi(currentExpense.location, "business").appendTo(expenseLi);
    var expenseItems =
        createLi(itemListString(currentExpense.items), "items").appendTo(expenseLi);
    var expenseCat =
        createLi(currentExpense.category, "category").appendTo(expenseLi);
    var expenseCost =
        createLi(parseFloat(currentExpense.cost / 100).toFixed(2), "cost").appendTo(expenseLi);

    activeExpenseUl.append(expenseLi);
  }
}
