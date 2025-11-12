package io.jona.memestore.dto;

import io.jona.framework.JonaDb;
import io.jona.framework.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

@AllArgsConstructor
public class Client extends Table {
    public static final String FULL_COLUMNS = "id, email, password ";
    @Setter @Getter
    private long id;
    @Getter
    private String email;
    private String password;

    public Client(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static ThrowingFunction<ResultSet, Client, SQLException> getFullMapping() {
        return rs -> new Client(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3)
        );
    }

    public static Client findByEmail(String email) {
        return JonaDb.selectSingle("select " + FULL_COLUMNS + "from client where email = ?", Client.getFullMapping(), email);
    }

    public String getInsert() {
        setId(nextId());
        return "insert into client (id, email, password) values(?,?,?)";
    }

    public String getUpdate() {
        return "update client set password = ? where email = '" + email + "'";
    }

    public String getDelete() {
        return "delete from client where id = " + id;
    }

    public Object[] getValues() {
        return new Object[] {id, email, password};
    }

    public static Client findClient(String email, String password) {
        return JonaDb.selectSingle("select " + FULL_COLUMNS + "from client where email = ? and password = ?",
                Client.getFullMapping(),
                email, password
        );
    }

    @Override
    public String toString() {
        return "Client id = " + id +
                ", email = " + email +
                ", password = " + password;
    }
}
