import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.jsx";
import "bootstrap/dist/css/bootstrap.min.css";
import {
  createBrowserRouter,
  createRoutesFromElements,
  RouterProvider,
  Route,
} from "react-router-dom";
import ListEmployeeComponent from "./components/ListEmployeeComponent.jsx";
import Employee from "./components/Employee.jsx";

const router = createBrowserRouter(
  createRoutesFromElements(
    <Route path="/" element={<App />}>
      <Route path="" element={<ListEmployeeComponent />} />
      <Route path="/employees" element={<ListEmployeeComponent />} />
      <Route path="/add-employee" element={<Employee />} />
      <Route path="/update-employee/:id" element={<Employee />} />
    </Route>,
  ),
);

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
