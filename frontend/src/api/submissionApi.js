import api from "./Axios";

// 🔹 SUBMIT CODE
export const submitCodeApi = async ({ problemId, code, language }) => {
  if (!problemId) throw new Error("problemId is required");
  if (!code) throw new Error("code is required");

  const res = await api.post("/submissions", {
    problemId,
    code,
    language
  });

  return res.data.data;
};


// 🔹 GET SUBMISSION DETAILS (with testcase results)
export const getSubmissionDetailApi = async (submissionId) => {
  if (!submissionId) throw new Error("submissionId is required");

  const res = await api.get(`/submissions/${submissionId}`);
  return res.data.data;
};


// 🔹 GET ALL USER SUBMISSIONS
export const getMySubmissionsApi = async () => {
  const res = await api.get("/submissions/my");
  return res.data.data;
};


// 🔥 NEW — GET SUBMISSIONS BY PROBLEM
export const getProblemSubmissionsApi = async (problemId) => {
  if (!problemId) throw new Error("problemId is required");

  const res = await api.get(`/submissions/problem/${problemId}`);
  return res.data.data;
};


// 🔹 POLLING (for live result updates)
export const pollSubmissionApi = async (
  submissionId,
  onUpdate,
  maxAttempts = 20
) => {
  let attempts = 0;

  return new Promise((resolve, reject) => {
    const interval = setInterval(async () => {
      try {
        const data = await getSubmissionDetailApi(submissionId);

        onUpdate(data);
        attempts++;

        if (
          !["PENDING", "RUNNING"].includes(data.status) ||
          attempts >= maxAttempts
        ) {
          clearInterval(interval);
          resolve(data);
        }

      } catch (err) {
        clearInterval(interval);
        reject(err);
      }
    }, 2000);
  });
};