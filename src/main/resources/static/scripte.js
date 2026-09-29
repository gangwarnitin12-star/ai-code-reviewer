async function reviewCode() {

    const code = document.getElementById("code").value;
    const btn = document.getElementById("reviewBtn");
    const result = document.getElementById("result");
    const compilerBox = document.getElementById("compilerBox");
    const analysisBox = document.getElementById("analysisBox");
    const reviewBox = document.getElementById("reviewBox");

    console.log("Review button clicked");

    if (!code.trim()) {
        alert("Please enter Java code.");
        return;
    }

    btn.disabled = true;
    btn.textContent = "⏳ Compiling...";

    result.classList.remove("hidden");
    analysisBox.classList.add("hidden");
    reviewBox.classList.add("hidden");

    try {

        const response = await fetch("/review", {
            method: "POST",
            headers: {
                "Content-Type": "text/plain"
            },
            body: code
        });

        console.log("Response status:", response.status);

        if (!response.ok) {
            throw new Error("Server returned HTTP " + response.status);
        }

        const data = await response.json();

        console.log("Review response:", data);

        if (data.compiled === true) {

            compilerBox.className = "resultBox success";

            compilerBox.innerHTML = `
                <h3>✅ Java Compiler</h3>
                <p>${escapeHtml(data.compilerMessage)}</p>
            `;

            analysisBox.className = "resultBox analysis";

            analysisBox.innerHTML = `
                <h3>🤖 AI Analysis</h3>
                <p>${escapeHtml(data.aiAnalysis)}</p>
            `;

            analysisBox.classList.remove("hidden");

            reviewBox.className = "resultBox review";

            reviewBox.innerHTML = `
                <h3>🔍 Review Results</h3>
                <pre>${escapeHtml(data.review || "")}</pre>
            `;

            reviewBox.classList.remove("hidden");

        } else {

            compilerBox.className = "resultBox error";

            const errors =
                (data.compilerErrors || []).join("\n");

            compilerBox.innerHTML = `
                <h3>❌ Java Compiler</h3>
                <p>${escapeHtml(data.compilerMessage || "")}</p>
                <pre>${escapeHtml(errors)}</pre>
            `;
        }

    } catch (error) {

        console.error("Review error:", error);

        compilerBox.className = "resultBox error";

        compilerBox.innerHTML = `
            <h3>❌ Request Error</h3>
            <p>${escapeHtml(error.message)}</p>
        `;

    } finally {

        btn.disabled = false;
        btn.textContent = "▶ Review Code";
    }
}


function escapeHtml(value) {

    return String(value).replace(
        /[&<>"']/g,
        function (character) {

            return {
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#039;"
            }[character];

        }
    );
}