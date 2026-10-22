// =============================================================================
// MongoDB Basic Assignment
// Database: company
// Collection: employees
// =============================================================================

// -----------------------------------------------------------------------------
// Task 1: Local Setup and Data Creation
// -----------------------------------------------------------------------------

// Step 1: Verify MongoDB Connection
// Command in terminal: mongosh
// show dbs;

// Step 2: Select the Database
use("company");

// Step 3: Insert the Dataset
db.employees.insertMany([
  {
    _id: "EMP001",
    name: "John",
    department: "Engineering",
    experience: 4,
    skills: ["Java", "Spring Boot"],
    active: true,
    address: {
      city: "Pune",
      country: "India"
    }
  },
  {
    _id: "EMP002",
    name: "Alice",
    department: "HR",
    experience: 3,
    skills: ["Recruitment", "Communication"],
    active: true,
    address: {
      city: "Mumbai",
      country: "India"
    }
  },
  {
    _id: "EMP003",
    name: "David",
    department: "Engineering",
    experience: 6,
    skills: ["Java", "MongoDB"],
    active: true,
    address: {
      city: "Bengaluru",
      country: "India"
    }
  },
  {
    _id: "EMP004",
    name: "Emma",
    department: "Finance",
    experience: 2,
    skills: ["Accounting", "Excel"],
    active: false,
    address: {
      city: "Pune",
      country: "India"
    }
  },
  {
    _id: "EMP005",
    name: "Robert",
    department: "Engineering",
    experience: 5,
    skills: ["Java", "Docker"],
    active: true,
    address: {
      city: "Delhi",
      country: "India"
    }
  }
]);

// Verify database and collection creation
// show dbs;
// show collections;
db.employees.countDocuments();


// -----------------------------------------------------------------------------
// Task 2: Read and Query Operations
// -----------------------------------------------------------------------------

// 2.1 Engineering Employees
// Retrieve employees whose department is Engineering.
// Return only: _id, name, experience.
// Sort the result by name in ascending order.
db.employees.find(
  { department: "Engineering" },
  { _id: 1, name: 1, experience: 1 }
).sort({ name: 1 });

// 2.2 Employees with 5 or More Years of Experience
// Retrieve employees whose experience is greater than or equal to 5.
// Return only: name, experience. Exclude _id and sort by experience in descending order.
db.employees.find(
  { experience: { $gte: 5 } },
  { _id: 0, name: 1, experience: 1 }
).sort({ experience: -1 });

// 2.3 Nested Document Query
// Retrieve employees whose address city is Pune.
// Return only employee name and city and sort by name.
db.employees.find(
  { "address.city": "Pune" },
  { _id: 0, name: 1, "address.city": 1 }
).sort({ name: 1 });

// 2.4 Array Query
// Using $in, retrieve employees whose skills contain either: MongoDB or Spring Boot.
// Sort the result by name in ascending order.
db.employees.find(
  { skills: { $in: ["MongoDB", "Spring Boot"] } }
).sort({ name: 1 });

// 2.5 Sorting and Limiting
// Retrieve the top 2 employees with the highest experience.
// Return: name, experience.
db.employees.find(
  {},
  { _id: 0, name: 1, experience: 1 }
).sort({ experience: -1 }).limit(2);


// -----------------------------------------------------------------------------
// Task 3: Update Operations
// -----------------------------------------------------------------------------

// 3.1 $inc
// Increase Alice's experience by 1.
db.employees.updateOne(
  { name: "Alice" },
  { $inc: { experience: 1 } }
);

// Verify Alice's updated experience
db.employees.find({ name: "Alice" }, { _id: 0, name: 1, experience: 1 });

// 3.2 $set
// Change Emma's active value from: false to true.
db.employees.updateOne(
  { name: "Emma" },
  { $set: { active: true } }
);

// Verify Emma's updated active status
db.employees.find({ name: "Emma" }, { _id: 0, name: 1, active: 1 });

// 3.3 $addToSet
// Add MongoDB to Robert's skills array using $addToSet.
db.employees.updateOne(
  { name: "Robert" },
  { $addToSet: { skills: "MongoDB" } }
);

// Execute the same $addToSet operation again and verify that MongoDB is not duplicated
db.employees.updateOne(
  { name: "Robert" },
  { $addToSet: { skills: "MongoDB" } }
);

// Verify Robert's skills
db.employees.find({ name: "Robert" }, { _id: 0, name: 1, skills: 1 });


// -----------------------------------------------------------------------------
// Task 4: Delete Operation
// -----------------------------------------------------------------------------

// Insert temporary document
db.employees.insertOne({
  _id: "EMP999",
  name: "Temporary Employee",
  department: "Training",
  experience: 0
});

// Delete only this document using deleteOne()
db.employees.deleteOne({ _id: "EMP999" });

// Verify that it returns null
db.employees.findOne({ _id: "EMP999" });

// Verify original 5 documents remain
db.employees.countDocuments();


// -----------------------------------------------------------------------------
// Task 5: Indexing
// -----------------------------------------------------------------------------

// 5.1 Check Existing Indexes
// Identify the automatically created MongoDB index (_id_).
db.employees.getIndexes();

// 5.2 Create an Index
// Create an ascending index on: department
db.employees.createIndex({ department: 1 });

// 5.3 Verify the Index
// Check indexes again. Should contain _id_ and department_1.
db.employees.getIndexes();


// -----------------------------------------------------------------------------
// Task 6: Aggregation
// -----------------------------------------------------------------------------

// 6.1 Employees per Department
// Count the number of employees in each department, sorted by department name.
db.employees.aggregate([
  {
    $group: {
      _id: "$department",
      count: { $sum: 1 }
    }
  },
  {
    $sort: { _id: 1 }
  }
]);

// 6.2 Average Experience per Department
// Calculate average employee experience for each department.
// Sort result by department name in ascending order.
db.employees.aggregate([
  {
    $group: {
      _id: "$department",
      averageExperience: { $avg: "$experience" }
    }
  },
  {
    $sort: { _id: 1 }
  }
]);

// 6.3 Top Engineering Employees
// Using an aggregation pipeline:
// 1. Select only Engineering employees.
// 2. Return only name and experience.
// 3. Exclude _id.
// 4. Sort experience in descending order.
// 5. Return only the top 2 employees.
db.employees.aggregate([
  {
    $match: { department: "Engineering" }
  },
  {
    $project: {
      _id: 0,
      name: 1,
      experience: 1
    }
  },
  {
    $sort: { experience: -1 }
  },
  {
    $limit: 2
  }
]);
