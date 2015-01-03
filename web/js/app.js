// Screens 
var expenseLogEl = document.getElementById("expenseLog");
var expenseFormEl = document.getElementById("newExpenseForm");

// Buttons
var newExpenseBtn = document.getElementById("newExpBtn");
var cancelBtn = document.getElementById("cancelBtn");
var saveBtn = document.getElementById("saveExpBtn");

// Question block elements
var busQ = document.getElementById("busQ");
var catQ = document.getElementById("catQ");
var costQ = document.getElementById("costQ");
var itemsQ = document.getElementById("itemsQ");

//Input elements
var busNameInputEl = document.getElementById("busNameInput");
var catInputEl = document.getElementById("catInput");
var costInputEl = document.getElementById("costInput");
var itemsInputEl = document.getElementById("itemsInput");

// Item List
var itemListEl = document.getElementById("itemList");

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
newExpBtn.onclick = function() {
	expenseLogEl.classList.add('hidden');
	expenseFormEl.classList.remove('hidden');
	newExpense = new Expense();
}

cancelBtn.onclick = function() {
	var input = confirm("Your changes will not be saved. Continue?");
	if (input == true) {
		expenseLogEl.classList.remove('hidden');
		expenseFormEl.classList.add('hidden');
		busNameInputEl.value="";
	}
}

saveBtn.onclick = function() {
	console.log("newExpense.busName");
	console.log("newExpense.category");
	console.log("newExpense.cost");
	console.log("newExpense.items");
}

// Functions for submitting data
function submitBusName(event) {
	if (event.keyCode === 13) {
		newExpense.busName = busNameInputEl.value; 
		console.log(newExpense.busName);
		busQ.classList.add('hidden');
		catQ.classList.remove('hidden');
	}
}

function submitCat(event) {
	if (event.keyCode === 13) {
		newExpense.category = catInputEl.value; 
		console.log(newExpense.category);
		catQ.classList.add('hidden');
		costQ.classList.remove('hidden');
	}
}

function submitCost(event) {
	if (event.keyCode === 13) {
		newExpense.cost = costInputEl.value; 
		console.log(newExpense.cost);
		costQ.classList.add('hidden');
		itemsQ.classList.remove('hidden');
	}
}

function submitItems(event) {
	if (event.keyCode === 13) {
		newExpense.items = itemsInputEl.value;
		newListItem(newExpense.items);
	}
}

// Creates a new list item
function newListItem(items) {
	var newLi = document.createElement("li");
	var newSpan = document.createElement("span");
	newSpan.innerHTML = items;
	newLi.appendChild(newSpan);
	itemsInputEl.value="";
	var existingListItems = document.getElementById("itemList").getElementsByTagName("li");
	if (existingListItems.length = 0) {
		itemListEl.appendChild(newLi);
	} else {
		itemListEl.insertBefore(newLi, existingListItems[0]);
	}
}

// Event Listeners
busNameInput.addEventListener("keypress", submitBusName)
catInput.addEventListener("keypress", submitCat)
costInput.addEventListener("keypress", submitCost)
itemsInput.addEventListener("keypress", submitItems)