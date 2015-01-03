var newExpenseBtn = document.getElementById("newExpBtn");
var expenseLogEl = document.getElementById("expenseLog");
var expenseFormEl = document.getElementById("newExpenseForm");
var itemListEl = document.getElementById("itemList");

var busQ = document.getElementById("busQ");
var catQ = document.getElementById("catQ");
var costQ = document.getElementById("costQ");
var itemsQ = document.getElementById("itemsQ");

var cancelBtn = document.getElementById("cancelBtn");

var busNameInputEl = document.getElementById("busNameInput");
var catInputEl = document.getElementById("catInput");
var costInputEl = document.getElementById("costInput");
var itemsInputEl = document.getElementById("itemsInput");

var Expense = function() {
	var self = this;

	this.busName;
	this.category;
	this.cost;
	this.items = [];
}

var newExpense = undefined;

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
		newExpense.items.splice(0, 0, itemsInputEl.value);
		console.log(newExpense.items);
	}
}

busNameInput.addEventListener("keypress", submitBusName)
catInput.addEventListener("keypress", submitCat)
costInput.addEventListener("keypress", submitCost)
itemsInput.addEventListener("keypress", submitItems)