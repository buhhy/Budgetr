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

	$.get("/api/expenselist/1", function(data) {
		var activeDate;
		var activeExpenseUl;

		for (var i = data.expenses.length-1; i >= 0; i--) {
			var currentExpense = data.expenses[i];
			var currentConvertedDate = convertTime(currentExpense.createDate);

			if (currentConvertedDate !== activeDate) {
				// Create a header for a new day
				activeDate = convertTime(currentExpense.createDate);
				activeExpenseUl = $("<ul class='expense-list'></ul>");

				var newDayContainerEl = $("<div class='day-log'></div>");
				var newDateHeading = $("<h2 class='date-heading'>"+activeDate+"</h2>");
				var tableHeadings = $("<li class='table-headings'></li>");
				var tableColumn = $("<div class='table-column'></div>");
				var columns = ["Business", "Description", "Category", "Cost"];

				// Add all table headings
				for (var k = 0; k < columns.length; k++) {
					$("<div class='table-column'>"+columns[k]+"</div>").appendTo(tableHeadings);
				}

				activeExpenseUl.append(tableHeadings);

				newDayContainerEl.append(newDateHeading);
				newDayContainerEl.append(activeExpenseUl);

				// Add the new element to the document DOM tree
				expenseLogEl.append(newDayContainerEl);
			}

			var busName = currentExpense.location;
			var items = currentExpense.description;
			var amount = parseFloat(currentExpense.amount/100).toFixed(2);
			var createdLi = createExpense(busName, items, amount);
			activeExpenseUl.append(createdLi);
		}
	});
});

// Expense class
var Expense = function() {
	var self = this;

	this.busName;
	this.category;
	this.cost;
	this.items = [];
}

var newExpense = undefined;

// On-click events
newExpBtn.click(
	function() {
		expenseLogEl.addClass('hidden');
		expenseFormEl.removeClass('hidden');
		newExpense = new Expense();
		$("#prog1").addClass('current-prog');
		busNameInputEl.focus();
	}
);

cancelBtn.click(
	function() {
		var input = confirm("Your changes will not be saved. Continue?");
		if (input == true) {
			expenseLogEl.removeClass('hidden');
			expenseFormEl.addClass('hidden');
			resetExpenseForm();
		}
	}
);

saveBtn.click(
	function() {
		expensePOST();
		resetExpenseForm();
	}
);

notifBtn.click(
	function() {
		notifBtn.addClass('hidden');
	}
);

// Functions for submitting data
function submitBusName(event) {
	if (event.keyCode === 13) {
		newExpense.busName = $(busNameInputEl).val(); 
		busQ.addClass('hidden');
		catQ.removeClass('hidden');
		$("#prog2").addClass('current-prog');
		catInputEl.focus();
	}
}

function submitCat(event) {
	if (event.keyCode === 13) {
		newExpense.cat = catInputEl.val();
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
			itemsInputEl.val() == '';
		}
	}
}

function expensePOST() {
	var itemDescription = "";
	for (j = 0; j < newExpense.items.length; j++) {
		itemDescription += newExpense.items[j];
		if (j < newExpense.items.length-1) {
			itemDescription += ", ";
		}
		if (j == newExpense.items.length-2) {
			itemDescription += "and ";
		}
	}
	console.log(itemDescription);

	var expenseData = {
	  "value": {
	    "location": newExpense.busName,
	    "description": itemDescription,
	    "categoryId": newExpense.cat,
	    "amount": newExpense.cost,
	    "parentListId": 1,
	    "participants": []
	  }
	}

	$.ajax({
		type: "post",
		url: "/api/category",
		data: JSON.stringify({
			"value": {
				"name": newExpense.cat,
				"parentListId": 1
			}
		}),
		success: function (data) {
			$.ajax({
				type: "post",
				url: "/api/expense",
				data: JSON.stringify({
				 	"value": {
					    "location": newExpense.busName,
					    "description": itemDescription,
					    "categoryId": data.expenseCategoryId,
					    "amount": newExpense.cost,
					    "parentListId": 1,
					    "participants": []
					}
				}),
				"contentType": "application/json",
				success: function() {
					location.reload();
				}
			});
		},
		"contentType": "application/json"
	});
}

// Creates a new date heading
function createNewDayHeader(activeDate, newDay) {
	return expenseUl
}

// Fills out each expense 
function createExpense(busName, items, amount, category) {
	var expenseLi = $("<li class='expense'></li>");
	var expenseBusName = $("<div class='bus-name'><span>"+busName+"</span></div>");
	var expenseCat = $("<div class='category'><span>"+category+"</span></div>");
	var expenseItems = $("<div class='items'><span>"+items+"</span></div>");
	var expenseCost = $("<div class='cost'><span>"+amount+"</span></div>");

	expenseLi.append(expenseBusName);
	expenseLi.append(expenseItems);
	expenseLi.append(expenseCat);
	expenseLi.append(expenseCost);

	return expenseLi;
}

// Item list 
function updateList(item) {
	var newLi = $("<li>"+item+"</li>");
	$("#itemList").append(newLi);
	$("#itemsInput").val('');
}

// Converts time
function convertTime(epochTime) {
	var convertedTime = new Date(epochTime)
	var options = { weekday: 'long', month: 'long', day: 'numeric', timeZone: 'America/Los_Angeles' }
	return convertedTime.toLocaleDateString('en-US', options);
}

// Event Listeners
busNameInputEl.on("keypress", submitBusName)
busNameInput.addEventListener("keypress", submitBusName)
catInputEl.on("keypress", submitCat)
costInputEl.on("keypress", submitCost)
itemsInputEl.on("keypress", submitItems)

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