import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const AddEditManagerPage = () => {
  const { managerId } = useParams();
  const isEditing = !!managerId;
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const messageRef = useRef(null);

  // ✅ Only show success message after form submission
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    if (isEditing) {
      fetchManagerById();
    }
  }, [managerId]);

  const fetchManagerById = async () => {
    try {
      const res = await ApiService.getUserById(managerId);
if (res && res.status === 200 && res.user) {
  const user = res.user;
        setName(user.name || "");
        setEmail(user.email || "");
        setPhoneNumber(user.phoneNumber || "");
        setPassword(""); // Never prefill password
      } else {
        showMessage(res.message || "Manager not found.");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Error fetching manager: " + error
      );
    }
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      if (messageRef.current) {
        messageRef.current.scrollIntoView({ behavior: "smooth" });
      }
    }, 100);
    setTimeout(() => setMessage(""), 4000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = { name, email, phoneNumber };
    if (password) {
      payload.password = password;
    }

    try {
      if (isEditing) {
        await ApiService.updateUser(managerId, payload);
        showMessage("Manager updated successfully");
      } else {
        payload.role = "MANAGER";
        await ApiService.registerUser(payload);
        showMessage("Manager created successfully");
      }
      setShowSuccess(true);

      setTimeout(() => {
      navigate("/managers");
    }, 2500);
  } catch (error) {
    showMessage(
      error.response?.data?.message ||
      `Error ${isEditing ? "updating" : "creating"} manager`
    );
  }
};

  return (
    <Layout>

      {message && <p className="message">{message}</p>}

      <div className="purchase-form-page">
        <h2>{isEditing ? "Edit Manager" : "Add Manager"}</h2>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter name"
              required
            />
          </div>

          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter email"
              required
            />
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="text"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              placeholder="Enter phone number"
              required
            />
          </div>

          <div className="form-group">
            <label>
              Password {isEditing && <span>(leave blank to keep current)</span>}
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
              required={!isEditing}
            />
          </div>

          <button type="submit">
            {isEditing ? "Update Manager" : "Create Manager"}
          </button>
        </form>
      </div>
    </Layout>
  );
};

export default AddEditManagerPage;
