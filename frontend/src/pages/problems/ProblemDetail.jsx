import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getProblemByIdApi } from "../../api/problemApi";
import { submitCodeApi, pollSubmissionApi } from "../../api/submissionApi";
import { getPublicTestcasesApi } from "../../api/testcaseApi";
import Editor from "@monaco-editor/react";
import "./Problems.css";

const LANGUAGES = [
  { value: "python", label: "Python 3.9", monacoLang: "python" },
  { value: "java",   label: "Java 17",    monacoLang: "java"   },
  { value: "cpp",    label: "C++",        monacoLang: "cpp"    },
];

const ProblemDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [problem, setProblem]       = useState(null);
  const [code, setCode]             = useState("");
  const [language, setLanguage]     = useState("python");
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult]         = useState(null);
  const [error, setError]           = useState("");
  const [testcases, setTestcases]   = useState([]);
  const [loading, setLoading]       = useState(true);
  const [pollStatus, setPollStatus] = useState("");

  useEffect(() => {
    const fetchProblem = async () => {
      try {
        const data = await getProblemByIdApi(id);
        setProblem(data);
      } catch {
        setError("Failed to load problem");
      } finally {
        setLoading(false);
      }
    };
    fetchProblem();
  }, [id]);

  useEffect(() => {
    const fetchTestcases = async () => {
      try {
        const data = await getPublicTestcasesApi(id);
        setTestcases(data);
      } catch (e) {
        console.error("Failed to load testcases", e);
      }
    };
    if (id) fetchTestcases();
  }, [id]);

  const handleSubmit = async () => {
    if (!code.trim()) { alert("Code cannot be empty"); return; }

    setSubmitting(true);
    setError("");
    setResult(null);
    setPollStatus("Queuing submission...");

    try {
      const submission = await submitCodeApi({ problemId: id, code, language });

      setPollStatus("Judging...");

      let attempt = 0;
      await pollSubmissionApi(
        submission.submissionId,
        (data) => {
          attempt++;
          setPollStatus(`Judging... (${attempt * 2}s)`);
          setResult(data);
        }
      );

    } catch {
      setError("Submission failed. Please try again.");
    } finally {
      setSubmitting(false);
      setPollStatus("");
    }
  };

  const selectedLang = LANGUAGES.find((l) => l.value === language);

  if (loading) {
    return (
      <div className="problem-detail-page">
        <div className="container section flex-center" style={{ minHeight: "500px" }}>
          <div className="loader"></div>
        </div>
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="problem-detail-page">
        <div className="container section">
          <div className="alert alert-error">Problem not found</div>
        </div>
      </div>
    );
  }

  return (
    <div className="problem-detail-page">
      <div className="container section">

        {/* HEADER */}
        <div className="problem-header">
          <div style={{ display: "flex", gap: "10px" }}>
            <button
              className="btn btn-outline btn-small"
              onClick={() => navigate("/problems")}
            >
              ← Back
            </button>

            {/* 🔥 NEW BUTTON */}
            <button
              className="btn btn-outline btn-small"
              onClick={() => navigate(`/problems/${id}/submissions`)}
            >
              📄 View Submissions
            </button>
          </div>

          <div>
            <h1>{problem.title}</h1>
            <div className="problem-meta">
              <span className={`difficulty-badge difficulty-${problem.difficulty.toLowerCase()}`}>
                {problem.difficulty}
              </span>
              <span className="problem-id">Problem #{problem.id}</span>
            </div>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        {/* MAIN LAYOUT */}
        <div className="problem-layout">

          {/* LEFT PANEL */}
          <div className="problem-panel description-panel">
            <div className="panel-content">
              <div className="section-block">
                <h2 className="section-title">📝 Description</h2>
                <p className="description-text">{problem.description}</p>
              </div>

              {testcases.length > 0 && (
                <div className="section-block">
                  <h2 className="section-title">📋 Sample Testcases</h2>
                  <div className="testcases-list">
                    {testcases.map((tc, idx) => (
                      <div key={idx} className="testcase-card">
                        <div className="testcase-header">
                          <h4>Test Case {idx + 1}</h4>
                        </div>
                        <div className="testcase-content">
                          <div className="testcase-input">
                            <label>Input:</label>
                            <pre><code>{tc.input}</code></pre>
                          </div>
                          <div className="testcase-output">
                            <label>Expected Output:</label>
                            <pre><code>{tc.expectedOutput}</code></pre>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* RIGHT PANEL */}
          <div className="problem-panel editor-panel">
            <div className="panel-header">
              <h2 className="section-title">💾 Solution</h2>

              <select
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
                disabled={submitting}
                className="language-select"
              >
                {LANGUAGES.map((l) => (
                  <option key={l.value} value={l.value}>{l.label}</option>
                ))}
              </select>
            </div>

            <div className="editor-container">
              <Editor
                height="500px"
                language={selectedLang.monacoLang}
                theme="vs-dark"
                value={code}
                onChange={(value) => setCode(value || "")}
                options={{
                  fontSize: 14,
                  minimap: { enabled: false },
                  readOnly: submitting,
                  wordWrap: "on",
                }}
              />
            </div>

            <div className="submit-section">
              <button
                className="btn btn-primary submit-btn"
                onClick={handleSubmit}
                disabled={submitting}
              >
                {submitting ? (pollStatus || "Submitting...") : "✓ Submit Solution"}
              </button>
            </div>
          </div>
        </div>

        {/* RESULT */}
        {result && (
          <div className="result-section">
            <h2 className="section-title">📊 Submission Result</h2>
            <div className={`result-card result-${result.status?.toLowerCase()}`}>
              <div className="result-header">
                <span className="result-status">{result.status}</span>
              </div>
              <div className="result-details">
                <strong>Output:</strong>
                <p>{result.output}</p>
              </div>

              <button
                className="btn btn-secondary btn-small"
                onClick={() => navigate(`/submissions/${result.submissionId}`)}
              >
                View Details
              </button>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default ProblemDetail;
