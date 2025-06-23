import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate } from "react-router-dom";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import PaginationComponent from "../component/PaginationComponent";

const TransactionsPage = () => {
  const [transactions, setTransactions] = useState([]);
  const [message, setMessage] = useState("");
  const [filter, setFilter] = useState("");
  const [valueToSearch, setValueToSearch] = useState("");

  const [typeFilter, setTypeFilter] = useState("");
  const [dateFilter, setDateFilter] = useState("");

  const navigate = useNavigate();

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const itemsPerPage = 10;

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const response = await ApiService.getAllTransactions(valueToSearch);
        if (response.status === 200) {
          let filtered = response.transactions;

          // Filter by type
          if (typeFilter) {
            filtered = filtered.filter(tx => tx.transactionType === typeFilter);
          }

          // Filter by date
          if (dateFilter) {
            filtered = filtered.filter(tx =>
              new Date(tx.createdAt).toISOString().slice(0, 10) === dateFilter
            );
          }

          setTotalPages(Math.ceil(filtered.length / itemsPerPage));
          setTransactions(filtered.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage));
        }
      } catch (error) {
        showMessage(error.response?.data?.message || "Error getting transactions");
      }
    };

    fetchTransactions();
  }, [currentPage, valueToSearch, typeFilter, dateFilter]);

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  const handleSearch = () => {
    setCurrentPage(1);
    setValueToSearch(filter);
  };

  const navigateToTransactionDetailsPage = (id) => {
    navigate(`/transaction/${id}`);
  };

  const getBase64ImageFromUrl = async (url) => {
    const res = await fetch(url);
    const blob = await res.blob();
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve(reader.result);
      reader.readAsDataURL(blob);
    });
  };

  const downloadAllTransactionsPDF = async () => {
    try {
      const response = await ApiService.getAllTransactions(valueToSearch);
      if (response.status !== 200) throw new Error("Failed to fetch");

      let allTransactions = response.transactions;

      if (typeFilter) {
        allTransactions = allTransactions.filter(tx => tx.transactionType === typeFilter);
      }

      if (dateFilter) {
        allTransactions = allTransactions.filter(tx =>
          new Date(tx.createdAt).toISOString().slice(0, 10) === dateFilter
        );
      }

      const doc = new jsPDF();

      const logoUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTID9aUxYJUv66MhnZ4IlmkxE-OQWxoikm8XA&s";
      const logoBase64 = await getBase64ImageFromUrl(logoUrl);
      doc.addImage(logoBase64, "PNG", 10, 10, 30, 15);

      doc.setFontSize(12);
      doc.text("All Transactions Report", 50, 20);

      const tableColumn = ["Product", "Date", "Type", "Quantity", "Unit Price", "Total Price"];

      const tableRows = allTransactions.map(tx => [
        tx.product?.name || "NA",
        new Date(tx.createdAt).toLocaleDateString(),
        tx.transactionType,
        tx.totalProducts,
        tx.product?.price?.toFixed(2) || "0.00",
        tx.totalPrice?.toFixed(2) || "0.00",
      ]);

      const totalQuantity = allTransactions.reduce((sum, tx) => sum + tx.totalProducts, 0);
      const totalPrice = allTransactions.reduce((sum, tx) => sum + parseFloat(tx.totalPrice || 0), 0);

      autoTable(doc, {
        startY: 30,
        head: [tableColumn],
        body: tableRows,
        foot: [["", "", "Total", totalQuantity, "", totalPrice.toFixed(2)]],
        theme: "striped",
      });

      window.open(doc.output("bloburl"), "_blank");
    } catch (error) {
      console.error("PDF Error:", error);
      showMessage("Error generating PDF");
    }
  };

  return (
    <Layout>
      {message && <p className="message">{message}</p>}

      <div className="transactions-page">
        <div className="transactions-header">
          <h1>Transactions</h1>
          <div className="transaction-search">
            <input
              type="text"
              placeholder="Search by name or note..."
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
            />
            <input
              type="date"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
            />
            <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
              <option value="">All Types</option>
              <option value="PURCHASE">PURCHASE</option>
              <option value="SALE">SALE</option>
              <option value="RETURN_TO_SUPPLIER">RETURN TO SUPPLIER</option>
            </select>
            <button onClick={handleSearch}>Search</button>
            <button onClick={downloadAllTransactionsPDF}>Download</button>
          </div>
        </div>

        {transactions && (
          <table className="transactions-table">
            <thead>
              <tr>
                <th>TYPE</th>
                <th>STATUS</th>
                <th>TOTAL PRICE</th>
                <th>TOTAL PRODUCTS</th>
                <th>DATE</th>
                <th>ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => (
                <tr key={tx.id}>
                  <td>{tx.transactionType}</td>
                  <td>{tx.status}</td>
                  <td>{tx.totalPrice}</td>
                  <td>{tx.totalProducts}</td>
                  <td>{new Date(tx.createdAt).toLocaleString()}</td>
                  <td>
                    <button onClick={() => navigateToTransactionDetailsPage(tx.id)}>
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <PaginationComponent
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </Layout>
  );
};

export default TransactionsPage;
