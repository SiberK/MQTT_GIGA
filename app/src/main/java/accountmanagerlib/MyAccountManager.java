package accountmanagerlib;

// AccountManager.java
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyAccountManager implements Serializable {
    private static final String FILE_NAME = "accounts.dat";
    private List<Account> accounts;
    private Context context;

    public MyAccountManager(Context context) {
        this.context = context.getApplicationContext();
        this.accounts = new ArrayList<>();
        loadAccounts();
    }

    public void addAccount(Account account) {
        accounts.add(account);
        saveAccounts();
    }

    public void updateAccount(int position, Account newAccount) {
        if (position >= 0 && position < accounts.size()) {
            accounts.set(position, newAccount);
            saveAccounts();
        }
    }

    public void removeAccount(int position) {
        if (position >= 0 && position < accounts.size()) {
            accounts.remove(position);
            saveAccounts();
        }
    }

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts);
    }

    public Account getAccount(int position) {
        if (position >= 0 && position < accounts.size()) {
            return accounts.get(position);
        }
        return null;
    }

    public String getAccountDisplayText(int position) {
        Account account = getAccount(position);
        return account != null ? account.toString() : "";
    }

    private void saveAccounts() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), FILE_NAME));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new ArrayList<>(accounts));
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAccounts() {
        try {
            FileInputStream fis = new FileInputStream(new File(context.getFilesDir(), FILE_NAME));
            ObjectInputStream ois = new ObjectInputStream(fis);
            accounts = (List<Account>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            accounts = new ArrayList<>();
        }
    }
}