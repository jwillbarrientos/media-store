package io.jona.framework;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JonaDb {
    private static String url;
    private static String user;
    private static String password;

    public static void init(String url,
                            String user,
                            String password,
                            boolean startH2Server,
                            String initSchema,
                            String testTable) {

        try {
            Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JonaDb.url = url;
        JonaDb.user = user;
        JonaDb.password = password;

        /*
            select count(*) from testTable
            if select success, then assume the databaswe exists and it has th8e schema already created,
            otherwise, execute all statements of the initSchema file using jdbc to create the database
         */

    }

    public static boolean insertSingle(Table table) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(table.getInsert());
            setMapping(stmt, table.getValues());
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                log.info("A new element in table was inserted successfully!");
                return true;
            }
        } catch (SQLException e) {
            log.error("Insert fail: ", e);
        }
        return false;
    }

    public static boolean updateSingle(Table table) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(table.getUpdate());
            setMapping(stmt, table.getValues());
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                log.info("Element in table updated successfully");
                return true;
            }
        } catch (SQLException e) {
            log.error("Update fail: ", e);
        }
        return false;
    }

    public static boolean deleteSingle(Table table) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(table.getDelete());
            int rowsDeleted = stmt.executeUpdate();
            log.info("Element in table deleted successfully!");
            return rowsDeleted > 0;
        } catch (SQLException e) {
            log.error("Delete fail: ", e);
        }
        return false;
    }

    public static boolean updateGeneric(String query, Object... queryMappings) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(query);
            setMapping(stmt, queryMappings);
            int rowsDeleted = stmt.executeUpdate();
            log.info("Element/s in table updated successfully!");
            return rowsDeleted > 0;
        } catch (SQLException e) {
            log.error("Update generic fail: ", e);
        }
        return false;
    }

    public static <T extends Table> T selectSingle(String query, Table.ThrowingFunction<ResultSet, T, SQLException> rowMapper, Object... queryMappings) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(query);
            setMapping(stmt, queryMappings);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rowMapper.apply(rs);
        } catch (SQLException e) {
            log.error("Select single fail: ", e);
        }
        return null;
    }

    public static <T extends Table> List<T> selectList(String query, Table.ThrowingFunction<ResultSet, T, SQLException> rowMapper, Object... queryMappings) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
           PreparedStatement stmt = conn.prepareStatement(query);
           setMapping(stmt, queryMappings);
           ResultSet rs = stmt.executeQuery();
           List<T> list = new ArrayList<>();
           while (rs.next())
               list.add(rowMapper.apply(rs));
           return list;
        } catch (SQLException e) {
            log.error("Select list fail: ", e);
        }
        return null;
    }

    public static int findCount(String query, Object... queryMappings) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement(query);
            setMapping(stmt, queryMappings);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error checking if element in table exists: ", e);
        }
        return 0;
    }

    private static void setMapping(PreparedStatement ps, Object... queryMappings) throws SQLException {
        int i = 1;
        for(Object mapping : queryMappings) {
            if (mapping instanceof Long)
                ps.setLong(i++, (Long) mapping);
            else if (mapping instanceof Integer)
                ps.setInt(i++, (Integer) mapping);
            else if (mapping instanceof Timestamp)
                ps.setTimestamp(i++, (Timestamp) mapping);
            else
                ps.setString(i++, mapping.toString());
        }
    }
}
