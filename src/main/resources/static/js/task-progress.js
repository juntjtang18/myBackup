(function() {
    let tasks = {};

    function initTaskProgressPanel(targetElementId) {
        // Get the parent element where progress bars will be attached
        const parentElement = document.getElementById(targetElementId);
        if (!parentElement) {
            console.error(`Element with ID '${targetElementId}' not found.`);
            return;
        }

        // WebSocket setup
        const socket = new SockJS('/backup-progress-websocket');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            // Subscribe to the backup progress topic
            stompClient.subscribe('/topic/backup-progress', function(message) {
                const event = JSON.parse(message.body);
                updateBackupTasks(event, targetElementId); // Pass targetElementId here
            });
        });
    }

    // Function to update ongoing backup tasks based on the event received
    function updateBackupTasks(data, targetElementId) { // Accept targetElementId as a parameter
        console.log('Received event:', data);
        const taskId = data.backupTask.taskId; // Replace with actual field in your BackupTask class

        if (tasks[taskId]) {
            // Update existing task
            tasks[taskId].progressPercentage = data.progressPercentage;
            updateProgressBar(taskId, data.progressPercentage, data.backupTask.status);
        } else {
            // Add new task
            tasks[taskId] = {
                srcDirectory: data.backupTask.srcDir,
                destDirectory: data.backupTask.dstDir,
                progressPercentage: data.progressPercentage,
                status: data.backupTask.status
            };

            addNewProgressBar(taskId, data.backupTask.srcDir, data.backupTask.dstDir, targetElementId);
            updateProgressBar(taskId, data.progressPercentage, data.backupTask.status);
        }
    }

    // Update progress bar and add new progress bar functions remain unchanged
    function updateProgressBar(taskId, progressPercentage, status) {
        const progressBar = document.getElementById(`progress-bar-${taskId}`);
        const progressRow = document.getElementById(`progress-row-${taskId}`);

        if (progressBar) {
            // Update the width and aria value
            progressBar.style.width = `${progressPercentage}%`;
            progressBar.setAttribute('aria-valuenow', progressPercentage);
            progressBar.textContent = `${progressPercentage}%`;
            
            // Check for failed status
            if (status === 'FAILED') {
                progressBar.classList.add('bg-danger'); // Add a warning class
                progressBar.textContent += ' - Failed!'; // Append failed text
                
                // Add an icon to indicate failure
                const failIcon = document.createElement('i');
                failIcon.className = 'fas fa-times-circle text-danger'; // Font Awesome icon
                document.getElementById(`icon-container-${taskId}`).appendChild(failIcon); // Append icon to the icon container
            } else {
                // Reset the class if the status is not failed
                progressBar.classList.remove('bg-danger');
            }
        }

        // If progress is 100%, set a timeout to delete the row
        if (progressPercentage === 100 && progressRow) {
            setTimeout(function() {
                progressRow.remove();  // Remove the row element after 5 seconds
            }, 5000); // 5 seconds delay
        }
    }

    function addNewProgressBar(taskId, srcDirectory, destDirectory, targetElementId) { // Accept targetElementId as a parameter
        const progressContainer = document.getElementById(targetElementId); // Use the passed targetElementId
        const newProgressBar = document.createElement('div');

        newProgressBar.className = 'row mt-2'; // Use Bootstrap's row class
        newProgressBar.id = 'progress-row-' + taskId; // Assign a unique ID to the row
        newProgressBar.innerHTML = `
            <div class="col-8">
                <div class="progress-info">
                    ${srcDirectory} <i class="fas fa-arrow-right"></i> ${destDirectory}
                </div>
            </div>
            <div class="col-3">
                <div class="progress">
                    <div id="progress-bar-${taskId}" class="progress-bar" role="progressbar" style="width: 0%;" aria-valuenow="0">
                        0%
                    </div>
                </div>
            </div>
            <div class="col-1">
                <div id="icon-container-${taskId}" class="icon-container">
                    <!-- Icon will be added here -->
                </div>
            </div>
        `;

        progressContainer.appendChild(newProgressBar);
    }

    // Expose the init function to the global scope
    window.initTaskProgressPanel = initTaskProgressPanel;
})();
