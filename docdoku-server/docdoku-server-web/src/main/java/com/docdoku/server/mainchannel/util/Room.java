/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.mainchannel.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Room {

    private static final ConcurrentMap<String, Room> DB = new ConcurrentHashMap<String, Room>();

    private String keyName;
    private String user1;
    private String user2;
    
    public Room() {
    }

    public Room(String roomKey) {
        keyName = roomKey;
        put();
    }

    public String getUser1(){
        return user1;
    }
    public String getUser2(){
        return user2;
    }
    
    
    /** Retrieve a {@link com.docdoku.server.mainchannel.util.Room} instance from database */
    public static Room getByKeyName(String roomKey) {
        return DB.get(roomKey);
    }

    /** Called to disconnect a user (eventually remove the reserved {@link com.docdoku.server.mainchannel.util.Room})
     * when one of the conference legs are cut, i.e. a WebSocket connection is closed */
    public static void disconnect(String token) {
        if (token != null) {
            String[] values = token.split("/");
            String room_key = values[0];
            String user = values[1];

            Room room = Room.getByKeyName(room_key);
            if (room != null && room.hasUser(user)) {
                String other_user = room.getOtherUser(user);
                room.removeUser(user);
                if (other_user != null) {
                    //SignalingWebSocket.send(Helper.makeToken(room, other_user), "{'type':'bye'}");
                } else {
                    room.delete();
                }
            }
        }
    }

    public static ConcurrentMap<String, Room> getDB() {
        return DB;
    }


    /** @return a {@link String} representation of this room */
    @Override
    public String toString() {
        String str = "[";
        if (user1 != null && !user1.equals("")) {
            str += user1;
        }
        if (user2 != null && !user2.equals("")) {
            str += ", " + user2;
        }
        str += "]";
        return str;
    }

    /** @return number of participant in this room */
    public int getOccupancy() {
        int occupancy = 0;
        if (user1 != null && !user1.equals("")) {
            occupancy += 1;
        }
        if (user2 != null && !user2.equals("")) {
            occupancy += 1;
        }
        return occupancy;
    }

    /** @return the name of the other participant, null if none */
    public String getOtherUser(String user) {
        if (user.equals(user1)) {
            return user2;
        } else if (user.equals(user2)) {
            return user1;
        } else {
            return null;
        }
    }

    /** @return true if one the participant is named as the input parameter, false otherwise */
    public boolean hasUser(String user) {
        return (user != null && (user.equals(user1) || user.equals(user2)));
    }

    /** Add a new participant to this room
     * @return if participant is found */
    public boolean addUser(String user) {

        boolean success = true;

        // avoid a user to be added in the room many times.
        if(this.hasUser(user)){
            return success;
        }

        if (user1 == null || user1.equals("")) {
            user1 = user;
        } else if (user2 == null || user2.equals("")) {
            user2 = user;
        } else {
            success = false;
        }
        return success;
    }

    /** Removed a participant form current room */
    public void removeUser(String user) {
        if (user != null && user.equals(user2)) {
            user2 = null;
        }
        if (user != null && user.equals(user1)) {
            if (user2 != null && !user2.equals("")) {
                user1 = user2;
                user2 = null;
            } else {
                user1 = null;
            }
        }
        if (getOccupancy() > 0) {
            put();
        } else {
            delete();
        }
    }

    /**@return the key of this room. */
    public String key() {
        return keyName;
    }

    /** Store current instance into database */
    public void put() {
        DB.put(keyName, this);
    }

    /** Delete/Remove current {@link com.docdoku.server.mainchannel.util.Room} instance from database */
    public void delete() {
        if (keyName != null) {
            DB.remove(keyName);
            keyName = null;
        }
    }

    public static void removeUserFromAllRoom(String callerLogin) {
        //log("removeUserFromAllRoom : "+callerLogin);
        Set<Map.Entry<String, Room>> rooms = new HashSet<Map.Entry<String, Room>>(DB.entrySet());
        for (Map.Entry<String, Room> e : rooms) {
            if (e.getValue().hasUser(callerLogin)) {
                //log("REMOVE USER "+callerLogin+" from ROOM "+e.getKey());
                e.getValue().removeUser(callerLogin);
            }
        }

    }

    public static void debug(){

        System.out.println("===========ROOM DEBUG BEGIN================");

        Set<Map.Entry<String, Room>> rooms = new HashSet<Map.Entry<String, Room>>(DB.entrySet());
        for (Map.Entry<String, Room> e : rooms) {
            System.out.println("===========================");
            System.out.println("ROOM DEBUG : roomKey = "+e.getKey());
            System.out.println("ROOM DEBUG : occupancy = "+e.getValue().getOccupancy());
            System.out.println("ROOM DEBUG : user1 = "+e.getValue().getUser1());
            System.out.println("ROOM DEBUG : user2 = "+e.getValue().getUser2());
            System.out.println("===========================");
        }
        System.out.println("===========ROOM DEBUG END================");

    }

    public static void log(String message) {
        Logger.getLogger(Room.class.getName()).log(Level.WARNING, "DEBUG ROOM : "+message);
    }
}