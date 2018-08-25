package com.github.iyboklee.cache;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.cache.store.CacheStoreSession;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.CacheStoreSessionResource;
import org.apache.ignite.transactions.Transaction;

import com.github.iyboklee.model.Book;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.dbutils.DbUtils.closeQuietly;

@Slf4j
public class BookCacheStore extends CacheStoreAdapter<String, Book> {

    private static final String ATTR_CONN = "JDBC_STORE_CONNECTION";

    @CacheStoreSessionResource
    private CacheStoreSession session;

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Book load(String key) throws CacheLoaderException {
        log.debug("Load value from database [cache: {}, key: {}]", session.cacheName(), key);

        Connection conn = null;

        try {
            conn = connection();

            try (PreparedStatement stmt = conn.prepareStatement("select * from books where isbn = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? new Book(rs.getString("isbn"), rs.getString("title")) : null;
                }
            }
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load value from database [cache: " + session.cacheName() + "key: " + key + "]", e);
        } finally {
            closeIfNoTransaction(conn);
        }
    }

    @Override
    public void write(Cache.Entry<? extends String, ? extends Book> entry) throws CacheWriterException {
        log.debug("Start write entry to database [cache: {}, entry: {}]", session.cacheName(), entry);

        String key = entry.getKey();
        Book value = entry.getValue();

        Connection conn = null;

        try {
            conn = connection();

            int updated = 0;
            // Try update first. If it does not work, then try insert.
            try (PreparedStatement stmt = conn.prepareStatement("update books set title = ? where isbn = ?")) {
                stmt.setString(1, value.getTitle());
                stmt.setString(2, key);
                updated = stmt.executeUpdate();
            }
            // If update failed, try to insert.
            if (updated == 0) {
                try (PreparedStatement stmt = conn.prepareStatement("insert into books (isbn, title) values (?, ?)")) {
                    stmt.setString(1, key);
                    stmt.setString(2, value.getTitle());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new CacheWriterException("Failed to write value to database [cache: " + session.cacheName() + ", entry: " + entry + "]", e);
        } finally {
            closeIfNoTransaction(conn);
        }
    }

    @Override
    public void delete(Object key) throws CacheWriterException {
        log.debug("Remove value from database [cache: {}, key: {}]", session.cacheName(), key);

        Connection conn = null;

        try {
            conn = connection();

            try (PreparedStatement stmt = conn.prepareStatement("delete from books where isbn = ?")) {
                stmt.setString(1, (String) key);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CacheWriterException("Failed to remove value from database [cache: " + session.cacheName() + "key: " + key + "]", e);
        } finally {
            closeIfNoTransaction(conn);
        }
    }

    @Override
    public void loadCache(IgniteBiInClosure<String, Book> clo, Object... args) {
        if (args == null || args.length == 0 || args[0] == null)
            throw new CacheLoaderException("Expected entry count parameter is not provided.");

        int limitCount = (Integer) args[0];

        Connection conn = null;

        try {
            conn = connection();

            try (PreparedStatement stmt = conn.prepareStatement("select * from books limit ?")) {
                stmt.setInt(1, limitCount);
                try (ResultSet rs = stmt.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        Book book = new Book(rs.getString("isbn"), rs.getString("title"));
                        clo.apply(book.getIsbn(), book);
                        count++;
                    }
                    log.debug("Loaded {} values into cache.", count);
                }
            }
        } catch (SQLException e) {
            throw new CacheLoaderException("Failed to load values from cache store.", e);
        } finally {
            closeIfNoTransaction(conn);
        }
    }

    @Override
    public void sessionEnd(boolean commit) {
        Transaction tx = session.transaction();
        if (tx != null) {
            Map<String, Connection> properties = session.properties();
            Connection conn = properties.remove(ATTR_CONN);
            if (conn != null) {
                try {
                    if (commit)
                        conn.commit();
                    else
                        conn.rollback();
                } catch (SQLException e) {
                    throw new CacheWriterException("Failed to end transaction [xid=" + tx.xid() + ", commit=" + commit + ']', e);
                } finally {
                    closeQuietly(conn);
                }
            }
            log.debug("Transaction ended [xid=" + tx.xid() + ", commit=" + commit + ']');
        }
    }

    private Connection connection() throws SQLException {
        if (session.transaction() != null) {
            Map<String, Connection> properties = session.properties();
            Connection conn = properties.get(ATTR_CONN);
            if (conn == null) {
                conn = openConnection(false);
                properties.put(ATTR_CONN, conn);
            }
            return conn;
        }

        // Transaction can be null in case of simple load operation.
        return openConnection(true);
    }

    private Connection openConnection(boolean autocommit) throws SQLException {
        assert dataSource != null;

        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(autocommit);
        return conn;
    }

    private void closeIfNoTransaction(Connection conn) {
        if (session.transaction() == null)
            closeQuietly(conn);
    }

}
