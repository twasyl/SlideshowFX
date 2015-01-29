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
 * This methods retrieves both content and send it to SlideshowFX which will execute it.
 * The code snippet is sent in Base64.
 */
function executeCodeSnippet(snippetExecutorCode, codeSnippet, consoleElementId) {
    // Clear the previous console output if any
    var consoleElement = document.getElementById(consoleElementId);

    if(consoleElement != undefined) {
        consoleElement.innerHTML = '';
    }

    sfx.executeCodeSnippet(snippetExecutorCode,
                           codeSnippet,
                           consoleElementId);
}

/*
 * This method updates a given console with a given output. The console is identified by consoleId and the output by
 * consoleOutput.
 * The console output is sent in Base64 to this method.
 */
function updateCodeSnippetConsole(consoleId, consoleOutput) {
    var consoleElement = document.getElementById(consoleId);

    // Ensure the console exists
    if(consoleElement != undefined) {
        consoleElement.innerHTML = consoleElement.innerHTML
                                    + decodeURIComponent(escape(window.atob(consoleOutput)))
                                    + '<br />';
    }
}