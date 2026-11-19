import React, { useActionState, useEffect, useState } from "react";
import {
  createEmployee,
  getEmployeeById,
  updateEmployee,
} from "../service/EmployeeService";
import { redirect, useParams } from "react-router-dom";
import { useNavigate } from "react-router-dom";

function Employee() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [errors, setErrors] = useState({
    firstName: "",
    lastName: "",
    email: "",
  });
  const navigate = useNavigate();
  const { id } = useParams();

  const [error, submitAction, isPending] = useActionState(
    async (prevState, formData) => {
      const firstName = formData.get("firstName");
      const lastName = formData.get("lastName");
      const email = formData.get("email");
      if (!validateform(firstName, lastName, email)) {
        return prevState;
      }
      const employee = {
        firstName,
        lastName,
        email,
      };
      if (id) {
        try {
          await updateEmployee(id, employee);
          navigate("/employees");
        } catch (error) {
          console.error("Error updating employee:", error);
          return error;
        }
      } else {
        try {
          await createEmployee(employee);
          navigate("/employees");
        } catch (error) {
          console.error("Error creating employee:", error);
          return error;
        }
      }
    },
    null,
  );

  function validateform(firstName, lastName, email) {
    let valid = true;
    const errorsCopy = { ...error };

    if (firstName.trim() == "") {
      errorsCopy.firstName = "First name is required";
      valid = false;
    }
    if (lastName.trim() == "") {
      errorsCopy.lastName = "Last name is required";
      valid = false;
    }
    if (email.trim() == "") {
      errorsCopy.email = "Email is required";
      valid = false;
    }
    setErrors(errorsCopy);
    return valid;
  }

  useEffect(() => {
    if (id) {
      getEmployeeById(id).then((response) => {
        const employee = response.data;
        setFirstName(employee.firstName);
        setLastName(employee.lastName);
        setEmail(employee.email);
      });
    }
  }, []);

  return (
    <div className="container ">
      <div className="card col-md-6 offset-md-3 offset-md-3 mt-5">
        <h2 className="text-center">{id ? "Update" : "Add"} Employee</h2>
        <div className="card-body">
          <form action={submitAction}>
            <div className=" form-group mb-3">
              <label className="form-label">First Name</label>
              <input
                type="text"
                name="firstName"
                placeholder="Enter first name"
                defaultValue={firstName}
                className={`form-control ${errors.firstName ? "is-invalid" : ""}`}
              ></input>
              {errors.firstName && (
                <div className="invalid-feedback">{errors.firstName}</div>
              )}
            </div>
            <div className="form-group mb-3">
              <label className="form-label">Last Name</label>
              <input
                type="text"
                name="lastName"
                placeholder="Enter last name"
                defaultValue={lastName}
                className={`form-control ${errors.lastName ? "is-invalid" : ""}`}
              ></input>
              {errors.lastName && (
                <div className="invalid-feedback">{errors.lastName}</div>
              )}
            </div>
            <div className="form-group mb-3">
              <label className="form-label">Email</label>
              <input
                type="email"
                name="email"
                placeholder="Enter email"
                defaultValue={email}
                className={`form-control ${errors.email ? "is-invalid" : ""}`}
              ></input>
              {errors.email && (
                <div className="invalid-feedback">{errors.email}</div>
              )}
            </div>
            <button
              className="btn btn-success"
              type="submit"
              disabled={isPending}
            >
              {isPending ? "Submitting..." : "Submit"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default Employee;
