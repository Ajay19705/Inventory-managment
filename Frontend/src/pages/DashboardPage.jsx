
import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

const DashboardPage = () => {
  const [message, setMessage] = useState("");
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedData, setSelectedData] = useState("amount");
  const [transactionData, setTransactionData] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const transactionResponse = await ApiService.getAllTransactions();
        if (transactionResponse.status === 200) {
          setTransactionData(
            transformTransactionData(
              transactionResponse.transactions,
              selectedMonth,
              selectedYear
            )
          );
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Error fetching transactions: " + error
        );
      }
    };
    fetchData();
  }, [selectedMonth, selectedYear]);

  const transformTransactionData = (transactions, month, year) => {
    const dailyData = {};
    const daysInMonth = new Date(year, month, 0).getDate();

    for (let day = 1; day <= daysInMonth; day++) {
      dailyData[day] = {
        day,
        count: 0,
        quantity: 0,
        amount: 0,
        purchase: 0,
        sale: 0,
      };
    }

    transactions.forEach((transaction) => {
      const transactionDate = new Date(transaction.createdAt);
      const transactionMonth = transactionDate.getMonth() + 1;
      const transactionYear = transactionDate.getFullYear();

      if (transactionMonth === month && transactionYear === year) {
        const day = transactionDate.getDate();
        dailyData[day].count += 1;
        dailyData[day].quantity += transaction.totalProducts;
        dailyData[day].amount += transaction.totalPrice;

        if (transaction.transactionType === "PURCHASE") {
          dailyData[day].purchase += transaction.totalPrice;
        } else if (transaction.transactionType === "SALE") {
          dailyData[day].sale += transaction.totalPrice;
        }
      }
    });

    return Object.values(dailyData);
  };

  const handleMonthChange = (e) => {
    setSelectedMonth(parseInt(e.target.value, 10));
  };

  const handleYearChange = (e) => {
    setSelectedYear(parseInt(e.target.value, 10));
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
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

  const downloadPDF = async () => {
    const doc = new jsPDF();
    const logoUrl =
      "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTID9aUxYJUv66MhnZ4IlmkxE-OQWxoikm8XA&s";
    const logoBase64 = await getBase64ImageFromUrl(logoUrl);
    doc.addImage(logoBase64, "PNG", 10, 10, 30, 15);

    const title = `Monthly Transactions Report - ${new Date(0, selectedMonth - 1).toLocaleString(
      "default",
      { month: "long" }
    )} ${selectedYear}`;
    doc.setFontSize(12);
    doc.text(title, 50, 20);

    const tableColumn = ["Date", "Transactions", "Quantity", "Purchase", "Sale", "Amount"];
    const filteredData = transactionData.filter(
      (row) => row.count > 0 || row.quantity > 0 || row.amount > 0
    );

    const tableRows = filteredData.map((row) => [
      `${String(row.day).padStart(2, "0")}-${String(selectedMonth).padStart(2, "0")}-${selectedYear}`,
      row.count,
      row.quantity,
      row.purchase.toFixed(2),
      row.sale.toFixed(2),
      row.amount.toFixed(2),
    ]);

    const totalTransactions = filteredData.reduce((sum, row) => sum + row.count, 0);
    const totalQuantity = filteredData.reduce((sum, row) => sum + row.quantity, 0);
    const totalPurchase = filteredData.reduce((sum, row) => sum + row.purchase, 0);
    const totalSale = filteredData.reduce((sum, row) => sum + row.sale, 0);
    const totalAmount = filteredData.reduce((sum, row) => sum + row.amount, 0);

    if (tableRows.length === 0) {
      doc.text("No transactions found for this month.", 14, 50);
    } else {
      autoTable(doc, {
        startY: 30,
        head: [tableColumn],
        body: tableRows,
        foot: [
          [
            "Total",
            totalTransactions,
            totalQuantity,
            totalPurchase.toFixed(2),
            totalSale.toFixed(2),
            totalAmount.toFixed(2),
          ],
        ],
        theme: "grid",
        footStyles: { fillColor: [0, 128, 128] },
      });
    }

    window.open(doc.output("bloburl"), "_blank");
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="dashboard-page">
        <div className="button-group">
          <button onClick={() => setSelectedData("count")}>Total No Of Transactions</button>
          <button onClick={() => setSelectedData("quantity")}>Product Quantity</button>
          <button onClick={() => setSelectedData("amount")}>Amount</button>
          <button onClick={downloadPDF}>Download PDF</button>
        </div>

        <div className="dashboard-content">
          <div className="filter-section">
            <label htmlFor="month-select">Select Month:</label>
            <select id="month-select" value={selectedMonth} onChange={handleMonthChange}>
              {Array.from({ length: 12 }, (_, i) => (
                <option key={i + 1} value={i + 1}>
                  {new Date(0, i).toLocaleString("default", { month: "long" })}
                </option>
              ))}
            </select>

            <label htmlFor="year-select">Select Year:</label>
            <select id="year-select" value={selectedYear} onChange={handleYearChange}>
              {Array.from({ length: 5 }, (_, i) => {
                const year = new Date().getFullYear() - i;
                return (
                  <option key={year} value={year}>
                    {year}
                  </option>
                );
              })}
            </select>
          </div>

          <div className="chart-section">
            <div className="chart-container">
              <h3>DAILY TRANSACTION DETAILS</h3>
              <ResponsiveContainer width="100%" height={400}>
                <LineChart data={transactionData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="day"
                    label={{ value: "Day", position: "insideBottomRight", offset: -5 }}
                  />
                  <YAxis />
   
                  <Tooltip />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey={selectedData}
                    stroke="#008080"
                    fillOpacity={0.3}
                    fill="#008080"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default DashboardPage;
