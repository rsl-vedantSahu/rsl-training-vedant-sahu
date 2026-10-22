# Task 7: MongoDB Fundamentals — MCQs

Q1: B
Q2: C
Q3: C
Q4: C
Q5: B
Q6: C
Q7: B
Q8: C
Q9: C
Q10: B

---

### Detailed Explanations

- **Q1: B (Documents)**  
  MongoDB is a document-oriented NoSQL database that stores data in flexible, JSON-like BSON documents.

- **Q2: C (_id)**  
  Every MongoDB document requires a unique `_id` field that serves as the primary key. If not explicitly specified, MongoDB automatically creates an ObjectId for it and creates an index on `_id`.

- **Q3: C ({ upsert: true })**  
  The `upsert: true` option in `updateOne()` performs an insert operation if no document matches the query filter.

- **Q4: C ($addToSet)**  
  The `$addToSet` operator adds an element to an array only if the value does not already exist in the array, preventing duplicates.

- **Q5: B ($group)**  
  The `$group` stage groups input documents by a specified identifier expression and applies accumulator expressions (e.g., `$sum`, `$avg`).

- **Q6: C ({ "address.city": "Pune" })**  
  To query a nested field in MongoDB using dot notation, the path must be enclosed in quotes: `"address.city"`.

- **Q7: B (Embedding)**  
  Embedding (denormalization) stores related data directly inside nested documents or arrays within the parent document.

- **Q8: C (Deletes all documents in the collection)**  
  Passing an empty filter `{}` to `deleteMany()` deletes all documents matching the criteria, which corresponds to all documents in the collection.

- **Q9: C (Store them using secure configuration/environment variables or secrets management)**  
  Sensitive credentials must never be committed to source code or version control, but rather injected via environment variables or secret management services.

- **Q10: B (Only _id is automatically indexed by default.)**  
  MongoDB automatically creates a unique index on the `_id` field upon collection creation. Secondary indexes on other fields must be manually created.
