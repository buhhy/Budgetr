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
		for (var i = data.expenses.length-1; i >= 0; i--) {
			var currentExpense = data.expenses[i];
			var currentConvertedDate = convertTime(currentExpense.createDate);
			if (currentConvertedDate !== activeDate) {
				var activeDate = convertTime(currentExpense.createDate);
				var newDay = $("<div class='day-log'></div>");
				createNewDay(activeDate, newDay);
			}
			var busName = currentExpense.location;
			var items = currentExpense.description;
			var amount = parseFloat(currentExpense.amount/100).toFixed(2);
			createExpense(newDay, busName, items, amount);
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
$(newExpBtn).click(
	function() {
		$(expenseLogEl).addClass('hidden');
		$(expenseFormEl).removeClass('hidden');
		newExpense = new Expense();
		$("#prog1").addClass('current-prog');
		$(busNameInputEl).focus();
	}
);

newExpBtn.onclick = function() {
	$(expenseLogEl).addClass('hidden');
	$(expenseFormEl).removeClass('hidden');
	newExpense = new Expense();
	$("#prog1").addClass('current-prog');
	$(busNameInputEl).focus();
}

cancelBtn.onclick = function() {
	var input = confirm("Your changes will not be saved. Continue?");
	if (input == true) {
		$(expenseLogEl).removeClass('hidden');
		$(expenseFormEl).addClass('hidden');
		resetExpenseForm();
	}
}

saveBtn.onclick = function() {
	expensePOST();
	resetExpenseForm();
}

notifBtn.onclick = function() {
	$(notifBtn).addClass('hidden');
} 
// Functions for submitting data
function submitBusName(event) {
	if (event.keyCode === 13) {
		newExpense.busName = $(busNameInputEl).val(); 
		console.log(newExpense.busName);
		$(busQ).addClass('hidden');
		$(catQ).removeClass('hidden');
		$("#prog2").addClass('current-prog');
		$(catInputEl).focus();
	}
}

function submitCat(event) {
	if (event.keyCode === 13) {
		newExpense.category = $(catInputEl).val(); 
		console.log(newExpense.category);
		$(catQ).addClass('hidden');
		$(costQ).removeClass('hidden');
		$("#prog3").addClass('current-prog');
		$(costInputEl).focus();
	}
}

function submitCost(event) {
	if (event.keyCode === 13) {
		newExpense.cost = Number.parseInt($(costInputEl).val())*100; 
		console.log(newExpense.cost);
		$(costQ).addClass('hidden');
		$(itemsQ).removeClass('hidden');
		$("#prog4").addClass('current-prog');
		$(itemsInputEl).focus();
	}
}

function submitItems(event) {
	if (event.keyCode === 13) {
		newExpense.items.push($(itemsInputEl).val());
		updateList($(itemsInputEl).val());
	}
}

function expensePOST() {
	var itemDescription = "";
	for (var j = 0; j < newExpense.items.length; j++) {
		itemDescription += newExpense.items[j];
		if (j==newExpense.items[j]<1) {
			itemDescription += ", ";
		}
	}

	var expenseData = {
	  "value": {
	    "location": newExpense.busName,
	    "description": newExpense.items,
	    "amount": newExpense.cost,
	    "parentId": 1
	  }
	}

	$.ajax({
		type: "post",
		url: "/api/expense",
		data: JSON.stringify(expenseData),
		"contentType": "application/json"
	});
}

// Creates a new date heading
function createNewDay(activeDate, newDay) {
	var newDateHeading = $("<h2 class='date-heading'>"+activeDate+"</h2>");
	$(expenseLogEl).append(newDay);
	$(newDay).append(newDateHeading);
}

// Fills out each expense 
function createExpense(newDay, busName, items, amount) {
	var expenseUl = $("<ul class='expense-list'></ul>");
	var expenseLi = $("<li class='expense'></li>");
	var expenseBusName = $("<div class='bus-name'><span>"+busName+"</span></div>");
	var expenseItems = $("<div class='items'>"+items+"</div>");
	var expenseCost = $("<div class='cost'>"+amount+"</div>");
	$(newDay).append(expenseUl);
	$(expenseUl).append(expenseLi);
	$(expenseLi).append(expenseBusName);
	$(expenseLi).append(expenseItems);
	$(expenseLi).append(expenseCost);
}

// Item list 
function updateList(item) {
	var newLi = $("<li>"+item+"</li>");
	$("#itemList").append(newLi);
	console.log("mar");
	$("#itemsInput").val('');
}

// Converts time
function convertTime(epochTime) {
	var convertedTime = new Date(epochTime)
	var options = { weekday: 'long', month: 'long', day: 'numeric', timeZone: 'America/Los_Angeles' }
	return convertedTime.toLocaleDateString('en-US', options);
}

// Event Listeners
busNameInput.addEventListener("keypress", submitBusName)
catInput.addEventListener("keypress", submitCat)
costInput.addEventListener("keypress", submitCost)
itemsInput.addEventListener("keypress", submitItems)

// Reset expense form 
function resetExpenseForm() {
	busNameInputEl.value="";
	catInputEl.value="";
	costInputEl.value="";
	itemsInputEl.value="";
	itemListEl.innerHTML="";
	itemsQ.classList.add('hidden');
	busQ.classList.remove('hidden');
	expenseFormEl.classList.add('hidden');
	$(expenseLogEl).removeClass('hidden');	
	document.getElementById("prog1").classList.remove('current-prog');
	document.getElementById("prog2").classList.remove('current-prog');
	document.getElementById("prog3").classList.remove('current-prog');
	document.getElementById("prog4").classList.remove('current-prog');
}