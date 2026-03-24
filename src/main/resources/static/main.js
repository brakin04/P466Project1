// JS for search and other
document.addEventListener("DOMContentLoaded", () => {

    // Sorting speeches
    const tableBody = document.querySelector("tbody");
    if (tableBody) {
        const allRows = Array.from(tableBody.querySelectorAll("tr"));

        const sortTable = (criteria) => {
            const sortedRows = [...allRows].sort((a, b) => {
                // Get text from criteria cells
                let valA = a.cells[criteria].innerText.toLowerCase();
                let valB = b.cells[criteria].innerText.toLowerCase();

                // Audio
                if (criteria === 4) {
                    return valA === valB ? 0 : valA ? -1 : 1;
                }
                // Date
                if (criteria === 0) {
                    valA = valA.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$3/$2/$1");
                    valB = valB.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$3/$2/$1");
                }
                
                return valA.localeCompare(valB);
            });

            // Clear and Re-append
            tableBody.innerHTML = "";
            sortedRows.forEach(row => tableBody.appendChild(row));
        };

        // Event Listeners (assuming index 0=Date, 1=Topic, 2=Speaker, 4=Audio)
        document.getElementById("srtDate")?.addEventListener("click", () => sortTable(0));
        document.getElementById("srtTopic")?.addEventListener("click", () => sortTable(1));
        document.getElementById("srtSpeak")?.addEventListener("click", () => sortTable(2));
        document.getElementById("srtAudio")?.addEventListener("click", () => sortTable(4));

    }

    const addSpeechForm = document.getElementById("addSpeech");
    const editSpeechForm = document.getElementById("editSpeech");
    const addSpeechBtn = document.getElementById("addSpeechBtn");
    const addSpeechDiv = document.getElementById("addSpeechDiv");
    const submitBtn = document.getElementById("submitBtn");
    const dateInput = document.getElementById("dateInput");
    
    // Only run this logic if the elements actually exist (Admin Page)
    if (addSpeechBtn && addSpeechDiv) {
        addSpeechDiv.style.display = 'none';
        addSpeechBtn.style.display = 'inline-block';
        addSpeechBtn.addEventListener("click", () => {
            addSpeechDiv.style.display = 'block';
            addSpeechBtn.style.display = 'none';
        });
    }

    // Check form input completion and validity
    if (addSpeechForm || editSpeechForm) {
        let text = "";
        let allInputs;
        if (addSpeechForm) {
            allInputs = Array.from(addSpeechForm.querySelectorAll("input, textarea"));
            text = "Add Speech";
            addSpeechForm.addEventListener("input", () => validateForm(text, allInputs));
        } else {
            allInputs = Array.from(editSpeechForm.querySelectorAll("input, textarea"));
            text = "Update";
            editSpeechForm.addEventListener("input", () => validateForm(text, allInputs));
        }
    }

    // Validate the form
    const validateForm = (text, allInputs) => {
        const dateRegex = /^(0[0-9]||1[0-2])\/\d{2}\/\d{4}$/;
        let allFilled = true;
        for (const input of allInputs) {
            if (input.name !== "audioFile") {
                if (input.value === "") {
                    allFilled = false;
                    break;
                }
            }
        }
        const isDateValid = dateRegex.test(dateInput.value);
        if (dateInput.value === "") {
            dateInput.style.color = ""; // Default
        } else {
            dateInput.style.color = isDateValid ? "black" : "red";
        }
        submitBtn.disabled = !(allFilled && isDateValid);
        if (submitBtn.disabled) {
            submitBtn.style.backgroundColor = 'red';
            submitBtn.textContent = "Incomplete";
        } else {
            submitBtn.style.backgroundColor = 'lightskyblue';
            submitBtn.textContent = text;
        }
    };

});

function confirmDelete() {
    answer = prompt(`Type DELETE to confirm`);
    if (answer === "DELETE") {
        return true; 
    }
    return false;
}