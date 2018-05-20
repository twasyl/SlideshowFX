package com.twasyl.slideshowfx.dao;

import com.twasyl.slideshowfx.utils.concurrent.SlideshowFXTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides access for all tasks that are running or scheduled in the application. This class acts as a
 * singleton. To get the instance call {@link TaskDAO#getInstance()}.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class TaskDAO {
    public interface StartAction {
        void execute(final SlideshowFXTask task);
    }

    private static final TaskDAO singleton = new TaskDAO();

    private final ObservableList<SlideshowFXTask> currentTasks = FXCollections.observableArrayList();
    private final List<StartAction> startTaskActions = new ArrayList<>();

    private TaskDAO() {
    }

    public static TaskDAO getInstance() { return singleton; }

    /**
     * Add an action that will be run each time a {@link SlideshowFXTask task} is started by the
     * {@link #startTask(SlideshowFXTask)} method.
     *
     * @param action An action that should be run when a task is started.
     */
    public void addStartTaskAction(final StartAction action) {
        if(action != null) {
            this.startTaskActions.add(action);
        }
    }

    /**
     * Start the given task and add it to the list of current tasks.
     * @param task The task to start.
     * @throws NullPointerException If the given task is null.
     */
    public void startTask(final SlideshowFXTask task) {
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

        this.startTaskActions.forEach(action -> action.execute(task));
        new Thread(task).start();
    }
}
