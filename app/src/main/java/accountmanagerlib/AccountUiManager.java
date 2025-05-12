package accountmanagerlib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mqtt_giga.R;
import java.util.List;
import accountmanagerlib.AccManager.Account;
//---------------------------------------------------------------------------
public class AccountUiManager {
    public final Context               context;
    public final AccManager            accManager;
    public final AccountActionListener listener;
    public       Account               curAccount;
//---------------------------------------------------------------------------
    public interface AccountActionListener {
        void onAccountSelected(AccManager.Account _account);
        void onAccountListDismissed();
        void onAccountUpdated();
    }
//---------------------------------------------------------------------------
    public AccountUiManager(Context _context, AccountActionListener _listener) {
        context    = _context          ;
        accManager = new AccManager(_context);
        listener   = _listener        ;
        curAccount = null       ;
    }
    //---------------------------------------------------------------------------
    public void showAddAccountDialog(AccManager.Account accountToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_account, null);
        builder.setView(dialogView);

        EditText etServer   = dialogView.findViewById(R.id.etServer)    ;
        EditText etPort     = dialogView.findViewById(R.id.etPort)      ;
        EditText etLogin    = dialogView.findViewById(R.id.etLogin)     ;
        EditText etPassword = dialogView.findViewById(R.id.etPassword)  ;

        if (accountToEdit != null) {
            etServer  .setText(accountToEdit.getServer());
            etPort    .setText(accountToEdit.getPort());
            etLogin   .setText(accountToEdit.getLogin());
            etPassword.setText(accountToEdit.getPassword());
        }

        builder.setTitle(accountToEdit == null ? "Добавить аккаунт" : "Редактировать аккаунт")
                .setPositiveButton("OK", (dialog, id) -> {
                    String server   = etServer  .getText().toString();
                    String port     = etPort    .getText().toString();
                    String login    = etLogin   .getText().toString();

                    String password = etPassword.getText().toString();

                    if (server.isEmpty() || port.isEmpty()) {
                        Toast.makeText(context, "поля Server и Port должны быть заполнены", Toast.LENGTH_SHORT).show();
                        return;}

                    curAccount = new Account(server, port, login, password);
                    if (accountToEdit == null) accManager.addAccount(curAccount);
                    else {
                        int position = accManager.getAllAccounts().indexOf(accountToEdit);
                        if (position != -1) accManager.updateAccount(position, curAccount);
                    }

                    if (listener != null) listener.onAccountSelected(curAccount);
                })
                .setNegativeButton("Отменить", (dialog, id) -> dialog.cancel());

        builder.setCancelable(false) ;
        builder.create().show();
    }
//---------------------------------------------------------------------------
    public void showAccountsList() {
        List<AccManager.Account> accounts = accManager.getAllAccounts();

        if (accounts.isEmpty()) {
//            Toast.makeText(context, "Нет сохраненных аккаунтов", Toast.LENGTH_SHORT).show();
//            if (listener != null) {
//                listener.onAccountListDismissed();
//            }
//            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Выберите аккаунт");

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        AccountAdapter adapter = new AccountAdapter(accounts);
        recyclerView.setAdapter(adapter);

        // Используем AccountSwipeController
        new AccountSwipeController(adapter, this).attachTo(recyclerView);

        builder.setView(recyclerView);
        builder.setNeutralButton("Править",(dialog,which)->{
            showAddAccountDialog(curAccount)    ;}) ;
        builder.setNegativeButton("Добавить",(dialog,which)->{
            showAddAccountDialog(null)              ;});
        builder.setPositiveButton("Закрыть", (dialog, which) -> {
            if (listener != null) listener.onAccountListDismissed()     ;});
        builder.setCancelable(false) ;

        AlertDialog dialog = builder.create();
        dialog.show();
    }
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
//---------------------------------------------------------------------------
    public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
        private final List<AccManager.Account> accounts;

        AccountAdapter(List<AccManager.Account> _accounts) {
            accounts = _accounts;
        }

        @NonNull
        @Override
        public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.def_layout, parent, false);
            return new AccountViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
            holder.bind(accounts.get(position));
        }

        @Override
        public int getItemCount() {
            return accounts.size();
        }

        class AccountViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            AccountViewHolder(View itemView) {
                super(itemView);
//                textView = itemView.findViewById(android.R.id.text1);
                textView = itemView.findViewById(R.id.tvAccItem);
                if(textView != null)
                    itemView.setOnClickListener(v -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            curAccount = accounts.get(position) ;
                            if (listener != null) {
                                listener.onAccountSelected(curAccount);
                            }
                        }
                    });
            }
            void bind(AccManager.Account account) {
                textView.setText(account.toString());
            }
        }
    }
//---------------------------------------------------------------------------
}