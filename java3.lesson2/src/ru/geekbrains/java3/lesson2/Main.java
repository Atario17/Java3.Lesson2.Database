package ru.geekbrains.java3.lesson2;
//                          Домашнее задание
//•	Сформировать таблицу товаров (id, prodid, title, cost) запросом из Java приложения.
// id - порядковый номер записи, первичный ключ prodid - уникальный номер товара
//        title - название товара cost - стоимость
//        •	При запуске приложения очистить таблицу и заполнить 10.000 товаров вида:
//        id_товара 1 товар1 10 id_товара 2 товар2 20 id_товара 3 товар3 30
//        ...
//        id_товара 10000 товар10000 100010 т.е. просто тестовые данные
//        •	Написать консольное приложение, которое позволяет узнать цену товара по его имени,
// либо если такого товара нет, то должно быть выведено сообщение "Такого товара нет".
// Пример консольной комманды для получения цены: "/цена товар545"
//•	В этом же приложении должна быть возможность изменения цены товара(указываем имя товара
// и новую цену). Пример: "/сменитьцену товар10 10000"
//        •	Вывести товары в заданном ценовом диапазоне. Консольная комманда: "/товарыпоцене 100 600"

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement ps;

    public static void main(String[] args) {
        connect();
        try {
            stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS Products (ID INTEGER, Prodid INTEGER PRIMARY KEY AUTOINCREMENT, Title  TEXT, Cost INTEGER);");
            stmt.execute("DELETE FROM Products");
            ps = connection.prepareStatement("INSERT INTO Products (ID,Title,Cost) VALUES (?,?,?);");
            connection.setAutoCommit(false);
            for (int i = 1; i <= 10000; i++) {
                ps.setInt(1, i);
                ps.setString(2, "Product" + i);
                ps.setInt(3, i * 10);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
            System.out.println("Введите /например/цена product545, чтобы узнать стоимость товара по его названию;\n" +
                    "Введите /например/сменитьцену product10 10000, чтобы изменить цену товара по по его названию;\n" +
                    "Введите /например/товарыпоцене 100 600, чтобы узнать какие товары находятся в заданном вами диапазоне;\n" +
                    "Введите выход, чтобы выйти;\n");
            Scanner sc = new Scanner(System.in);
            while (true) {
                String str = sc.nextLine();
                System.out.println("Вы ввели: "+str);
                String arr[] = str.split(" ");
                if(str.startsWith("цена")&&arr.length>=1){
                    getCostByTitle(arr[1]);
                }if(str.startsWith("сменитьцену")&&arr.length>=2){
                    setCostByTitle(arr[1], Integer.parseInt(arr[2]));
                }if(str.startsWith("товарыпоцене")&&arr.length>=2){
                    betweenMinAndMax(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
                }if(str.equals("выход")){
                    break;
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }
    public static void getCostByTitle(String title){
        try {
            ps = connection.prepareStatement("SELECT Cost FROM Products WHERE Title Like ? ");
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println(rs.getInt("Cost"));
            }
            rs.close();
            ps.close();
            if (!found) {
                System.out.println("Товары c таким именем не найден.");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void setCostByTitle(String title, int cost){
        try {
            ps = connection.prepareStatement("UPDATE Products SET Cost=? WHERE Title Like ? ");
            ps.setInt(1,cost);
            ps.setString(2,title);
            System.out.println("Изменено: "+ps.executeUpdate()+" товаров.");


            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void betweenMinAndMax(int min, int max){
        try {
            ps = connection.prepareStatement("SELECT * FROM Products WHERE Cost BETWEEN ? AND ?");
            ps.setInt(1, min);
            ps.setInt(2, max);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while(rs.next()){
                found = true;
                System.out.println(rs.getInt("ID")+" "+rs.getInt("Prodid")+" "+rs.getString("Title")+" "+rs.getInt("Cost"));
            }
            rs.close();
            ps.close();
            if(!found){
                System.out.println("Товары в заданном диапазоне не найдены.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:MainDB.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

