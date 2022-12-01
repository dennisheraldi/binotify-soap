package com.binotify.soap.database.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.binotify.soap.database.Model;

public class Subscription extends Model {
    protected static final String TABLE_NAME = "subscription";
    protected static final String CREATE_SQL = String.join("",
        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (",
        "    `creator_id` INT NOT NULL,",
        "    `subscriber_id` INT NOT NULL,",
        "    `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',",
        "    PRIMARY KEY (`creator_id`, `subscriber_id`)",
        ");"
    );

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    };

    private int old_creator_id;
    private int old_subscriber_id;
    public int creator_id;
    public int subscriber_id;
    public Status status = Status.PENDING;

    public static void init() {
        try {
            db.runExecute(CREATE_SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Subscription get(int creator_id, int subscriber_id) throws SQLException {
        Subscription l = null;
        ArrayList<Subscription> res = find(creator_id, subscriber_id, 1);
        if (res.size() > 0)
            l = res.get(0);
        return l;
    }

    public static ArrayList<Subscription> all() throws SQLException {
        return find(-1, -1, -1);
    }

    public static ArrayList<Subscription> all(int limit) throws SQLException {
        return find(-1, -1, limit);
    }

    public static ArrayList<Subscription> find(int creator_id, int subscriber_id, int limit) throws SQLException {
        PreparedStatement stmt = db.prep(
            "SELECT * FROM " + TABLE_NAME +
            (creator_id == -1 && subscriber_id == -1 ? "" : " WHERE ") +
            (creator_id == -1 ? "" : "creator_id = ?") +
            (subscriber_id == -1 ? "" : (creator_id == -1 ? "":" AND ") + "subscriber_id = ?") +
            (limit == -1 ? "" : " LIMIT ?")
        );
        if (creator_id != -1)
            stmt.setInt(1, creator_id);
        if (subscriber_id != -1)
            stmt.setInt(2 - (creator_id == -1 ? 1 : 0), subscriber_id);
        if (limit != -1)
            stmt.setInt(3 - (creator_id == -1 ? 1 : 0) - (subscriber_id == -1 ? 1 : 0), limit);
        ResultSet rs = stmt.executeQuery();
        ArrayList<Subscription> res = new ArrayList<>();
        while (rs.next()) {
            Subscription l = new Subscription();
            l.creator_id = rs.getInt("creator_id");
            l.subscriber_id = rs.getInt("subscriber_id");
            l.status = Status.valueOf(rs.getString("status"));
            l.old_creator_id = l.creator_id;
            l.old_subscriber_id = l.subscriber_id;
            l.is_persisted = true;
            res.add(l);
        }
        stmt.close();
        return res;
    }

    public void save() throws SQLException {
        PreparedStatement stmt;
        if (this.is_persisted) {
            stmt = db.prep(
                "UPDATE " + TABLE_NAME + " SET creator_id=?, subscriber_id=?, status=? WHERE creator_id=? AND subscriber_id=?"
            );
            stmt.setInt(4, this.old_creator_id);
            stmt.setInt(5, this.old_subscriber_id);
        } else {
            stmt = db.prep(
                "INSERT INTO " + TABLE_NAME + " (`creator_id`, `subscriber_id`, `status`) VALUES(?,?,?)",
                true
            );
        }
        stmt.setInt(1, this.creator_id);
        stmt.setInt(2, this.subscriber_id);
        stmt.setString(3, this.status.toString());
        System.out.println(stmt.toString());
        if (stmt.executeUpdate() == 0)
            throw new SQLException("Failed to persist subscription");
        this.old_creator_id = this.creator_id;
        this.old_subscriber_id = this.subscriber_id;
        this.is_persisted = true;
        stmt.close();
    }

    public void delete() throws SQLException {
        if (!this.is_persisted) return;
        try (PreparedStatement stmt = db.prep("DELETE FROM " + TABLE_NAME + " WHERE creator_id=? AND subscriber_id=?")) {
            stmt.setInt(1, this.old_creator_id);
            stmt.setInt(2, this.old_subscriber_id);
            if (stmt.executeUpdate() == 0)
                throw new SQLException(
                    "Failed to delete subcriber creator_id " +
                    this.old_creator_id + " and subscriber_id " +
                    this.old_subscriber_id
                );
            this.is_persisted = false;
        }
    }
}
