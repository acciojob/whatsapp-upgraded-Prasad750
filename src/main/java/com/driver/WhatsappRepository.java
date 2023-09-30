package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;



@Repository
public class WhatsappRepository {

    int groupCount;

    int messageId;

    HashMap<String,User> userDb;
    HashMap<String,Group> groupDb;
    HashMap<String,List<User>> groupMemberListDb;
    HashMap<Group,List<Message>> groupMessagesDb;

    HashMap<User,List<Message>> userMessageDb;



    public WhatsappRepository()
    {
        this.groupCount=1;
        this.messageId=1;
        userDb=new HashMap<>();
        groupDb=new HashMap<>();
        groupMemberListDb=new HashMap<>();
        groupMessagesDb=new HashMap<>();
        userMessageDb=new HashMap<>();
    }


    public String createUser(String name, String mobile)throws Exception {

       if(userDb.containsKey(mobile))
       {
           throw  new Exception("User already exists");
       }

       User user=new User(name,mobile);

       userDb.put(mobile,user);

       return "SUCCESS";


    }

    public Group createGroup(List<User> users) {

       String groupName="";

        if (users.size()==2)
        {
           groupName=users.get(0).getName();

        }
        else
        {
            groupName="Group"+" "+groupCount;
            groupCount++;
        }
        Group group=new Group(groupName,users.size());

        groupDb.put(groupName,group);
        groupMemberListDb.put(groupName,users);

        return group;



    }

    public int createMessage(String content) {

        Message message=new Message(messageId,content);
        messageId++;
        return messageId;

    }

    public int sendMessage(Message message, User sender, Group group)throws Exception {

        if (!groupDb.containsKey(group.getName()))
        {
            throw new Exception("Group does not exist");
        }

        if(!groupMemberListDb.get(group.getName()).contains(sender))
        {
            throw new Exception("You are not allowed to send message");
        }

        List<Message> msgs=new ArrayList<>();
        if(groupMessagesDb.containsKey(group))
        {
            msgs=groupMessagesDb.get(group);
        }
        msgs.add(message);

        List<Message> userMsgs=new ArrayList<>();
        if(userMessageDb.containsKey(sender))
        {
            userMsgs=userMessageDb.get(sender);

        }
        userMsgs.add(message);

        return msgs.size();


    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if (!groupDb.containsKey(group.getName()))
        {
            throw new Exception("Group does not exist");
        }

        if(!groupMemberListDb.get(group.getName()).get(0).equals(approver))
        {
            throw new Exception("Approver does not have rights");
        }

        if(!groupMemberListDb.get(group.getName()).contains(user))
        {
            throw new Exception("User is not a participant");
        }

        groupMemberListDb.get(group.getName()).remove(user);
        groupMemberListDb.get(group.getName()).add(0,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {

        Group group=null;
        for(String grp:groupDb.keySet())
        {
            if(groupMemberListDb.get(grp).contains(user));
            {
                group=groupDb.get(grp);
                break;
            }
        }

        if(group==null)
        {
            throw new Exception("User not found");
        }

        if(groupMemberListDb.get(group.getName()).get(0).equals(user))
        {
            throw new Exception("Cannot remove admin");
        }


        for (Message msg:userMessageDb.get(user))
        {
            groupMessagesDb.get(group).remove(msg);
        }

        userMessageDb.remove(user);
        userDb.remove(user.getMobile());
        groupMemberListDb.get(group.getName()).remove(user);

        int overallMag=0;
        for(User u :userMessageDb.keySet())
        {
            overallMag+=userMessageDb.get(u).size();
        }

        return groupMemberListDb.size() + groupMessagesDb.get(group).size() + overallMag;

    }

    public String findMessage(Date start, Date end, int K) throws Exception {
        List<Message> list = new ArrayList<>();
        for(User user:userMessageDb.keySet()){
            for (Message m:userMessageDb.get(user)) {
                if (m.getTimestamp().compareTo(start) > 0 && m.getTimestamp().compareTo(end) < 0) {
                    list.add(m);
                }
            }
        }
        Collections.sort(list,(a, b)->a.getTimestamp().compareTo(b.getTimestamp()));
        if(K>list.size()){
            throw new Exception("K is greater than the number of messages");
        }
        return list.get(K-1).getContent();
    }
}
