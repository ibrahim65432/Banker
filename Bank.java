import java.util.*;

public class Bank {
    private int cycle = 0;
    private int numOfResource = 0;
    private int numOfTask = 0;
    private int numOfTaskDenied = 0;
    private ArrayList<Integer> resources = new ArrayList<>();
    private ArrayList<Integer> resourcesForRelease = new ArrayList<>();
    private ArrayList<Integer> resourceInMemory = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> initialClaimList = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> taskList = new HashMap<>();
    private ArrayList<Task> taskArrayList = new ArrayList<>();
    private ArrayList<Task> completed = new ArrayList<>();
    private ArrayList<Task> blockedList = new ArrayList<>();
    private ArrayList<Task> computedList = new ArrayList<>();
    private ArrayList<Task> aborted = new ArrayList<>();

    //Making the Bank object, makes the list of tasks going through the Task Numbers.
    //Also sets the number of resources
    public Bank(int numOfTask, int numOfResource) {
        this.numOfTask = numOfTask;
        for(int i =1; i < numOfTask + 1; i++){
            taskArrayList.add(new Task(i));
        }
        this.numOfResource = numOfResource;

    }
    //Constructs the actual Bank operation. Uses the initial claim to make the
    //Bank, then adds necessary data into each place.
    public void constructTable(){
        //Will go through the Initial Claim which stores all
        //the taskNumbers and the Activity list strings for each.
        Object[] keys = initialClaimList.keySet().toArray();
        //Sort them in Task Number, from lowest to biggest
        Arrays.sort(keys);
        //Make an arrayList of string to point to the activity list of each Task(though, this function only
        //reads the initiate Strings, in order to construct the data into respective locations.
        ArrayList<String> action = new ArrayList<>();
        //Divides the first activity from activity list, so that we can see each
        //individually.
        String[] divideResources = null;
        //Saves the TaskNumber of the activity. Uses it to store Alloc, Need, and Claim data from initiate.
        int taskLoc = 0;
        for(int i = 0; i < keys.length; i++){
            //Gets the ArrayList<String> of activities for initiate
            action = initialClaimList.get(keys[i]);
            //Go through each activity
            for(int u = 0; u < action.size(); u++){
                //Goes through the initiate String
                divideResources = action.get(u).split("\\s+");
                //Stores location, Task Number
                taskLoc = Integer.parseInt(divideResources[1]);
                //Saves the resource.
                int resource = Integer.parseInt(divideResources[3]);
                //Add each one, Alloc has initially 0, Need, needs the number of resource,
                //and the amount each claims.
                taskArrayList.get(taskLoc - 1).getAlloc().add(0);
                taskArrayList.get(taskLoc - 1).getNeed().add(resource);
                taskArrayList.get(taskLoc - 1).getClaim().add(resource);
                //Making the resource arrayList, so that we can only set them later
                //instead of needed to instantiate them with numbers later on.
                resourcesForRelease.add(0);
                resourceInMemory.add(0);
            }
        }
        //Going through each resource adds a cycle, so add them all in the end of building
        //the list.
        cycle+=numOfResource;
    }

