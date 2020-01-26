import java.util.ArrayList;
import java.util.Arrays;

public class Task implements Cloneable, Comparable<Task>{
    private int taskNum;
    private int waitTime = 0;
    private int completedTime = 0;
    private int blockedDuration = 0;
    private boolean finished = false;
    private boolean aborted = false;
    private boolean blocked = false;
    private boolean blockedBeforeTerminateForCompute = false;
    private boolean shortBlockRequestGrant = false;
    private boolean safetyCheckFinish = false;
    private ArrayList<Integer> need = new ArrayList<>();
    private ArrayList<Integer> alloc = new ArrayList<>();
    private ArrayList<Integer> claim = new ArrayList<>();
    private ArrayList<String> activities = new ArrayList<>();
    //Make functions with this
    Task(int taskNum){
        this.taskNum = taskNum;
    }
    //This creates a clone of the Task object.
    Task(Task taskObject){
        this.taskNum = taskObject.getTaskNum();
        this.need.addAll(taskObject.getNeed());
        this.alloc.addAll(taskObject.getAlloc());
        this.claim.addAll(taskObject.getClaim());
        this.activities.addAll(taskObject.getActivities());
        this.finished = taskObject.isFinished();
        this.aborted = taskObject.aborted;
    }
    //The printing format of the Task, useful for debugging.
    public String toString(){
        StringBuilder matrix = new StringBuilder();
        matrix.append("[ ").append(Arrays.toString(alloc.toArray())).append(" | ").append(Arrays.toString(claim.toArray())).append(" | ").append(Arrays.toString(need.toArray())).append(" ]\n");
        return matrix.toString();
    }
    //BlockRequestGrant is responsible for whenever a program was removed
    //from the blockedList, it will be told that it will not run for the rest of the current cycle.
    public boolean isShortBlockRequestGrant() {
        return shortBlockRequestGrant;
    }
    public void setShortBlockRequestGrant(boolean shortBlockRequestGrant) { this.shortBlockRequestGrant = shortBlockRequestGrant; }
    //To not add an additional cycle for computed, since it will end after some time, and will go into the terminate
    //function. To prevent it from gaining an extra, cycle, this boolean is used to not add an extra one.
    public boolean isBlockedBeforeTerminateForCompute() { return blockedBeforeTerminateForCompute; }
    public void setBlockedBeforeTerminateForCompute(boolean blockedBeforeTerminateForCompute) { this.blockedBeforeTerminateForCompute = blockedBeforeTerminateForCompute; }
    //Used for compute, reads the blocked amount left.
    public int getBlockedDuration() { return blockedDuration; }
    //Decreases block time of compute, after each cycle until it reaches 0.
    public void decBlockedDuration(){ this.blockedDuration-=1; }
    //When making the compute, set it to the number the input said.
    public void setBlockedDuration(int blockedDuration) { this.blockedDuration = blockedDuration; }
    //Used to see if the Task is safe or not. Only in cloned Tasks.
    public boolean isSafetyCheckFinish() {
        return safetyCheckFinish;
    }
    //Sets the Task as either safe or not.
    public void setSafetyCheckFinish(boolean safetyCheckFinish) {
        this.safetyCheckFinish = safetyCheckFinish;
    }
    //Stores all the Need of the Task.
    public ArrayList<Integer> getNeed() {
        return need;
    }
    //Stores all the Allocated of the Task.
    public ArrayList<Integer> getAlloc() {
        return alloc;
    }
    //Stores the claims of the Task
    public ArrayList<Integer> getClaim() {
        return claim;
    }
    //Setting a claim.
    public void setClaim(ArrayList<Integer> claim) {
        this.claim = claim;
    }
    //Get the complete time, needed for the final printing.
    public int getCompletedTime() {
        return completedTime;
    }
    //Setting the complete time after the Task finishes.
    public void setCompletedTime(int completedTime) {
        this.completedTime = completedTime;
    }
    //If the task had to be ended before completion, it can be checked by this method,
    //which will help us avoid Aborted Tasks when looking through every Task in the
    //Task Array.
    public boolean isAborted() {
        return aborted;
    }
    //If a Task has to be aborted, it can be set.
    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }
    //If a function is blocked because of unreasonable action, it checks
    //and avoids these blocked function when going through the Task
    //to perform its activity.
    public boolean isBlocked() { return blocked; }
    //Puts the state as blocked or not depending on the resource available.
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
    //If a task is finished, no need to read the Task.
    public boolean isFinished() {
        return finished;
    }
    //When Task is complete, set it to finish so the program knows to avoid
    //the Task next time.
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    //Everytime the function is stopped because of not compute, incr wait time
    //by 1. This is used for the final printing step.
    public void incrWaitTime(){
        waitTime+=1;
    }
    //Get the wait time of the function
    public int getWaitTime() {
        return waitTime;
    }
    //Sets the wait time.
    public void setBlockedTime(int waitTime) { this.waitTime = waitTime; }
    //Get the TaskNum, which is the identity of the Task. Sometimes needed
    //when you are looking for a specific numbered Task or when debugging.
    public int getTaskNum() {
        return taskNum;
    }
    //Stores all the activity a Task has to do.
    public ArrayList<String> getActivities() {
        return activities;
    }
    //Cloning.
    @Override
    protected Object clone() throws CloneNotSupportedException{ return super.clone(); }
    //Used to sort the Tasks from lowest Task Number to biggest. Helps for the
    //printing step.
    @Override
    public int compareTo(Task o) {
        if(o.taskNum > this.taskNum) return -1;
        else if(o.taskNum < taskNum) return 1;
        return 0;
    }
}
