package com.myapp.rollcall.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    // 鼠要测试记得改成自己的  分别是：数据库名 用户名 密码
    private static final String URL =
            "jdbc:mysql://localhost:3306/software_project?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "19781029Zhf!";

    private Db() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

