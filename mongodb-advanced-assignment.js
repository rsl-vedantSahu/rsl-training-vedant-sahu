// =============================================================================
// MongoDB Advanced Assignment
// Database: company_advanced
// =============================================================================

// -----------------------------------------------------------------------------
// Task 1: Schema Validation and Data Creation
// -----------------------------------------------------------------------------

// Step 1: Select a Fresh Database
use("company_advanced");

// Step 2: Create the Validated Collection
db.createCollection("employees", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "departmentId", "experience", "active"],
      properties: {
        name: {
          bsonType: "string",
          description: "must be a string and is required"
        },
        departmentId: {
          bsonType: "int",
          description: "must be an integer and is required"
        },
        experience: {
          bsonType: "int",
          description: "must be an integer and is required"
        },
        active: {
          bsonType: "bool",
          description: "must be a boolean and is required"
        }
      }
    }
  }
});

// Step 3: Insert the Starting Dataset
db.employees.insertMany([
  {
    _id: 1,
    name: "John",
    departmentId: 10,
    skills: ["Java", "MongoDB"],
    experience: 4,
    active: true,
    certifications: [
      { name: "MongoDB", status: "Expired", expiryYear: 2026 }
    ]
  },
  {
    _id: 2,
    name: "Alice",
    departmentId: 10,
    skills: ["Python", "MongoDB"],
    experience: 6,
    active: true,
    certifications: [
      { name: "MongoDB", status: "Active", expiryYear: 2028 }
    ]
  },
  {
    _id: 3,
    name: "David",
    departmentId: 20,
    skills: ["Communication"],
    experience: 3,
    active: false,
    certifications: [
      { name: "Communication", status: "Active", expiryYear: 2027 }
    ]
  }
]);

// Step 4: Test the Validation Rule (Must be rejected)
try {
  db.employees.insertOne({
    _id: 99,
    name: 123,
    departmentId: "HR",
    experience: "one",
    active: "yes"
  });
} catch (e) {
  print("Invalid document successfully rejected by validator: " + e.message);
}

// Verify valid employee count
db.employees.countDocuments();


// -----------------------------------------------------------------------------
// Task 2: Advanced Array Queries and Updates
// -----------------------------------------------------------------------------

// 2.1 Find One Matching Array Object
// Use $elemMatch to find employees whose certifications contain the same object
// with name: "MongoDB" and status: "Active".
db.employees.find({
  certifications: {
    $elemMatch: {
      name: "MongoDB",
      status: "Active"
    }
  }
});

// 2.2 Update One Matching Array Element
// Use the positional $ operator to change John's MongoDB certification status from Expired to Active.
db.employees.updateOne(
  { name: "John", "certifications.name": "MongoDB" },
  { $set: { "certifications.$.status": "Active" } }
);

// 2.3 Update Selected Array Elements
// Use arrayFilters to change the status to Renewal Due for certification elements whose expiryYear < 2027.
db.employees.updateMany(
  {},
  { $set: { "certifications.$[elem].status": "Renewal Due" } },
  { arrayFilters: [{ "elem.expiryYear": { $lt: 2027 } }] }
);

// Verify final certification states
db.employees.find({}, { name: 1, certifications: 1 });


// -----------------------------------------------------------------------------
// Task 3: Bulk Write Operations
// -----------------------------------------------------------------------------

// Complete all three operations in one bulkWrite() call:
// 1. Increase John's experience by 1
// 2. Set Alice's active value to false
// 3. Insert Emma
db.employees.bulkWrite([
  {
    updateOne: {
      filter: { name: "John" },
      update: { $inc: { experience: 1 } }
    }
  },
  {
    updateOne: {
      filter: { name: "Alice" },
      update: { $set: { active: false } }
    }
  },
  {
    insertOne: {
      document: {
        _id: 4,
        name: "Emma",
        departmentId: 20,
        skills: ["Excel"],
        experience: 2,
        active: true,
        certifications: [
          { name: "Excel", status: "Active", expiryYear: 2026 }
        ]
      }
    }
  }
]);

// Verify final state:
// John.experience = 5
db.employees.find({ name: "John" }, { name: 1, experience: 1 });

// Alice.active = false
db.employees.find({ name: "Alice" }, { name: 1, active: 1 });

// Employee document count = 4
db.employees.countDocuments();


// -----------------------------------------------------------------------------
// Task 4: Advanced Aggregation
// -----------------------------------------------------------------------------

// Step 1: Create the Departments Collection
db.departments.insertMany([
  { _id: 10, name: "Engineering" },
  { _id: 20, name: "HR" }
]);

// 4.1 Join Employees with Departments
// - Join employees.departmentId with departments._id
// - Return employee name and departmentName only
// - Sort by employee name in ascending order
db.employees.aggregate([
  {
    $lookup: {
      from: "departments",
      localField: "departmentId",
      foreignField: "_id",
      as: "dept"
    }
  },
  {
    $unwind: "$dept"
  },
  {
    $project: {
      _id: 0,
      name: 1,
      departmentName: "$dept.name"
    }
  },
  {
    $sort: { name: 1 }
  }
]);

// 4.2 Count Employees for Each Skill
// Use $unwind on skills, group by skill, count employees, and sort by count descending.
// For equal counts, sort skill name ascending.
db.employees.aggregate([
  {
    $unwind: "$skills"
  },
  {
    $group: {
      _id: "$skills",
      count: { $sum: 1 }
    }
  },
  {
    $sort: {
      count: -1,
      _id: 1
    }
  }
]);

// 4.3 Return Active Employees and a Count
// Use $facet to return both active employee names and total active employee count in one pipeline.
// Sort names ascending.
db.employees.aggregate([
  {
    $match: { active: true }
  },
  {
    $facet: {
      activeEmployees: [
        { $project: { _id: 0, name: 1 } },
        { $sort: { name: 1 } }
      ],
      totalActiveCount: [
        { $count: "count" }
      ]
    }
  }
]);


// -----------------------------------------------------------------------------
// Task 5: Indexing and Query Performance
// -----------------------------------------------------------------------------

// 5.1 Create and Verify a Compound Index
// Create ascending compound index on departmentId followed by name.
db.employees.createIndex({ departmentId: 1, name: 1 });

// Verify created index
db.employees.getIndexes();

// 5.2 Read explain("executionStats")
db.employees
  .find({ departmentId: 10 })
  .sort({ name: 1 })
  .explain("executionStats");

// 5.3 Create a TTL Index
// Insert session document
db.sessions.insertOne({
  _id: 1,
  userName: "John",
  expiresAt: new Date(Date.now() + 600000)
});

// Create TTL index on expiresAt with expireAfterSeconds: 0
db.sessions.createIndex(
  { expiresAt: 1 },
  { expireAfterSeconds: 0 }
);

// Verify TTL index
db.sessions.getIndexes();
