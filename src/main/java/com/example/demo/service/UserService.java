package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class UserService {

    private static final String FILE = "/Users/abhinav.harsh/Downloads/users.csv";

    /* ---------- READ ---------- */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                users.add(parseLineToUser(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /* ---------- UPDATE ---------- */
    public String updateUser(int id, String email, String name) {
        List<User> users = getAllUsers();

        for (User u : users) {
            if (u.getId() == id) {
                u.setEmail(email);
                u.setName(name);
                writeToFile(users);
                return "User updated";
            }
        }
        return "User not found";
    }

    public String patchUser(int id,String email,String name){
        List<User> users = getAllUsers();

        for(User u : users){
            if(u.getId()==id){

                if(email!=null){
                    u.setEmail(email);
                }

                if(name!=null){
                    u.setName(name);
                }

                writeToFile(users);
                return "User updated partially";
            }
        }
        return "User not found";
    }


    /* ---------- DELETE ---------- */
    public String deleteUser(int id) {
        List<User> users = getAllUsers();
        Iterator<User> iterator = users.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().getId() == id) {
                iterator.remove();
                writeToFile(users);
                return "User deleted";
            }
        }
        return "User not found";
    }

    /* ---------- HELPERS ---------- */

    private User parseLineToUser(String line) {
        Map<String, String> map = new HashMap<>();

        String[] pairs = line.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            map.put(kv[0], kv[1]);
        }

        return new User(
                Integer.parseInt(map.get("id")),
                map.get("email"),
                map.get("name")
        );
    }

    private void writeToFile(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (User u : users) {
                pw.println(
                        "id=" + u.getId() +
                                ",email=" + u.getEmail() +
                                ",name=" + u.getName()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
