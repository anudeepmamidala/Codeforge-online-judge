import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getSubmissionDetailApi } from "../../api/submissionApi";
import Editor from "@monaco-editor/react";

const LANG_MAP = {
  python: "python",
  java: "java",
  cpp: "cpp"
};

const SubmissionDetail = () => {
  const { id } = useParams();

  const [submission, setSubmission] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;

    let interval;

    const fetchAndPoll = async () => {
      try {
        const data = await getSubmissionDetailApi(id);
        setSubmission(data);
        setLoading(false);

        // 🔥 polling if still running
        if (["PENDING", "RUNNING"].includes(data.status)) {
          interval = setInterval(async () => {
            try {
              const updated = await getSubmissionDetailApi(id);
              setSubmission(updated);

              if (!["PENDING", "RUNNING"].includes(updated.status)) {
                clearInterval(interval);
              }
            } catch {
              clearInterval(interval);
            }
          }, 2000);
        }
      } catch (err) {
        console.error(err);
        setError("Failed to load submission");
        setLoading(false);
      }
    };

    fetchAndPoll();

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [id]);

  if (loading) {
    return <div style={{ padding: 40 }}>Loading submission...</div>;
  }

  if (error) {
    return <div style={{ padding: 40, color: "red" }}>{error}</div>;
  }

  if (!submission) {
    return <div style={{ padding: 40 }}>No submission found</div>;
  }

  const monacoLang =
    LANG_MAP[submission.language?.toLowerCase()] || "python";

  const isProcessing =
    ["PENDING", "RUNNING"].includes(submission.status);

  return (
    <div style={{ maxWidth: 900, margin: "auto", padding: 24 }}>
      <h2>Submission #{submission.submissionId}</h2>

      <p>
        <b>Status:</b>{" "}
        <span
          style={{
            color:
              submission.status === "PASSED"
                ? "green"
                : submission.status === "FAILED" ||
                  submission.status === "ERROR"
                ? "red"
                : "orange",
            fontWeight: "bold"
          }}
        >
          {submission.status} {isProcessing && "⏳"}
        </span>
      </p>

      <p><b>Language:</b> {submission.language}</p>
      <p><b>Result:</b> {submission.output}</p>
      <p><b>Submitted At:</b> {new Date(submission.createdAt).toLocaleString()}</p>

      {/* 🔥 CODE VIEW */}
      {submission.code && (
        <div style={{ marginTop: 20 }}>
          <h3>Submitted Code</h3>
          <Editor
            height="350px"
            language={monacoLang}
            theme="vs-dark"
            value={submission.code}
            options={{
              readOnly: true,
              fontSize: 14,
              minimap: { enabled: false }
            }}
          />
        </div>
      )}

      {/* 🔥 TESTCASE RESULTS (UPGRADED UI) */}
      {submission.testcaseResults &&
        submission.testcaseResults.length > 0 && (
          <div style={{ marginTop: 20 }}>
            <h3>Testcase Results</h3>

            {submission.testcaseResults.map((r, index) => (
              <div
                key={r.testcaseId}
                style={{
                  border: "1px solid #444",
                  borderRadius: "8px",
                  padding: "12px",
                  marginBottom: "12px",
                  background: "#1e1e1e"
                }}
              >
                <div
                  style={{
                    display: "flex",
                    color: "white",
                    justifyContent: "space-between"
                  }}
                >
                  <strong>Testcase {index + 1}</strong>

                  <span
                    style={{
                      color: r.passed ? "lightgreen" : "red",
                      fontWeight: "bold"
                    }}
                  >
                    {r.passed ? "✓ PASSED" : "✗ FAILED"}
                  </span>
                </div>

                {r.executionTime != null && (
                  <div
                    style={{
                      fontSize: "13px",
                      marginTop: "4px",
                      color: "#aaa"
                    }}
                  >
                    Time: {r.executionTime} ms
                  </div>
                )}

                {r.output && (
                  <div style={{ color: "white", marginTop: "8px" }}>
                    <b>Output:</b>
                    <pre
                      style={{
                        color: "white",
                        background: "#111",
                        padding: "6px",
                        borderRadius: "4px"
                      }}
                    >
                      {r.output}
                    </pre>
                  </div>
                )}

                {r.error?.trim() && (
                  <div style={{ marginTop: "8px", color: "red" }}>
                    <b>Error:</b>
                    <pre
                      style={{
                        background: "#111",
                        padding: "6px",
                        borderRadius: "4px"
                      }}
                    >
                      {r.error}
                    </pre>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
    </div>
  );
};

export default SubmissionDetail;
