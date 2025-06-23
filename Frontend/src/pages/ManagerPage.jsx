import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../service/ApiService";
import Layout from "../component/Layout";

const ManagerPage = () => {
  const [message, setMessage] = useState("");
  const [allUsers, setAllUsers] = useState([]);

  const navigate = useNavigate();

  useEffect(() => {
    const getAllUsers = async () => {
      try {
        const responseData = await ApiService.getAllUsers();
        console.log("API Response:", responseData);
        if (responseData.status === 200 && Array.isArray(responseData.users)) {
          setAllUsers(responseData.users); // 👈 Use `users` key
        } else {
          showMessage(responseData.message || "Failed to load users");
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Error Getting Users: " + error
        );
        console.log(error);
      }
    };
    getAllUsers();
  }, []);


  const handleDeleteManager = async (userId) => {
    if (!userId) {
      showMessage("Invalid user ID");
      return;
    }

    if (window.confirm("Are you sure you want to delete this Manager?")) {
      try {
        await ApiService.deleteUser(userId);
        showMessage("Manager successfully deleted");

        // Update UI without reload
        setAllUsers((prevUsers) => prevUsers.filter((user) => user.id !== userId));
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Error deleting Manager: " + error
        );
      }
    }
  };



  const handleEdit = (userId) => {
    navigate(`/edit-manager/${userId}`);
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };



  return (
    <Layout>
      <div className="transactions-page">
        <div className="product-header">
          <h1>Managers</h1>
          <button
            className="add-product-btn"
            onClick={() => navigate("/add-manager")}
          >
            Add Manager
          </button>
        </div>

        {message && <div className="message">{message}</div>}

        <table className="transactions-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Phone Number</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {Array.isArray(allUsers) &&
              allUsers.map((user) => (
                <tr key={user.id}>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td>{user.phoneNumber}</td>
                  <td>
                    <div className="button-group1">
                      <button onClick={() => handleEdit(user.id)}>Edit</button>
                      <button onClick={() => handleDeleteManager(user.id)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>
    </Layout>
  );
};





export default ManagerPage;