    /*
    Splits between the FIFO and the Banker. The FIFO is the ID==0 and Banker is the one in the else statement.
    So, this method takes in the Task Num, and tries to see if it is possible to take that amount of requested
    resource from the resource, if not possible, FIFO and Banker will do its respective measures in order to
    make it possible. For Banker, it is possible to find such path, but for FIFO, it is possible for there to be
    a deadlock state, where no tasks can be completed, if that happens, then we abort as many, and see if the
    resources we got from aborting is enough to make the request for the following Task. We do so until we can
    fulfill a request of a task or until all task becomes aborted.
     */
    public boolean request(int taskNum) throws CloneNotSupportedException {
        if(bankers.ID == 0){
            //Looks at the first activity on top, and splits them into pieces to see them individually.
            String[] activity = taskArrayList.get(taskNum).getActivities().get(0).split("\\s+");
            //If the resource the task request is more than the resources available.
            if(Integer.parseInt(activity[3]) > resources.get(Integer.parseInt(activity[2]) - 1)){
                //And if numOfTaskDenied, responsible for counting the number of tasks that were
                //unable to fulfill the request previously, if that number is the same as the number
                //of Tasks minus the completed amount, then it can try doing its method of FIFO.
                if(numOfTaskDenied==taskArrayList.size() - completed.size()) {
                    //Goes through every single Task and sees if it is possible to abort it to make
                    //enough resource to do the job.
                    for (int i = 0; i < taskArrayList.size(); i++) {
                        //Temporarlly store the string pieces into an array, so that we can see the parts of each
                        //of the activity of the task it needs to do.
                        String[] tempActivity = taskArrayList.get(i).getActivities().get(0).split("\\s+");
                        //If the task itself is not aborted, or not finished, then it means it is possible for it
                        //to be aborted. If the number of requested resource is more than the resource available, then
                        //it means it can be aborted.
                        if(!taskArrayList.get(i).isAborted() && !taskArrayList.get(i).isFinished() && Integer.parseInt(tempActivity[3]) > resources.get(Integer.parseInt(tempActivity[2]) - 1)){
                            //Once again checks if it is fine, if it is, then moves on to the for loop to move all the
                            //resources allocated already in the Task and add it into resource, and set the amount
                            //allocated to zero, and set the task in aborted, no longer will be touched.
                            if (!taskArrayList.get(i).isFinished() && !taskArrayList.get(i).isAborted()){
                                for(int k = 0; k < resources.size(); k++){
                                    resources.set(k, resources.get(k) + taskArrayList.get(i).getAlloc().get(k));
                                    taskArrayList.get(i).getAlloc().set(k, 0);
                                }
                                taskArrayList.get(i).setAborted(true);
                                //Adds the task into abortedList, so we can keep track of it. So that we can avoid
                                //reading that aborted in the future.
                                aborted.add(taskArrayList.get(i));
                            }
                        }
                        //If the task is not finished or aborted, and if the requested resource is less than the resource available, then it is possible to give that amount, without needing to
                        //abort any other tasks.
                        if(!taskArrayList.get(i).isFinished() && !taskArrayList.get(i).isAborted() && Integer.parseInt(tempActivity[3]) <= resources.get(Integer.parseInt(tempActivity[2]) - 1)){
                            taskArrayList.get(i).getAlloc().set(Integer.parseInt(tempActivity[2]) - 1, taskArrayList.get(i).getAlloc().get(Integer.parseInt(tempActivity[2]) - 1) + Integer.parseInt(tempActivity[3]));
                            taskArrayList.get(i).getNeed().set(Integer.parseInt(tempActivity[2])-1, taskArrayList.get(i).getNeed().get(Integer.parseInt(tempActivity[2]) - 1) - Integer.parseInt(tempActivity[3]));
                            //Since the task was denied once before, it was placed into the blockedList, so we need to remove it since it is now a working Task.
                            blockedList.remove(taskArrayList.get(i));
                            //Remove the first activity from task since we did what it asked for.
                            taskArrayList.get(i).getActivities().remove(0);
                            //Don't need to look at the same task again in the same cycle.
                            taskArrayList.get(i).setShortBlockRequestGrant(true);
                            //Since we found a task that is possible to run in the cycle, for this cycle, there is no
                            //deadlock, so we set the numOfTaskDenied to 0.
                            numOfTaskDenied = 0;
                            //True because request was granted.
                            return true;
                        }
                    }
                }
                //If the number of resource requested was more than the resource available, but NOT in deadlock
                //state, then we will only increment the wait time, and add if the task is not already in the
                //blockedList.
                else{
                    taskArrayList.get(taskNum).incrWaitTime();
                    if(!blockedList.contains(taskArrayList.get(taskNum))){
                        blockedList.add(taskArrayList.get(taskNum));
                    }
                    //Since the task was denied, and it can potentially mean a deadlock, we will add one. If there is no
                    //deadlock in the cycle, the variable will be set to zero.
                    numOfTaskDenied++;
                    //Since request was not successful.
                    return false;
                }
            }
            //If requested amount is less than the resource available, then it is completely fine to grant that request.
            else{
                resources.set(Integer.parseInt(activity[2]) - 1, resources.get(Integer.parseInt(activity[2]) - 1) - Integer.parseInt(activity[3]));
                taskArrayList.get(taskNum).getAlloc().set(Integer.parseInt(activity[2]) - 1, taskArrayList.get(taskNum).getAlloc().get(Integer.parseInt(activity[2]) - 1) + Integer.parseInt(activity[3]));
                taskArrayList.get(taskNum).getNeed().set(Integer.parseInt(activity[2]) - 1, taskArrayList.get(taskNum).getNeed().get(Integer.parseInt(activity[2]) - 1) - Integer.parseInt(activity[3]));
                taskArrayList.get(taskNum).getActivities().remove(0);
                //Because request was granted.
                return true;
            }
            //If not false anywhere it is true :)
            return true;
        }
        //The following else statement is for the Banker, the method before this was all FIFO. The difference is, Banker looks to see if a request is safe, and safe means it will not cause a deadlock.
        //If safe, request is granted, if not, then that Task is put to hold.
        else {
            //Looks at the first activity on top, and splits them into pieces to see them individually.
            String[] activity = taskArrayList.get(taskNum).getActivities().get(0).split("\\s+");
            //If the requested amount of the Task is less than or equal to the amount the task needs, and the task is not aborted, then it is possible for it to be safe.
            //If not the Task will be aborted since a request cannot ask for more than it needs.
            if (Integer.parseInt(activity[3]) <= taskArrayList.get(taskNum).getNeed().get(Integer.parseInt(activity[2]) - 1) && !taskArrayList.get(taskNum).isAborted()) {
                //If the requested amount is less than or equal to the number of resources available, then it is possible for the request to be fulfilled. If not, the
                //task is blocked for this cycle.
                if (Integer.parseInt(activity[3]) <= resources.get(Integer.parseInt(activity[2]) - 1)) {
                    //Checks if the activity of the Task is safe to perform by applying the Banker's algorithm throughout
                    //all the Tasks. If every process is possible without deadlock, then the request of the Task is safe
                    //and thus it can be fulfilled.
                    if (isSafe(taskNum)) {
                        resources.set(Integer.parseInt(activity[2]) - 1, resources.get(Integer.parseInt(activity[2]) - 1) - Integer.parseInt(activity[3]));
                        taskArrayList.get(taskNum).getAlloc().set(Integer.parseInt(activity[2]) - 1, taskArrayList.get(taskNum).getAlloc().get(Integer.parseInt(activity[2]) - 1) + Integer.parseInt(activity[3]));
                        taskArrayList.get(taskNum).getNeed().set(Integer.parseInt(activity[2]) - 1, taskArrayList.get(taskNum).getNeed().get(Integer.parseInt(activity[2]) - 1) - Integer.parseInt(activity[3]));
                        taskArrayList.get(taskNum).getActivities().remove(0);
                        return true;

                    }
                    //If the Task's request cannot be fulfilled because it was not safe, then the Task is put into blocked state, and added into the
                    //blockedList array.
                    else {
                        taskArrayList.get(taskNum).incrWaitTime();
                        if (!blockedList.contains(taskArrayList.get(taskNum))) {
                            blockedList.add(taskArrayList.get(taskNum));

                        }
                        //Since was not successful to fulfill the request.
                        return false;
                    }

                }
                //Since the amount the Task request for was more than the resource had, but it is less than what
                //the Task needs, it will be placed into blocked state for the cycle.
                else {
                    taskArrayList.get(taskNum).incrWaitTime();
                    if (!blockedList.contains(taskArrayList.get(taskNum))) {
                        blockedList.add(taskArrayList.get(taskNum));
                    }
                    //Since request was not fulfilled.
                    return false;
                }
            }
            //When the task requests for more than it needs, then it is not possible for the Task to run in Banker. Thus,
            //the Task is blocked.
            else if (!taskArrayList.get(taskNum).isAborted()) {
                //Prints out the task that was aborted, and when it was aborted, and what was the reason.
                System.out.println("During cycle " + cycle + "-" + (cycle + 1) + " of Banker's algorithms ");
                System.out.println("\tTask " + (taskNum + 1) + "'s request exceeds its claim; aborted; " + resources.toString()
                        + " units available next cycle\n");
                //Takes out all the resource the Task has, since it no longer needs it since it is aborted, then
                //puts them into the resource list. Sets Allocated to zero, just to keep it clean and clear.
                for (int i = 0; i < resources.size(); i++) {
                    resources.set(i, resources.get(i) + taskArrayList.get(taskNum).getAlloc().get(i));
                    taskArrayList.get(taskNum).getAlloc().set(i, 0);
                }
                //Sets the task to aborted, so it will no longer be read by other functions.
                taskArrayList.get(taskNum).setAborted(true);
                //Adding completion time, though not necessary, but for it to be clean and clear.
                taskArrayList.get(taskNum).setCompletedTime(cycle);
                //Adds the task to list of Aborted so that it will be saved for future application
                //when we need to see the aborted Task's instead of going through all the tasks.
                aborted.add(taskArrayList.get(taskNum));
                //False because request was not fulfilled.
                return false;
            }
            //False because, sometimes it happens.
            return false;
        }
    }

