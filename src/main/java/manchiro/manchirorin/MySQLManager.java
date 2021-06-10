package manchiro.manchirorin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLManager {

    private final JavaPlugin plugin;
    private String HOST = null;
    private String DB = null;
    private String USER = null;
    private String PASS = null;
    private String PORT = null;
    private boolean connected;
    private Statement st = null;
    private Connection con = null;
    private MySQLFunc MySQL;

    public boolean isConnected() {
        return connected;
    }

    ////////////////////////////////
    //      コンストラクタ
    ////////////////////////////////
    public MySQLManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.connected = false;
        loadConfig();

        this.connected = Connect(HOST, DB, USER, PASS,PORT);

        if(!this.connected) {
            plugin.getLogger().info("Unable to establish a MySQL connection.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        execute("create table if not exists manchiro_log(" +
                "id int unsigned auto_increment primary key," +
                "wager decimal(14, 3) unsigned," +
                "oya_mcid varchar(16)," +
                "oya_uuid varchar(36)," +
                "oya_bet decimal(12, 3) unsigned," +
                "oya_bal decimal(12, 3) unsigned," +
                "oya_role varchar(9)," +
                "ko text," +
                "debug boolean," +
                "save_date datetime default current_timestamp" +
                ") engine=InnoDB default charset=utf8;");
    }

    /////////////////////////////////
    //       設定ファイル読み込み
    /////////////////////////////////
    public void loadConfig(){
        plugin.reloadConfig();
        HOST = plugin.getConfig().getString("mysql.host");
        USER = plugin.getConfig().getString("mysql.user");
        PASS = plugin.getConfig().getString("mysql.pass");
        PORT = plugin.getConfig().getString("mysql.port");
        DB = plugin.getConfig().getString("mysql.db");
    }

    public void commit(){
        try {
            this.con.commit();
        } catch (Exception ignored){

        }
    }

    ////////////////////////////////
    //       接続
    ////////////////////////////////
    public Boolean Connect(String host, String db, String user, String pass,String port) {
        this.HOST = host;
        this.DB = db;
        this.USER = user;
        this.PASS = pass;
        this.MySQL = new MySQLFunc(host, db, user, pass,port);
        this.con = this.MySQL.open();
        if(this.con == null){
            Bukkit.getLogger().info("failed to open MYSQL");
            return false;
        }

        try {
            this.st = this.con.createStatement();
            this.connected = true;
            this.plugin.getLogger().info("Connected to the database.");
        } catch (SQLException var6) {
            this.connected = false;
            this.plugin.getLogger().info("Could not connect to the database.");
        }

        this.MySQL.close(this.con);
        return this.connected;
    }

    //////////////////////////////////////////
    //         接続確認
    //////////////////////////////////////////
    public boolean connectCheck(){
        return Connect(HOST,DB,USER,PASS,PORT);
    }

    ////////////////////////////////
    //     行数を数える
    ////////////////////////////////
    public int countRows(String table) {
        int count = 0;
        ResultSet set = this.query(String.format("SELECT * FROM %s", table));

        try {
            while(set.next()) {
                ++count;
            }
        } catch (SQLException var5) {
            Bukkit.getLogger().severe("Could not select all rows from table: " + table + ", error: " + var5.getErrorCode());
        }

        return count;
    }
    ////////////////////////////////
    //     レコード数
    ////////////////////////////////
    public int count(String table) {
        int count = 0;
        ResultSet set = this.query(String.format("SELECT count(*) from %s", table));

        try {
            count = set.getInt("count(*)");

        } catch (SQLException var5) {
            Bukkit.getLogger().severe("Could not select all rows from table: " + table + ", error: " + var5.getErrorCode());
            return -1;
        }

        return count;
    }
    ////////////////////////////////
    //      実行
    ////////////////////////////////
    public boolean execute(String query) {
        this.MySQL = new MySQLFunc(this.HOST, this.DB, this.USER, this.PASS,this.PORT);
        this.con = this.MySQL.open();
        if(this.con == null){
            Bukkit.getLogger().warning("failed to open MYSQL");
            return false;
        }
        boolean ret = true;

        try {
            this.st = this.con.createStatement();
            this.st.execute(query);
        } catch (SQLException var3) {
            this.plugin.getLogger().severe("Error executing statement: " +var3.getErrorCode() +":"+ var3.getLocalizedMessage());
            this.plugin.getLogger().severe(query);
            ret = false;

        }

        this.close();
        return ret;
    }

    ////////////////////////////////
    //      クエリ
    ////////////////////////////////
    public ResultSet query(String query) {
        this.MySQL = new  MySQLFunc(this.HOST, this.DB, this.USER, this.PASS,this.PORT);
        this.con = this.MySQL.open();
        ResultSet rs = null;
        if(this.con == null){
            Bukkit.getLogger().warning("failed to open MYSQL");
            return null;
        }

        try {
            this.st = this.con.createStatement();
            rs = this.st.executeQuery(query);
        } catch (SQLException var4) {
            this.plugin.getLogger().severe("Error executing query: " + var4.getErrorCode());
            this.plugin.getLogger().severe(query);
        }

//        this.close();

        return rs;
    }


    public void close(){

        try {
            this.st.close();
            this.con.close();
            this.MySQL.close(this.con);

        } catch (SQLException ignored) {
        }

    }
}