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
    const mfiError = document.getElementById('mfiError');
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

    const processDataTitle = document.getElementById('processDataTitle');
    const totalUploadedRows = document.getElementById('totalUploaded');
    const totalCleanedRows = document.getElementById('totalCleaned');
    const totalIgnoredRows = document.getElementById('totalIgnored');

    // Download Button
    const mainBtn = document.querySelector('.main-btn');
    const cleanedBtn = document.getElementById('cleanedBtn');
    const ignoredBtn = document.getElementById('ignoredBtn');
    const popupBtns = [cleanedBtn, ignoredBtn];

    //Data Segmentation Start
    var modal = document.getElementById("myModal");
    var btn = document.getElementById("myBtn");
    var span = document.getElementsByClassName("close")[0];
    var loader = document.getElementById("loader");
    var validationMessage = document.getElementById("validationMessage");

    btn.onclick = function() {
        modal.style.display = "block";
    }

    span.onclick = function() {
        modal.style.display = "none";
    }

    window.onclick = function(event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    }

    document.getElementById('segmentButton').addEventListener('click', async function() {
        const fileInput = document.getElementById('mixData');
        const validationMessage = document.getElementById('validationMessage');
        const loader = document.getElementById('loader');

        validationMessage.textContent = ''; // Clear previous validation message

        if (fileInput.files.length === 0) {
            validationMessage.textContent = "Please select a file first.";
            return;
        }

        const formData = new FormData();
        formData.append('file', fileInput.files[0]);

        loader.style.display = "block"; // Show loader

        try {
            const response = await fetch('/datashift/upload/file', {
                method: 'POST',
                body: formData,
                timeout: 600000 // Set timeout to 10 minutes
            });

            if (response.ok) {
                const fileUrls = await response.json(); // Assuming the server returns the list of segmented file URLs with paths

                // Create a ZIP file using JSZip
                const zip = new JSZip();
                const rootFolderName = "Branch-wise Data Segmentation"; // Name of the root folder in the ZIP
                const rootFolder = zip.folder(rootFolderName); // Create root folder in ZIP

                for (let fileUrl of fileUrls) {
                    const filePath = fileUrl.replace('.\\dataset\\Branch-wise Data Segmentation', ''); // Adjust this to match your root folder path on the server
                    const response = await fetch(`/datashift/download/segment/file?fileName=${encodeURIComponent(filePath)}`);

                    if (response.ok) {
                        const blob = await response.blob();
                        rootFolder.file(filePath, blob); // Add file to the correct path in the ZIP
                    } else {
                        alert("Error downloading the file: " + fileUrl);
                        return;
                    }
                }

                const zipBlob = await zip.generateAsync({ type: "blob" });
                const zipFileName = rootFolderName + ".zip";
                const zipUrl = window.URL.createObjectURL(zipBlob);
                const link = document.createElement('a');
                link.href = zipUrl;
                link.download = zipFileName;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                window.URL.revokeObjectURL(zipUrl);

            } else {
                alert("Error processing the file.");
            }
        } catch (error) {
            console.error("Error:", error);
            alert("Error processing the file.");
        } finally {
            loader.style.display = "none"; // Hide loader
        }
        await fetch('/datashift/delete-directory', {
            method: 'DELETE',
        });
    });


    //Data Segmentation End

    // Event listener for form input validation
    testCaseForm.addEventListener('input', function() {
        const testcaseInput = document.getElementById('testCaseName').value;
        const browserInput = document.getElementById('browserName').value;
        const mfiInput = document.getElementById('mfi').value;
        const usernameInput = document.getElementById('username').value;
        const passwordInput = document.getElementById('password').value;

        const isValid = testcaseInput && browserInput && mfiInput && usernameInput && passwordInput;
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
            previewDatasetBtn.dataset.cleanedFilePath = data.cleanedFilePath;
            previewDatasetBtn.dataset.ignoredFilePath = data.ignoredFilePath;

            processDataTitle.textContent = `System Migration Information`;
            totalUploadedRows.textContent = `Total data of the uploaded file: ${data.totalUploadedRows}`;
            totalCleanedRows.textContent = `Total migratable data: ${data.totalCleanedRows}`;
            totalIgnoredRows.textContent = `Total non migratable data: ${data.totalIgnoredRows}`;
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

    // Event listener for previewing dataset
    previewDatasetBtn.addEventListener('click', function() {
        popup.style.display = "block";
        console.log('Loading Excel data:', previewDatasetBtn.dataset.filePath);
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
        const mfiInput = document.getElementById('mfi');
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');

        const testcase = testcasesInput.value.trim();
        const browser = browserInput.value.trim();
        const mfi = mfiInput.value.trim();
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        // Clear previous error messages
        testcasesError.innerText = "";
        browserError.innerText = "";
        mfiError.innerText = "";
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

        if (!mfi) {
            mfiError.innerText = "Please enter MFI name.";
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
                mfi: mfi,
                username: username,
                password: password
            })
        })
            .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || 'Internal server error');
                });
            }
            return response.json();
        })
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

    function loadExcelData() {
        const filePath = "/datashift/process-data";
        fetch(filePath)
            .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
            .then(data => {
            createExcelTable(data);
        })
            .catch(error => {
            console.error('Error loading Excel data:', error);
        });
    }

    function createExcelTable(data) {
        var table = document.createElement("table");
        var thead = document.createElement("thead");
        var tbody = document.createElement("tbody");
        var columns = Object.keys(data[0]);

        var headerRow = document.createElement("tr");
        columns.forEach(function(key) {
            var th = document.createElement("th");
            th.textContent = key;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);

        // Create the table body
        data.forEach(function(row) {
            var tr = document.createElement("tr");
            columns.forEach(function(column) {
                var td = document.createElement("td");
                td.textContent = row[column];
                tr.appendChild(td);
            });
            tbody.appendChild(tr);
        });

        table.appendChild(thead);
        table.appendChild(tbody);
        document.getElementById("excelTable").appendChild(table);
    }


    // Process Data Download functionality

    mainBtn.addEventListener('click', function() {
        popupBtns.forEach(btn => btn.classList.toggle('hidden'));
    });

    cleanedBtn.addEventListener('click', function() {
        downloadDataset('cleaned');
    });

    ignoredBtn.addEventListener('click', function() {
        downloadDataset('ignored');
    });

    function downloadDataset(type) {
        let path;
        if (type === 'cleaned') {
            path = previewDatasetBtn.dataset.cleanedFilePath;
        } else if (type === 'ignored') {
            path = previewDatasetBtn.dataset.ignoredFilePath;
        }
        const encodedPath = encodeURIComponent(path);
        const url = `/datashift/download?filePath=${encodedPath}`;
        const link = document.createElement('a');
        link.href = url;
        link.download = path.split('\\').pop(); // Adjusted to handle Windows file paths
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

});