    //Release simply releases the resources from the Task, and puts that into an ArrayList of resources, but into a temp variable,
    //in order to prevent it from influencing other Tasks since other Tasks are not supposed to know of the existence
    //of extra resource that was released in the same cycle.
    public void release(int taskNum){
        String[] activity = taskArrayList.get(taskNum).getActivities().get(0).split("\\s+");
        resourcesForRelease.set(Integer.parseInt(activity[2]) - 1, resources.get(Integer.parseInt(activity[2]) - 1) + Integer.parseInt(activity[3]));
        taskArrayList.get(taskNum).getAlloc().set(Integer.parseInt(activity[2]) - 1, taskArrayList.get(taskNum).getAlloc().get(Integer.parseInt(activity[2]) - 1) - Integer.parseInt(activity[3]));
        taskArrayList.get(taskNum).getNeed().set(Integer.parseInt(activity[2]) - 1, taskArrayList.get(taskNum).getNeed().get(Integer.parseInt(activity[2]) - 1) + Integer.parseInt(activity[3]));
        taskArrayList.get(taskNum).getActivities().remove(0);
    }

    //Terminates a task, the end of the task. The task will no longer run once this function runs.
    public void terminate(int taskNum){
        //Sets it to 0 since an activity did run, and thus not possible for the cycle to have
        //a deadlock state.
        numOfTaskDenied = 0;
        //Since the task is finished, label it finished so that other functions know to avoid this task.
        taskArrayList.get(taskNum).setFinished(true);
        //If the task was blocked before terminating due to compute,
        //then it will store the time of the current cycle, if not,
        //add 1 to the cycle since it needs one to make it work.
        if(taskArrayList.get(taskNum).isBlockedBeforeTerminateForCompute()){
            taskArrayList.get(taskNum).setCompletedTime(cycle);
        }
        else{
            taskArrayList.get(taskNum).setCompletedTime(cycle+1);
        }
        //Add the task into the completed Array, since it is done and we can keep track of all the
        //completed array, which will help us for example, to also end the program.
        completed.add(taskArrayList.get(taskNum));
    }

