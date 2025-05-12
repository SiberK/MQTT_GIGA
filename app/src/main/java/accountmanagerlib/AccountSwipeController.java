package accountmanagerlib;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class AccountSwipeController extends ItemTouchHelper.Callback {
    private final AccountUiManager.AccountAdapter adapter;
    private final AccountUiManager uiManager;

    public AccountSwipeController(AccountUiManager.AccountAdapter _adapter, AccountUiManager _uiManager) {
        this.adapter = _adapter;
        this.uiManager = _uiManager;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        AccManager.Account account = uiManager.accManager.getAccount(position);

        if (direction == ItemTouchHelper.RIGHT) {
            uiManager.accManager.removeAccount(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(uiManager.context, "Аккаунт удален", Toast.LENGTH_SHORT).show();
        } else if (direction == ItemTouchHelper.LEFT) {
            uiManager.showAddAccountDialog(account);
            adapter.notifyItemChanged(position);
        }

        if (uiManager.listener != null) {
            uiManager.listener.onAccountUpdated();
        }
    }

    public void attachTo(RecyclerView recyclerView) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}