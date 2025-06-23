import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate, useParams } from "react-router-dom";
import html2pdf from "html2pdf.js";



const TransactionDetailsPage = () => {
  const { transactionId } = useParams();
  const [transaction, setTransaction] = useState(null);
  const [message, setMessage] = useState("");
  const [status, setStatus] = useState("");

  const navigate = useNavigate();

  // Convert image URL to base64
  const toBase64 = (url) =>
    fetch(url)
      .then((response) => response.blob())
      .then(
        (blob) =>
          new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(blob);
          })
      );

  

 useEffect(() => {
    const getTransaction = async () => {
      try {
        const response = await ApiService.getTransactionById(transactionId);
        if (response.status === 200) {
          const trx = response.transaction;

          // Convert product image URL to base64 if exists
          if (trx.product?.imageUrl) {
            trx.product.base64Image = await toBase64(trx.product.imageUrl);
          }

          setTransaction(trx);
          setStatus(trx.status);
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Error Getting transaction: " + error
        );
      }
    };

    getTransaction();
  }, [transactionId]);


  //update transaction status
  const handleUpdateStatus = async () => {
    try {
      ApiService.updateTransactionStatus(transactionId, status);
      navigate("/transaction")
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Error Updating a transactions: " + error
      );

    }
  }

  const downloadPDF = () => {
    const element = document.getElementById("transaction-pdf-content");

    const images = element.querySelectorAll("img");
    const loadPromises = Array.from(images).map((img) => {
      return new Promise((resolve) => {
        if (img.complete) resolve();
        else img.onload = img.onerror = resolve;
      });
    });

    Promise.all(loadPromises).then(() => {
      html2pdf()
        .set({
          margin: 0.5,
          filename: `transaction_${transactionId}.pdf`,
          image: { type: "jpeg", quality: 0.98 },
          html2canvas: { scale: 2 },
          jsPDF: { unit: "in", format: "a4", orientation: "portrait" }
        })
        .from(element)
        .save();
    });
  };


  //Method to show message or errors
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };




return (
    <Layout>
      {message && <p className="message">{message}</p>}
      <div className="transaction-details-page">
        {transaction && (
          <>
            
            <div className="section-card" id="transaction-controls">
              <label>Status: </label>
              <select value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="PENDING">PENDING</option>
                <option value="PROCESSING">PROCESSING</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
              <div className="button-group1">
                <button onClick={handleUpdateStatus}>Update Status</button>
              <button  posi onClick={downloadPDF}>Download as PDF</button>
              </div>
            </div>

            {/* PDF content */}
            <div id="transaction-pdf-content" className="pdf-content">
              <div className="section-card">
                <h2>Transaction Information</h2>
                <p>Type: {transaction.transactionType}</p>
                <p>Status: {transaction.status}</p>
                <p>Description: {transaction.description}</p>
                <p>Note: {transaction.note}</p>
                <p>Total Products: {transaction.totalProducts}</p>
                <p>Total Price: ₹{transaction.totalPrice.toFixed(2)}</p>
                <p>Created At: {new Date(transaction.createdAt).toLocaleString()}</p>
                {transaction.updatedAt && (
                  <p>Updated At: {new Date(transaction.updatedAt).toLocaleString()}</p>
                )}
              </div>

              <div className="section-card">
                <h2>Product Information</h2>
                <p>Name: {transaction.product.name}</p>
                <p>SKU: {transaction.product.sku}</p>
                <p>Price: ₹{transaction.product.price.toFixed(2)}</p>
                <p>Stock Quantity: {transaction.product.stockQuantity}</p>
                <p>Description: {transaction.product.description}</p>
                {transaction.product.imageUrl && (
                  <img
                    src={transaction.product.base64Image}
                    alt={transaction.product.name}
                    className="product-image"
                  />
                )}
              </div>

              <div className="section-card">
                <h2>User Information</h2>
                <p>Name: {transaction.user.name}</p>
                <p>Email: {transaction.user.email}</p>
                <p>Phone Number: {transaction.user.phoneNumber}</p>
                <p>Role: {transaction.user.role}</p>
              </div>

              {transaction.supplier && (
                <div className="section-card">
                  <h2>Supplier Information</h2>
                  <p>Name: {transaction.supplier.name}</p>
                  <p>Contact: {transaction.supplier.contactInfo}</p>
                  <p>Address: {transaction.supplier.address}</p>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </Layout>
  );
};

export default TransactionDetailsPage;