    //Compute blocks a task for some set time. But does not add into wait time, so no adding to blockedList.
    public void compute(int taskNum){
        taskArrayList.get(taskNum).setBlocked(true);
        taskArrayList.get(taskNum).setBlockedDuration(Integer.parseInt(taskArrayList.get(taskNum).getActivities().get(0).split("\\s+")[2]) - 1);
        computedList.add(taskArrayList.get(taskNum));
        taskArrayList.get(taskNum).getActivities().remove(0);
    }

    //Stores the original resource of the cycle before it starts changing.
    public void createResourceInMemory() {
        for(int i =0; i < resources.size(); i++){
            this.resourceInMemory.set(i, resources.get(i));
        }
    }

    //After all the tasks finish running in the cycle, resource will have a new value from
    //the release that it could not get, and to find how much the resource gained, we use the
    //original resource array to find the difference, then add that difference into the resource
    //that has gone through the request handling.
    public void convertReleaseResourceToResource(){
        for(int i = 0 ; i < resources.size(); i++){
            this.resources.set(i, resources.get(i) + (resourcesForRelease.get(i) - resourceInMemory.get(i)));
        }
    }

    //The arrayList which will store the releases of resources of each function. This arrayList is used
    //when finding the final resource ArrayList.
    public void setResourcesForRelease() {
        for(int i = 0; i < resources.size(); i++){
            this.resourcesForRelease.set(i, resources.get(i));
        }
    }

    //Helps to see if there was a deadlock or not, so we can proceed
    //to abort some tasks.
    public int getNumOfTaskDenied() {
        return numOfTaskDenied;
    }

