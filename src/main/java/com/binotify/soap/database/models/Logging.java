package com.binotify.soap.database.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.binotify.soap.database.Model;

public class Logging extends Model {
    protected static final String TABLE_NAME = "logging";
    protected static final String CREATE_SQL = String.join(System.getProperty("line.separator"),
        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (",
        "    id INT NOT NULL AUTO_INCREMENT,",
        "    description VARCHAR(255) NOT NULL,",
        "    ip VARCHAR(255) NOT NULL,",
        "    endpoint VARCHAR(255) NOT NULL,",
        "    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,",
        "    PRIMARY KEY (id)",
        ");"
    );

    public int id = -1;
    public String description;
    public String ip;
    public String endpoint;
    public Timestamp requested_at = null;

    public static void init() {
        try {
            db.runExecute(CREATE_SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Logging get(int id) throws SQLException {
        Logging l = null;
        ArrayList<Logging> res = find(id, -1);
        if (res.size() > 0)
            l = res.get(0);
        return l;
    }

    public static ArrayList<Logging> all() throws SQLException {
        return find(-1, -1);
    }

    public static ArrayList<Logging> all(int limit) throws SQLException {
        return find(-1, limit);
    }

    public static ArrayList<Logging> find(int id, int limit) throws SQLException {
        PreparedStatement stmt = db.prep(
            "SELECT * FROM " + TABLE_NAME +
            (id == -1 ? "" : " WHERE id = ?") +
            (limit == -1 ? "" : " LIMIT ?")
        );
        if (id == -1) {
            stmt.setInt(1, limit);
        } else {
            stmt.setInt(1, id);
            if (limit != -1)
                stmt.setInt(2, limit);
        }
        ResultSet rs = stmt.executeQuery();
        ArrayList<Logging> res = new ArrayList<>();
        while (rs.next()) {
            Logging l = new Logging();
            l.id = rs.getInt("id");
            l.description = rs.getString("description");
            l.ip = rs.getString("ip");
            l.endpoint = rs.getString("endpoint");
            l.requested_at = rs.getTimestamp("requested_at");
            l.is_persisted = true;
            res.add(l);
        }
        stmt.getConnection().close();
        return res;
    }

    public void save() throws SQLException {
        if (!this.is_persisted)
            this.requested_at = new Timestamp(new Date().getTime());
        PreparedStatement stmt;
        if (this.is_persisted) {
            stmt = db.prep(
                "UPDATE " + TABLE_NAME + " SET description=?, ip=?, endpoint=?, requested_at=? WHERE id=?"
            );
            stmt.setInt(5, this.id);
        } else {
            stmt = db.prep(
                "INSERT INTO " + TABLE_NAME + " (description, ip, endpoint, requested_at) VALUES(?,?,?,?)",
                true
            );
        }
        stmt.setString(1, this.description);
        stmt.setString(2, this.ip);
        stmt.setString(3, this.endpoint);
        stmt.setTimestamp(4, this.requested_at);
        if (stmt.executeUpdate() == 0) {
            throw new SQLException("Failed to persist log");
        }
        if (!this.is_persisted) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
            } else {
                throw new SQLException("Failed to get ID of inserted log.");
            }
            this.is_persisted = true;
        }
        stmt.getConnection().close();
    }

    public void delete() throws SQLException {
        if (!this.is_persisted) return;
        try (PreparedStatement stmt = db.prep("DELETE FROM " + TABLE_NAME + " WHERE id=?")) {
            stmt.setInt(1, this.id);
            if (stmt.executeUpdate() == 0)
                throw new SQLException("Failed to delete logging id " + this.id);
            this.is_persisted = false;
        }
    }
}