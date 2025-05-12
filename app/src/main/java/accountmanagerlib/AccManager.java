package accountmanagerlib;

// AccountManager.java

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AccManager{
    private final String TAG = "MNGR_ACC"  ;
    private static final String FILE_NAME = "accounts";
    private       List<Account> accounts;
    private final Context       context;

    public AccManager(Context context) {
        this.context = context.getApplicationContext();
        this.accounts = new ArrayList<>();
        loadFromJson();
    }

    public void addAccount(Account account) {
        accounts.add(account);
        saveToJson();
    }

    public void updateAccount(int position, Account newAccount) {
        if (position >= 0 && position < accounts.size()) {
            accounts.set(position, newAccount);
            saveToJson();
        }
    }

    public void removeAccount(int position) {
        if (position >= 0 && position < accounts.size()) {
            accounts.remove(position);
            saveToJson();
        }
    }

    public List<Account> getAllAccounts() {return new ArrayList<>(accounts) ;}
    public Account getAccount(int pos) {
        if (pos < 0 || pos >= accounts.size()) return null;
        return accounts.get(pos)        ;}
    public String getAccountDisplayText(int position) {
        Account account = getAccount(position);
        return account != null ? account.toString() : "";
    }

    private void saveToJson() {
        if(accounts == null) return   ;

        File   file = new File(context.getFilesDir(), FILE_NAME +".json");
        File tempFile = new File(context.getFilesDir(), FILE_NAME + ".tmp");// Сначала пишем во временный файл

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            new Gson().toJson(accounts, writer)                       ;// Сериализация и запись
            writer.close()                                              ;// Закрываем writer перед переименованием
            if(tempFile.renameTo(file)) Log.i(TAG,"Save ListAcc OK")    ;
            else Log.w(TAG,"Не удалось переименовать временный файл")   ;
        } catch (IOException | JsonIOException e) {
            Log.e(TAG, "Ошибка сохранения ListAcc: " + e.getMessage())  ;
        }finally{
            if (tempFile.exists() && !tempFile.delete()) {
                Log.w(TAG, "Не удалось удалить временный файл")         ;}
        }
    }
    private void loadFromJson() {
        accounts = null   ;
        File file = new File(context.getFilesDir(), FILE_NAME +".json");
        if(file.exists() && file.length() != 0){
            try(InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
                Gson gson = new Gson()                                  ;
                Type type = new TypeToken<List<Account>>(){}.getType()   ;// Указываем тип для корректной десериализации: List<Account>
                accounts = gson.fromJson(reader, type)                ;  // Читаем JSON в List<Account>
            } catch(IOException | JsonSyntaxException | JsonIOException e){
                Log.e("JSON", "Ошибка загрузки: " + e.getMessage())     ;}
            Log.i(TAG,"Load ListAcc OK")    ;
        }
        if (accounts == null) accounts = new ArrayList<>()          ;  // Если файл пустой
    }
//=================================================================================
//=================================================================================
    public static class Account {
        private String server;
        private String port;
        private String login;
        private String password;

        public Account(String _server, String _port, String _login, String _password) {
            server = _server;
            port = _port;
            login = _login;
            password = _password;
        }

        // Геттеры и сеттеры
        public String getServer() { return server; }
        public void setServer(String server) { this.server = server; }

        public String getPort() { return port; }
        public void setPort(String port) { this.port = port; }

        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        @NonNull
        @Override
        public String toString() {
            return server + " : " + port + " (" + login + ")";
        }
    }
}