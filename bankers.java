    import java.io.File;
    import java.io.FileNotFoundException;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Scanner;

    public class bankers {
        public static int cycle = 0;
        public static int ID = 0;
        public static void main(String args[]) throws FileNotFoundException, CloneNotSupportedException {
            Scanner scn = new Scanner(new File(args[0]));
            int numOfTask = scn.nextInt();
            int numOfResourceType = scn.nextInt();
            //Make two Bank object to use for FIFO and Banker respectively.
            Bank bank = new Bank(numOfTask, numOfResourceType);
            Bank bank1 = new Bank(numOfTask, numOfResourceType);

            //Adding resources to each FIFO and Banker.
            for(int i = 0; i < numOfResourceType; i++){
                int temp = scn.nextInt();
                bank.getResources().add(temp);
                bank1.getResources().add(temp);
            }
            //This function reads through the input.
            while(scn.hasNextLine()){
                String temp = scn.nextLine();
                while(temp.trim().isEmpty()){
                    temp = scn.nextLine();
                }
                //Since we scanned strings, need to convert to store as Integer for some parts of it.
                int taskNumber = Integer.parseInt(temp.split("\\s+")[1]);
                //If the split line gives us initiate as its first String, then it will create that taskNumbered
                //object into each of the Bank object, then add the entire line into the Bank object to its taskNumbered
                //place so that each task stored in the Bank object has its list of activities to do, such as initiate,
                //compute, release, and terminate.
                if(!bank.getTaskList().containsKey(taskNumber) && !temp.split("\\s+")[0].equals("initiate")){
                    bank.getTaskList().put(taskNumber, new ArrayList<>());
                    bank1.getTaskList().put(taskNumber, new ArrayList<>());

                    //Subtract by one, because ArrayLists starts from 0, and taskNum in the input file starts from 1.
                    bank.getTaskArrayList().get(taskNumber - 1).getActivities().add(temp);
                    bank1.getTaskArrayList().get(taskNumber - 1).getActivities().add(temp);
                    bank.getTaskList().get(taskNumber).add(temp);
                    bank1.getTaskList().get(taskNumber).add(temp);
                }
                //If the first String is not initiate, then it is not responsible for making the initial
                //matrix of Banker, so we can simply add each line of activity into its respective taskNum.
                else if(!temp.split("\\s+")[0].equals("initiate")){
                    bank.getTaskList().get(taskNumber).add(temp);
                    bank1.getTaskList().get(taskNumber).add(temp);
                    bank.getTaskArrayList().get(taskNumber - 1).getActivities().add(temp);
                    bank1.getTaskArrayList().get(taskNumber - 1).getActivities().add(temp);

                }
                //Stores all the initiate starting lines into an HashMap in the Bank which is used
                //to make the matrix itself.
                if(temp.split("\\s+")[0].equals("initiate")){
                    //If it does not exist, make the space for it.
                    if(bank.getInitialClaimList().get(taskNumber)==null){
                        bank.getInitialClaimList().put(taskNumber, new ArrayList<String>());
                        bank1.getInitialClaimList().put(taskNumber, new ArrayList<String>());

                    }
                    //Then add the line into the HashMap
                    bank.getInitialClaimList().get(taskNumber).add(temp);
                    bank1.getInitialClaimList().get(taskNumber).add(temp);
                }
            }
            //Performs the FIFO operation onto the input.
            optimisticManager(bank);
            //Performs the BANKER'S operation onto the input.
            bankersAlgorithm(bank1);
        }

        /*
        This function takes in the Bank object, which is the matrix itself, and where all the operations are done.
         */
        public static void optimisticManager(Bank bank) throws CloneNotSupportedException{
            //Setting the ID so that the Bank object which does both FIFO and Banker's can know
            //which operation to perform. For this function, the FIFO function.
            ID = 0;
            //Set the cycle to zero, as the beginning of the function, not yet started.
            cycle = 0;
            //Makes the matrix table using the input data.
            bank.constructTable();
            //The two boolean below prevents the same methods to run more than once per cycle. Without this,
            //the for loop will go through it every time, which can confuse the program.
            boolean checkedComputedList;
            boolean checkedBlockedList;
            //If the size of the completed arrayList is not equal to the number of tasks in total, then that means the operations are
            //not yet finished. But, since it is FIFO, there can be instances of an aborted function, so that has to be considered, so the
            //other && is responsible for finding an operation that has aborted methods, and calculates respectively to find the end of the
            //operation.
            while (bank.getCompleted().size() != bank.getTaskList().size() && bank.getTaskArrayList().size() - bank.getAborted().size() != bank.getCompleted().size()) {
                //Responsible for making an arrayList of resources, which stores every available unit. This makes a new ArrayList of resources
                //which will contain all the changes to the "release" command, since the released units should not be available in the same cycle.
                //And thus, by creating a new ArrayList of the same resources the resource has, it can be used to find the final arrayList of resources
                //following each cycle.
                bank.setResourcesForRelease();
                //This function checks if there are any function which has spent its time in compute, and if it did, then it will allow it to be run in
                //the program, by removing it from the computeList, which restricted it from applying its activities, such as request, so by removing it,
                //it can run like any other program.
                bank.unBlockComputedTasks();
                //This block is responsible for turning all the short blocks false, which is very useless for when
                //the block is only needed for that cycle, but not needed for the next cycles. This can be used in situations
                //such as the program which has run after abort statements, and to prevent the program to run once more,
                //the block is made(as the tasks within blockedList happens in the beginning, so it can be followed by
                //doing the same task again with new activity, which will mean it does its function twice per cycle,
                //to avoid that, create a short block, which is here, made false.
                bank.shortBlockUnBlock();
                //This creates the initial resource for each cycle, this keeps the resource in order to provide a base
                //line for when "release" is made. Since we made two other resource arrayLists which are responsible for,
                //one, for doing the operations without having knowledge of the release, and the other which stores
                //the number of released. So making this ArrayList of resource that is not changed, gives us the way to
                //return to new ArrayList with the released units as well as the fully used resource after the cycle.
                bank.createResourceInMemory();
                //The two booleans are set false here because it is only used for each cycle, and to prevent the for loop
                //within the cycle to go through the same method couple of times, and by making them false here, the loop
                //can initiate the method first, but it will then be blocked afterwards.
                checkedComputedList = false;
                checkedBlockedList = false;
                //Goes through the blockedList and adds them to this list. This is
                //used so that the function in the future can avoid those blocked
                //tasks when it does the initiate, release, compute, and terminate.
                ArrayList<Task> blockedTask = new ArrayList<>();
                //This function goes through every single tasks in the array including the ones finished, aborted, and
                //still not done.
                for (int i = 0; i < bank.getTaskArrayList().size(); i++) {
                    //If the computed list is more than one, then there is something inside, which also is not yet completed,
                    //because if it was, it would have been removed from the list by the previous method unBlockComputedTasks,
                    //so if there is a task in the list of the computed list, then its blocked duration has to be lowered by
                    //1, and make it so that the for loop will not go to that function again by making the boolean into true.
                    if(bank.getComputedList().size() > 0 && !checkedComputedList){
                        for(int j = 0; j < bank.getComputedList().size(); j++) {
                            //Decreases the time of the tasks in computedList!
                            if (bank.getComputedList().get(j).getBlockedDuration() > 0){
                                bank.getComputedList().get(j).decBlockedDuration();
                            }
                        }
                    }
                    //Sets it to true to prevent the above method to run again in the same iteration of the for loop.
                    checkedComputedList = true;
                    //Since we need to go through the blocked tasks first, we will see if there are any, if there are,
                    //then the blocked tasks will be explored first. The boolean expression is there to prevent the method
                    //to run an additional time after the iteration it runs.
                    if(bank.getBlockedList().size() > 0 && !checkedBlockedList){
                        //A boolean to see if the "request" did occur and not simply put into a blocked state again.
                        boolean requestAccepted;
                        //Goes through the blockedList!
                        for(int k = 0; k < bank.getBlockedList().size(); k++){
                            //Store the list of lines of activity each task has to do into an ArrayList. This helps it
                            //easier to utilize that activity list from that task.
                            ArrayList<String> blockedTemp = bank.getBlockedList().get(k).getActivities();
                            //Splitting the first activity in the list, since it is stored in order, it has to be the
                            //first one to be run first. Split it since it is a string, and we want parts of that string.
                            String[] blockedActivity = blockedTemp.get(0).split("\\s+");
                            //Add the task into the blockedTask, since it will run, so for this running, it will not
                            //go on to the next statements.
                            blockedTask.add(bank.getTaskArrayList().get(Integer.parseInt(blockedActivity[1]) - 1));
                            //Stores the result from request, this is used to see if the task was successful in doing its
                            //activity.
                            requestAccepted = bank.request(Integer.parseInt(blockedActivity[1]) - 1);
                            //If the task was successful, then that task will be removed from the blocked list. If it
                            //was not, then there was most likely not enough resource to accomplish that task. So, it will
                            //stay in the blocked list.
                            if(requestAccepted){
                                for(int j = 0; j < bank.getBlockedList().size(); j++){
                                    if(bank.getBlockedList().get(j).getTaskNum() == Integer.parseInt(blockedActivity[1])){
                                        bank.getBlockedList().remove(k);
                                    }
                                }
                            }
                        }
                    }
                    //Once you go through the blocked methods ^ , then no need to go through it again after each for loop
                    //iteration.
                    checkedBlockedList = true;

                    //Making an arrayList to store the list of all activities for that one task.
                    ArrayList<String> temp = bank.getTaskArrayList().get(i).getActivities();
                    //Split it in order to get the parts we want to analyze.
                    String[] activity = temp.get(0).split("\\s+");
                    //If the task is in computed, then it cannot be requested since it is supposed to pause the task from
                    //running for a specific time, and blockRequestGrant is responsible for whenever a program was removed
                    //from the blockedList, it will be told that it will not run for the rest of the current cycle. If the
                    // blockTask contains the task, it will not run either, since it is supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is request, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    if(!bank.getComputedList().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isShortBlockRequestGrant() &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isFinished() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isAborted() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isBlocked() &&
                            bank.getTaskArrayList().get(i).getBlockedDuration() == 0 &&
                            activity[0].equals("request")){

                        bank.request(i);
                    }
                    //If the task is in computed, then it cannot be requested since it is supposed to pause the task from
                    //running for a specific time, and blockRequestGrant is responsible for whenever a program was removed
                    //from the blockedList, it will be told that it will not run for the rest of the current cycle. If the
                    // blockTask contains the task, it will not run either, since it is supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is release, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    else if(!bank.getComputedList().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isShortBlockRequestGrant() &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isFinished() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isAborted() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isBlocked() &&
                            bank.getTaskArrayList().get(i).getBlockedDuration() == 0 && activity[0].equals("release")){

                        //Since the release statement is usually right before the "terminate" statement, and since the
                        //"terminate" statement is not supposed to consume a cycle, we can call terminate right after
                        //the release statement, as the activity list will have its top activity removed, which was responsible
                        //for the release of unit(s) then it can simply terminate by going to the next activity.
                        bank.release(i);
                        if(bank.getTaskArrayList().get(i).getActivities().get(0).split("\\s+")[0].equals("terminate")){
                            bank.terminate(i);

                        }
                    }
                    //If the task is in computed, then it cannot be requested since it is supposed to pause the task from
                    //running for a specific time, and blockRequestGrant is responsible for whenever a program was removed
                    //from the blockedList, it will be told that it will not run for the rest of the current cycle. If the
                    // blockTask contains the task, it will not run either, since it is supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is compute, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    else if(!bank.getComputedList().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isShortBlockRequestGrant() &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isFinished() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isAborted() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isBlocked() &&
                            bank.getTaskArrayList().get(i).getBlockedDuration() == 0 && activity[0].equals("compute")){

                        bank.compute(i);
                        //Since the compute statement is usually right before the "terminate" statement, and since the
                        //"terminate" statement is not supposed to consume a cycle, we can call terminate right after
                        //the compute statement, as the activity list will have its top activity removed, which was responsible
                        //for the blocking of Task(s) then it can simply terminate by going to the next activity after the
                        //duration of Task is 0.
                        if(bank.getTaskArrayList().get(i).getActivities().get(0).split("\\s+")[0].equals("terminate")){
                            bank.getTaskArrayList().get(i).setBlockedBeforeTerminateForCompute(true);
                        }
                    }
                    //If the task is in computed, then it cannot be requested since it is supposed to pause the task from
                    //running for a specific time, and blockRequestGrant is responsible for whenever a program was removed
                    //from the blockedList, it will be told that it will not run for the rest of the current cycle. If the
                    // blockTask contains the task, it will not run either, since it is supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is terminate, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    else if(!bank.getComputedList().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isShortBlockRequestGrant() &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isFinished() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isAborted() &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1).isBlocked() &&
                            bank.getTaskArrayList().get(i).getBlockedDuration() == 0 && activity[0].equals("terminate")){

                        bank.terminate(i);
                    }
                }
                //Responsible for combining the 3 arrayList of resources created in the beginning, finds the difference
                //between the original and the released, then add the difference into the resource arrayList. This will
                //make the resource for the next cycle.
                bank.convertReleaseResourceToResource();
                //If there is a deadlock, we can see if there is one whenever the NumOfTaskDenied is equal to the number
                //of tasks which are not complete, and all the tasks that are not complete denied the request and put
                //into blocked. This allows us to use special methods for this situation.
                if(bank.getNumOfTaskDenied() < bank.getTaskArrayList().size() - bank.getCompleted().size()){
                    bank.setNumOfTaskDenied(0);
                }
                //Increment the cycle after each task is gone over.
                bank.incCycle();
            }

            //Since not all tasks are put in order, we can sort them by Task Numbers. The sorting was implemented
            //on the Task Object.
            Collections.sort(bank.getTaskArrayList());
            int total_work_time = 0;
            int total_wait_time = 0;

            //The below printing statements are for printing,and to make the printing look even, used string formatting.
            //so that a big number will not push other numbers to the side. But yes, the calculations for the total
            //is done below this and the printing of every task and its total. These print statements marks the end of
            //the FIFO function.
            System.out.println("              FIFO");
            for(int y = 0; y < bank.getTaskArrayList().size(); y++){
                System.out.print(String.format("%11s", "Task"));
                System.out.print(String.format("%2s", bank.getTaskArrayList().get(y).getTaskNum()));
                if(bank.getTaskArrayList().get(y).isAborted()){
                    System.out.println(String.format("%13s", "aborted"));
                }
                else{
                    total_work_time+=bank.getTaskArrayList().get(y).getCompletedTime();
                    total_wait_time+=bank.getTaskArrayList().get(y).getWaitTime();
                    System.out.print(String.format("%8s", bank.getTaskArrayList().get(y).getCompletedTime()));
                    System.out.print(String.format("%4s", bank.getTaskArrayList().get(y).getWaitTime()));
                    System.out.println(String.format("%6s", Math.round(bank.getTaskArrayList().get(y).getWaitTime()*100/
                            (double)bank.getTaskArrayList().get(y).getCompletedTime()) + "%"));
                }
            }
            System.out.print(String.format("%12s", "total"));
            System.out.print(String.format("%9s", total_work_time));
            System.out.print(String.format("%4s", total_wait_time));
            System.out.println(String.format("%6s", Math.round(total_wait_time*100/(double) total_work_time) + "%"));
        }

        /*
        This function takes in the Bank object, which is the matrix itself, and where all the operations are done.
         */
        public static void bankersAlgorithm(Bank bank) throws CloneNotSupportedException {
            //Setting the ID so that the Bank object which does both FIFO and Banker's can know
            //which operation to perform. For this function, the Banker's function.
            ID = 1;
            //Set the cycle to zero, as the beginning of the function, not yet started.
            cycle = 0;
            //Makes the matrix table using the input data.
            bank.constructTable();
            //For banker, if there is any task that has a "claim" of more than the total resource
            //available, then aborts those Task(s).
            bank.labelAbortTasks();
            //The two boolean below prevents the same methods to run more than once per cycle. Without this,
            //the for loop will go through it every time, which can confuse the program.
            boolean checkedBlockedList;
            boolean checkedComputedList;
            //If the size of the completed ArrayList is not equal to the number of tasks in total, then that means the operations are
            //not yet finished. But, since it is Banker, there can be instances of an aborted function, so that has to be considered.
            while (bank.getCompleted().size() != bank.getTaskList().size() - bank.getAborted().size()) {
                //This function checks if there are any function which has spent its time in compute, and if it did, then it will allow it to be run in
                //the program, by removing it from the computeList, which restricted it from applying its activities, such as request, so by removing it,
                //it can run like any other program.
                bank.unBlockComputedTasks();
                //Responsible for making an arrayList of resources, which stores every available unit. This makes a new ArrayList of resources
                //which will contain all the changes to the "release" command, since the released units should not be available in the same cycle.
                //And thus, by creating a new ArrayList of the same resources the resource has, it can be used to find the final arrayList of resources
                //following each cycle.
                bank.setResourcesForRelease();
                //This creates the initial resource for each cycle, this keeps the resource in order to provide a base
                //line for when "release" is made. Since we made two other resource arrayLists which are responsible for,
                //one, for doing the operations without having knowledge of the release, and the other which stores
                //the number of released. So making this ArrayList of resource that is not changed, gives us the way to
                //return to new ArrayList with the released units as well as the fully used resource after the cycle.
                bank.createResourceInMemory();
                //The two booleans are set false here because it is only used for each cycle, and to prevent the for loop
                //within the cycle to go through the same method couple of times, and by making them false here, the loop
                //can initiate the method first, but it will then be blocked afterwards.
                checkedBlockedList = false;
                checkedComputedList = false;
                //Goes through the blockedList and adds them to this list. This is
                //used so that the function in the future can avoid those blocked
                //tasks when it does the initiate, release, compute, and terminate.
                ArrayList<Task> blockedTask = new ArrayList<>();
                //This function goes through every single tasks in the array including the ones finished, aborted, and
                //still not done.
                for (int i = 0; i < bank.getTaskArrayList().size(); i++) {
                    //If the computed list is more than one, then there is something inside, which also is not yet completed,
                    //because if it was, it would have been removed from the list by the previous method unBlockComputedTasks,
                    //so if there is a task in the list of the computed list, then its blocked duration has to be lowered by
                    //1, and make it so that the for loop will not go to that function again by making the boolean into true.
                    if(bank.getComputedList().size() > 0 && !checkedComputedList){
                        for(int j = 0; j < bank.getComputedList().size(); j++){
                            //Decreases the time of the tasks in computedList!
                            if(bank.getComputedList().get(j).getBlockedDuration() > 0){
                                bank.getComputedList().get(j).decBlockedDuration();
                            }
                        }
                    }
                    //Sets it to true to prevent the above method to run again in the same iteration of the for loop.
                    checkedComputedList = true;
                    //Since we need to go through the blocked tasks first, we will see if there are any, if there are,
                    //then the blocked tasks will be explored first. The boolean expression is there to prevent the method
                    //to run an additional time after the iteration it runs.
                    if(bank.getBlockedList().size() > 0 && !checkedBlockedList){
                        //A boolean to see if the "request" did occur and not simply put into a blocked state again.
                        boolean requestAccepted;
                        //Goes through the blockedList!
                        for(int j = 0; j < bank.getBlockedList().size(); j++){
                            //Store the list of lines of activity each task has to do into an ArrayList. This helps it
                            //easier to utilize that activity list from that task.
                                ArrayList<String> blockedTemp = bank.getBlockedList().get(j).getActivities();
                            //Splitting the first activity in the list, since it is stored in order, it has to be the
                            //first one to be run first. Split it since it is a string, and we want parts of that string.
                                String[] blockedActivity = blockedTemp.get(0).split("\\s+");
                            //Add the task into the blockedTask, since it will run, so for this running, it will not
                            //go on to the next statements.
                                blockedTask.add(bank.getTaskArrayList().get(Integer.parseInt(blockedActivity[1]) - 1));
                            //Stores the result from request, this is used to see if the task was successful in doing its
                            //activity.
                                requestAccepted = bank.request(Integer.parseInt(blockedActivity[1]) - 1);
                            //If the task was successful, then that task will be removed from the blocked list. If it
                            //was not, then there was most likely not enough resource to accomplish that task. So, it will
                            //stay in the blocked list.
                                if (requestAccepted) {
                                    for (int k = 0; k < bank.getBlockedList().size(); k++) {
                                        if (bank.getBlockedList().get(k).getTaskNum() == Integer.parseInt(blockedActivity[1])) {
                                            bank.getBlockedList().remove(k);
                                    }
                                }
                            }
                        }
                    }
                    //Once you go through the blocked methods ^ , then no need to go through it again after each for loop
                    //iteration.
                    checkedBlockedList = true;
                    //Making an arrayList to store the list of all activities for that one task.
                    ArrayList<String> temp = bank.getTaskArrayList().get(i).getActivities();
                    String[] activity = temp.get(0).split("\\s+");
                    //If the blockTask contains the task, it will not run, since it's supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is request, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    if (!bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isBlocked() &&
                            !bank.getAborted().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1)) &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isFinished() &&
                            activity[0].equals("request")) {
                        bank.request(i);
                    }
                    //If the blockTask contains the task, it will not run, since it's supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is release, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    else if (!bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isBlocked() &&
                            !bank.getAborted().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1)) &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isFinished() &&
                            activity[0].equals("release")) {

                        bank.release(i);
                        //Since the release statement is usually right before the "terminate" statement, and since the
                        //"terminate" statement is not supposed to consume a cycle, we can call terminate right after
                        //the release statement, as the activity list will have its top activity removed, which was responsible
                        //for the release of unit(s) then it can simply terminate by going to the next activity.
                        if(bank.getTaskArrayList().get(i).getActivities().get(0).split("\\s+")[0].equals("terminate")){
                            bank.terminate(i);
                        }
                    }
                    //If the blockTask contains the task, it will not run, since it's supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is compute, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    else if(!bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isBlocked() &&
                            !bank.getAborted().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1)) &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isFinished() &&
                            activity[0].equals("compute")){

                        bank.compute(i);
                        //Since the compute statement is usually right before the "terminate" statement, and since the
                        //"terminate" statement is not supposed to consume a cycle, we can call terminate right after
                        //the compute statement, as the activity list will have its top activity removed, which was responsible
                        //for the blocking of Task(s) then it can simply terminate by going to the next activity after the
                        //duration of Task is 0.
                        if(bank.getTaskArrayList().get(i).getActivities().get(0).split("\\s+")[0].equals("terminate")){
                            bank.getTaskArrayList().get(i).setBlockedBeforeTerminateForCompute(true);
                        }
                    }
                    //If the blockTask contains the task, it will not run, since it's supposed to be blocked. Then it checks
                    //if it is finished, and if finished, no need to run that, if aborted, no need to run either, and if
                    //the task is blocked, no need to run, and if the blockDuration is 0, then it no longer needs to wait.
                    //Then it checks if the String is terminate, and if so, then it will call the Bank object with the
                    //respective function to perform to it.
                    else if (!bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isBlocked() &&
                            !bank.getAborted().contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1)) &&
                            !blockedTask.contains(bank.getTaskArrayList().get(Integer.parseInt(activity[1]) - 1)) &&
                            !bank.getTaskArrayList().get(Integer.parseInt(activity[1]) -1).isFinished() &&
                            activity[0].equals("terminate")) {

                        bank.terminate(i);
                    }
                }
                //Responsible for combining the 3 arrayList of resources created in the beginning, finds the difference
                //between the original and the released, then add the difference into the resource arrayList. This will
                //make the resource for the next cycle.
                bank.convertReleaseResourceToResource();
                bank.incCycle();
            }
            //Since not all tasks are put in order, we can sort them by Task Numbers. The sorting was implemented
            //on the Task Object.
            Collections.sort(bank.getTaskArrayList());
            int total_work_time = 0;
            int total_wait_time = 0;
            //The below printing statements are for printing,and to make the printing look even, used string formatting.
            //so that a big number will not push other numbers to the side. But yes, the calculations for the total
            //is done below this and the printing of every task and its total. These print statements marks the end of
            //the Banker function.
            System.out.println("              BANKER'S");
            for(int y = 0; y < bank.getTaskArrayList().size(); y++){
                System.out.print(String.format("%11s", "Task"));
                System.out.print(String.format("%2s", bank.getTaskArrayList().get(y).getTaskNum()));
                if(bank.getTaskArrayList().get(y).isAborted()){
                    System.out.println(String.format("%13s", "aborted"));
                }
                else{
                    total_work_time+=bank.getTaskArrayList().get(y).getCompletedTime();
                    total_wait_time+=bank.getTaskArrayList().get(y).getWaitTime();
                    System.out.print(String.format("%8s", bank.getTaskArrayList().get(y).getCompletedTime()));
                    System.out.print(String.format("%4s", bank.getTaskArrayList().get(y).getWaitTime()));
                    System.out.println(String.format("%6s", Math.round(bank.getTaskArrayList().get(y).getWaitTime()*100/
                            (double)bank.getTaskArrayList().get(y).getCompletedTime()) + "%"));
                }
            }
            System.out.print(String.format("%12s", "total"));
            System.out.print(String.format("%9s", total_work_time));
            System.out.print(String.format("%4s", total_wait_time));
            System.out.println(String.format("%6s", Math.round(total_wait_time*100/(double) total_work_time) + "%"));
        }
    }