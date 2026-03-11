import { useState } from "react";

type Status = "idle" | "loading" | "success" | "error";

function App() {
  const [file, setFile] = useState<File | null>(null);
  const [email, setEmail] = useState("");
  const [instructions, setInstructions] = useState("");
  const [status, setStatus] = useState<Status>("idle");
  const [message, setMessage] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      setMessage("Please upload a CSV/XLSX file.");
      setStatus("error");
      return;
    }
    if (!email) {
      setMessage("Please enter recipient email.");
      setStatus("error");
      return;
    }

    setStatus("loading");
    setMessage("");

    const metadata = { recipientEmail: email, subject: "", instructions };
    const formData = new FormData();
    formData.append("file", file);
    formData.append(
      "metadata",
      new Blob([JSON.stringify(metadata)], { type: "application/json" })
    );

    try {
      const res = await fetch(
        `${import.meta.env.VITE_API_URL}/api/v1/summaries`,
        {
          method: "POST",
          headers: {
            "X-API-KEY": import.meta.env.VITE_API_KEY as string,
          },
          body: formData,
        }
      );

      if (!res.ok) {
        throw new Error(`Request failed with status ${res.status}`);
      }
      const data = await res.json();
      setStatus("success");
      setMessage(
        `Summary request ${data.requestId} sent to ${data.recipientEmail}.`
      );
    } catch (err: any) {
      setStatus("error");
      setMessage(err.message || "Unexpected error occurred.");
    }
  };

  return (
    <div className="app">
      <div className="card">
        <h1>Sales Insight Automator</h1>
        <p>Upload your quarterly sales file and email an AI summary.</p>

        <form onSubmit={handleSubmit}>
          <label>
            Sales file (.csv / .xlsx)
            <input
              type="file"
              accept=".csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
            />
          </label>

          <label>
            Recipient email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="exec@example.com"
            />
          </label>

          <label>
            Optional instructions
            <textarea
              value={instructions}
              onChange={(e) => setInstructions(e.target.value)}
              placeholder="E.g. Focus on North region, highlight risks."
            />
          </label>

          <button type="submit" disabled={status === "loading"}>
            {status === "loading" ? "Generating..." : "Send Summary"}
          </button>
        </form>

        {status === "loading" && <div className="info">Processing...</div>}
        {status === "success" && <div className="success">{message}</div>}
        {status === "error" && <div className="error">{message}</div>}
      </div>
    </div>
  );
}

export default App;

