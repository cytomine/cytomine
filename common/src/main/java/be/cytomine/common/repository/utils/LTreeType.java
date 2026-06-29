package be.cytomine.common.repository.utils;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

public class LTreeType implements UserType<String> {
    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<String> returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(String s, String j1) {
        return s != null && s.equals(j1);
    }

    @Override
    public int hashCode(String s) {
        return s.hashCode();
    }

    @Override
    public String nullSafeGet(
        ResultSet resultSet,
        int i,
        SharedSessionContractImplementor sharedSessionContractImplementor,
        Object o
    ) throws SQLException {
        return resultSet.getString(i);
    }

    @Override
    public void nullSafeSet(
        PreparedStatement preparedStatement,
        String s,
        int i,
        SharedSessionContractImplementor sharedSessionContractImplementor
    ) throws SQLException {
        preparedStatement.setObject(i, s, Types.OTHER);
    }

    @Override
    public String deepCopy(String s) {
        if (s == null) {
            return null;
        }
        return new String(s);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(String s) {
        return s;
    }

    @Override
    public String assemble(Serializable serializable, Object o) {
        return (String) serializable;
    }

    @Override
    public String replace(String s, String j1, Object o) {
        return deepCopy(s);
    }
}
