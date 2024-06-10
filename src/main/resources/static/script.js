document.addEventListener('DOMContentLoaded', function() {
    const previewDatasetBtn = document.getElementById('previewDataset');
    const okayButton = document.getElementById('okayButton');
    const previewSection = document.getElementById('previewSection');
    const testCaseForm = document.getElementById('testCaseForm');
    const preprocessDatasetBtn = document.getElementById('preprocessDataset');
    const runTestCaseBtn = document.getElementById('runTestCase');
    const statusMessage = document.getElementById('statusMessage');
    const testcasesError = document.getElementById('testCaseError');
    const browserError = document.getElementById('browserError');
    const urlError = document.getElementById('urlError');
    const usernameError = document.getElementById('usernameError');
    const passwordError = document.getElementById('passwordError');
    const datasetInput = document.getElementById('dataset');

    const testCaseDropdown = document.getElementById('testCaseName');
    const datasetRow = document.querySelector('.row');
    const datasetLabel = datasetRow.querySelector('label');

    // Get the popup
    var popup = document.getElementById("popup");

    // Get the close button
    var closeBtn = document.getElementsByClassName("close-button")[0];

    // Event listener for form input validation
    testCaseForm.addEventListener('input', function() {
        const testcaseInput = document.getElementById('testCaseName').value;
        const browserInput = document.getElementById('browserName').value;
        const urlInput = document.getElementById('url').value;
        const usernameInput = document.getElementById('username').value;
        const passwordInput = document.getElementById('password').value;

        const isValid = testcaseInput && browserInput && urlInput && usernameInput && passwordInput;
        runTestCaseBtn.disabled = !isValid;
    });

    // Event listener for file input change
    datasetInput.addEventListener('change', function() {
        const isValid = datasetInput.checkValidity();
        preprocessDatasetBtn.disabled = !isValid;
        runTestCaseBtn.disabled = !isValid;
    });

    // Event listener for test case selection change
    testCaseDropdown.addEventListener('change', function() {
        const selectedTestCase = testCaseDropdown.value.trim();

        // Update dataset section based on selected test case
        if (selectedTestCase === 'Working Area Migration') {
            datasetLabel.textContent = 'Upload Working Area Dataset:';
        } else if (selectedTestCase === 'Employee Migration') {
            datasetLabel.textContent = 'Upload Employee Dataset:';
        }else if (selectedTestCase === 'Samity Migration') {
            datasetLabel.textContent = 'Upload Samity Dataset:';
        }
        else if (selectedTestCase === 'Member Migration') {
            datasetLabel.textContent = 'Upload Member Dataset:';
        }
        else if (selectedTestCase === 'Savings Migration') {
            datasetLabel.textContent = 'Upload Savings Dataset:';
        }
        else if (selectedTestCase === 'Loans Migration') {
            datasetLabel.textContent = 'Upload Loans Dataset:';
        }
    });


    // Event listener for preprocessing dataset
    preprocessDatasetBtn.addEventListener('click', function() {
        const file = datasetInput.files[0];
        const testcasesInput = document.getElementById('testCaseName');
        const testcase = testcasesInput.value.trim();
        testcasesError.innerText = "";
        let isValid = true;
        if (!testcase) {
            testcasesError.innerText = "Please select a migration.";
            isValid = false;
        }
        if (!file) {
            alert('Please select a file.');
            return;
        }
        const formData = new FormData();
        formData.append('testcase', testcase);
        formData.append('file', file);

        // Show SweetAlert with loading spinner
        Swal.fire({
            title: 'Processing dataset',
            html: '<div id="loader"></div>Please wait...',
            showConfirmButton: false, // Hide the confirm button while loading
            allowOutsideClick: false,
            onBeforeOpen: () => {
                const loader = document.getElementById('loader');
                loader.classList.add('loader');
            }
        });

        fetch('/datashift/preprocessDataset', {
            method: 'POST',
            body: formData,
        })
            .then(response => response.json())
            .then(data => {
            Swal.close();
            Swal.fire({
                icon: "success",
                title: data.message,
                showConfirmButton: true,
            });
            previewDatasetBtn.disabled = false;
        })
            .catch(error => {
            Swal.close();
            Swal.fire({
                icon: "error",
                title: "Error preprocessing dataset",
                text: error,
                showConfirmButton: true,
            });
        });
    });
    // Proceed with preprocessing
    //        alert('Preprocessing dataset...');
    //        setTimeout(function() {
    //            alert('Dataset preprocessing completed.');
    //            previewDatasetBtn.disabled = false;
    //            preprocessDatasetBtn.style.display = 'none'; // Hide preprocess button
    //        }, 2000);
    //    });

//     Event listener for previewing dataset
//     previewDatasetBtn.addEventListener('click', function() {
//         Swal.fire({
//             text: "Process Dataset",
//             showCancelButton: true,
//             confirmButtonColor: "#3085d6",
//             cancelButtonColor: "#d33",
//             confirmButtonText: "Process Again"
//         }).then((result) => {
//             if (result.isConfirmed) {
//                 Swal.fire({
////                     title: "Deleted!",
//                     text: "Please upload dataset again",
////                     icon: "success"
//                 });
//             }
//         });
//     });


    // Event listener for previewing dataset
    previewDatasetBtn.addEventListener('click', function() {
        popup.style.display = "block";
        loadExcelData();
    });

    // Close the popup when the close button is clicked
    closeBtn.onclick = function() {
        popup.style.display = "none";
        document.getElementById("excelTable").innerHTML = "";
    };

    // Close the popup when the user clicks outside of it
    window.onclick = function(event) {
        if (event.target === popup) {
            popup.style.display = "none";
            document.getElementById("excelTable").innerHTML = "";
        }
    };



    // Event listener for running test cases
    runTestCaseBtn.addEventListener('click', function() {
        // Validate inputs
        const testcasesInput = document.getElementById('testCaseName');
        const browserInput = document.getElementById('browserName');
        const urlInput = document.getElementById('url');
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');

        const testcase = testcasesInput.value.trim();
        const browser = browserInput.value.trim();
        const url = urlInput.value.trim();
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        // Clear previous error messages
        testcasesError.innerText = "";
        browserError.innerText = "";
        urlError.innerText = "";
        usernameError.innerText = "";
        passwordError.innerText = "";

        // Validate input and display error messages
        let isValid = true;
        const file = datasetInput.files[0];
        if (!file) {
            alert('Please select a file.');
            return;
        }

        if (!testcase) {
            testcasesError.innerText = "Please select a migration.";
            isValid = false;
        }

        if (!browser) {
            browserError.innerText = "Please select a browser.";
            isValid = false;
        }

        if (!url) {
            urlError.innerText = "Please enter a URL.";
            isValid = false;
        }

        if (!username) {
            usernameError.innerText = "Please enter a username.";
            isValid = false;
        }

        if (!password) {
            passwordError.innerText = "Please enter a password.";
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Show SweetAlert with loading spinner
        Swal.fire({
            title: 'Data Migrating',
            html: '<div id="loader"></div>Please wait...',
            showConfirmButton: false, // Hide the confirm button while loading
            allowOutsideClick: false,
            onBeforeOpen: () => {
                const loader = document.getElementById('loader');
                loader.classList.add('loader');
            }
        });

        // Send data to backend
        fetch('/datashift/runAutomation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                testcase: testcase,
                browser: browser,
                url: url,
                username: username,
                password: password
            })
        })
            .then(response => response.json())
            .then(data => {
            Swal.close();
            Swal.fire({
                title: data.status,
                text: "Do you want to view the report",
                icon: "success",
                showCancelButton: true,
                confirmButtonColor: "#3085d6",
                cancelButtonColor: "#d33",
                confirmButtonText: "Yes"
            }).then((result) => {
                if (result.isConfirmed) {
                    window.open(data.reportUrl, '_blank');
                }
            });

//            statusMessage.innerText = data.status;
        })
            .catch(error => {
            Swal.close();
            Swal.fire({
                icon: "error",
                title: "Error occurred during migration",
                text: error,
                showConfirmButton: true,
            });
        });
    });

    // Function to load and create the Excel table
    function loadExcelData() {
        const filePath = 'D:\\Automation\\DataShift\\dataset\\processed\\Member Migration processed dataset.xlsx'; // Example URL
        fetch(filePath)
            .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.blob();
        })
            .then(blob => {
            var reader = new FileReader();
            reader.onload = function(e) {
                var data = new Uint8Array(e.target.result);
                var workbook = XLSX.read(data, { type: 'array' });
                var worksheet = workbook.Sheets[workbook.SheetNames[0]];
                var jsonData = XLSX.utils.sheet_to_json(worksheet);
                createExcelTable(jsonData);
            };
            reader.readAsArrayBuffer(blob);
        })
            .catch(error => {
            console.error('Error loading Excel data:', error);
        });
    }


    // Function to create the Excel table
    function createExcelTable(data) {
        var table = document.createElement("table");
        var thead = document.createElement("thead");
        var tbody = document.createElement("tbody");

        // Create the table header
        var headerRow = document.createElement("tr");
        Object.keys(data[0]).forEach(function(key) {
            var th = document.createElement("th");
            th.textContent = key;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);

        // Create the table body
        data.forEach(function(row) {
            var tr = document.createElement("tr");
            Object.values(row).forEach(function(value) {
                var td = document.createElement("td");
                td.textContent = value;
                tr.appendChild(td);
            });
            tbody.appendChild(tr);
        });

        table.appendChild(thead);
        table.appendChild(tbody);
        document.getElementById("excelTable").appendChild(table);
    }


});
