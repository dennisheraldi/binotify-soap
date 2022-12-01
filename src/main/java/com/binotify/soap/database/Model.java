package com.binotify.soap.database;

import java.sql.SQLException;

public abstract class Model {
    protected static final String TABLE_NAME = "";
    protected static final String CREATE_SQL = "";
    protected boolean is_persisted = false;

    protected static final Client db = Client.getInstance();

    public abstract void save() throws SQLException;
    public abstract void delete() throws SQLException;
}
