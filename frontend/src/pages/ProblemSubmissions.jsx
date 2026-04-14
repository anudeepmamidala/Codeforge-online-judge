import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getProblemSubmissionsApi } from "../api/submissionApi";

export default function ProblemSubmissions() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [submissions, setSubmissions] = useState([]);

  useEffect(() => {
    getProblemSubmissionsApi(id)
      .then(data => {
        console.log("API RESPONSE:", data);
        setSubmissions(Array.isArray(data) ? data : []);
      })
      .catch(err => console.error(err));
  }, [id]);

  return (
    <div style={{ padding: "20px" }}>
      <h2>Submissions for Problem {id}</h2>

      {submissions.length === 0 ? (
        <p>No submissions found</p>
      ) : (
        <table border="1" cellPadding="10" style={{ marginTop: "20px", width: "100%" }}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Status</th>
              <th>Output</th>
            </tr>
          </thead>

          <tbody>
            {submissions.map(sub => (
              <tr
                key={sub.submissionId} // ✅ FIX
                style={{ cursor: "pointer" }}
                onClick={() => navigate(`/submissions/${sub.submissionId}`)} // ✅ CLICK FIX
              >
                <td>{sub.submissionId}</td> {/* ✅ FIX */}
                <td>{sub.status}</td>
                <td>{sub.output}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}