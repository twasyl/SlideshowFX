/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This method execute a code snippet in SlideshowFX. The code snippet is given through the codeSnippet parameter encoded
 * in Bas64, and the executor is contained in the value of the snippetExecutorCode.
 * The console output of the execution will be re-render in the element identified by the consoleElementId.
 * The consoleElementId is the timestamp ending the console ouput and code.
 * This methods retrieves both content and send it to SlideshowFX which will execute it.
 * The code snippet is sent in Base64.
 */
function executeCodeSnippet(snippetExecutorCode, codeSnippet, codeSnippetId) {
    // Clear the previous console output if any
    var codeSnippetOutputElement = document.getElementById("code-snippet-output-" + codeSnippetId);

    if(codeSnippetOutputElement != undefined) {
        codeSnippetOutputElement.style.display = "block";

        // Hide the code snippet
        var codeSnippetConsoleElement =  document.getElementById("code-snippet-console-" + codeSnippetId);
        codeSnippetConsoleElement.style.display = "none";

        // Clear the previous output
        var outputContent = document.querySelector("#code-snippet-output-" + codeSnippetId + " code");
        outputContent.innerHTML = '';

        // Insert the refresh button if not already present
        if(document.getElementById("code-snippet-refresh-" + codeSnippetId) == undefined) {
            var refreshButton = document.createElement("i");
            refreshButton.id = "code-snippet-refresh-" + codeSnippetId;
            refreshButton.className = "fa fa-refresh fa-fw";
            refreshButton.onclick = function() {
                codeSnippetOutputElement.style.display = "none";
                codeSnippetConsoleElement.style.display = "block";
                refreshButton.parentElement.removeChild(refreshButton);
            };

            var executeButton = document.getElementById("code-snippet-execute-" + codeSnippetId);
            executeButton.parentElement.appendChild(refreshButton);
        }
    }

    sfx.executeCodeSnippet(snippetExecutorCode,
                           codeSnippet,
                           codeSnippetId);
}

/*
 * This method updates a given console with a given output. The console is identified by consoleId and the output by
 * consoleOutput.
 * The console output is sent in Base64 to this method.
 */
function updateCodeSnippetConsole(consoleId, consoleOutput) {
    var consoleElement = document.querySelector("#code-snippet-output-" + consoleId + " code");

    // Ensure the console exists
    if(consoleElement != undefined) {
        consoleElement.innerHTML = consoleElement.innerHTML
                                    + decodeURIComponent(escape(window.atob(consoleOutput)))
                                    + '<br />';
    }
}