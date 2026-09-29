async function reviewCode() {

    const codeInput = document.getElementById("codeInput");
    const result = document.getElementById("result");
    const loading = document.getElementById("loading");
    const button = document.getElementById("reviewBtn");

    const code = codeInput.value;

    if (!code.trim()) {
        result.textContent = "⚠️ Please enter some code first.";
        return;
    }

    loading.classList.remove("hidden");
    button.disabled = true;
    button.textContent = "Analyzing...";

    try {

        const response = await fetch("/review", {
            method: "POST",
            headers: {
                "Content-Type": "text/plain;charset=UTF-8"
            },
            body: code
        });

        const data = await response.text();

        if (!response.ok) {
            throw new Error(data);
        }

        result.textContent = data;

    } catch (error) {

        console.error("Review error:", error);

        result.textContent =
            "❌ Review request failed.\n\n" +
            error.message;

    } finally {

        loading.classList.add("hidden");
        button.disabled = false;
        button.textContent = "🔍 Review Code";
    }
}