    //When we need to set the NumOfTasksDenied to 0, since no deadlock
    //was detected for that cycle.
    public void setNumOfTaskDenied(int numOfTaskDenied) {
        this.numOfTaskDenied = numOfTaskDenied;
    }

    //Is used after a task runs before the main running, but have the state which makes it
    //possible to run, so adding this expression into the Task prevents the same task from
    //running twice on the same cycle.
    public void shortBlockUnBlock(){
        for(int i = 0; i < taskArrayList.size(); i++){
            if(taskArrayList.get(i).isShortBlockRequestGrant()){
                taskArrayList.get(i).setShortBlockRequestGrant(false);
            }
        }
    }

    //Deadlock prevention code for Banker.
    public boolean isSafe(int taskNum) throws CloneNotSupportedException {
        //Looks at the first activity for the Task.
        String[] activity = taskArrayList.get(taskNum).getActivities().get(0).split("\\s+");
        //Make a temp resource which has all the value of resources, as a new object, not related to resources
        //by pointer.
        ArrayList<Integer> tempResource = new ArrayList<>(resources);
        //Makes a deep clone of the Task in the arrayList, not a complete clone, but deep enough to have
        //all the necessary characteristics.
        ArrayList<Task> tempTaskList = new ArrayList<>();
        for(int i =0; i < taskArrayList.size(); i++){
            tempTaskList.add(new Task(taskArrayList.get(i)));
        }

        //Apply the request of the resource into temp resource and tempTaskList. We will assume the request work, then see if
        //any deadlock is possible from it. If deadlock is possible, then the request will be blocked.
        tempResource.set(Integer.parseInt(activity[2]) - 1, tempResource.get(Integer.parseInt(activity[2]) - 1) - Integer.parseInt(activity[3]));
        tempTaskList.get(taskNum).getAlloc().set(Integer.parseInt(activity[2]) -1, tempTaskList.get(taskNum).getAlloc().get(Integer.parseInt(activity[2]) -1) + Integer.parseInt(activity[3]));
        tempTaskList.get(taskNum).getNeed().set(Integer.parseInt(activity[2]) - 1, tempTaskList.get(taskNum).getNeed().get(Integer.parseInt(activity[2]) - 1) - Integer.parseInt(activity[3]));

        //Counts number of tasks that is safe, and if not equal to the number of task that is running, then it is not safe to fulfill the request.
        int safeTasks = 0;
        //Goes through every single task.
        for(int i =0; i < tempTaskList.size(); i++ ){
            //Counts the number of resources the task needs is less than or equal to the resource available for that
            //unit. If the number of resource that is safe is equal to the size of resource, then the task is safe.
            int numberOfSafeResource = 0;
            //Another resource arrayList, this is used as a temp variable so that the other temp ArrayList will
            //store the resources for when it worked for that task. Since we changed the resource when we are going
            //through the resources, it will change the resourceList. So, we will only add that new temp resource to
            //the older temp when that new temp resource is safe.
            ArrayList<Integer> tempResource2 = new ArrayList<>(tempResource);
            for(int j = 0; j < tempTaskList.get(i).getNeed().size(); j++){
                if(!tempTaskList.get(i).isAborted() && !tempTaskList.get(i).isFinished() && !tempTaskList.get(i).isSafetyCheckFinish() && tempTaskList.get(i).getNeed().get(j) <= tempResource2.get(j)){
                    tempResource2.set(j, tempResource2.get(j) + tempTaskList.get(i).getAlloc().get(j));
                    numberOfSafeResource++;
                }
            }
            //If every resource needed was less than or equal to the resources available, then the task is safe, so add that new temp resource to the old
            //temp resource.
            if(!tempTaskList.get(i).isAborted() && !tempTaskList.get(i).isFinished() && !tempTaskList.get(i).isSafetyCheckFinish() && numberOfSafeResource==tempTaskList.get(i).getNeed().size()){
                for(int k = 0; k < tempResource2.size(); k++){
                    tempResource.set(k, tempResource2.get(k));
                }
                tempTaskList.get(i).setSafetyCheckFinish(true);
                //If task is safe, then we can look from the first Task again
                //and go down. -1 because the for loop will add one, which will make it 0.
                i = -1;
                //If it reaches the same number as number of task minus completed minus aborted,
                //then it means the request is unsafe, and thus not happen.
                safeTasks++;
            }
        }
        //If the number of safe tasks is not equal to the number of tasks minus the completed and
        //minus the aborted, then that means there is a task that was unsafe. So, the request
        //cannot be fulfilled.
        if(safeTasks!=taskArrayList.size() - completed.size() - aborted.size()){
            return false;
        }
        //If safeTask is equal, then it means the request is fine, so it
        //will be fulfilled.
        return true;
    }

