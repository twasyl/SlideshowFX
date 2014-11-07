package com.twasyl.slideshowfx.dao;

import com.twasyl.slideshowfx.concurrent.SavePresentationTask;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides access for all tasks that are running or scheduled in the application. This class acts as a
 * singleton. To get the instance call {@link TaskDAO#getInstance()}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
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
