# Task 5.2: explain("executionStats") Values

- **winning plan stage**: IXSCAN (with FETCH)
- **nReturned**: 2
- **totalDocsExamined**: 2
- **totalKeysExamined**: 2

---

# Task 7: MongoDB Advanced Concepts — MCQs

Q1: B
Q2: B
Q3: C
Q4: A
Q5: C
Q6: B
Q7: C
Q8: B
Q9: C
Q10: B
Q11: A
Q12: C

---

### Detailed Explanations

- **Q1: B (It rejects documents that do not follow required fields or data types)**  
  Schema validation rules (using `$jsonSchema`) enforce document structure by rejecting inserts or updates that violate the specified constraints and data types.

- **Q2: B (Multiple conditions match the same array object)**  
  The `$elemMatch` operator matches documents that contain an array field with at least one element satisfying all specified query criteria simultaneously.

- **Q3: C (To update selected array elements that match a condition)**  
  The `arrayFilters` option in update operations specifies which array elements to modify using identifier syntax (e.g. `$[elem]`).

- **Q4: A (When several write operations should be sent together)**  
  `bulkWrite()` performs batches of write operations (inserts, updates, deletes) in a single round-trip, optimizing network performance and execution throughput.

- **Q5: C ($lookup)**  
  The `$lookup` aggregation stage performs an equality or correlated join between the current collection and documents in another collection in the same database.

- **Q6: B (MongoDB used an index)**  
  `IXSCAN` indicates an index scan where MongoDB navigated an index rather than scanning the full collection (`COLLSCAN`).

- **Q7: C (Uncommitted transaction changes are rolled back)**  
  `abortTransaction()` cancels the multi-document transaction and discards any uncommitted changes made within that transaction session.

- **Q8: B (A majority of voting data-bearing replica-set members acknowledge the write)**  
  A write concern of `w: "majority"` ensures that the write has been committed to a majority of calculated replica set members before returning success.

- **Q9: C (Eligible members hold an election for a new Primary)**  
  When the Primary replica set member becomes unreachable, secondaries initiate an automatic election process to choose a new Primary member.

- **Q10: B (A high-cardinality field that distributes traffic and matches common queries)**  
  An effective shard key has high cardinality to distribute data evenly across shards, avoids monotonically increasing write hotspots, and supports query routing.

- **Q11: A (Listen for inserts, updates, replacements, and deletes)**  
  Change Streams allow applications to subscribe to real-time data changes at the collection, database, or deployment level without polling.

- **Q12: C (Use TLS, restricted network access, and least-privilege credentials)**  
  Security best practices include encrypting in-transit traffic via TLS, binding to private networks/firewalls, and assigning role-based least privilege accounts.