    //Whenever we start a new cycle, this function checks for any task that
    //has a block duration of 0 or under, and if that is so, and if the
    //block is also blocked, then it will make it not blocked, and removed
    //from compute list. Compute list is only for tasks that are blocked
    //by compute duration.
    public void unBlockComputedTasks(){
        for(int i =0; i < computedList.size(); i++){
            if(computedList.get(i).getBlockedDuration() <= 0 && computedList.get(i).isBlocked()){
                computedList.get(i).setBlocked(false);
                computedList.remove(i);
                //When we remove, we lower the size of the computedList, so start from beginning.
                //-1 because the for loop will add one, to make it 0.
                i = -1;
            }
        }
    }

    //After constructing the Bank object, we will check if any of the
    //task is not possible to occur, thus placing it as aborted. Used in
    //Banker but not FIFO.
    public void labelAbortTasks(){
        for(int i = 0; i < taskArrayList.size(); i ++){
            for(int k = 0; k < taskArrayList.get(i).getClaim().size(); k++){
                if(taskArrayList.get(i).getClaim().get(k) > resources.get(k)){
                    taskArrayList.get(i).setAborted(true);
                    aborted.add(taskArrayList.get(i));
                }
            }
        }

        //Will print every aborted Task with the reason and with the task number.
        for(int i = 0; i < taskArrayList.size(); i++){
            if(taskArrayList.get(i).isAborted()){
                System.out.println("  Banker aborts task " + taskArrayList.get(i).getTaskNum() +" before run begins:");
                for(int k = 0; k < taskArrayList.get(i).getClaim().size(); k++){
                    if(taskArrayList.get(i).getClaim().get(k)>resources.get(k)){
                        System.out.println("       claim for resource " + (k+1) + " (" + taskArrayList.get(i).getClaim().get(k) + ") exceeds " +
                                "number of units present (" + resources.get(k) + ") ");
                    }
                }
                System.out.println("");
            }
        }
    }

    //Viewing the computed ArrayList can help in removing computed
    //Tasks that have completed its blockedDuration, and can
    //also help in not allowing tasks that are included in compute
    //to be running in some other functions.
    public ArrayList<Task> getComputedList() {
        return computedList;
    }

    //Getting the aborted list helps in knowing if a Task should
    //be done or not, and if aborted, it helps us to skip the
    //aborted tasks.
    public ArrayList<Task> getAborted() {
        return aborted;
    }

    //Setting an aborted Array.
    public void setAborted(ArrayList<Task> aborted) {
        this.aborted = aborted;
    }

    //Whenever a cycle needs to be adjusted, then can use this function.
    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    //Going through every task that are in blocked list helps us
    //to increase the wait time on them, and see if the tasks
    //can be run before checking other tasks.
    public ArrayList<Task> getBlockedList() {
        return blockedList;
    }

    //Useful for knowing when a task was aborted,
    //and for debugging.
    public int getCycle() {
        return cycle;
    }

    //After every cycle, the cycle needs to be incremented.
    public void incCycle() {
        this.cycle+=1;
    }

    //Knowing what Tasks were completed helps us avoid reading
    //them.
    public ArrayList<Task> getCompleted() {
        return completed;
    }

    //Stored all the initiates of the input, and used to make the
    //Bank object.
    public HashMap<Integer, ArrayList<String>> getInitialClaimList() {
        return initialClaimList;
    }

    //Contains a list of task.
    public HashMap<Integer, ArrayList<String>> getTaskList() {
        return taskList;
    }

    //Gets the resources available at the moment. Important in
    //this entire project.
    public ArrayList<Integer> getResources() {
        return resources;
    }

    //When a resource should be set to something else.
    public void setResources(ArrayList<Integer> resources) {
        this.resources = resources;
    }

    //The heart of the code, this arrayList which stores
    //all the pointers to all the task objects allows
    //me to complete each task's activity.
    public ArrayList<Task> getTaskArrayList() {
        return taskArrayList;
    }
}
