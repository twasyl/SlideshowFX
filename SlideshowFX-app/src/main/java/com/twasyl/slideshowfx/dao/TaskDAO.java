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

package com.twasyl.slideshowfx.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

import java.util.logging.Logger;

/**
 * This class provides access for all tasks that are running or scheduled in the application. This class acts as a
 * singleton. To get the instance call {@link TaskDAO#getInstance()}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class TaskDAO {
    private static final Logger LOGGER = Logger.getLogger(TaskDAO.class.getName());
    private static final TaskDAO singleton = new TaskDAO();
    private final ObservableList<Task> currentTasks = FXCollections.observableArrayList();


    private TaskDAO() {
    }

    public static TaskDAO getInstance() { return singleton; }

    /**
     * Start the given task and add it to the list of current tasks.
     * @param task The task to start.
     * @throws java.lang.NullPointerException If the given task is null.
     */
    public void startTask(final Task task) {
        if(task == null) throw new NullPointerException("The task to start can not be null");

        synchronized(this.currentTasks) {
            this.currentTasks.add(task);
        }

        task.stateProperty().addListener((value, oldState, newState) -> {
            if(newState != null && (
                    newState == Worker.State.CANCELLED ||
                    newState == Worker.State.SUCCEEDED ||
                    newState == Worker.State.FAILED)) {
                synchronized (this.currentTasks) {
                    this.currentTasks.remove(task);
                }
            }
        });

        new Thread(task).start();
    }
}
